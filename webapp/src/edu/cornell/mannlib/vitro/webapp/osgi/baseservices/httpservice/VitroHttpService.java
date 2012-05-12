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

	/** Registered servlets and resource groups, mapped by their aliases. */
	private final Map<String, Servicer> registry = new HashMap<String, Servicer>();

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

		ServletContextWrapper ctxWrapper = obtainServletContextWrapper(httpContext);
		Servicer servicer = new ServletServicer(alias, nonNull(httpContext),
				servlet, initparams, ctxWrapper);

		registerServicer(servicer);

		servicer.init();
	}

	@Override
	public void registerResources(String alias, String name,
			HttpContext httpContext) throws NamespaceException {
		Servicer servicer = new ResourceGroupServicer(alias,
				nonNull(httpContext), servletContext, name);

		registerServicer(servicer);
	}

	private void registerServicer(Servicer servicer) throws NamespaceException {
		String alias = servicer.getAlias();

		if (registry.containsKey(alias)) {
			throw new NamespaceException("Alias '" + alias
					+ "' is already registered.");
		}

		log.debug("register servicer: " + servicer);
		registry.put(alias, servicer);
	}

	private HttpContext nonNull(HttpContext httpContext) {
		if (httpContext == null) {
			return createDefaultHttpContext();
		} else {
			return httpContext;
		}
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
		}

		Servicer servicer = registry.get(matchingAlias);
		servicer.service(req, resp);
		return true;
	}

	@Override
	public void unregister(String alias) {
		if (registry.containsKey(alias)) {
			log.debug("removing '" + alias + "' from servlet map");
			Servicer servicer = registry.remove(alias);
			servicer.dispose();
			return;
		}
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

		for (Servicer sri : registry.values()) {
			if (sri.getHttpContext().equals(httpContext)) {
				if (sri instanceof ServletServicer) {
					return ((ServletServicer) sri).getServletContext();
				}
			}
		}

		return new ServletContextWrapper(servletContext, httpContext);
	}

	private String findMatchingAlias(String requestPath) {
		for (String path = requestPath; !path.isEmpty(); path = removeTail(path)) {
			if (registry.containsKey(path)) {
				log.debug("alias '" + path + "' matches requestPath '"
						+ requestPath + "'");
				return path;
			}
		}

		if (registry.containsKey("/")) {
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
