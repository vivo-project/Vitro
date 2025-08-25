/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

import static edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch.JsonTree.EMPTY_JSON_MAP;
import static edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch.JsonTree.ifPositive;
import static edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch.JsonTree.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery.Order;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Accept a SearchQuery and make it available as a JSON string, suitable for
 * Elasticsearch.
 */
public class QueryConverter {
    private static final Log log = LogFactory.getLog(QueryConverter.class);

    private final SearchQuery query;
    private final Map<String, Object> queryAndFilters;
    private final Map<String, Object> sortFields;
    private final Map<String, Object> facets;
    private final Map<String, Object> highlighter;
    private final List<String> returnFields;
    private final Map<String, Object> fullMap;

    public QueryConverter(SearchQuery query, boolean treatAsLuceneQuery) {
        this.query = query;
        this.queryAndFilters = filteredOrNot(treatAsLuceneQuery);
        this.sortFields = figureSortFields();
        this.facets = figureFacets();
        this.highlighter = figureHighlighter();
        this.returnFields = figureReturnFields();

        this.fullMap = figureFullMap();
    }

    private Map<String, Object> filteredOrNot(boolean treatAsStructuredQuery) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(getES8Query(treatAsStructuredQuery).replaceFirst("Query: ", ""),
                new TypeReference<Map<String, Object>>() {
                });
        } catch (JsonProcessingException e) {
            log.error("Query parsing for ES8 failed, parsing it as unstructured query.");
            if (treatAsStructuredQuery) {
                return filteredOrNot(false);
            }
        }

        log.error("Query parsing for ES8 failed, falling back to old parsing method for query " + query.getQuery());
        return new QueryStringMap(query.getQuery()).map;
    }

    private Map<String, Object> buildFilterStructure() {
        return tree() //
            .put("bool", tree() //
                .put("must", new QueryStringMap(query.getQuery()).map) //
                .put("filter", buildFiltersList())) //
            .asMap();
    }

    private String getES8Query(Boolean treatAsStructuredQuery) {
        ExpressionTransformer transformer = new ExpressionTransformer();

        StringBuilder queryForParsing;
        if (treatAsStructuredQuery) {
            queryForParsing = new StringBuilder("( " + ExpressionTransformer.fillInMissingOperators(
                ExpressionTransformer.removeWhitespacesFromRangeExpression(
                    query.getQuery()
                        .replace("(", "( ")
                        .replace(")", " )")
                )) + " )");
        } else {
            queryForParsing = new StringBuilder("( " + Arrays.stream(query.getQuery().trim().split("\\s+"))
                .filter(token -> !token.isBlank() && !token.equals("\""))
                .map(token -> {
                    if (token.startsWith("classgroup:")) {
                        return token;
                    }

                    return "ALLTEXT:" + token + " OR nameLowercaseSingleValued:" + token;
                })
                .collect(Collectors.joining(" OR ")) + " )");
        }

        for (String filter : query.getFilters()) {
            queryForParsing.append(" AND ").append(filter);
        }

        List<String> queryTokens = new ArrayList<>(Arrays.asList(queryForParsing.toString().split(" ")));
        queryTokens.removeIf(String::isEmpty);

        return BoolQuery.of(q -> {
            q.must(transformer.parseAdvancedQuery(queryTokens));
            return q;
        })._toQuery().toString();
    }

    private List<Map<String, Object>> buildFiltersList() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String filter : query.getFilters()) {
            list.add(new QueryStringMap(filter).map);
        }
        return list;
    }

    private Map<String, Object> figureSortFields() {
        Map<String, Order> fields = query.getSortFields();
        Map<String, Object> map = new HashMap<>();
        for (String name : fields.keySet()) {
            String sortOrder = fields.get(name).toString().toLowerCase();
            if (name.equals("score")) {
                map.put("_score", sortOrder);
            } else {
                map.put(name, sortOrder);
            }
        }
        return map;
    }

    private Map<String, Object> figureFacets() {
        Map<String, Object> map = new HashMap<>();
        for (String field : query.getFacetFields()) {
            map.put("facet_" + field, figureFacet(field));
        }
        return map;
    }

    private Map<String, Object> figureHighlighter() {
        return tree() //
            .put("fields", tree() //
                .put("ALLTEXT", EMPTY_JSON_MAP))
            .asMap();
    }

    private Map<String, Object> figureFacet(String field) {
        String fieldToAggregate = field.endsWith("_ss") ? field + ".keyword" : field;

        return tree()
            .put("terms", tree()
                .put("field", fieldToAggregate)
                .put("size", ifPositive(query.getFacetLimit()))
                .put("min_doc_count", ifPositive(query.getFacetMinCount()))
            )
            .put("aggs", tree()
                .put("top_label", tree()
                    .put("top_hits", tree()
                        .put("size", 1)
                        .put("_source",
                            List.of("es_label_display")  // fetch default display label from index source
                        )
                    )
                )
            )
            .asMap();
    }

    private List<String> figureReturnFields() {
        return new ArrayList<>(query.getFieldsToReturn());
    }

    private Map<String, Object> figureFullMap() {
        return tree() //
            .put("track_total_hits", true)
            .put("query", queryAndFilters) //
            .put("from", ifPositive(query.getStart())) //
            .put("highlight", highlighter)
            .put("size", ifPositive(query.getRows())) //
            .put("sort", sortFields) //
            .put("_source", returnFields) //
            .put("aggs", facets) //
            .asMap();
    }

    public String asString() throws SearchEngineException {
        try {
            return new ObjectMapper().writeValueAsString(fullMap);
        } catch (JsonProcessingException e) {
            throw new SearchEngineException(e);
        }
    }

    private static class QueryStringMap {
        public final Map<String, Object> map;

        public QueryStringMap(String queryString) {
            map = new HashMap<>();
            map.put("query_string", makeInnerMap(escape(queryString)));
        }

        /**
         * This is a kluge, but perhaps it will work for now.
         * <p>
         * Apparently Solr is willing to put up with query strings that contain
         * special characters in odd places, but Elasticsearch is not.
         * <p>
         * So, a query string of "classgroup:http://this/that" must be escaped
         * as "classgroup:http\:\/\/this\/that". Notice that the first colon
         * delimits the field name, and so must not be escaped.
         * <p>
         * But what if no field is specified? Then all colons must be escaped.
         * How would we distinguish that?
         * <p>
         * And what if the query is more complex, and more than one field is
         * specified? What if other special characters are included?
         * <p>
         * This could be a real problem.
         */
        private String escape(String queryString) {
            return queryString.replace(":", "\\:").replace("/", "\\/")
                .replaceFirst("\\\\:", ":");
        }

        private Map<String, String> makeInnerMap(String queryString) {
            Map<String, String> inner = new HashMap<>();
            inner.put("default_field", "ALLTEXT");
            inner.put("query", queryString);
            return inner;
        }
    }

}
