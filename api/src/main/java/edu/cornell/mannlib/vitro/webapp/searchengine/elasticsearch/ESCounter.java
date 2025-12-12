/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.utils.http.ESHttpBasicClientFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

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
            String url = baseUrl + "/_count";
            HttpClient httpClient = ESHttpBasicClientFactory.getHttpClient(baseUrl);
            HttpResponse response = httpClient.execute(new HttpGet(url));
            String json = EntityUtils.toString(response.getEntity());

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
