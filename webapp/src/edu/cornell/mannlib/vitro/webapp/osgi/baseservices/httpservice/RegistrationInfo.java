/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.httpservice;

import javax.servlet.Servlet;

import org.osgi.service.http.HttpContext;

/**
 * The information that is held when you register a servlet or a group of
 * resources.
 */
public abstract class RegistrationInfo {
	private final String alias;
	private final HttpContext httpContext;

	public RegistrationInfo(String alias, HttpContext httpContext) {
		this.alias = alias;
		this.httpContext = httpContext;
	}

	public String getAlias() {
		return alias;
	}

	public HttpContext getHttpContext() {
		return httpContext;
	}
}

/**
 * Register a servlet.
 */
class ServletRegistrationInfo extends RegistrationInfo {
	private final Servlet servlet;
	private final BasicServletConfig servletConfig;
	private final ServletContextWrapper servletContext;

	public ServletRegistrationInfo(String alias, HttpContext httpContext,
			Servlet servlet, BasicServletConfig servletConfig,
			ServletContextWrapper servletContext) {
		super(alias, httpContext);
		this.servlet = servlet;
		this.servletConfig = servletConfig;
		this.servletContext = servletContext;
	}

	public Servlet getServlet() {
		return servlet;
	}

	public BasicServletConfig getServletConfig() {
		return servletConfig;
	}

	public ServletContextWrapper getServletContext() {
		return servletContext;
	}

}

/**
 * Register a group of resources.
 */
class ResourcesRegistrationInfo extends RegistrationInfo {
	private final String internalName;

	public ResourcesRegistrationInfo(String alias, HttpContext httpContext,
			String internalName) {
		super(alias, httpContext);
		this.internalName = internalName;
	}

	public String getInternalName() {
		return internalName;
	}
}
