/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.WebappDaoFactoryFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilterUtils;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.search.SearchIndexer;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;
import edu.cornell.mannlib.vitro.webapp.search.indexing.SearchReindexingListener;
import edu.cornell.mannlib.vitro.webapp.searchindex.indexing.IndexingUriFinder;
import edu.cornell.mannlib.vitro.webapp.startup.ComponentStartupStatusImpl;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;
import edu.cornell.mannlib.vitro.webapp.utils.developer.Key;
import edu.cornell.mannlib.vitro.webapp.utils.developer.listeners.DeveloperDisabledModelChangeListener;

/**
 * TODO A silly implementation that just wraps the old IndexBuilder with a new
 * SearchIndexerImpl.
 */
public class SearchIndexerSetup implements ServletContextListener {
	private static final Log log = LogFactory.getLog(SearchIndexerSetup.class);

	private ServletContext ctx;
	private OntModel displayModel;
	private ConfigurationBeanLoader beanLoader;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		this.ctx = sce.getServletContext();
		this.displayModel = ModelAccess.on(ctx).getOntModel(DISPLAY);
		this.beanLoader = new ConfigurationBeanLoader(displayModel, ctx);

		ServletContext context = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(context);
		SearchEngine searchEngine = ApplicationUtils.instance()
				.getSearchEngine();

		{ // >>>>> TODO
			try {
//				/* setup search indexer */
//				SearchIndexer searchIndexer = new SearchIndexer(searchEngine,
//						indToSearchDoc);
//
//				// Make the IndexBuilder
//				IndexBuilder builder = new IndexBuilder(searchIndexer, wadf,
//						uriFinders);
//
//				// Create listener to notify index builder of changes to model
//				// (can be disabled by developer setting.)
//				ModelContext
//						.registerListenerForChanges(
//								context,
//								new DeveloperDisabledModelChangeListener(
//										new SearchReindexingListener(builder),
//										Key.SEARCH_INDEX_SUPPRESS_MODEL_CHANGE_LISTENER));
//
//				ss.info(this, "Setup of search indexer completed.");
//
			} catch (Throwable e) {
				ss.fatal(this, "could not setup search engine", e);
			}
		}
		ApplicationUtils
				.instance()
				.getSearchIndexer()
				.startup(ApplicationUtils.instance(),
						new ComponentStartupStatusImpl(this, ss));
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ApplicationUtils.instance().getSearchIndexer()
				.shutdown(ApplicationUtils.instance());

		{ // >>>>> TODO
			IndexBuilder builder = (IndexBuilder) sce.getServletContext()
					.getAttribute(IndexBuilder.class.getName());
			if (builder != null)
				builder.stopIndexingThread();
		}
	}

}
