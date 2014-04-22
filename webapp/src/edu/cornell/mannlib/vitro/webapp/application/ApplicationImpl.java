/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.application;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.searchengine.SearchEngineWrapper;
import edu.cornell.mannlib.vitro.webapp.searchengine.solr.SolrSearchEngine;

/**
 * The basic implementation of the Application interface.
 */
public class ApplicationImpl implements Application {
	private final ServletContext ctx;
	private SearchEngine searchEngine;

	public ApplicationImpl(ServletContext ctx) {
		this.ctx = ctx;
		setSearchEngine(new SolrSearchEngine());
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
		if (searchEngine instanceof SearchEngineWrapper) {
			this.searchEngine = searchEngine;
		} else {
			this.searchEngine = new SearchEngineWrapper(searchEngine);
		}
	}
}
