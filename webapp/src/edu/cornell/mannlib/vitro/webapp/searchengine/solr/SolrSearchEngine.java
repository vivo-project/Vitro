/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.solr;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.ServletContext;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineNotRespondingException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchQuery;

/**
 * The Solr-based implementation of SearchEngine.
 */
public class SolrSearchEngine implements SearchEngine {
	private HttpSolrServer server;

	/**
	 * Set up the http connection with the solr server
	 */
	@Override
	public void startup(Application application, ComponentStartupStatus css) {
		ServletContext ctx = application.getServletContext();
		String solrServerUrlString = ConfigurationProperties.getBean(ctx)
				.getProperty("vitro.local.solr.url");
		if (solrServerUrlString == null) {
			css.fatal("Could not find vitro.local.solr.url in "
					+ "runtime.properties.  Vitro application needs the URL of "
					+ "a solr server that it can use to index its data. It "
					+ "should be something like http://localhost:${port}"
					+ ctx.getContextPath() + "solr");
			return;
		}

		try {
			server = new HttpSolrServer(solrServerUrlString);
			server.setSoTimeout(10000); // socket read timeout
			server.setConnectionTimeout(10000);
			server.setDefaultMaxConnectionsPerHost(100);
			server.setMaxTotalConnections(100);
			server.setMaxRetries(1);
			css.info("Set up the Solr search engine; URL = '"
					+ solrServerUrlString + "'.");
		} catch (Exception e) {
			css.fatal("Could not set up the Solr search engine", e);
		}
	}

	@Override
	public void shutdown(Application application) {
		server.shutdown();
	}

	@Override
	public void ping() throws SearchEngineException {
		try {
			server.ping();
		} catch (SolrServerException | IOException e) {
			throw appropriateException("Solr server did not respond to ping.",
					e);
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
		try {
			server.add(SolrConversionUtils.convertToSolrInputDocuments(docs));
		} catch (SolrServerException | IOException e) {
			throw appropriateException("Solr server failed to add documents "
					+ docs, e);
		}
	}

	@Override
	public void commit() throws SearchEngineException {
		try {
			server.commit();
		} catch (SolrServerException | IOException e) {
			throw appropriateException("Failed to commit to Solr server.", e);
		}
	}

	@Override
	public void commit(boolean wait) throws SearchEngineException {
		try {
			server.commit(wait, wait);
		} catch (SolrServerException | IOException e) {
			throw appropriateException("Failed to commit to Solr server.", e);
		}
	}

	@Override
	public void deleteById(String... ids) throws SearchEngineException {
		deleteById(Arrays.asList(ids));
	}

	@Override
	public void deleteById(Collection<String> ids) throws SearchEngineException {
		try {
			server.deleteById(new ArrayList<>(ids));
		} catch (SolrServerException | IOException e) {
			throw appropriateException(
					"Solr server failed to delete documents: " + ids, e);
		}
	}

	@Override
	public void deleteByQuery(String query) throws SearchEngineException {
		try {
			server.deleteByQuery(query);
		} catch (SolrServerException | IOException e) {
			throw appropriateException(
					"Solr server failed to delete documents: " + query, e);
		}
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
	public SearchResponse query(SearchQuery query) throws SearchEngineException {
		try {
			SolrQuery solrQuery = SolrConversionUtils.convertToSolrQuery(query);
			QueryResponse response = server.query(solrQuery);
			return SolrConversionUtils.convertToSearchResponse(response);
		} catch (SolrServerException e) {
			throw appropriateException(
					"Solr server failed to execute the query" + query, e);
		}
	}

	@Override
	public int documentCount() throws SearchEngineException {
		SearchResponse response = query(createQuery("*:*"));
		return (int) response.getResults().getNumFound();
	}

	/**
	 * If there is a SocketTimeoutException in the causal chain for this
	 * exception, then wrap it in a SearchEngineNotRespondingException instead
	 * of a generic SearchEngineException.
	 */
	private SearchEngineException appropriateException(String message,
			Exception e) {
		Throwable cause = e;
		while (cause != null) {
			if (cause instanceof SocketTimeoutException) {
				return new SearchEngineNotRespondingException(message, e);
			}
			cause = cause.getCause();
		}
		return new SearchEngineException(message, e);
	}

}
