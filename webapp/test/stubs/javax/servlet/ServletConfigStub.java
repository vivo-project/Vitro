/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.javax.servlet;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * A simple stub for testing servlets.
 */
public class ServletConfigStub implements ServletConfig {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private ServletContext servletContext;

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public String getInitParameter(String arg0) {
		throw new RuntimeException(
				"ServletConfigStub.getInitParameter() not implemented.");
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getInitParameterNames() {
		throw new RuntimeException(
				"ServletConfigStub.getInitParameterNames() not implemented.");
	}

	@Override
	public String getServletName() {
		throw new RuntimeException(
				"ServletConfigStub.getServletName() not implemented.");
	}

}
