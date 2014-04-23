/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.application;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.searchengine.SearchEngineWrapper;
import edu.cornell.mannlib.vitro.webapp.searchengine.solr.SolrSearchEngine;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * The basic implementation of the Application interface.
 */
public class ApplicationImpl implements Application {
	// ----------------------------------------------------------------------
	// The instance
	// ----------------------------------------------------------------------

	private final ServletContext ctx;
	private SearchEngine searchEngine;

	public ApplicationImpl(ServletContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public ServletContext getServletContext() {
		return ctx;
	}

	@Override
	public SearchEngine getSearchEngine() {
		return searchEngine;
	}

	public void setSearchEngine(SearchEngine searchEngine) {
		this.searchEngine = searchEngine;
	}

	// ----------------------------------------------------------------------
	// The Setup class.
	// ----------------------------------------------------------------------

	public static class Setup implements ServletContextListener {

		@Override
		public void contextInitialized(ServletContextEvent sce) {
			ServletContext ctx = sce.getServletContext();
			StartupStatus ss = StartupStatus.getBean(ctx);

			try {
				SearchEngine searchEngine = new SearchEngineWrapper(
						new SolrSearchEngine());

				ApplicationImpl application = new ApplicationImpl(ctx);
				application.setSearchEngine(searchEngine);
				ApplicationUtils.setInstance(application);
				ss.info(this, "Appliation is configured.");
			} catch (Exception e) {
				ss.fatal(this, "Failed to initialize the Application.", e);
			}
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce) {
			// Nothing to tear down.
		}

	}
}
