/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.httpservice;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;

/**
 * A common base class for objects that process requests for registered servlets
 * or registered resources.
 */
public abstract class Servicer {
	private final String alias;
	private final Bundle bundle;
	private final HttpContext httpContext;

	public Servicer(String alias, Bundle bundle, HttpContext httpContext) {
		if (alias == null) {
			throw new NullPointerException("alias may not be null.");
		}
		if (bundle == null) {
			throw new NullPointerException("bundle may not be null.");
		}
		if (httpContext == null) {
			throw new NullPointerException("httpContext may not be null.");
		}
		this.alias = alias;
		this.bundle = bundle;
		this.httpContext = httpContext;
	}

	public String getAlias() {
		return alias;
	}

	public Bundle getBundle() {
		return bundle;
	}

	public HttpContext getHttpContext() {
		return httpContext;
	}

	/** The URI we are looking for doesn't include the context path. */
	protected String figureRequestUri(HttpServletRequest req) {
		if (req.getPathInfo() == null) {
			return req.getServletPath();
		} else {
			return req.getServletPath() + req.getPathInfo();
		}
	}

	/** For debugging purposes, the bundle looks like this. */
	protected String formatBundle() {
		return "'" + bundle.getSymbolicName() + "'[" + bundle.getBundleId()
				+ "]";
	}

	/**
	 * This servicer has been selected to process this HTTP request. Processing
	 * ends here, so if it can't be processed properly, send the appropriate
	 * error code and message.
	 */
	public abstract void service(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException;

	/**
	 * The servlet or resource group has been registered. Do any necessary
	 * setup.
	 */
	public abstract void init() throws ServletException;

	/**
	 * This servlet or resource group has been unregistered. Do any necessary
	 * cleanup.
	 */
	public abstract void dispose();

}
