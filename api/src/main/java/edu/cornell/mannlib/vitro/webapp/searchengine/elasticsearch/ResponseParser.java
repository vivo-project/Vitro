/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
            this.responseMap = new ObjectMapper().readValue(responseString,
                    HashMap.class);
        } catch (IOException e) {
            throw new SearchEngineException(e);
        }
    }

    public SearchResponse parse() {
        parseDocumentList();
        parseFacetFields();
        SearchResponse response = new BaseSearchResponse(highlightingMap,
                facetFieldsMap,
                new ElasticSearchResultDocumentList(documentList, totalHits));
        log.debug("ESQuery.ResponseParser.parse: " + response);
        return response;
    }

    private void parseFacetFields() {
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
                parseFacetField(name, aggregations.get(key));
            }
        }
    }

    private void parseFacetField(String name, Map<String, Object> facetMap) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> bucketsList = (List<Map<String, Object>>) facetMap
                .get("buckets");
        if (bucketsList == null) {
            return;
        }

        List<Count> counts = new ArrayList<>();
        for (Map<String, Object> bucket : bucketsList) {
            counts.add(new BaseCount((String) bucket.get("key"),
                    (Integer) bucket.get("doc_count")));
        }

        facetFieldsMap.put(name, new BaseSearchFacetField(name, counts));
    }

    private void parseDocumentList() {
        documentList = new ArrayList<>();
        highlightingMap = new HashMap<>();

        @SuppressWarnings("unchecked")
        Map<String, Object> uberHits = (Map<String, Object>) responseMap
                .get("hits");
        if (uberHits == null) {
            log.warn("Didn't find a 'hits' field " + "in the query response: "
                    + responseMap);
            return;
        }

        Integer total = (Integer) uberHits.get("total");
        if (total == null) {
            log.warn("Didn't find a 'hits.total' field "
                    + "in the query response: " + responseMap);
            return;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> hits = (List<Map<String, Object>>) uberHits
                .get("hits");
        if (hits == null) {
            log.warn("Didn't find a 'hits.hits' field "
                    + "in the query response: " + responseMap);
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
        Map<String, Collection<Object>> sourceMap = (Map<String, Collection<Object>>) hitMap
                .get("_source");
        if (sourceMap == null) {
            log.warn("Didn't find a '_source' field in the hit: " + hitMap);
            return null;
        }

        String id = (String) hitMap.get("_id");
        if (id == null) {
            log.warn("Didn't find a '_id' field in the hit: " + hitMap);
            return null;
        }

        return new BaseSearchResultDocument(id, sourceMap);
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