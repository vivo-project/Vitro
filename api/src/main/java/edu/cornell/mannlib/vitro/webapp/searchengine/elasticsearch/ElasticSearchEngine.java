/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchQuery;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Validation;

/**
 * A first draft of an Elasticsearch implementation.
 */
public class ElasticSearchEngine implements SearchEngine {
    private static final Log log = LogFactory.getLog(ElasticSearchEngine.class);

    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    private String baseUrl;

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasBaseUrl")
    public void setBaseUrl(String url) {
        if (baseUrl == null) {
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
            baseUrl = url;
        } else {
            throw new IllegalStateException(
                    "Configuration includes multiple base URLs: " + url
                            + ", and " + baseUrl);
        }
    }

    @Validation
    public void validate() throws Exception {
        if (baseUrl == null) {
            throw new IllegalStateException(
                    "Configuration did not include a base URL.");
        }
    }

    // ----------------------------------------------------------------------
    // The instance
    // ----------------------------------------------------------------------

    @Override
    public void startup(Application application, ComponentStartupStatus ss) {
        log.warn("ElasticSearchEngine.startup() not implemented."); // TODO
    }

    @Override
    public void shutdown(Application application) {
        // TODO Flush the buffers
        log.warn("ElasticSearchEngine.shutdown not implemented.");
    }

    @Override
    public void ping() throws SearchEngineException {
        // TODO What's the simplest we can do? Another smoke test?
        log.warn("ElasticSearchEngine.ping() not implemented."); // TODO
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
