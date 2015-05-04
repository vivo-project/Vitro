/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.migration.rel18;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.servlet.setup.UpdateKnowledgeBase;

/**
 * Handle the tasks that move an installation from 1.7 to 1.8.
 */
public class Release18Migrator implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();

		new UpdateKnowledgeBase("/WEB-INF/ontologies/update17to18/", this).contextInitialized(sce);

		new FauxPropertiesUpdater(ctx, this).migrate();
		new RemoveObsoleteMetadataGraphs(ctx, this).migrate();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to tear down.
	}

}
