/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.framework;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.framework.Felix;
import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.service.log.LogService;

import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * A wrapper around the embedded OSGi framework.
 * 
 * From the outside, this should be framework-agnostic. Internally, it must
 * include some code that is specific to Felix.
 */
public class OsgiFramework {
	private static final Log log = LogFactory.getLog(OsgiFramework.class);

	private static final String ATTRIBUTE_NAME = OsgiFramework.class.getName();

	/**
	 * The directory that contains the "framework bundles". These are
	 * OSGi-related bundles like ConfigurationAdmin, SCR, LogService,
	 * FileInstall, etc.
	 */
	public static final String FRAMEWORK_BUNDLES_DIR = "WEB-INF/bundles/framework";

	/**
	 * The directory that contains the "application bundles". These are bundles
	 * that are specific to Vitro/VIVO.
	 */
	public static final String APPLICATION_BUNDLES_DIR = "WEB-INF/bundles/application";

	/**
	 * The packages that must be exported for application bundles to use. If a
	 * package is needed in the base of the application as well as in the
	 * bundles, it must be included here.
	 * 
	 * It would be nice if these packages could be wrapped in bundles, but then
	 * they would not be usable by the base of the application (the non-OSGi
	 * part).
	 * 
	 * This results from the fact that we are straddling the line between OSGi
	 * and non-OSGi.
	 */
	public static final Collection<String> EXPORTED_PACKAGES = Collections
			.unmodifiableList(Arrays.asList("aQute.bnd.annotation.component",
					"edu.cornell.mannlib.vitro.webapp.osgi.interfaces",
					"javax.servlet", "javax.servlet.http",
					"org.apache.commons.logging", "org.osgi.service.log"));

	public static OsgiFramework getFramework(ServletContext ctx) {
		Object o = ctx.getAttribute(ATTRIBUTE_NAME);
		if (o instanceof OsgiFramework) {
			return (OsgiFramework) o;
		} else {
			throw new IllegalStateException("Expected to find an instance of '"
					+ OsgiFramework.class.getName() + "', but found " + o);
		}
	}

	// ----------------------------------------------------------------------
	// The framework
	// ----------------------------------------------------------------------

	private final OsgiFrameworkLogger logger;
	private final OsgiFrameworkProperties props;
	private final Felix felix;

	public OsgiFramework(OsgiFrameworkLogger logger,
			OsgiFrameworkProperties props) {
		this.logger = logger;
		this.props = props;

		this.felix = new Felix(props.getPropertyMap());
		log.debug("Created the Felix framework.");
	}

	public void start() throws BundleException {
		/*
		 * Must call init() to set up the BundleContext.
		 */
		felix.init();
		BundleContext bundleContext = felix.getBundleContext();
		log.debug("Initialized the Felix framework.");
		log.debug("Exported bundles: " + bundleContext.getProperty(Constants.FRAMEWORK_SYSTEMPACKAGES));
		log.debug("Extra exported bundles: " + bundleContext.getProperty(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA));

		/*
		 * Log all framework events (if enabled);
		 */
		new OsgiEventLogger().addToContext(bundleContext);

		/*
		 * Set up the LogService that 3rd-party bundles will use.
		 */
		bundleContext.registerService(LogService.class, logger, null);

		/*
		 * Install and start all of the bundles from FRAMEWORK_BUNDLES_DIR:
		 * ConfigurationAdmin, SCR, FileInstall, etc.
		 */
		AutoProcessor.process(props.getPropertyMap(), bundleContext);
		log.debug("Ran the AutoProcessor.");

		/*
		 * Start the framework.
		 */
		felix.start();
		log.debug("Started the Felix framework.");
	}

	public void stop() throws BundleException {
		try {
			log.debug("Stopping Felix framework.");
			felix.stop();
			felix.waitForStop(0);
			log.debug("Stopped the Felix framework.");
		} catch (InterruptedException e) {
			log.warn("Interrupted while stopping the Felix framework", e);
		}
	}

	// ----------------------------------------------------------------------
	// Setup and teardown
	// ----------------------------------------------------------------------

	/**
	 * When the webapp starts, launch the OSGi framework.
	 */
	public static class Setup implements ServletContextListener {
		@Override
		public void contextInitialized(ServletContextEvent sce) {
			ServletContext ctx = sce.getServletContext();
			StartupStatus ss = StartupStatus.getBean(ctx);

			try {
				OsgiFrameworkLogger logger = new OsgiFrameworkLogger(log);
				OsgiFrameworkProperties props = new OsgiFrameworkProperties(
						ctx, logger);

				OsgiFramework framework = new OsgiFramework(logger, props);
				framework.start();
				ctx.setAttribute(ATTRIBUTE_NAME, framework);
			} catch (BundleException e) {
				ss.fatal(this, "Failed to start the embedded OSGi framework", e);
			}

			ss.info(this, "Started the embedded OSGi framework.");
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce) {
			ServletContext ctx = sce.getServletContext();
			Object o = ctx.getAttribute(ATTRIBUTE_NAME);

			if (!(o instanceof OsgiFramework)) {
				log.warn("Expected to find an instance of '"
						+ OsgiFramework.class.getName() + "', but found " + o);
				return;
			}

			OsgiFramework framework = (OsgiFramework) o;
			try {
				framework.stop();
			} catch (BundleException e) {
				log.warn("Problem while stopping the embedded OSGi framework",
						e);
			} finally {
				ctx.removeAttribute(ATTRIBUTE_NAME);
			}
		}

	}

}
