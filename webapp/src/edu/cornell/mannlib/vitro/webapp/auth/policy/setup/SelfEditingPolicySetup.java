/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.setup;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.ActiveIdentifierBundleFactories;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.SelfEditingPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.AbortStartup;

/**
 * Policy for SelfEditors. This will set up the self-editing policy which will
 * will look for SelfEditing identifier in the IdentifierBundle. If the user is
 * associated with a URI in the system then they will be allowed to edit
 * resources related to that URI.
 * 
 * To use this add it as a listener to the web.xml.
 */
public class SelfEditingPolicySetup implements ServletContextListener {
	private static final Log log = LogFactory
			.getLog(SelfEditingPolicySetup.class.getName());
	public static final String SELF_EDITING_POLICY_WAS_SETUP = "selfEditingPolicyWasSetup";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();

		if (AbortStartup.isStartupAborted(ctx)) {
			return;
		}

		try {
			log.debug("Setting up SelfEditingPolicy");

			// need to make a policy and add it to the ServletContext
			SelfEditingPolicy cep = new SelfEditingPolicy(ctx);
			ServletPolicyList.addPolicy(ctx, cep);

			// need to put an IdentifierFactory for CuratorEditingIds into the
			// ServletContext
			ActiveIdentifierBundleFactories.addFactory(sce,
					new SelfEditingIdentifierFactory());

			sce.getServletContext().setAttribute(SELF_EDITING_POLICY_WAS_SETUP,
					Boolean.TRUE);

			log.debug("Finished setting up SelfEditingPolicy: " + cep);
		} catch (Exception e) {
			log.error("could not run SelfEditingPolicySetup: " + e);
			AbortStartup.abortStartup(ctx);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) { /* nothing */
	}
}