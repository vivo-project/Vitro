/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationPropertiesImpl;
import edu.cornell.mannlib.vitro.webapp.modules.interfaces.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.modules.interfaces.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.osgi.baseservices.httpservice.VitroHttpServiceFactory;
import edu.cornell.mannlib.vitro.webapp.osgi.baseservices.logservice.VitroLogServiceFactory;

/**
 * When the OSGi framework starts, register some expected services. When it
 * stops, unregister them.
 */
public class BaseServicesActivator implements BundleActivator {
	private static final Log log = LogFactory
			.getLog(BaseServicesActivator.class);

	private final ServletContext ctx;

	private ServiceRegistration<?> logSr;

	private ServiceRegistration<ConfigurationProperties> configSr;
	private ServiceRegistration<StartupStatus> startupSr;

	private VitroHttpServiceFactory httpFactory;
	private ServiceRegistration<?> httpSr;

	public BaseServicesActivator(ServletContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		/* LogService */
		VitroLogServiceFactory logFactory = new VitroLogServiceFactory();
		log.debug("Register the LogServiceFactory");
		logSr = bundleContext.registerService(LogService.class.getName(),
				logFactory, null);

		/* ConfigurationProperties */
		edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties cp = edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties
				.getBean(ctx);
		ConfigurationPropertiesImpl cpi = (ConfigurationPropertiesImpl) cp;
		log.debug("Register the ConfigurationProperties");
		configSr = bundleContext.registerService(ConfigurationProperties.class,
				cpi, null);

		/* StartupStatus */
		StartupStatus ss = edu.cornell.mannlib.vitro.webapp.startup.StartupStatus
				.getBean(ctx);
		log.debug("Register the StartupStatus");
		startupSr = bundleContext.registerService(StartupStatus.class, ss, null);

		/* HttpService */
		httpFactory = new VitroHttpServiceFactory(ctx);
		log.debug("Register the HttpServiceFactory");
		httpSr = bundleContext.registerService(HttpService.class.getName(),
				httpFactory, null);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		log.debug("Unregister the VitroHttpServiceFactory");
		httpFactory.shutdown();
		httpSr.unregister();

		log.debug("Unregister the StartupStatus");
		startupSr.unregister();

		log.debug("Unregister the ConfigurationProperties");
		configSr.unregister();

		log.debug("Unregister the VitroLogServiceFactory");
		logSr.unregister();
	}

}
