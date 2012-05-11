/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.httpservice;

import javax.servlet.Servlet;

import org.osgi.service.http.HttpContext;

/**
 * A simple data class that records the info associated with a registered
 * servlet.
 */
public class ServletRegistrationInfo {
	private final String alias;
	private final Servlet servlet;
	private final BasicServletConfig servletConfig;
	private final HttpContext httpContext;
	private final ServletContextWrapper servletContext;

	public ServletRegistrationInfo(String alias, Servlet servlet,
			BasicServletConfig servletConfig, HttpContext httpContext,
			ServletContextWrapper servletContext) {
		this.alias = alias;
		this.servlet = servlet;
		this.servletConfig = servletConfig;
		this.httpContext = httpContext;
		this.servletContext = servletContext;
	}

	public String getAlias() {
		return alias;
	}

	public Servlet getServlet() {
		return servlet;
	}

	public BasicServletConfig getServletConfig() {
		return servletConfig;
	}

	public HttpContext getHttpContext() {
		return httpContext;
	}

	public ServletContextWrapper getServletContext() {
		return servletContext;
	}

}
