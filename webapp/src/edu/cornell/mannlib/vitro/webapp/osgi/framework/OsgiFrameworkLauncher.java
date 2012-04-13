/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.framework;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Launch the embedded OSGi framework, and publish some services that tie the
 * bundles to the main Vitro code.
 * 
 * This code requires that the ConfigurationProperties is already initialized.
 * 
 * Some of this code is specific to the Apache Felix OSGi framework. It would be
 * nice if we could be truly framework-agnostic at some point.
 */
public class OsgiFrameworkLauncher implements ServletContextListener {
	private static final Log log = LogFactory
			.getLog(OsgiFrameworkLauncher.class);

	private static final String ATTRIBUTE_NAME = Felix.class.getName();
	private static final String FRAMEWORK_BUNDLES_DIR = "WEB-INF/bundles/framework";
	private static final String[] EXPORTED_PACKAGES = new String[] {
			"javax.servlet", "javax.servlet.http",
			"org.apache.commons.logging",
			"edu.cornell.mannlib.vitro.webapp.osgi.interfaces" };

	/**
	 * When the webapp starts, launch the OSGi framework.
	 * 
	 * Although we are trying to be framework-agnostic, some of this code is
	 * specific to the Apache Felix OSGi framework.
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);
		Map<String, Object> configProperties = getStartupConfigurationProperties(ctx);

		Felix framework = new Felix(configProperties);
		try {
			framework.init();
			addLoggingListener(framework);
			loadFrameworkBundles(framework, configProperties);
			framework.start();
			ctx.setAttribute(ATTRIBUTE_NAME, framework);
			ss.info(this, "Started the embedded OSGi framework");
		} catch (BundleException e) {
			ss.fatal(this, "Problem starting the frameword: ", e);
		}

	}

	private Map<String, Object> getStartupConfigurationProperties(
			ServletContext ctx) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
				StringUtils.join(EXPORTED_PACKAGES, ","));
		map.put(Constants.FRAMEWORK_STORAGE, figureCacheDirPath(ctx));
		map.put(Constants.FRAMEWORK_STORAGE_CLEAN,
				Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
		map.put(FelixConstants.LOG_LEVEL_PROP, figureLogLevel());
		map.put(AutoProcessor.AUTO_DEPLOY_DIR_PROPERY, figureBundleDirPath(ctx));
		map.put(AutoProcessor.AUTO_DEPLOY_ACTION_PROPERY,
				AutoProcessor.AUTO_DEPLOY_INSTALL_VALUE + ","
						+ AutoProcessor.AUTO_DEPLOY_START_VALUE);
		log.debug("Felix startup properties: " + map);
		return map;
	}

	private String figureCacheDirPath(ServletContext ctx) {
		String tempDirPath = System.getProperty("java.io.tmpdir");
		if (tempDirPath == null) {
			return "felix-cache";
		}
		File tempDir = new File(tempDirPath);
		File cacheDir = new File(tempDir, figureCleanContextPath(ctx)
				+ "felix-cache");
		return cacheDir.getPath();
	}

	private String figureCleanContextPath(ServletContext ctx) {
		String rawContextPath = ctx.getContextPath();
		if (rawContextPath.isEmpty()) {
			return "";
		}
		String cleanContextPath = rawContextPath.replaceAll("[/\\s]", "");
		return cleanContextPath + "-";
	}

	private String figureLogLevel() {
		if (log.isDebugEnabled()) {
			return "4";
		}
		if (log.isInfoEnabled()) {
			return "3";
		}
		if (log.isDebugEnabled()) {
			return "2";
		}
		if (log.isErrorEnabled()) {
			return "1";
		}
		return "0";
	}

	private String figureBundleDirPath(ServletContext ctx) {
		String appBasePath = ctx.getRealPath("/");
		File appBase = new File(appBasePath);
		File bundle = new File(appBase, FRAMEWORK_BUNDLES_DIR);
		return bundle.getAbsolutePath();
	}

	private void addLoggingListener(Felix framework) {
		BundleContext bundleContext = framework.getBundleContext();
		LoggingFrameworkListener listener = new LoggingFrameworkListener();
		bundleContext.addBundleListener(listener);
		bundleContext.addServiceListener(listener);
		bundleContext.addFrameworkListener(listener);
	}

	private void loadFrameworkBundles(Felix framework,
			Map<String, Object> configProperties) {
		BundleContext bundleContext = framework.getBundleContext();
		AutoProcessor.process(configProperties, bundleContext);
	}

	/**
	 * When the webapp stops, stop the OSGi framework.
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		Object o = ctx.getAttribute(ATTRIBUTE_NAME);
		try {
			if (o instanceof Felix) {
				log.debug("Stopping Felix framework");
				Felix framework = (Felix) o;
				framework.stop();
				framework.waitForStop(0);
				log.debug("Felix framework stopped");
			} else {
				log.warn("Expected to find an instance of Felix, but found "
						+ o);
			}
		} catch (BundleException e) {
			log.error("Failed to stop the Felix framework", e);
		} catch (InterruptedException e) {
			log.warn("Interrupted while stopping the Felix framework", e);
		} finally {
			ctx.removeAttribute(ATTRIBUTE_NAME);
		}
	}

}
