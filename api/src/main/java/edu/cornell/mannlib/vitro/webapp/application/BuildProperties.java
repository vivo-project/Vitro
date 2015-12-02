/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.application;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Obtains and provides the contents of the build.properties file.
 */
public class BuildProperties {
	private static final Log log = LogFactory.getLog(BuildProperties.class);

	/** Path to the file of build properties baked into the webapp. */
	public static final String WEBAPP_PATH_BUILD_PROPERTIES = "/WEB-INF/resources/build.properties";

	private final Map<String, String> propertyMap;

	public BuildProperties(ServletContext ctx) {
		Map<String, String> map = new HashMap<>();

		try (InputStream stream = ctx
				.getResourceAsStream(WEBAPP_PATH_BUILD_PROPERTIES)) {
			if (stream == null) {
				log.debug("Didn't find a resource at '"
						+ WEBAPP_PATH_BUILD_PROPERTIES + "'.");
			} else {
				Properties props = new Properties();
				props.load(stream);
				for (String key : props.stringPropertyNames()) {
					map.put(key, props.getProperty(key));
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to load from '"
					+ WEBAPP_PATH_BUILD_PROPERTIES + "'.", e);
		}
		propertyMap = Collections.unmodifiableMap(map);
	}

	public Map<String, String> getMap() {
		return this.propertyMap;
	}

}
