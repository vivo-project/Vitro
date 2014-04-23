/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.startup.ComponentStartupStatusImpl;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Whatever search engine we have, start it up and shut it down.
 */
public class SearchEngineSetup implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		Application application = ApplicationUtils.instance();
		StartupStatus ss = StartupStatus.getBean(sce.getServletContext());
		ComponentStartupStatus css = new ComponentStartupStatusImpl(this, ss);
		application.getSearchEngine().startup(application, css);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		Application application = ApplicationUtils.instance();
		application.getSearchEngine().shutdown(application);
	}

}
