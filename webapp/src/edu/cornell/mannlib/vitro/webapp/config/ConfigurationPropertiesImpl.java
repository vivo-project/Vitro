/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The basic implementation of ConfigurationProperties. It loads the
 * configuration properties from a properties file and stores them in a map. It
 * also permits the caller to supply a map of "preemptive" properties that will
 * be included and will override any matching properties from the file.
 * 
 * Leading and trailing white space are trimmed from the property values.
 * 
 * Once the properties have been parsed and stored, they are immutable.
 */
public class ConfigurationPropertiesImpl extends ConfigurationProperties {
	private static final Log log = LogFactory
			.getLog(ConfigurationPropertiesImpl.class);

	private final Map<String, String> propertyMap;

	public ConfigurationPropertiesImpl(InputStream stream,
			Map<String, String> preemptiveProperties) throws IOException {
		Properties props = loadFromPropertiesFile(stream);
		Map<String, String> map = copyPropertiesToMap(props);

		if (preemptiveProperties != null) {
			map.putAll(preemptiveProperties);
		}

		trimWhiteSpaceFromValues(map);

		this.propertyMap = Collections.unmodifiableMap(map);
		log.debug("Configuration properties are: " + map);
	}

	private Properties loadFromPropertiesFile(InputStream stream)
			throws IOException {
		Properties props = new Properties();
		props.load(stream);
		return props;
	}

	private Map<String, String> copyPropertiesToMap(Properties props) {
		Map<String, String> map = new HashMap<String, String>();
		for (Enumeration<?> keys = props.keys(); keys.hasMoreElements();) {
			String key = (String) keys.nextElement();
			String value = props.getProperty(key);
			map.put(key, value);
		}
		return map;
	}

	private void trimWhiteSpaceFromValues(Map<String, String> map) {
		for (String key : map.keySet()) {
			map.put(key, map.get(key).trim());
		}
	}

	@Override
	public String getProperty(String key) {
		return propertyMap.get(key);
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		if (propertyMap.containsKey(key)) {
			return propertyMap.get(key);
		} else {
			return defaultValue;
		}
	}

	@Override
	public Map<String, String> getPropertyMap() {
		return new HashMap<String, String>(propertyMap);
	}

	@Override
	public String toString() {
		return "ConfigurationPropertiesImpl[propertyMap="
				+ new TreeMap<String, String>(propertyMap) + "]";
	}

}
