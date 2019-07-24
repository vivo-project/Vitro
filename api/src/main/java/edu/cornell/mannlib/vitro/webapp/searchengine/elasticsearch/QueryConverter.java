/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

import static edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch.JsonTree.EMPTY_JSON_MAP;
import static edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch.JsonTree.ifPositive;
import static edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch.JsonTree.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery.Order;

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

    public QueryConverter(SearchQuery query) {
        this.query = query;
        this.queryAndFilters = filteredOrNot();
        this.sortFields = figureSortFields();
        this.facets = figureFacets();
        this.highlighter = figureHighlighter();
        this.returnFields = figureReturnFields();

        this.fullMap = figureFullMap();
    }

    private Map<String, Object> filteredOrNot() {
        if (query.getFilters().isEmpty()) {
            return new QueryStringMap(query.getQuery()).map;
        } else {
            return buildFilterStructure();
        }
    }

    private Map<String, Object> buildFilterStructure() {
        return tree() //
                .put("bool", tree() //
                        .put("must", new QueryStringMap(query.getQuery()).map) //
                        .put("filter", buildFiltersList())) //
                .asMap();
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
            map.put(name, sortOrder);
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
        return tree() //
                .put("terms", tree() //
                        .put("field", field) //
                        .put("size", ifPositive(query.getFacetLimit())) //
                        .put("min_doc_count",
                                ifPositive(query.getFacetMinCount()))) //
                .asMap();
    }

    private List<String> figureReturnFields() {
        return new ArrayList<>(query.getFieldsToReturn());
    }

    private Map<String, Object> figureFullMap() {
        return tree() //
                .put("query", queryAndFilters) //
                .put("from", ifPositive(query.getStart())) //
                .put("highlight", highlighter)
                .put("size", ifPositive(query.getRows())) //
                .put("sort", sortFields) //
                .put("_source", returnFields) //
                .put("aggregations", facets) //
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
         * 
         * Apparently Solr is willing to put up with query strings that contain
         * special characters in odd places, but Elasticsearch is not.
         * 
         * So, a query string of "classgroup:http://this/that" must be escaped
         * as "classgroup:http\:\/\/this\/that". Notice that the first colon
         * delimits the field name, and so must not be escaped.
         * 
         * But what if no field is specified? Then all colons must be escaped.
         * How would we distinguish that?
         * 
         * And what if the query is more complex, and more than one field is
         * specified? What if other special characters are included?
         * 
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
