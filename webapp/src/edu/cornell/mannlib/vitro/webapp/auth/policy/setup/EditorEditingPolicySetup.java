/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.setup;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.ActiveIdentifierBundleFactories;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.EditorEditingIdentifierFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.EditorEditingPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.AbortStartup;

/**
 * Sets up RoleBasedPolicy and IdentifierBundleFactory. This will cause the
 * vitro native login to add Identifiers that can be used by the Auth system and
 * the in-line editing.
 * 
 * To use this add it as a listener to the web.xml.
 * 
 * See RoleBasedPolicy.java
 */
public class EditorEditingPolicySetup implements ServletContextListener {
	private static final Log log = LogFactory
			.getLog(EditorEditingPolicySetup.class.getName());

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();

		if (AbortStartup.isStartupAborted(ctx)) {
			return;
		}

		try {
			log.debug("Setting up EditorEditingPolicy");

			// need to make a policy and add it to the ServletContext
			EditorEditingPolicy cep = new EditorEditingPolicy(ctx);
			ServletPolicyList.addPolicy(ctx, cep);

			// need to put an IdentifierFactory for EditorEditingIds into the
			// ServletContext
			ActiveIdentifierBundleFactories.addFactory(sce,
					new EditorEditingIdentifierFactory());

			log.debug("Finished setting up EditorEditingPolicy: " + cep);
		} catch (Exception e) {
			log.error("could not run EditorEditingPolicySetup: " + e);
			AbortStartup.abortStartup(ctx);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) { /* nothing */
	}
}
