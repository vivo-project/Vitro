/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationPropertiesImpl;
import edu.cornell.mannlib.vitro.webapp.modules.interfaces.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.modules.interfaces.StartupStatus;

/**
 * When the OSGi framework starts, register some expected services. When it
 * stops, unregister them.
 */
public class BaseServicesActivator implements BundleActivator {
	private static final Log log = LogFactory
			.getLog(BaseServicesActivator.class);

	private final ServletContext ctx;

	private ServiceRegistration<ConfigurationProperties> cpsr;
	private ServiceRegistration<StartupStatus> sssr;

	public BaseServicesActivator(ServletContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		/* ConfigurationProperties */
		edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties cp = edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties
				.getBean(ctx);
		ConfigurationPropertiesImpl cpi = (ConfigurationPropertiesImpl) cp;
		log.debug("Register the ConfigurationProperties");
		cpsr = bundleContext
				.registerService(ConfigurationProperties.class, cpi, null);
		
		/* StartupStatus */
		StartupStatus ss = edu.cornell.mannlib.vitro.webapp.startup.StartupStatus.getBean(ctx);
		log.debug("Register the StartupStatus");
		sssr = bundleContext.registerService(StartupStatus.class, ss, null);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		log.debug("Unregister the StartupStatus");
		sssr.unregister();

		log.debug("Unregister the ConfigurationProperties");
		cpsr.unregister();
	}

}
