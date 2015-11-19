/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.migration.rel17;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.servlet.setup.UpdateKnowledgeBase;

/**
 * Call UpdateKnowledgeBase; migrate from release 1.6 to release 1.7
 * 
 * Remove permissions that are no longer used.
 */
public class Release17Migrator implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		new UpdateKnowledgeBase("/WEB-INF/ontologies/update16to17/", this).contextInitialized(sce);
		
		new RemoveObsoletePermissions().contextInitialized(sce);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to do
	}

}
