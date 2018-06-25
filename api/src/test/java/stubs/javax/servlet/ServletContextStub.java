/* $This file is distributed under the terms of the license in LICENSE$ */

package stubs.javax.servlet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A simple stand-in for the {@link ServletContext}, for use in unit tests.
 */
public class ServletContextStub implements ServletContext {
	private static final Log log = LogFactory.getLog(ServletContextStub.class);

	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private String contextPath = ""; // root context returns ""
	private final Map<String, Object> attributes = new HashMap<String, Object>();
	private final Map<String, String> mockResources = new HashMap<String, String>();
	private final Map<String, String> realPaths = new HashMap<String, String>();

	public void setContextPath(String contextPath) {
		if (contextPath == null) {
			throw new NullPointerException("contextPath may not be null.");
		}
	}

	public void setMockResource(String path, String contents) {
		if (path == null) {
			throw new NullPointerException("path may not be null.");
		}
		if (contents == null) {
			mockResources.remove(path);
		} else {
			mockResources.put(path, contents);
		}
	}

	public void setRealPath(String path, String filepath) {
		if (path == null) {
			throw new NullPointerException("path may not be null.");
		}
		if (filepath == null) {
			log.debug("removing real path for '" + path + "'");
			realPaths.remove(path);
		} else {
			log.debug("adding real path for '" + path + "' = '" + filepath
					+ "'");
			realPaths.put(path, filepath);
		}
	}

	/**
	 * Call setRealPath for each of the files in this directory (non-recursive).
	 * The prefix is the "pretend" location that we're mapping these files to,
	 * e.g. "/config/". Use the prefix and the filename as the path.
	 */
	public void setRealPaths(String pathPrefix, File dir) {
		for (File file : dir.listFiles()) {
			setRealPath(pathPrefix + file.getName(), file.getPath());
		}
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public String getContextPath() {
		return contextPath;
	}

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

	@Override
	public InputStream getResourceAsStream(String path) {
		if (mockResources.containsKey(path)) {
			return new ByteArrayInputStream(mockResources.get(path).getBytes());
		} else {
			return null;
		}
	}

	@Override
	public String getRealPath(String path) {
		String real = realPaths.get(path);
		log.debug("Real path for '" + path + "' is '" + real + "'");
		return real;
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
	public String getInitParameter(String arg0) {
		throw new RuntimeException(
				"ServletContextStub.getInitParameter() not implemented.");
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getInitParameterNames() {
		throw new RuntimeException(
				"ServletContextStub.getInitParameterNames() not implemented.");
	}

	@Override
	public boolean setInitParameter(String s, String s1) {
		return false;
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
	public int getEffectiveMajorVersion() {
		return 0;
	}

	@Override
	public int getEffectiveMinorVersion() {
		return 0;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) {
		throw new RuntimeException(
				"ServletContextStub.getNamedDispatcher() not implemented.");
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
	@SuppressWarnings("rawtypes")
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
	@Deprecated
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
	public ServletRegistration.Dynamic addServlet(String s, String s1) {
		return null;
	}

	@Override
	public ServletRegistration.Dynamic addServlet(String s, Servlet servlet) {
		return null;
	}

	@Override
	public ServletRegistration.Dynamic addServlet(String s, Class<? extends Servlet> aClass) {
		return null;
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> aClass) throws ServletException {
		return null;
	}

	@Override
	public ServletRegistration getServletRegistration(String s) {
		return null;
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		return null;
	}

	@Override
	public FilterRegistration.Dynamic addFilter(String s, String s1) {
		return null;
	}

	@Override
	public FilterRegistration.Dynamic addFilter(String s, Filter filter) {
		return null;
	}

	@Override
	public FilterRegistration.Dynamic addFilter(String s, Class<? extends Filter> aClass) {
		return null;
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> aClass) throws ServletException {
		return null;
	}

	@Override
	public FilterRegistration getFilterRegistration(String s) {
		return null;
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		return null;
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		return null;
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> set) {

	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		return null;
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		return null;
	}

	@Override
	public void addListener(String s) {

	}

	@Override
	public <T extends EventListener> void addListener(T t) {

	}

	@Override
	public void addListener(Class<? extends EventListener> aClass) {

	}

	@Override
	public <T extends EventListener> T createListener(Class<T> aClass) throws ServletException {
		return null;
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		return null;
	}

	@Override
	public ClassLoader getClassLoader() {
		return null;
	}

	@Override
	public void declareRoles(String... strings) {

	}

	@Override
	public String getVirtualServerName() {
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	@Deprecated
	public Enumeration getServletNames() {
		throw new RuntimeException(
				"ServletContextStub.getServletNames() not implemented.");
	}

	@Override
	@SuppressWarnings("rawtypes")
	@Deprecated
	public Enumeration getServlets() {
		throw new RuntimeException(
				"ServletContextStub.getServlets() not implemented.");
	}

	@Override
	public void log(String arg0) {
		throw new RuntimeException("ServletContextStub.log() not implemented.");
	}

	@Override
	@Deprecated
	public void log(Exception arg0, String arg1) {
		throw new RuntimeException("ServletContextStub.log() not implemented.");
	}

	@Override
	public void log(String arg0, Throwable arg1) {
		throw new RuntimeException("ServletContextStub.log() not implemented.");
	}

}
