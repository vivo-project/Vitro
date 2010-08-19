/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.javax.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * A simple stand-in for the {@link ServletContext}, for use in unit tests.
 */
@SuppressWarnings("deprecation")
public class ServletContextStub implements ServletContext {

	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private final Map<String, Object> attributes = new HashMap<String, Object>();

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attributes.keySet());
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	@Override
	public void setAttribute(String name, Object object) {
		if (object == null) {
			removeAttribute(name);
		} else {
			attributes.put(name, object);
		}
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public ServletContext getContext(String arg0) {
		throw new RuntimeException(
				"ServletContextStub.getContext() not implemented.");
	}

	@Override
	public String getContextPath() {
		throw new RuntimeException(
				"ServletContextStub.getContextPath() not implemented.");
	}

	@Override
	public String getInitParameter(String arg0) {
		throw new RuntimeException(
				"ServletContextStub.getInitParameter() not implemented.");
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration getInitParameterNames() {
		throw new RuntimeException(
				"ServletContextStub.getInitParameterNames() not implemented.");
	}

	@Override
	public int getMajorVersion() {
		throw new RuntimeException(
				"ServletContextStub.getMajorVersion() not implemented.");
	}

	@Override
	public String getMimeType(String arg0) {
		throw new RuntimeException(
				"ServletContextStub.getMimeType() not implemented.");
	}

	@Override
	public int getMinorVersion() {
		throw new RuntimeException(
				"ServletContextStub.getMinorVersion() not implemented.");
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) {
		throw new RuntimeException(
				"ServletContextStub.getNamedDispatcher() not implemented.");
	}

	@Override
	public String getRealPath(String arg0) {
		throw new RuntimeException(
				"ServletContextStub.getRealPath() not implemented.");
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		throw new RuntimeException(
				"ServletContextStub.getRequestDispatcher() not implemented.");
	}

	@Override
	public URL getResource(String arg0) throws MalformedURLException {
		throw new RuntimeException(
				"ServletContextStub.getResource() not implemented.");
	}

	@Override
	public InputStream getResourceAsStream(String arg0) {
		throw new RuntimeException(
				"ServletContextStub.getResourceAsStream() not implemented.");
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set getResourcePaths(String arg0) {
		throw new RuntimeException(
				"ServletContextStub.getResourcePaths() not implemented.");
	}

	@Override
	public String getServerInfo() {
		throw new RuntimeException(
				"ServletContextStub.getServerInfo() not implemented.");
	}

	@Override
	public Servlet getServlet(String arg0) throws ServletException {
		throw new RuntimeException(
				"ServletContextStub.getServlet() not implemented.");
	}

	@Override
	public String getServletContextName() {
		throw new RuntimeException(
				"ServletContextStub.getServletContextName() not implemented.");
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration getServletNames() {
		throw new RuntimeException(
				"ServletContextStub.getServletNames() not implemented.");
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration getServlets() {
		throw new RuntimeException(
				"ServletContextStub.getServlets() not implemented.");
	}

	@Override
	public void log(String arg0) {
		throw new RuntimeException("ServletContextStub.log() not implemented.");
	}

	@Override
	public void log(Exception arg0, String arg1) {
		throw new RuntimeException("ServletContextStub.log() not implemented.");
	}

	@Override
	public void log(String arg0, Throwable arg1) {
		throw new RuntimeException("ServletContextStub.log() not implemented.");
	}

}
