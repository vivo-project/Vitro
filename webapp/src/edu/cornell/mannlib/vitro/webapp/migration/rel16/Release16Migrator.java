/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.migration.rel16;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.servlet.setup.UpdateKnowledgeBase;

/**
 * Call UpdateKnowledgeBase; migrate from release 1.5 to release 1.6
 */
public class Release16Migrator implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		new UpdateKnowledgeBase("/WEB-INF/ontologies/update15to16/", this).contextInitialized(sce);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to do
	}

}
