/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import edu.cornell.mannlib.vitro.webapp.utils.http.HttpClientFactory;
import edu.cornell.mannlib.vitro.webapp.utils.http.ESHttpBasicClientFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;

/**
 * Convert a SearchQuery to JSON, send it to Elasticsearch, and convert the JSON
 * response to a SearchResponse.
 */
public class ESQuery {
    private static final Log log = LogFactory.getLog(ESQuery.class);

    private final String baseUrl;

    public ESQuery(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public SearchResponse query(SearchQuery query)
            throws SearchEngineException {
        boolean treatAsStructuredQuery = !query.isSimpleQuery();

        String queryString = new QueryConverter(query, treatAsStructuredQuery).asString();
        String response = doTheQuery(queryString);
        return new ResponseParser(response)
            .parse(query.getFacetTextToMatch(), query.isFacetTextCompareCaseInsensitive());
    }

    private String doTheQuery(String queryString) {
        log.debug("QUERY: " + queryString);
        try {
            String url = baseUrl + "/_search";
            HttpResponse response = new ESFunkyGetRequest(url)
                    .bodyString(queryString, ContentType.APPLICATION_JSON)
                    .execute();
            String responseString = IOUtils
                    .toString(response.getEntity().getContent());
            log.debug("RESPONSE: " + responseString);
            return responseString;
        } catch (Exception e) {
            log.error("Failed to put to Elasticsearch", e);
            return "";
        }
    }

    // ----------------------------------------------------------------------
    // Helper class -- a GET request that accepts a body
    // ----------------------------------------------------------------------

    /**
     * The HttpClient implementations, both regular and conversational, do not
     * allow you to put a body on a GET request. In online discussion, some say
     * that the HTTP spec is ambiguous on this point, so each implementation
     * makes its own choice. For example, CURL allows it.
     *
     * More to the point however, is that ElasticSearch requires it. So here's a
     * simple class to make that possible.
     *
     * USE POST INSTEAD!!
     */
    private static class ESFunkyGetRequest
            extends HttpEntityEnclosingRequestBase {
        public ESFunkyGetRequest(String url) throws SearchEngineException {
            super();
            try {
                setURI(new URI(url));
            } catch (URISyntaxException e) {
                throw new SearchEngineException(e);
            }
        }

        public ESFunkyGetRequest bodyString(String contents,
                ContentType contentType) {
            setEntity(new StringEntity(contents, contentType));
            return this;
        }

        public HttpResponse execute() throws SearchEngineException {
            try {
                if (this.getURI().getScheme().equals("https")) {
                    return ESHttpBasicClientFactory.getHttpsClient().execute(this);
                }
                return ESHttpBasicClientFactory.getHttpClient().execute(this);
            } catch (IOException e) {
                throw new SearchEngineException(e);
            }
        }

        @Override
        public String getMethod() {
            return "GET";
        }

    }

}
