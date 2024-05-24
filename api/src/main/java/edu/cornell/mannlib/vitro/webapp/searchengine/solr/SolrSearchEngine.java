/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.solr;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.ServletContext;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.config.ContextPath;
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
	private SolrClient queryEngine;
	private ConcurrentUpdateSolrClient updateEngine;

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
					+ ContextPath.getPath(ctx) + "solr");
			return;
		}

		try {
			HttpSolrClient.Builder builder = new HttpSolrClient.Builder(solrServerUrlString);

			builder.withSocketTimeout(10000); // socket read timeout
			builder.withConnectionTimeout(10000);

			HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
			httpClientBuilder.setMaxConnPerRoute(100);
			httpClientBuilder.setMaxConnTotal(100);
			httpClientBuilder.setRetryHandler(new StandardHttpRequestRetryHandler(1, false));

			builder.withHttpClient(httpClientBuilder.build());

			queryEngine = builder.build();

			ConcurrentUpdateSolrClient.Builder updateBuilder =
					new ConcurrentUpdateSolrClient.Builder(solrServerUrlString);
			updateBuilder.withConnectionTimeout(10000);
			// no apparent 7.4.0 analogy to `setPollQueueTime(25)`

			updateEngine = updateBuilder.build();
			
			SolrFieldInitializer.initializeFields(queryEngine, updateEngine);
			
			css.info("Set up the Solr search engine; URL = '" + solrServerUrlString + "'.");
		} catch (Exception e) {
			css.fatal("Could not set up the Solr search engine", e);
		}
	}

	@Override
	public void shutdown(Application application) {
		try {
			queryEngine.close();
		} catch (IOException e) {
			throw new RuntimeException("Error shutting down 'queryEngine'", e);
		}
		updateEngine.shutdownNow();
	}

	@Override
	public void ping() throws SearchEngineException {
		try {
			queryEngine.ping();
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
			updateEngine.add(SolrConversionUtils.convertToSolrInputDocuments(docs), 100);
		} catch (SolrServerException | IOException e) {
			throw appropriateException("Solr server failed to add documents "
					+ docs, e);
		}
	}

	@Override
	public void commit() throws SearchEngineException {
		try {
			updateEngine.commit();
			updateEngine.optimize();
		} catch (SolrServerException | IOException e) {
			throw appropriateException("Failed to commit to Solr server.", e);
		}
	}

	@Override
	public void commit(boolean wait) throws SearchEngineException {
		try {
			updateEngine.commit(wait, wait);
			updateEngine.optimize(wait, wait);
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
			updateEngine.deleteById(new ArrayList<>(ids), 100);
		} catch (SolrServerException | IOException e) {
			throw appropriateException(
					"Solr server failed to delete documents: " + ids, e);
		}
	}

	@Override
	public void deleteByQuery(String query) throws SearchEngineException {
		try {
			updateEngine.deleteByQuery(query, 100);
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
			QueryResponse response = queryEngine.query(solrQuery);
			return SolrConversionUtils.convertToSearchResponse(response);
		} catch (SolrServerException | IOException e) {
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
