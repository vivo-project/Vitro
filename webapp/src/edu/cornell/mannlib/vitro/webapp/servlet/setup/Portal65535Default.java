/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;

/**
 * This is a stopgap solution so we can make VIVO default to the ALL (65535) portal 
 * without affecting other clones.  We'll just put this in VIVO's web.xml listener list.
 * @author bjl23
 */
public class Portal65535Default implements ServletContextListener {
	
	public void contextInitialized(ServletContextEvent arg0) {
		Portal.DEFAULT_PORTAL_ID = 65535;
	}

	public void contextDestroyed(ServletContextEvent arg0) {
		// nothing to do here
	}
	
}
