/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import org.apache.log4j.Logger;

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
 * @author jeb228
 */
public class ConfigurationProperties {
	private static final Logger LOG = Logger
			.getLogger(ConfigurationProperties.class);
	
	/**
	 * The name of the JNDI environment mapping for the path to the
	 * configuration file (or resource).
	 */
	private static final String PATH_CONFIGURATION = "path.configuration";
	private static volatile Map<String, String> theMap;

	static {
		try {
			// Obtain our environment naming context
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");

			// Get the name of the configuration properties file.
			String configPath = (String) envCtx.lookup(PATH_CONFIGURATION);
			if (configPath == null) {
				throw new IllegalStateException(
						"Could not find a JNDI Environment naming for '"
								+ PATH_CONFIGURATION + "'.");
			}

			InputStream inStream = null;
			// Try to find this as a file.
			File file = new File(configPath);
			try {
				inStream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				inStream = null;
			}
			
			// If no file, try to find it as a resource.
			if (inStream == null) {
				inStream = ConfigurationProperties.class.getClassLoader()
						.getResourceAsStream(configPath);
			}
			
			// If neither file nor resource, give up.
			if (inStream == null) {
				throw new IllegalArgumentException(
						"Failed to find a configuration properties file at '"
								+ file.getAbsolutePath()
								+ "', or a resource at '" + configPath + "'");
			}

			// Load a properties object - it will handle the syntax of the file.
			Properties props = new Properties();
			try {
				props.load(inStream);
			} catch (IOException e) {
				throw new IllegalStateException("Problem while reading the "
						+ "configuration properties file at '" + configPath
						+ "'", e);
			}

			// It's awkward to copy from Properties to a Map.
			Map<String, String> newMap = new HashMap<String, String>();
			for (Enumeration<?> keys = props.keys(); keys.hasMoreElements();) {
				String key = (String) keys.nextElement();
				newMap.put(key, props.getProperty(key));
			}
			
			LOG.info("Configuration properties are: " + newMap);

			// Save an unmodifiable version of the Map
			theMap = Collections.unmodifiableMap(newMap);
		} catch (NamingException e) {
			throw new IllegalStateException(e);
		}

	}

	/**
	 * Get an unmodifiable copy of the map of configuration properties.
	 */
	public static Map<String, String> getMap() {
		return theMap;
	}

	/**
	 * Get the value of the specified property, or <code>null</code> if the
	 * property has not been assigned a value.
	 */
	public static String getProperty(String key) {
		return theMap.get(key);
	}

	/**
	 * Get the value of the specified property, or use the default value if the
	 * property has not been assigned a value.
	 */
	public static String getProperty(String key, String defaultValue) {
		String value = theMap.get(key);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}
}
