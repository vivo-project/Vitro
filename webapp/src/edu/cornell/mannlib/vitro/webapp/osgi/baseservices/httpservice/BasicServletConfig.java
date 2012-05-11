/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.httpservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * A simple implementation of the ServletConfig, for use by registered servlets.
 */
public class BasicServletConfig implements ServletConfig {
	private final ServletContext ctx;
	private final String servletName;
	private final Map<String, String> initParameters;

	public BasicServletConfig(ServletContext ctx, String servletName,
			Map<String, String> initParameters) {
		this.ctx = ctx;
		this.servletName = servletName;
		this.initParameters = new HashMap<String, String>(initParameters);
	}

	@Override
	public String getInitParameter(String name) {
		return initParameters.get(name);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getInitParameterNames() {
		return Collections.enumeration(new ArrayList<String>(initParameters
				.keySet()));
	}

	@Override
	public ServletContext getServletContext() {
		return ctx;
	}

	@Override
	public String getServletName() {
		return servletName;
	}

}
