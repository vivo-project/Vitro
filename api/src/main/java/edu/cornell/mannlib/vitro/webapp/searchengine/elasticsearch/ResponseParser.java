/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchFacetField;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchFacetField.Count;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocument;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchFacetField;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchFacetField.BaseCount;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchResponse;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchResultDocument;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Elastic search sends a JSON response to a query. parse it to a
 * SearchResponse.
 */
class ResponseParser {
    private static final Log log = LogFactory.getLog(ResponseParser.class);

    private final Map<String, Object> responseMap;

    private Map<String, Map<String, List<String>>> highlightingMap;
    private Map<String, SearchFacetField> facetFieldsMap;
    private long totalHits;
    private List<SearchResultDocument> documentList;

    @SuppressWarnings("unchecked")
    public ResponseParser(String responseString) throws SearchEngineException {
        try {
            this.responseMap = new ObjectMapper().readValue(responseString, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            throw new SearchEngineException(e);
        }
    }

    public SearchResponse parse(String facetTextToMatch, boolean isFacetTextCompareCaseInsensitive) {
        parseDocumentList();
        parseFacetFields(facetTextToMatch, isFacetTextCompareCaseInsensitive);
        SearchResponse response = new BaseSearchResponse(highlightingMap,
            facetFieldsMap,
            new ElasticSearchResultDocumentList(documentList, totalHits));
        log.debug("ESQuery.ResponseParser.parse: " + response);
        return response;
    }

    private void parseFacetFields(String facetTextToMatch, boolean isFacetTextCompareCaseInsensitive) {
        facetFieldsMap = new HashMap<>();

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> aggregations = (Map<String, Map<String, Object>>) responseMap
            .get("aggregations");
        if (aggregations == null) {
            return;
        }

        for (String key : aggregations.keySet()) {
            if (key.startsWith("facet_")) {
                String name = key.substring(6);
                parseFacetField(name, aggregations.get(key), facetTextToMatch, isFacetTextCompareCaseInsensitive);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void parseFacetField(String name, Map<String, Object> facetMap, String facetText, boolean ignoreCase) {
        List<Map<String, Object>> bucketsList = (List<Map<String, Object>>) facetMap.get("buckets");
        if (bucketsList == null) {
            return;
        }

        List<Count> counts = new ArrayList<>();
        for (Map<String, Object> bucket : bucketsList) {
            String key = (String) bucket.get("key");
            int count = (Integer) bucket.get("doc_count");

            String label = key;  // default value

            // Not needed initially, but useful as a POC for enhancing implementation
            // if we want to support this functionality with URI-key aggregations
            Map<String, Object> topLabel = (Map<String, Object>) bucket.get("top_label");
            if (topLabel != null && key.contains("/individual/")) {
                Map<String, Object> hits = (Map<String, Object>) topLabel.get("hits");
                if (hits != null) {
                    List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");
                    if (hitList != null && !hitList.isEmpty()) {
                        Map<String, Object> firstHit = hitList.get(0);
                        Map<String, Object> source = (Map<String, Object>) firstHit.get("_source");
                        if (source != null) {
                            for (Map.Entry<String, Object> entry : source.entrySet()) {
                                String fieldName = entry.getKey();
                                if (fieldName.endsWith("es_label_display") && entry.getValue() instanceof List) {
                                    List<?> labels = (List<?>) entry.getValue();
                                    if (!labels.isEmpty()) {
                                        label = String.valueOf(labels.get(0));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (facetText != null) {
                boolean matches = ignoreCase
                    ? label.toLowerCase().contains(facetText.toLowerCase())
                    : label.contains(facetText);
                if (!matches) {
                    continue;
                }
            }

            counts.add(new BaseCount(key, count));
        }
        facetFieldsMap.put(name, new BaseSearchFacetField(name, counts));
    }

    private void parseDocumentList() {
        documentList = new ArrayList<>();
        highlightingMap = new HashMap<>();

        @SuppressWarnings("unchecked")
        Map<String, Object> uberHits = (Map<String, Object>) responseMap.get("hits");
        if (uberHits == null) {
            log.warn("Didn't find a 'hits' field in the query response: " + responseMap);
            return;
        }

        // Updated handling of the 'total' field
        @SuppressWarnings("unchecked")
        Map<String, Object> totalMap = (Map<String, Object>) uberHits.get("total");
        if (totalMap == null) {
            log.warn("Didn't find a 'hits.total' field in the query response: " + responseMap);
            return;
        }
        Integer total = ((Number) totalMap.get("value")).intValue(); // Extract the integer value

        if (total == null) {
            log.warn("Didn't find a 'hits.total.value' field in the query response: " + responseMap);
            return;
        }
        totalHits = total;

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> hits = (List<Map<String, Object>>) uberHits.get("hits");
        if (hits == null) {
            log.warn("Didn't find a 'hits.hits' field in the query response: " + responseMap);
            return;
        }

        parseDocuments(hits);
    }


    private void parseDocuments(List<Map<String, Object>> hits) {
        for (Map<String, Object> hit : hits) {
            SearchResultDocument doc = parseDocument(hit);
            if (doc != null) {
                documentList.add(doc);

                Map<String, List<String>> highlight = parseHighlight(hit);
                if (highlight != null) {
                    highlightingMap.put(doc.getUniqueId(), highlight);
                }
            }
        }
    }

    private SearchResultDocument parseDocument(Map<String, Object> hitMap) {
        @SuppressWarnings("unchecked")
        Map<String, Object> sourceMap = (Map<String, Object>) hitMap.get("_source");
        if (sourceMap == null) {
            log.warn("Didn't find a '_source' field in the hit: " + hitMap);
            return null;
        }

        String id = (String) hitMap.get("_id");
        if (id == null) {
            log.warn("Didn't find a '_id' field in the hit: " + hitMap);
            return null;
        }

        Map<String, Collection<Object>> parsedSourceMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Collection) {
                parsedSourceMap.put(entry.getKey(), (Collection<Object>) value);
            } else if (value instanceof Map) {
                // This is done assuming the only "Map" field will be a _drsim field
                parsedSourceMap.put(entry.getKey(), Collections.singletonList(
                    ((Map<String, String>) value).get("gte") + " TO " + ((Map<String, String>) value).get("lte"))
                );
            } else {
                parsedSourceMap.put(entry.getKey(), Collections.singletonList(value));
            }
        }

        return new BaseSearchResultDocument(id, parsedSourceMap);
    }

    private Map<String, List<String>> parseHighlight(
        Map<String, Object> hitMap) {
        @SuppressWarnings("unchecked")
        Map<String, List<String>> highlightMap = (Map<String, List<String>>) hitMap
            .get("highlight");
        if (highlightMap == null) {
            log.debug("Didn't find a 'highlight' field in the hit: " + hitMap);
            return null;
        }

        @SuppressWarnings("unchecked")
        List<String> snippets = highlightMap.get("ALLTEXT");
        if (snippets == null) {
            log.warn("Didn't find a 'highlight.ALLTEXT' field in the hit: "
                + hitMap);
            return null;
        }

        Map<String, List<String>> snippetMap = new HashMap<>();
        snippetMap.put("ALLTEXT", snippets);
        return snippetMap;
    }
}