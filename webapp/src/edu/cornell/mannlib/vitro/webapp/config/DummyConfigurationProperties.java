/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.config;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * If somebody asks for ConfigurationProperties before it has been initialized,
 * they get this. It doesn't stop them from proceeding, it just yields no
 * properties while logging warning messages for each request.
 */
class DummyConfigurationProperties extends ConfigurationProperties {
	private static final Log log = LogFactory
			.getLog(DummyConfigurationProperties.class);

	@Override
	public String getProperty(String key) {
		log.warn("ConfigurationProperties has not been initialized: getProperty(\""
				+ key + "\")");
		return null;
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		log.warn("ConfigurationProperties has not been initialized: getProperty(\""
				+ key + "\", \"" + defaultValue + "\")");
		return defaultValue;
	}

	@Override
	public Map<String, String> getPropertyMap() {
		log.warn("ConfigurationProperties has not been initialized: "
				+ "getPropertyMap()");
		return Collections.emptyMap();
	}

}
