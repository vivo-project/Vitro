/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputField;

/**
 * The nuts and bolts of adding a document to the Elasticsearch index
 */
public class ESAdder {
    private static final Log log = LogFactory.getLog(ESAdder.class);

    private final String baseUrl;

    public ESAdder(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void add(Collection<SearchInputDocument> docs)
            throws SearchEngineException {
        for (SearchInputDocument doc : docs) {
            addDocument(doc);
        }
    }

    private void addDocument(SearchInputDocument doc)
            throws SearchEngineException {
        try {
            Map<String, List<Object>> map = convertDocToMap(doc);
            String json = new ObjectMapper().writeValueAsString(map);
            log.debug("Adding document for '" + doc.getField("DocId") + "': "
                    + json);

            putToElastic(json, (String) doc.getField("DocId").getFirstValue());
        } catch (Exception e) {
            throw new SearchEngineException("Failed to convert to JSON", e);
        }
    }

    /**
     * Some field values are collections. Add the members of the collection
     * instead.
     */
    private Map<String, List<Object>> convertDocToMap(SearchInputDocument doc) {
        Map<String, List<Object>> map = new HashMap<>();
        for (SearchInputField field : doc.getFieldMap().values()) {
            ArrayList<Object> list = new ArrayList<>();
            for (Object value : field.getValues()) {
                if (value instanceof Collection) {
                    Collection<?> cValue = (Collection<?>) value;
                    list.addAll(cValue);
                } else {
                    list.add(value);
                }
            }
            map.put(field.getName(), list);
        }
        return map;
    }

    private void putToElastic(String json, String docId)
            throws SearchEngineException {
        try {
            String url = baseUrl + "/_doc/"
                    + URLEncoder.encode(docId, "UTF8");
            Response response = Request.Put(url)
                    .bodyString(json, ContentType.APPLICATION_JSON).execute();
            log.debug("Response from Elasticsearch: "
                    + response.returnContent().asString());
        } catch (Exception e) {
            throw new SearchEngineException("Failed to put to Elasticsearch",
                    e);
        }
    }
}
