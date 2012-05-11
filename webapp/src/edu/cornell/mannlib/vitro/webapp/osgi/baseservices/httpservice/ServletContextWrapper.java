/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.httpservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.http.HttpContext;

/**
 * A wrapper for Tomcat's ServletContext, to be used in the implementation of
 * OSGi HttpService.
 * 
 * Delegates getMimeType(), getResource() and getResourceAsStream() to the
 * HttpContext that was supplied with the registered servlet or resource.
 * 
 * Keeps its own attributes map so Bundles can't communicate with each other or
 * with the application base through the attributes.
 * 
 * Delegates all other methods to Tomcat's ServletContext.
 */
public class ServletContextWrapper implements ServletContext {
	private static final Log log = LogFactory
			.getLog(ServletContextWrapper.class);

	@SuppressWarnings("unchecked")
	private static final Enumeration<Object> EMPTY_ENUMERATION = Collections
			.enumeration(Collections.EMPTY_LIST);

	private final ServletContext ctx;
	private final HttpContext httpContext;

	private final ConcurrentMap<String, Object> attributesMap = new ConcurrentHashMap<String, Object>();

	public ServletContextWrapper(ServletContext ctx, HttpContext httpContext) {
		this.ctx = ctx;
		this.httpContext = httpContext;
	}

	// ----------------------------------------------------------------------
	// Methods that delegate to HttpContext
	// ----------------------------------------------------------------------

	@Override
	public String getMimeType(String name) {
		String mimeType = httpContext.getMimeType(name);
		log.debug("getMimeType: '" + name + "' = '" + mimeType + "'");
		return mimeType;
	}

	@Override
	public URL getResource(String name) throws MalformedURLException {
		URL url = httpContext.getResource(name);
		log.debug("getResource: '" + name + "' = '" + url + "'");
		return url;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		URL url = httpContext.getResource(name);
		if (url == null) {
			log.debug("getResourceAsStream: not found");
			return null;
		}
		try {
			InputStream stream = url.openConnection().getInputStream();
			log.debug("getResourceAsStream: '" + stream + "' - success");
			return stream;
		} catch (IOException e) {
			log.debug("getResourceAsStream: exception" + e);
			return null;
		}
	}

	// ----------------------------------------------------------------------
	// Methods that are handled here
	// ----------------------------------------------------------------------

	@Override
	public Object getAttribute(String name) {
		return attributesMap.get(name);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getAttributeNames() {
		return Collections.enumeration(new HashSet<String>(attributesMap.keySet()));
	}

	@Override
	public void removeAttribute(String name) {
		attributesMap.remove(name);
	}

	@Override
	public void setAttribute(String name, Object object) {
		attributesMap.put(name, object);
	}
	
	// ----------------------------------------------------------------------
	// Methods that are delegated to Tomcat's ServletContext
	// ----------------------------------------------------------------------

	@Override
	public ServletContext getContext(String uripath) {
		return ctx.getContext(uripath);
	}

	@Override
	public String getContextPath() {
		return ctx.getContextPath();
	}

	@Override
	public String getInitParameter(String name) {
		return ctx.getInitParameter(name);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getInitParameterNames() {
		return ctx.getInitParameterNames();
	}

	@Override
	public int getMajorVersion() {
		return ctx.getMajorVersion();
	}

	@Override
	public int getMinorVersion() {
		return ctx.getMinorVersion();
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		return ctx.getNamedDispatcher(name);
	}

	@Override
	public String getRealPath(String path) {
		return ctx.getRealPath(path);
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return ctx.getRequestDispatcher(path);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Set getResourcePaths(String path) {
		return ctx.getResourcePaths(path);
	}

	@Override
	public String getServerInfo() {
		return ctx.getServerInfo();
	}

	@Deprecated
	@Override
	public Servlet getServlet(String name) throws ServletException {
		return null;
	}

	@Override
	public String getServletContextName() {
		return ctx.getServletContextName();
	}

	@Deprecated
	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getServletNames() {
		return EMPTY_ENUMERATION;
	}

	@Deprecated
	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getServlets() {
		return EMPTY_ENUMERATION;
	}

	@Override
	public void log(String msg) {
		ctx.log(msg);
	}

	@Deprecated
	@Override
	public void log(Exception throwable, String msg) {
		ctx.log(msg, throwable);
	}

	@Override
	public void log(String message, Throwable throwable) {
		ctx.log(message, throwable);
	}

}
