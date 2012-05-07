/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.framework;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.framework.Felix;
import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

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

	private final OsgiFrameworkProperties props;
	private final Felix felix;

	public OsgiFramework(OsgiFrameworkProperties props) {
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

		/*
		 * Log all framework events (if enabled);
		 */
		new OsgiEventLogger().addToContext(bundleContext);

		/*
		 * Start the framework.
		 */
		felix.start();
		log.debug("Started the Felix framework.");
		
		/*
		 * Install and start all of the bundles from FRAMEWORK_BUNDLES_DIR:
		 * ConfigurationAdmin, SCR, FileInstall, etc.
		 */
		AutoProcessor.process(props.getPropertyMap(), bundleContext);
		log.debug("Ran the AutoProcessor.");
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

				OsgiFramework framework = new OsgiFramework(props);
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
