/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;

/**
 * When the ConfigurationBeanLoader creates an instance of this class, it will
 * call this method, supplying ConfigurationProperties.
 */
public interface ConfigurationReader {
	void setConfigurationProperties(ConfigurationProperties properties);
}
