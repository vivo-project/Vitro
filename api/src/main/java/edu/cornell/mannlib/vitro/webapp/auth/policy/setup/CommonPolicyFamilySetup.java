/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.setup;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.ActiveIdentifierBundleFactories;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundleFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.factory.HasPermissionFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.factory.HasPermissionSetFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.factory.HasProfileOrIsBlacklistedFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.factory.HasProxyEditingRightsFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.factory.IsRootUserFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.factory.IsUserFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.DisplayRestrictedDataToSelfPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PermissionsPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.SelfEditingPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Set up the common policy family, with Identifier factories.
 */
public class CommonPolicyFamilySetup implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);

		try {
			policy(ctx, new PermissionsPolicy());
			policy(ctx, new DisplayRestrictedDataToSelfPolicy(ctx));
			policy(ctx, new SelfEditingPolicy(ctx));

			factory(ctx, new IsUserFactory(ctx));
			factory(ctx, new IsRootUserFactory(ctx));
			factory(ctx, new HasProfileOrIsBlacklistedFactory(ctx));
			factory(ctx, new HasPermissionSetFactory(ctx));
			factory(ctx, new HasPermissionFactory(ctx));
			factory(ctx, new HasProxyEditingRightsFactory(ctx));
		} catch (Exception e) {
			ss.fatal(this, "could not run CommonPolicyFamilySetup", e);
		}
	}

	private void policy(ServletContext ctx, PolicyIface policy) {
		ServletPolicyList.addPolicy(ctx, policy);
	}

	private void factory(ServletContext ctx, IdentifierBundleFactory factory) {
		ActiveIdentifierBundleFactories.addFactory(ctx, factory);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) { /* nothing */
	}

}
