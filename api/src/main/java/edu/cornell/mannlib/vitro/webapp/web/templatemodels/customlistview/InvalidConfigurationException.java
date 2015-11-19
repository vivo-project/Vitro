/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.customlistview;

/**
 * Indicates that there is a problem with the custom configuration. Perhaps the
 * file can't be found, can't be parsed, or the information it contains is
 * erroneous.
 */
public class InvalidConfigurationException extends Exception {

	public InvalidConfigurationException() {
		super();
	}

	public InvalidConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidConfigurationException(String message) {
		super(message);
	}

	public InvalidConfigurationException(Throwable cause) {
		super(cause);
	}

}
