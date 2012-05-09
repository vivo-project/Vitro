/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.interfaces;

import java.util.Map;

/**
 * Provides a mechanism for bundles to read the configuration properties that
 * were loaded from the deploy.properties file.
 */
public interface ConfigurationProperties {
	/**
	 * Get the value of the property, or <code>null</code> if the property has
	 * not been assigned a value.
	 */
	public abstract String getProperty(String key);

	/**
	 * Get the value of the property, or use the default value if the property
	 * has not been assigned a value.
	 */
	public abstract String getProperty(String key, String defaultValue);

	/**
	 * Get a copy of the map of the configuration properties and their settings.
	 * Because this is a copy, it cannot be used to modify the settings.
	 */
	public abstract Map<String, String> getPropertyMap();

}
