/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.javax.servlet.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
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
 * A simple stub for HttpServletRequest
 */
@SuppressWarnings("deprecation")
public class HttpServletRequestStub implements HttpServletRequest {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private URL requestUrl;
	private String httpMethodType = "GET";
	private String remoteAddr = "127.0.0.1";

	private HttpSession session;
	private final Map<String, List<String>> parameters;
	private final Map<String, Object> attributes;
	private final Map<String, List<String>> headers;

	public HttpServletRequestStub() {
		parameters = new HashMap<String, List<String>>();
		attributes = new HashMap<String, Object>();
		headers = new HashMap<String, List<String>>();
	}

	public HttpServletRequestStub(Map<String, List<String>> parameters,
			Map<String, Object> attributes) {
		this();
		this.parameters.putAll(parameters);
		this.attributes.putAll(attributes);
	}

	public void setRequestUrl(URL url) {
		this.requestUrl = url;
	}

	/** Set to "GET" or "POST", etc. */
	public void setMethod(String method) {
		this.httpMethodType = method;
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}
	
	public void setHeader(String name, String value) {
		name = name.toLowerCase();
		if (!headers.containsKey(name)) {
			headers.put(name, new ArrayList<String>());
		}
		headers.get(name).add(value);
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

	public void setSession(HttpSession session) {
		this.session = session;
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	public HttpSession getSession() {
		return getSession(true);
	}

	public HttpSession getSession(boolean create) {
		if (create && (session == null)) {
			session = new HttpSessionStub();
		}
		return session;
	}

	public String getContextPath() {
		String path = requestUrl.getPath();
		if (path.isEmpty()) {
			return "";
		}
		int secondSlash = path.indexOf("/", 1);
		if (secondSlash == -1) {
			return "";
		} else {
			return path.substring(0, secondSlash);
		}
	}

	public String getMethod() {
		return httpMethodType;
	}

	public String getRemoteAddr() {
		return remoteAddr;
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getParameterNames() {
		return Collections.enumeration(parameters.keySet());
	}

	@SuppressWarnings("rawtypes")
	public Map getParameterMap() {
		Map<String, String[]> map = new HashMap<String, String[]>();
		for (String key : parameters.keySet()) {
			map.put(key, parameters.get(key).toArray(new String[0]));
		}
		return map;
	}

	public String getParameter(String name) {
		if (!parameters.containsKey(name)) {
			return null;
		}
		return parameters.get(name).get(0);
	}

	public String[] getParameterValues(String name) {
		if (!parameters.containsKey(name)) {
			return null;
		}
		List<String> list = parameters.get(name);
		return list.toArray(new String[list.size()]);
	}

	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getAttributeNames() {
		return Collections.enumeration(attributes.keySet());
	}

	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	public void setAttribute(String name, Object value) {
		if (value == null) {
			removeAttribute(name);
		}
		attributes.put(name, value);
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getHeaderNames() {
		return Collections.enumeration(headers.keySet());
	}

	public String getHeader(String name) {
		name = name.toLowerCase();
		if (headers.containsKey(name)) {
			return headers.get(name).get(0);
		} else {
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getHeaders(String name) {
		name = name.toLowerCase();
		if (headers.containsKey(name)) {
			return Collections.enumeration(headers.get(name));
		} else {
			return Collections.enumeration(Collections.emptyList());
		}
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	public String getAuthType() {
		throw new RuntimeException(
				"HttpServletRequestStub.getAuthType() not implemented.");
	}

	public Cookie[] getCookies() {
		throw new RuntimeException(
				"HttpServletRequestStub.getCookies() not implemented.");
	}

	public long getDateHeader(String arg0) {
		throw new RuntimeException(
				"HttpServletRequestStub.getDateHeader() not implemented.");
	}

	public int getIntHeader(String arg0) {
		throw new RuntimeException(
				"HttpServletRequestStub.getIntHeader() not implemented.");
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

	@SuppressWarnings("rawtypes")
	public Enumeration getLocales() {
		throw new RuntimeException(
				"HttpServletRequestStub.getLocales() not implemented.");
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

	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		throw new RuntimeException(
				"HttpServletRequestStub.setCharacterEncoding() not implemented.");
	}

}
