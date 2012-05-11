/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.httpservice;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * A thin implementation of the OSGi HttpService API.
 * 
 * Allows clients to register servlets or resources. When Tomcat receives an
 * HTTP request, the HttpServiceFilter asks this service if it has anything
 * registered that can satisfy that request.
 * 
 * When a servlet is registered, it is provided with a ServletConfig that
 * provides a reference to a ServletContext. That ServletContext is mostly a
 * wrapper around Tomcat's ServletContext, but delegates some methods back to
 * the HttpContext that was supplied when the servlet was registered, as
 * required by the spec. It also keeps its own map of attributes, so servlets in
 * bundles can't communicate with each other, or with the application base,
 * through context attributes.
 * 
 * As per the spec, a servlet that is registered with no HttpContext gets a
 * default implementation, and if two servlets are registered with the same
 * HttpContext, they will share a single ServletContext.
 */
public class VitroHttpService implements HttpService {
	private static final Log log = LogFactory.getLog(VitroHttpService.class);

	private final ServletContext servletContext;
	private final HttpContext defaultHttpContext;

	private final Map<String, ServletRegistrationInfo> servletMap = new HashMap<String, ServletRegistrationInfo>();

	public VitroHttpService(ServletContext servletContext,
			HttpContext defaultHttpContext) {
		this.servletContext = servletContext;
		this.defaultHttpContext = defaultHttpContext;
	}

	@Override
	public HttpContext createDefaultHttpContext() {
		log.debug("createDefaultHttpContext");
		return defaultHttpContext;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void registerServlet(String alias, Servlet servlet,
			Dictionary initparams, HttpContext httpContext)
			throws ServletException, NamespaceException {
		log.debug("registerServlet alias=" + alias + ", servlet=" + servlet
				+ ", initparams=" + initparams + ", context=" + httpContext);

		if (servletMap.containsKey(alias)) {
			throw new NamespaceException("Alias '" + alias
					+ "' is already registered.");
		}

		ServletContextWrapper ctxWrapper = obtainServletContextWrapper(httpContext);
		Map<String, String> initparamsMap = toMap(initparams);
		BasicServletConfig sc = new BasicServletConfig(ctxWrapper, alias,
				initparamsMap);
		servletMap.put(alias, new ServletRegistrationInfo(alias, servlet, sc,
				httpContext, ctxWrapper));

		log.debug("initializing servlet at '" + alias + "'");
		servlet.init(sc);
	}

	@Override
	public void registerResources(String alias, String name, HttpContext context)
			throws NamespaceException {
		// TODO register resources
		log.error("VitroHttpService.registerResources not implemented. alias="
				+ alias + ", name=" + name + ", context=" + context);
	}

	/**
	 * If we can find a servlet or a resource mapped to this request, handle it
	 * and return true. Otherwise, return false.
	 */
	public boolean serviceRequest(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		String requestPath = req.getServletPath();
		String matchingAlias = findMatchingAlias(requestPath);
		if (matchingAlias == null) {
			log.debug("can't service request: " + requestPath);
			return false;
		} else {
			log.debug("service request: " + requestPath);
			ServletRegistrationInfo sri = servletMap.get(matchingAlias);
			RequestWrapper wrappedReq = new RequestWrapper(req, matchingAlias);
			delegateToRegisteredServlet(wrappedReq, resp, sri);
			return true;
		}
	}

	@Override
	public void unregister(String alias) {
		if (servletMap.containsKey(alias)) {
			log.debug("removing '" + alias + "' from servlet map");
			ServletRegistrationInfo sri = servletMap.remove(alias);
			sri.getServlet().destroy();
			return;
		}

		// TODO unregister a resources collection
		log.error("VitroHttpService.unregister not implemented. alias=" + alias);
	}

	public void shutdown() {
		// TODO Complain if any servlets or resources are still registered
		log.error("VitroHttpService.shutdown() not implemented.");
	}

	/**
	 * Servlets that have no HttpContext get the default instance. Servlets that
	 * share the same HttpContext get the same ServletContextWrapper.
	 */
	private ServletContextWrapper obtainServletContextWrapper(
			HttpContext httpContext) {
		if (httpContext == null) {
			httpContext = defaultHttpContext;
		}

		for (ServletRegistrationInfo sri : servletMap.values()) {
			if (sri.getHttpContext().equals(httpContext)) {
				return sri.getServletContext();
			}
		}

		return new ServletContextWrapper(servletContext, httpContext);
	}

	private Map<String, String> toMap(Dictionary<String, String> initparams) {
		Map<String, String> map = new HashMap<String, String>();
		Enumeration<String> keys = initparams.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			String value = initparams.get(key);
			map.put(key, value);
		}
		return map;
	}

	private void delegateToRegisteredServlet(HttpServletRequest req,
			HttpServletResponse resp, ServletRegistrationInfo sri)
			throws ServletException, IOException {
		boolean authorized = sri.getHttpContext().handleSecurity(req, resp);
		if (authorized) {
			sri.getServlet().service(req, resp);
		}
	}

	private String findMatchingAlias(String requestPath) {
		for (String path = requestPath; !path.isEmpty(); path = removeTail(path)) {
			if (servletMap.containsKey(path)) {
				log.debug("alias '" + path + "' matches requestPath '"
						+ requestPath + "'");
				return path;
			}
		}

		if (servletMap.containsKey("/")) {
			log.debug("alias '/' matches requestPath '" + requestPath + "'");
			return "/";
		}

		return null;
	}

	private String removeTail(String path) {
		int slashHere = path.lastIndexOf('/');
		if (slashHere == -1) {
			return "";
		} else {
			return path.substring(0, slashHere);
		}
	}
}
