/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.framework;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.Constants;

import edu.cornell.mannlib.vitro.webapp.osgi.baseservices.BaseServicesActivator;
import edu.cornell.mannlib.vitro.webapp.osgi.baseservices.OsgiFrameworkLogger;

/**
 * Create the properties map that will control the framework.
 * 
 * This includes properties for the bundles that we consider to be part of the
 * framework, like the LogService, SCR, and FileInstall. It also includes
 * Activators for the base-services bundles.
 */
public class OsgiFrameworkProperties {
	private static final Log log = LogFactory
			.getLog(OsgiFrameworkProperties.class);

	private final ServletContext ctx;
	private final OsgiFrameworkLogger logger;

	public OsgiFrameworkProperties(ServletContext ctx,
			OsgiFrameworkLogger logger) {
		this.ctx = ctx;
		this.logger = logger;
	}

	public Map<String, Object> getPropertyMap() {
		Map<String, Object> map = new HashMap<String, Object>();

		map.putAll(buildFrameworkProperties());
		map.putAll(buildAutodeployProperties());
		map.putAll(buildDeclarativeServicesProperties());
		map.putAll(buildFileinstallProperties());

		log.debug("OSGi framework properties: " + map);

		return map;
	}

	private Map<? extends String, ? extends Object> buildFrameworkProperties() {
		Map<String, Object> map = new HashMap<String, Object>();

		/*
		 * The "system bundle" will export these packages for any bundle to
		 * import.
		 */
		map.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
				OsgiFrameworkExportedPackages.getPackageList());

		/*
		 * Create a temp directory to store unpacked bundles. It will be cleaned
		 * and rebuilt every time we restart.
		 */
		map.put(Constants.FRAMEWORK_STORAGE, figureCacheDirPath());
		map.put(Constants.FRAMEWORK_STORAGE_CLEAN,
				Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

		/*
		 * Set up the logger for the Felix framework.
		 */
		map.put(FelixConstants.LOG_LOGGER_PROP, logger);
		map.put(FelixConstants.LOG_LEVEL_PROP, logger.osgiLogLevelString());

		/*
		 * Publish the base services so the bundles can use them.
		 */
		map.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, Arrays.asList(
				logger.getActivator(), new BaseServicesActivator(ctx)));

		return map;
	}

	private Map<? extends String, ? extends Object> buildAutodeployProperties() {
		Map<String, Object> map = new HashMap<String, Object>();

		/*
		 * This is where to find the bundles that we consider to be part of the
		 * framework.
		 */
		map.put(AutoProcessor.AUTO_DEPLOY_DIR_PROPERY,
				figurePathWithinWebapp(OsgiFramework.FRAMEWORK_BUNDLES_DIR));

		/**
		 * Install the bundles and start them when we start the framework.
		 */
		map.put(AutoProcessor.AUTO_DEPLOY_ACTION_PROPERY,
				AutoProcessor.AUTO_DEPLOY_INSTALL_VALUE + ","
						+ AutoProcessor.AUTO_DEPLOY_START_VALUE);

		return map;
	}

	private Map<? extends String, ? extends Object> buildDeclarativeServicesProperties() {
		Map<String, Object> map = new HashMap<String, Object>();

		/**
		 * The ServiceComponentRuntime bundle will use the same logger that the
		 * framework uses, so set the same logging level.
		 */
		map.put("ds.loglevel", logger.osgiLogLevelString());

		return map;
	}

	private Map<? extends String, ? extends Object> buildFileinstallProperties() {
		Map<String, Object> map = new HashMap<String, Object>();

		/*
		 * This is where to find the bundles that we consider to be part of the
		 * application.
		 */
		map.put("felix.fileinstall.dir",
				figurePathWithinWebapp(OsgiFramework.APPLICATION_BUNDLES_DIR));

		/*
		 * Use the same cache directory that the framework uses.
		 */
		map.put("felix.fileinstall.tmpdir", figureCacheDirPath());

		/**
		 * The FileInstall bundle will use the same logger that the framework
		 * uses, so set the same logging level.
		 */
		map.put("felix.fileinstall.log.level", logger.osgiLogLevelString());

		/*
		 * Poll every 10 seconds (in millis).
		 */
		map.put("felix.fileinstall.poll", "10000");

		/*
		 * Wait a bit before the first poll of the bundle directory.
		 */
		map.put("felix.fileinstall.noInitialDelay", "false");

		/*
		 * If some code in the app changes the configuration of a bundle, don't
		 * store the changed configuration. We want to start the same way each
		 * time, without persisting configuration state from previous runs.
		 */
		map.put("felix.fileinstall.enableConfigSave", "false");

		return map;
	}

	private String figureCacheDirPath() {
		String cacheDirName = figureCleanContextPath() + "osgi-cache";
		String tempDirPath = System.getProperty("java.io.tmpdir");
		if (tempDirPath == null) {
			return cacheDirName;
		} else {
			File cacheDir = new File(new File(tempDirPath), cacheDirName);
			return cacheDir.getPath();
		}
	}

	private String figureCleanContextPath() {
		String rawContextPath = ctx.getContextPath();
		if (rawContextPath.isEmpty()) {
			return "";
		}
		String cleanContextPath = rawContextPath.replaceAll("[/\\s]", "");
		return cleanContextPath + "-";
	}

	private String figurePathWithinWebapp(String path) {
		File webappBaseDir = new File(ctx.getRealPath("/"));
		return new File(webappBaseDir, path).getPath();
	}

}
