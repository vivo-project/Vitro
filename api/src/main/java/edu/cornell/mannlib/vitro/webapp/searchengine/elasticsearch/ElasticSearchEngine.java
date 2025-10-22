/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchQuery;
import edu.cornell.mannlib.vitro.webapp.utils.http.ESHttpBasicClientFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;

/**
 * A first version of an Elasticsearch engine implementation.
 */
public class ElasticSearchEngine implements SearchEngine {
    private static final Log log = LogFactory.getLog(ElasticSearchEngine.class);

    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    private String baseUrl;

    // ----------------------------------------------------------------------
    // The instance
    // ----------------------------------------------------------------------

    @Override
    public void startup(Application application, ComponentStartupStatus css) {
        String elasticUrlString = ConfigurationProperties.getInstance().getProperty("vitro.local.searchengine.url", "");
        if (elasticUrlString.isEmpty()) {
            css.fatal("Can't connect to ElasticSearch engine. "
                + "runtime.properties must contain a value for "
                + "vitro.local.searchengine.url");
        }

        baseUrl = elasticUrlString;
    }

    @Override
    public void shutdown(Application application) {
        try {
            new ESFlusher(baseUrl).flush(true);
        } catch (SearchEngineException e) {
            log.warn("Unexpected error upon Elasticsearch engine shutdown. A component has thrown an error: " +
                e.getMessage());
        }
    }

    @Override
    public void ping() throws SearchEngineException {
        HttpHead httpHead = new HttpHead(baseUrl);
        HttpClient httpClient = ESHttpBasicClientFactory.getHttpClient(baseUrl);

        try {
            HttpResponse response = httpClient.execute(httpHead);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new SearchEngineException(
                    "Failed to ping Elasticsearch - ES responded with status code " + statusCode);
            }
        } catch (SearchEngineException | IOException e) {
            throw new SearchEngineException("Failed to put to Elasticsearch - request failed");
        }
    }

    @Override
    public SearchInputDocument createInputDocument() {
        return new BaseSearchInputDocument();
    }

    @Override
    public void add(SearchInputDocument... docs) throws SearchEngineException {
        add(Arrays.asList(docs));
    }

    @Override
    public void add(Collection<SearchInputDocument> docs)
        throws SearchEngineException {
        new ESAdder(baseUrl).add(docs);
    }

    @Override
    public void commit() throws SearchEngineException {
        new ESFlusher(baseUrl).flush();
    }

    @Override
    public void commit(boolean wait) throws SearchEngineException {
        new ESFlusher(baseUrl).flush(wait);
    }

    @Override
    public void deleteById(String... ids) throws SearchEngineException {
        deleteById(Arrays.asList(ids));
    }

    @Override
    public void deleteById(Collection<String> ids)
        throws SearchEngineException {
        new ESDeleter(baseUrl).deleteByIds(ids);
    }

    @Override
    public void deleteByQuery(String query) throws SearchEngineException {
        new ESDeleter(baseUrl).deleteByQuery(query);
    }

    @Override
    public SearchQuery createQuery() {
        return new BaseSearchQuery();
    }

    @Override
    public SearchQuery createQuery(String queryText) {
        BaseSearchQuery query = new BaseSearchQuery();
        query.setQuery(queryText);
        return query;
    }

    @Override
    public SearchResponse query(SearchQuery query)
        throws SearchEngineException {
        return new ESQuery(baseUrl).query(query);
    }

    @Override
    public int documentCount() throws SearchEngineException {
        return new ESCounter(baseUrl).count();
    }
}
