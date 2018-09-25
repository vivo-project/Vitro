/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;

/**
 * The nuts and bolts of getting the number of documents in the Elasticsearch
 * index.
 */
public class ESCounter {
    private final String baseUrl;

    public ESCounter(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int count() throws SearchEngineException {
        try {
            String url = baseUrl + "/_doc/_count";
            Response response = Request.Get(url).execute();
            String json = response.returnContent().asString();

            @SuppressWarnings("unchecked")
            Map<String, Object> map = new ObjectMapper().readValue(json,
                    HashMap.class);
            return (Integer) map.get("count");
        } catch (Exception e) {
            throw new SearchEngineException("Failed to put to Elasticsearch",
                    e);
        }
    }

}
