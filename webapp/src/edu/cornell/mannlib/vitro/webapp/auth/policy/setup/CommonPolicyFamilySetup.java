/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.setup;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.ActiveIdentifierBundleFactories;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.CommonIdentifierBundleFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.DisplayRestrictedDataByRoleLevelPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.DisplayRestrictedDataToSelfPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.EditRestrictedDataByRoleLevelPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.SelfEditingPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.UseRestrictedPagesByRoleLevelPolicy;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Set up the common policy family, with Identifier factory.
 */
public class CommonPolicyFamilySetup implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);

		try {
			ServletPolicyList.addPolicy(ctx,
					new DisplayRestrictedDataByRoleLevelPolicy(ctx));
			ServletPolicyList.addPolicy(ctx,
					new DisplayRestrictedDataToSelfPolicy(ctx));
			ServletPolicyList.addPolicy(ctx,
					new EditRestrictedDataByRoleLevelPolicy(ctx));
			ServletPolicyList.addPolicy(ctx,
					new UseRestrictedPagesByRoleLevelPolicy());

			ServletPolicyList.addPolicy(ctx, new SelfEditingPolicy(ctx));

			// This factory creates Identifiers for all of the above policies.
			CommonIdentifierBundleFactory factory = new CommonIdentifierBundleFactory(
					ctx);

			ActiveIdentifierBundleFactories.addFactory(sce, factory);
		} catch (Exception e) {
			ss.fatal(this, "could not run CommonPolicyFamilySetup", e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) { /* nothing */
	}

}
