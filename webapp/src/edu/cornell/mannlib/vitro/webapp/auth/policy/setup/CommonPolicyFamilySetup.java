/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.setup;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.ActiveIdentifierBundleFactories;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.CommonIdentifierBundleFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.DisplayRestrictedDataByRoleLevelPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.DisplayRestrictedDataToSelfPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.AbortStartup;

/**
 * Set up the common policy family, with Identifier factory.
 */
public class CommonPolicyFamilySetup implements ServletContextListener {
	private static final Log log = LogFactory
			.getLog(CommonPolicyFamilySetup.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();

		if (AbortStartup.isStartupAborted(ctx)) {
			return;
		}

		try {
			ServletPolicyList.addPolicy(ctx,
					new DisplayRestrictedDataByRoleLevelPolicy(ctx));
			ServletPolicyList.addPolicy(ctx,
					new DisplayRestrictedDataToSelfPolicy(ctx));

			// This factory creates Identifiers for all of the above policies.
			CommonIdentifierBundleFactory factory = new CommonIdentifierBundleFactory();

			ActiveIdentifierBundleFactories.addFactory(sce, factory);
		} catch (Exception e) {
			log.error("could not run " + this.getClass().getSimpleName() + ": "
					+ e);
			AbortStartup.abortStartup(ctx);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) { /* nothing */
	}

}
