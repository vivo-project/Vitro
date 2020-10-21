/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.solr;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.request.GenericSolrRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;

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

	private static final Log log = LogFactory.getLog(SolrSearchEngine.class);

	private SolrClient queryEngine;

	private ConcurrentUpdateSolrClient updateEngine;

	// TODO: should be final
	private String solrServerUrl;

	// TODO: should be final
	private String solrCore;

	/**
	 * Set up the http connection with the solr server
	 */
	@Override
	public void startup(Application application, ComponentStartupStatus css) {
		ServletContext ctx = application.getServletContext();

		solrServerUrl = ConfigurationProperties.getBean(ctx)
				.getProperty("vitro.local.solr.url");
		if (solrServerUrl == null) {
			log.error("Solr URL not configured");
			css.fatal("Could not find vitro.local.solr.url in "
					+ "runtime.properties.  Vitro application needs the URL of "
					+ "a solr server that it can use to index its data. It "
					+ "should be something like http://localhost:8983/solr");
			return;
		}

		solrCore = ConfigurationProperties.getBean(ctx)
				.getProperty("vitro.local.solr.core");
		if (solrCore == null) {
			log.error("Solr core not configured");
			css.fatal("Could not find vitro.local.solr.core in "
					+ "runtime.properties.  Vitro application needs the core of "
					+ "a solr server that it can use to index its data. It "
					+ "should be something like vitrocore");
			return;
		}

		try {
			CloseableHttpClient httpClient = buildHttpClient();

			queryEngine = buildSolrClient(httpClient);

			updateEngine = buildConcurrentUpdateSolrClient();

			css.info(format("Connect to Solr; URL = '%s'.", solrServerUrl));
		} catch (Exception e) {
			css.fatal(format("Could not connect to Solr; URL = '%s'.", solrServerUrl), e);
		}

		try {
			if (coreExists()) {
				return;
			}
		} catch (Exception e) {
			css.fatal(format("Failed to check if core %s exists", solrCore), e);
			return;
		}

		String contextPath = StringUtils.removeEnd(ctx.getRealPath(File.separator), File.separator);

		try {
			initializeCore(contextPath);

			css.info(format("Created Solr core; core = '%s'.", solrCore));
		} catch (Exception e) {
			css.fatal(format("Failed to create Solr core; core = '%s'.", solrCore), e);
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
			queryEngine.ping(solrCore);
		} catch (SolrServerException | IOException e) {
			throw appropriateException("Solr server did not respond to ping.", e);
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
			updateEngine.add(solrCore, SolrConversionUtils.convertToSolrInputDocuments(docs), 100);
		} catch (SolrServerException | IOException e) {
			throw appropriateException("Solr server failed to add documents " + docs, e);
		}
	}

	@Override
	public void commit() throws SearchEngineException {
		try {
			updateEngine.commit(solrCore);
			updateEngine.optimize(solrCore);
		} catch (SolrServerException | IOException e) {
			throw appropriateException("Failed to commit to Solr server.", e);
		}
	}

	@Override
	public void commit(boolean wait) throws SearchEngineException {
		try {
			updateEngine.commit(solrCore, wait, wait);
			updateEngine.optimize(solrCore, wait, wait);
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
			updateEngine.deleteById(solrCore, new ArrayList<>(ids), 100);
		} catch (SolrServerException | IOException e) {
			throw appropriateException("Solr server failed to delete documents: " + ids, e);
		}
	}

	@Override
	public void deleteByQuery(String query) throws SearchEngineException {
		try {
			updateEngine.deleteByQuery(solrCore, query, 100);
		} catch (SolrServerException | IOException e) {
			throw appropriateException("Solr server failed to delete documents: " + query, e);
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
			QueryResponse response = queryEngine.query(solrCore, solrQuery);
			return SolrConversionUtils.convertToSearchResponse(response);
		} catch (SolrServerException | IOException e) {
			throw appropriateException("Solr server failed to execute the query" + query, e);
		}
	}

	@Override
	public int documentCount() throws SearchEngineException {
		SearchResponse response = query(createQuery("*:*"));
		return (int) response.getResults().getNumFound();
	}

	private CloseableHttpClient buildHttpClient() {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

		// TODO: configure settings from properties
		httpClientBuilder.setMaxConnPerRoute(100);
		httpClientBuilder.setMaxConnTotal(100);
		httpClientBuilder.setRetryHandler(new StandardHttpRequestRetryHandler(1, false));

		return httpClientBuilder.build();
	}

	private SolrClient buildSolrClient(CloseableHttpClient httpClient) {
		HttpSolrClient.Builder builder = new HttpSolrClient.Builder(solrServerUrl);

		// TODO: configure settings from properties
		builder.withSocketTimeout(10000); // socket read timeout
		builder.withConnectionTimeout(10000);
		builder.withHttpClient(httpClient);

		return builder.build();
	}

	private ConcurrentUpdateSolrClient buildConcurrentUpdateSolrClient() {
		ConcurrentUpdateSolrClient.Builder updateBuilder =
				new ConcurrentUpdateSolrClient.Builder(solrServerUrl);

		// TODO: configure settings from properties
		updateBuilder.withConnectionTimeout(10000);
		// no apparent 7.4.0 analogy to `setPollQueueTime(25)`

		return updateBuilder.build();
	}

	private boolean coreExists() throws SolrServerException, IOException {
		NamedList<String> params = new NamedList<>();
		params.add("wt", "json");
		params.add("action", "STATUS");
		params.add("core", solrCore);
		
		GenericSolrRequest systemRequest = new GenericSolrRequest(METHOD.GET, "/admin/cores", params.toSolrParams());
		NamedList<Object> systemResponse = queryEngine.request(systemRequest);

		log.info(format("Solr core %s status; response = '%s'.", solrCore, systemResponse));

		return Optional.ofNullable(systemResponse.get("status"))
			.map(NamedList.class::cast)
			.map(status -> status.get(solrCore))
			.map(NamedList.class::cast)
			.map(status -> status.get("name"))
			.isPresent();
	}

	private void initializeCore(String contextPath) throws SolrServerException, IOException {
		Path solrConfPath = Paths.get(contextPath + File.separator + "solr");
		UserPrincipal solrConfPathOwner = Files.getOwner(solrConfPath);

		NamedList<String> params = new NamedList<>();
		params.add("wt", "json");
		
		GenericSolrRequest systemRequest = new GenericSolrRequest(METHOD.GET, "/admin/info/system", params.toSolrParams());
		NamedList<Object> systemResponse = queryEngine.request(systemRequest);
		String solrHome = systemResponse.get("solr_home").toString();

		Path vitroCoreConfPath = Paths.get(solrHome + File.separator + solrCore);

		FileUtils.copyDirectory(solrConfPath.toFile(), vitroCoreConfPath.toFile());

		Files.setOwner(vitroCoreConfPath, solrConfPathOwner);

		CoreAdminRequest.Create createCoreRequest = new CoreAdminRequest.Create();
		createCoreRequest.setDataDir(solrHome + File.separator + solrCore + File.separator + "data");
		createCoreRequest.setInstanceDir(solrHome + File.separator + solrCore);
		createCoreRequest.setCoreName(solrCore);

		NamedList<Object> createCoreResponse = queryEngine.request(createCoreRequest);

		if (Optional.ofNullable(createCoreResponse.get("core")).isPresent()) {
			log.info(format("Created Solr core; response = '%s'.", createCoreResponse));
		} else {
			log.error(format("Failed to create Solr core; response = '%s'.", createCoreResponse));
		}
	}

	/**
	 * If there is a SocketTimeoutException in the causal chain for this
	 * exception, then wrap it in a SearchEngineNotRespondingException instead
	 * of a generic SearchEngineException.
	 */
	private SearchEngineException appropriateException(String message, Exception e) {
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
