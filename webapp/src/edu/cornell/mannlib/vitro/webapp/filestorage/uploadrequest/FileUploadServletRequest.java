/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * <p>
 * Wraps an HTTP request and parses it for file uploads, without losing the
 * request parameters.
 * </p>
 * <p>
 * Most methods are declared here, and simply delegate to the wrapped request.
 * Methods that have to do with parameters or files are handled differently for
 * simple requests and multipart request, and are implemented in the
 * sub-classes.
 * </p>
 */
public abstract class FileUploadServletRequest implements HttpServletRequest {
	// ----------------------------------------------------------------------
	// The factory method
	// ----------------------------------------------------------------------

	/**
	 * Wrap this {@link HttpServletRequest} in an appropriate wrapper class.
	 */
	public static FileUploadServletRequest parseRequest(
			HttpServletRequest request) throws IOException {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (isMultipart) {
			return new MultipartHttpServletRequest(request);
		} else {
			return new SimpleHttpServletRequestWrapper(request);
		}
	}

	// ----------------------------------------------------------------------
	// The constructor and the delegate.
	// ----------------------------------------------------------------------

	private final HttpServletRequest delegate;

	public FileUploadServletRequest(HttpServletRequest delegate) {
		this.delegate = delegate;
	}

	protected HttpServletRequest getDelegate() {
		return this.delegate;
	}

	// ----------------------------------------------------------------------
	// New functionality to be implemented by the subclasses.
	// ----------------------------------------------------------------------

	public abstract boolean isMultipart();

	public abstract Map<String, List<FileItem>> getFiles();

	// ----------------------------------------------------------------------
	// Delegated methods.
	// ----------------------------------------------------------------------

	@Override
	public String getAuthType() {
		return delegate.getAuthType();
	}

	@Override
	public String getContextPath() {
		return delegate.getContextPath();
	}

	@Override
	public Cookie[] getCookies() {
		return delegate.getCookies();
	}

	@Override
	public long getDateHeader(String name) {
		return delegate.getDateHeader(name);
	}

	@Override
	public String getHeader(String name) {
		return delegate.getHeader(name);
	}

	@Override
	public Enumeration<?> getHeaderNames() {
		return delegate.getHeaderNames();
	}

	@Override
	public Enumeration<?> getHeaders(String name) {
		return delegate.getHeaders(name);
	}

	@Override
	public int getIntHeader(String name) {
		return delegate.getIntHeader(name);
	}

	@Override
	public String getMethod() {
		return delegate.getMethod();
	}

	@Override
	public String getPathInfo() {
		return delegate.getPathInfo();
	}

	@Override
	public String getPathTranslated() {
		return delegate.getPathTranslated();
	}

	@Override
	public String getQueryString() {
		return delegate.getQueryString();
	}

	@Override
	public String getRemoteUser() {
		return delegate.getRemoteUser();
	}

	@Override
	public String getRequestURI() {
		return delegate.getRequestURI();
	}

	@Override
	public StringBuffer getRequestURL() {
		return delegate.getRequestURL();
	}

	@Override
	public String getRequestedSessionId() {
		return delegate.getRequestedSessionId();
	}

	@Override
	public String getServletPath() {
		return delegate.getServletPath();
	}

	@Override
	public HttpSession getSession() {
		return delegate.getSession();
	}

	@Override
	public HttpSession getSession(boolean create) {
		return delegate.getSession(create);
	}

	@Override
	public Principal getUserPrincipal() {
		return delegate.getUserPrincipal();
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return delegate.isRequestedSessionIdFromCookie();
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return delegate.isRequestedSessionIdFromURL();
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return delegate.isRequestedSessionIdFromUrl();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return delegate.isRequestedSessionIdValid();
	}

	@Override
	public boolean isUserInRole(String role) {
		return delegate.isUserInRole(role);
	}

	@Override
	public Object getAttribute(String name) {
		return delegate.getAttribute(name);
	}

	@Override
	public Enumeration<?> getAttributeNames() {
		return delegate.getAttributeNames();
	}

	@Override
	public String getCharacterEncoding() {
		return delegate.getCharacterEncoding();
	}

	@Override
	public int getContentLength() {
		return delegate.getContentLength();
	}

	@Override
	public String getContentType() {
		return delegate.getContentType();
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return delegate.getInputStream();
	}

	@Override
	public String getLocalAddr() {
		return delegate.getLocalAddr();
	}

	@Override
	public String getLocalName() {
		return delegate.getLocalName();
	}

	@Override
	public int getLocalPort() {
		return delegate.getLocalPort();
	}

	@Override
	public Locale getLocale() {
		return delegate.getLocale();
	}

	@Override
	public Enumeration<?> getLocales() {
		return delegate.getLocales();
	}

	@Override
	public String getProtocol() {
		return delegate.getProtocol();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return delegate.getReader();
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getRealPath(String path) {
		return delegate.getRealPath(path);
	}

	@Override
	public String getRemoteAddr() {
		return delegate.getRemoteAddr();
	}

	@Override
	public String getRemoteHost() {
		return delegate.getRemoteHost();
	}

	@Override
	public int getRemotePort() {
		return delegate.getRemotePort();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return delegate.getRequestDispatcher(path);
	}

	@Override
	public String getScheme() {
		return delegate.getScheme();
	}

	@Override
	public String getServerName() {
		return delegate.getServerName();
	}

	@Override
	public int getServerPort() {
		return delegate.getServerPort();
	}

	@Override
	public boolean isSecure() {
		return delegate.isSecure();
	}

	@Override
	public void removeAttribute(String name) {
		delegate.removeAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object o) {
		delegate.setAttribute(name, o);
	}

	@Override
	public void setCharacterEncoding(String env)
			throws UnsupportedEncodingException {
		delegate.setCharacterEncoding(env);
	}

}
