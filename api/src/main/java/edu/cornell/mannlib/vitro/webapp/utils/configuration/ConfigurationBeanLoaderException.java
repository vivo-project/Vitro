/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

/**
 * Indicates that the loading of configuration beans did not succeed.
 */
public class ConfigurationBeanLoaderException extends Exception {
	public ConfigurationBeanLoaderException(String message) {
		super(message);
	}

	public ConfigurationBeanLoaderException(String message, Throwable cause) {
		super(message, cause);
	}
}