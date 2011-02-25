/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.config;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;

/**
 * A version of ConfigurationProperties that we can use for unit tests. Unlike
 * the basic implementation, this starts as an empty map, and allows the user to
 * add properties as desired.
 * 
 * Call setBean() to store these properties in the ServletContext.
 */
public class ConfigurationPropertiesStub extends ConfigurationProperties {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private final Map<String, String> propertyMap = new HashMap<String, String>();

	public void setProperty(String key, String value) {
		propertyMap.put(key, value);
	}

	public void setBean(ServletContext ctx) {
		setBean(ctx, this);
	}

	@Override
	public String toString() {
		return "ConfigurationPropertiesStub[map=" + propertyMap + "]";
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

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

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

}
