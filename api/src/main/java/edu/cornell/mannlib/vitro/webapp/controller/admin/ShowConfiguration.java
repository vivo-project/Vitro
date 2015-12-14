/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * Show the current ConfigurationProperties and the Java system properties.
 */
public class ShowConfiguration extends FreemarkerHttpServlet {
	@Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		return SimplePermission.SEE_CONFIGURATION.ACTION;
	}

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("configurationProperties", getConfigurationProperties(vreq));
		body.put("javaSystemProperties", getSystemProperties());
		return new TemplateResponseValues("admin-showConfiguration.ftl", body);
	}

	private SortedMap<String, String> getConfigurationProperties(
			VitroRequest vreq) {
		ConfigurationProperties props = ConfigurationProperties.getBean(vreq);
		TreeMap<String, String> map = new TreeMap<>(props.getPropertyMap());
		for (String key : map.keySet()) {
			if (key.toLowerCase().endsWith("password")) {
				map.put(key, "********");
			}
		}
		return map;
	}

	private SortedMap<String, String> getSystemProperties() {
		Properties props = System.getProperties();
		SortedMap<String, String> map = new TreeMap<>();
		for (String key : props.stringPropertyNames()) {
			map.put(key, props.getProperty(key));
		}
		return map;
	}

}
