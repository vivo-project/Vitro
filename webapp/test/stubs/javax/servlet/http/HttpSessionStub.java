/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.javax.servlet.http;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

/**
 * A simple stand-in for the HttpSession, for use in unit tests.
 */
@SuppressWarnings("deprecation")
public class HttpSessionStub implements HttpSession {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private ServletContext context;

	public void setServletContext(ServletContext context) {
		this.context = context;
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public ServletContext getServletContext() {
		return this.context;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public Object getAttribute(String arg0) {
		throw new RuntimeException(
				"HttpSessionStub.getAttribute() not implemented.");
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getAttributeNames() {
		throw new RuntimeException(
				"HttpSessionStub.getAttributeNames() not implemented.");
	}

	@Override
	public long getCreationTime() {
		throw new RuntimeException(
				"HttpSessionStub.getCreationTime() not implemented.");
	}

	@Override
	public String getId() {
		throw new RuntimeException("HttpSessionStub.getId() not implemented.");
	}

	@Override
	public long getLastAccessedTime() {
		throw new RuntimeException(
				"HttpSessionStub.getLastAccessedTime() not implemented.");
	}

	@Override
	public int getMaxInactiveInterval() {
		throw new RuntimeException(
				"HttpSessionStub.getMaxInactiveInterval() not implemented.");
	}

	@Override
	public javax.servlet.http.HttpSessionContext getSessionContext() {
		throw new RuntimeException(
				"HttpSessionStub.getSessionContext() not implemented.");
	}

	@Override
	public Object getValue(String arg0) {
		throw new RuntimeException(
				"HttpSessionStub.getValue() not implemented.");
	}

	@Override
	public String[] getValueNames() {
		throw new RuntimeException(
				"HttpSessionStub.getValueNames() not implemented.");
	}

	@Override
	public void invalidate() {
		throw new RuntimeException(
				"HttpSessionStub.invalidate() not implemented.");
	}

	@Override
	public boolean isNew() {
		throw new RuntimeException("HttpSessionStub.isNew() not implemented.");
	}

	@Override
	public void putValue(String arg0, Object arg1) {
		throw new RuntimeException(
				"HttpSessionStub.putValue() not implemented.");
	}

	@Override
	public void removeAttribute(String arg0) {
		throw new RuntimeException(
				"HttpSessionStub.removeAttribute() not implemented.");
	}

	public void removeValue(String arg0) {
		throw new RuntimeException(
				"HttpSessionStub.removeValue() not implemented.");
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		throw new RuntimeException(
				"HttpSessionStub.setAttribute() not implemented.");
	}

	@Override
	public void setMaxInactiveInterval(int arg0) {
		throw new RuntimeException(
				"HttpSessionStub.setMaxInactiveInterval() not implemented.");
	}

}
