/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.javax.servlet.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 
 * @author jeb228
 */
@SuppressWarnings("deprecation")
public class HttpServletRequestStub implements HttpServletRequest {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private final Map<String, List<String>> parameters;
	private final Map<String, Object> attributes;

	public HttpServletRequestStub(Map<String, List<String>> parameters,
			Map<String, Object> attributes) {
		this();
		this.parameters.putAll(parameters);
		this.attributes.putAll(attributes);
	}

	public HttpServletRequestStub() {
		parameters = new HashMap<String, List<String>>();
		attributes = new HashMap<String, Object>();
	}

	public void addParameter(String name, String value) {
		if (!parameters.containsKey(name)) {
			parameters.put(name, new ArrayList<String>());
		}
		parameters.get(name).add(value);
	}

	/** Clear all values for a given parameter name. */
	public void removeParameter(String name) {
		parameters.remove(name);
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	public String getParameter(String name) {
		if (!parameters.containsKey(name)) {
			return null;
		}
		return parameters.get(name).get(0);
	}

	public Map getParameterMap() {
		Map<String, String[]> map = new HashMap<String, String[]>();
		for (String key : parameters.keySet()) {
			map.put(key, parameters.get(key).toArray(new String[0]));
		}
		return map;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	public String getAuthType() {
		throw new RuntimeException(
				"HttpServletRequestStub.getAuthType() not implemented.");
	}

	public String getContextPath() {
		throw new RuntimeException(
				"HttpServletRequestStub.getContextPath() not implemented.");
	}

	public Cookie[] getCookies() {
		throw new RuntimeException(
				"HttpServletRequestStub.getCookies() not implemented.");
	}

	public long getDateHeader(String arg0) {
		throw new RuntimeException(
				"HttpServletRequestStub.getDateHeader() not implemented.");
	}

	public String getHeader(String arg0) {
		throw new RuntimeException(
				"HttpServletRequestStub.getHeader() not implemented.");
	}

	public Enumeration getHeaderNames() {
		throw new RuntimeException(
				"HttpServletRequestStub.getHeaderNames() not implemented.");
	}

	public Enumeration getHeaders(String arg0) {
		throw new RuntimeException(
				"HttpServletRequestStub.getHeaders() not implemented.");
	}

	public int getIntHeader(String arg0) {
		throw new RuntimeException(
				"HttpServletRequestStub.getIntHeader() not implemented.");
	}

	public String getMethod() {
		throw new RuntimeException(
				"HttpServletRequestStub.getMethod() not implemented.");
	}

	public String getPathInfo() {
		throw new RuntimeException(
				"HttpServletRequestStub.getPathInfo() not implemented.");
	}

	public String getPathTranslated() {
		throw new RuntimeException(
				"HttpServletRequestStub.getPathTranslated() not implemented.");
	}

	public String getQueryString() {
		throw new RuntimeException(
				"HttpServletRequestStub.getQueryString() not implemented.");
	}

	public String getRemoteUser() {
		throw new RuntimeException(
				"HttpServletRequestStub.getRemoteUser() not implemented.");
	}

	public String getRequestURI() {
		throw new RuntimeException(
				"HttpServletRequestStub.getRequestURI() not implemented.");
	}

	public StringBuffer getRequestURL() {
		throw new RuntimeException(
				"HttpServletRequestStub.getRequestURL() not implemented.");
	}

	public String getRequestedSessionId() {
		throw new RuntimeException(
				"HttpServletRequestStub.getRequestedSessionId() not implemented.");
	}

	public String getServletPath() {
		throw new RuntimeException(
				"HttpServletRequestStub.getServletPath() not implemented.");
	}

	public HttpSession getSession() {
		throw new RuntimeException(
				"HttpServletRequestStub.getSession() not implemented.");
	}

	public HttpSession getSession(boolean arg0) {
		throw new RuntimeException(
				"HttpServletRequestStub.getSession() not implemented.");
	}

	public Principal getUserPrincipal() {
		throw new RuntimeException(
				"HttpServletRequestStub.getUserPrincipal() not implemented.");
	}

	public boolean isRequestedSessionIdFromCookie() {
		throw new RuntimeException(
				"HttpServletRequestStub.isRequestedSessionIdFromCookie() not implemented.");
	}

	public boolean isRequestedSessionIdFromURL() {
		throw new RuntimeException(
				"HttpServletRequestStub.isRequestedSessionIdFromURL() not implemented.");
	}

	public boolean isRequestedSessionIdFromUrl() {
		throw new RuntimeException(
				"HttpServletRequestStub.isRequestedSessionIdFromUrl() not implemented.");
	}

	public boolean isRequestedSessionIdValid() {
		throw new RuntimeException(
				"HttpServletRequestStub.isRequestedSessionIdValid() not implemented.");
	}

	public boolean isUserInRole(String arg0) {
		throw new RuntimeException(
				"HttpServletRequestStub.isUserInRole() not implemented.");
	}

	public Object getAttribute(String arg0) {
		throw new RuntimeException(
				"HttpServletRequestStub.getAttribute() not implemented.");
	}

	public Enumeration getAttributeNames() {
		throw new RuntimeException(
				"HttpServletRequestStub.getAttributeNames() not implemented.");
	}

	public String getCharacterEncoding() {
		throw new RuntimeException(
				"HttpServletRequestStub.getCharacterEncoding() not implemented.");
	}

	public int getContentLength() {
		throw new RuntimeException(
				"HttpServletRequestStub.getContentLength() not implemented.");
	}

	public String getContentType() {
		throw new RuntimeException(
				"HttpServletRequestStub.getContentType() not implemented.");
	}

	public ServletInputStream getInputStream() throws IOException {
		throw new RuntimeException(
				"HttpServletRequestStub.getInputStream() not implemented.");
	}

	public String getLocalAddr() {
		throw new RuntimeException(
				"HttpServletRequestStub.getLocalAddr() not implemented.");
	}

	public String getLocalName() {
		throw new RuntimeException(
				"HttpServletRequestStub.getLocalName() not implemented.");
	}

	public int getLocalPort() {
		throw new RuntimeException(
				"HttpServletRequestStub.getLocalPort() not implemented.");
	}

	public Locale getLocale() {
		throw new RuntimeException(
				"HttpServletRequestStub.getLocale() not implemented.");
	}

	public Enumeration getLocales() {
		throw new RuntimeException(
				"HttpServletRequestStub.getLocales() not implemented.");
	}

	public Enumeration getParameterNames() {
		throw new RuntimeException(
				"HttpServletRequestStub.getParameterNames() not implemented.");
	}

	public String[] getParameterValues(String arg0) {
		throw new RuntimeException(
				"HttpServletRequestStub.getParameterValues() not implemented.");
	}

	public String getProtocol() {
		throw new RuntimeException(
				"HttpServletRequestStub.getProtocol() not implemented.");
	}

	public BufferedReader getReader() throws IOException {
		throw new RuntimeException(
				"HttpServletRequestStub.getReader() not implemented.");
	}

	public String getRealPath(String arg0) {
		throw new RuntimeException(
				"HttpServletRequestStub.getRealPath() not implemented.");
	}

	public String getRemoteAddr() {
		throw new RuntimeException(
				"HttpServletRequestStub.getRemoteAddr() not implemented.");
	}

	public String getRemoteHost() {
		throw new RuntimeException(
				"HttpServletRequestStub.getRemoteHost() not implemented.");
	}

	public int getRemotePort() {
		throw new RuntimeException(
				"HttpServletRequestStub.getRemotePort() not implemented.");
	}

	public RequestDispatcher getRequestDispatcher(String arg0) {
		throw new RuntimeException(
				"HttpServletRequestStub.getRequestDispatcher() not implemented.");
	}

	public String getScheme() {
		throw new RuntimeException(
				"HttpServletRequestStub.getScheme() not implemented.");
	}

	public String getServerName() {
		throw new RuntimeException(
				"HttpServletRequestStub.getServerName() not implemented.");
	}

	public int getServerPort() {
		throw new RuntimeException(
				"HttpServletRequestStub.getServerPort() not implemented.");
	}

	public boolean isSecure() {
		throw new RuntimeException(
				"HttpServletRequestStub.isSecure() not implemented.");
	}

	public void removeAttribute(String arg0) {
		throw new RuntimeException(
				"HttpServletRequestStub.removeAttribute() not implemented.");
	}

	public void setAttribute(String arg0, Object arg1) {
		throw new RuntimeException(
				"HttpServletRequestStub.setAttribute() not implemented.");
	}

	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		throw new RuntimeException(
				"HttpServletRequestStub.setCharacterEncoding() not implemented.");
	}

}
