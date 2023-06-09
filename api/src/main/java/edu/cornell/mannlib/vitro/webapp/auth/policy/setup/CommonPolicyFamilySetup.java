/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.setup;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.ActiveIdentifierBundleFactories;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundleFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.factory.HasPermissionSetFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.factory.HasProfileOrIsBlacklistedFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.factory.HasProxyEditingRightsFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.factory.IsRootUserFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.factory.IsUserFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyLoader;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;
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
		    PolicyLoader.initialize(ModelAccess.getInstance().getRDFService(WhichService.CONFIGURATION));
		    PolicyLoader.getInstance().loadPolicies();
			factory(new IsUserFactory());
			factory(new IsRootUserFactory());
			factory(new HasProfileOrIsBlacklistedFactory());
			factory(new HasPermissionSetFactory());
			factory(new HasProxyEditingRightsFactory());
		} catch (Exception e) {
			ss.fatal(this, "could not run CommonPolicyFamilySetup", e);
		}
	}

	private void factory(IdentifierBundleFactory factory) {
		ActiveIdentifierBundleFactories.addFactory(factory);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) { /* nothing */
	}

}
