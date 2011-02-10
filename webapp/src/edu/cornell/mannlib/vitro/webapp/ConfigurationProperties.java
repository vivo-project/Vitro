/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Loads the configuration properties from a properties file. The path to the
 * file is specified an an Environment name in the Context, like this:
 * 
 * <pre>
 * &lt;Context override="true">
 *     &lt;Environment name="path.configuration" 
 *         value="/wherever/the/file/lives/deploy.properties"
 *         type="java.lang.String" 
 *         override="false" />
 * &lt;/Context>
 * </pre>
 * 
 * This initializer will look for a file at this path. If it does not find one,
 * it will look for a resource at this path. So, one might reasonable set this
 * to a value like <code>/usr/local/vitro/stuff/deploy.properties</code> for a
 * file, or like <code>deploy.properties</code> for a resource in the classpath.
 * 
 * When the properties file is loaded, the values are trimmed to remove leading
 * or trailing white space, since such white space is almost always an error.
 * 
 * @author jeb228
 */
public class ConfigurationProperties {
	private static final Log log = LogFactory
			.getLog(ConfigurationProperties.class);

	/**
	 * If we don't find the path to the config properties from a JNDI mapping,
	 * use this. Not final, so we can jigger it for unit tests.
	 */
	private static String DEFAULT_CONFIG_PATH = "deploy.properties";

	/**
	 * The JNDI naming context where Tomcat stores environment attributes.
	 */
	static final String JNDI_BASE = "java:comp/env";

	/**
	 * The name of the JNDI environment mapping for the path to the
	 * configuration file (or resource).
	 */
	static final String PATH_CONFIGURATION = "path.configuration";

	/**
	 * The map of the configuration properties.
	 * 
	 * This should not be accessed directly, but only through the synchronized
	 * method {@link #getTheMap() (and {@link #reset()} for unit tests).
	 */
	private static volatile Map<String, String> theMap;

	/**
	 * Get an unmodifiable copy of the map of configuration properties.
	 */
	public static Map<String, String> getMap() {
		return getTheMap();
	}

	/**
	 * Get the value of the specified property, or <code>null</code> if the
	 * property has not been assigned a value.
	 */
	public static String getProperty(String key) {
		return getTheMap().get(key);
	}

	/**
	 * Get the value of the specified property, or use the default value if the
	 * property has not been assigned a value.
	 */
	public static String getProperty(String key, String defaultValue) {
		String value = getTheMap().get(key);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

	/**
	 * Force the map to be reloaded on the next attempt to access it.
	 * 
	 * This and {@link #getTheMap()} should be the only access to
	 * {@link ConfigurationProperties#theMap}.
	 * 
	 * NOTE: This should only be used in Unit Tests.
	 */
	static synchronized void reset() {
		theMap = null;
	}

	/**
	 * This and {@link #reset()} should be the only access to {@link #theMap}.
	 */
	private static synchronized Map<String, String> getTheMap() {
		if (theMap == null) {
			theMap = loadTheMap();
		}
		return theMap;
	}

	/**
	 * The map is null, so find the properties file and load the map.
	 */
	private static synchronized Map<String, String> loadTheMap() {
		String configPath = getConfigurationFilePath();

		InputStream inStream = getConfigurationInputStream(configPath);

		// Load a properties object - it will parse the file easily.
		Properties props = new Properties();
		try {
			props.load(inStream);
		} catch (IOException e) {
			throw new IllegalStateException("Problem while reading the "
					+ "configuration properties file at '" + configPath + "'",
					e);
		} finally {
			try {
				inStream.close();
			} catch (IOException e) {
				log.error("Failed to close input stream", e);
			}
		}

		// It's awkward to copy from Properties to a Map.
		Map<String, String> newMap = new HashMap<String, String>();
		for (Enumeration<?> keys = props.keys(); keys.hasMoreElements();) {
			String key = (String) keys.nextElement();
			String value = props.getProperty(key);
			// While we're copying, remove leading and trailing white space.
			String trimmed = value.trim();
			newMap.put(key, trimmed);
		}

		log.info("Configuration properties are: " + newMap);

		// Save an unmodifiable version of the Map
		return Collections.unmodifiableMap(newMap);

	}

	/**
	 * Find the path to the Configuration properties file. If we can't find it
	 * by the JNDI mapping, use the default path.
	 * 
	 * @throws IllegalStateException
	 *             If we can't find the path.
	 */
	private static String getConfigurationFilePath() {
		try {
			Context envCtx = (Context) new InitialContext().lookup(JNDI_BASE);
			if (envCtx == null) {
				log.warn("JNDI Lookup on \"" + JNDI_BASE
						+ "\" failed. Is the context file missing?");
				return DEFAULT_CONFIG_PATH;
			}

			// Get the name of the configuration properties file.
			String configPath = (String) envCtx.lookup(PATH_CONFIGURATION);
			if (configPath == null) {
				log.warn("Could not find a JNDI Environment naming for '"
						+ PATH_CONFIGURATION
						+ "'. Is the context file set up correctly?");
				return DEFAULT_CONFIG_PATH;
			}

			log.info("deploy.property as specified by JNDI: " + configPath);
			return configPath;
		} catch (NamingException e) {
			log.warn("JNDI lookup failed. "
					+ "Using default path for config properties.", e);
			return DEFAULT_CONFIG_PATH;
		}
	}

	/**
	 * Find the Configuration properties file.
	 * 
	 * Interpret the path as a resource path (relative to WEB-INF/classes).
	 * 
	 * @throws IllegalArgumentException
	 *             If the path fails to locate a file or a resource.
	 */
	private static InputStream getConfigurationInputStream(String configPath) {
		InputStream inStream = ConfigurationProperties.class.getClassLoader()
				.getResourceAsStream(configPath);

		if (inStream != null) {
			return inStream;
		}

		throw new IllegalArgumentException(
				"Failed to find a configuration properties resource at '"
						+ configPath + "'");
	}

}
