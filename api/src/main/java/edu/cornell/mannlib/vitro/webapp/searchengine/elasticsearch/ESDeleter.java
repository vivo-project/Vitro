/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchQuery;

/**
 * The nuts and bolts of deleting documents from the Elasticsearch index.
 */
public class ESDeleter {
    private static final Log log = LogFactory.getLog(ESDeleter.class);

    private final String baseUrl;

    /**
     * @param baseUrl
     */
    public ESDeleter(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void deleteByIds(Collection<String> ids)
            throws SearchEngineException {
        for (String id : ids) {
            deleteById(id);
        }
    }

    private void deleteById(String id) throws SearchEngineException {
        try {
            String url = baseUrl + "/_doc/"
                    + URLEncoder.encode(id, "UTF8");
            Response response = Request.Delete(url).execute();
            String json = response.returnContent().asString();
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == 404) {
                // Don't care if it has already been deleted.
            } else {
                throw new SearchEngineException(
                        "Failed to delete Elasticsearch document " + id, e);
            }
        } catch (Exception e) {
            throw new SearchEngineException(
                    "Failed to delete Elasticsearch document " + id, e);
        }
    }

    public void deleteByQuery(String queryString) throws SearchEngineException {
        String url = baseUrl + "/_delete_by_query";
        SearchQuery query = new BaseSearchQuery().setQuery(queryString);
        String queryJson = new QueryConverter(query).asString();

        try {
            Response response = Request.Post(url)
                    .bodyString(queryJson, ContentType.APPLICATION_JSON)
                    .execute();

            BaseResponseHandler handler = new BaseResponseHandler();
            response.handleResponse(handler);
            if (handler.getStatusCode() >= 400) {
                log.warn(String.format(
                        "Failed to delete Elasticsearch documents by query: %s, %d - %s\n%s",
                        queryString, handler.getStatusCode(),
                        handler.getReasonPhrase(), handler.getContentString()));
            }
        } catch (IOException e) {
            throw new SearchEngineException("Failed to delete Elasticsearch "
                    + "documents by query " + queryString, e);
        }
    }

    // ----------------------------------------------------------------------
    // Helper class for interpreting HttpResponse errors
    // ----------------------------------------------------------------------

    private class BaseResponseHandler implements ResponseHandler<Object> {
        private int statusCode;
        private String reasonPhrase;
        private Map<String, List<String>> headers;
        private String contentString;

        @Override
        public Object handleResponse(org.apache.http.HttpResponse innerResponse)
                throws IOException {
            StatusLine statusLine = innerResponse.getStatusLine();
            statusCode = statusLine.getStatusCode();
            reasonPhrase = statusLine.getReasonPhrase();

            headers = new HashMap<>();
            for (Header header : innerResponse.getAllHeaders()) {
                String name = header.getName();
                if (!headers.containsKey(name)) {
                    headers.put(name, new ArrayList<String>());
                }
                headers.get(name).add(header.getValue());
            }

            HttpEntity entity = innerResponse.getEntity();
            if (entity == null) {
                contentString = "";
            } else {
                contentString = EntityUtils.toString(entity);
            }
            return "";
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getReasonPhrase() {
            return reasonPhrase;
        }

        public Map<String, List<String>> getHeaders() {
            return headers;
        }

        public String getContentString() {
            return contentString;
        }

    }

}
