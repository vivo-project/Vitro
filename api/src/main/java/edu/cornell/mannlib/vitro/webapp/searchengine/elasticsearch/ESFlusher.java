/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.utils.http.ESHttpBasicClientFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

/**
 * Just does a "commit" or "flush" to the index.
 */
public class ESFlusher {
    private static final Log log = LogFactory.getLog(ESFlusher.class);

    private final String baseUrl;

    public ESFlusher(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void flush() throws SearchEngineException {
        flush(false);
    }

    public void flush(boolean wait) throws SearchEngineException {
        try {
            String url = baseUrl + "/_flush"
                    + (wait ? "?wait_for_ongoing" : "");
            HttpClient httpClient = ESHttpBasicClientFactory.getHttpClient(baseUrl);
            HttpResponse response = httpClient.execute(new HttpGet(url));
            String json = EntityUtils.toString(response.getEntity());
            log.debug("flush response: " + json);
        } catch (Exception e) {
            throw new SearchEngineException("Failed to put to Elasticsearch",
                    e);
        }
    }

}
