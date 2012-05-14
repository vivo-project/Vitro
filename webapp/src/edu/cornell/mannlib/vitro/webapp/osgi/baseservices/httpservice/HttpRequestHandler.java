/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.httpservice;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.NamespaceException;

/**
 * Hold all of the registered Servicers, and use them to service the HTTP
 * requests. Or not.
 */
public class HttpRequestHandler {
	private static final Log log = LogFactory.getLog(HttpRequestHandler.class);

	private final ServletContext servletContext;

	/** Registered servlets and resource groups, mapped by their aliases. */
	private final ConcurrentMap<String, Servicer> registry = new ConcurrentHashMap<String, Servicer>();

	public HttpRequestHandler(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	// ----------------------------------------------------------------------
	// Servicer lifecycle methods.
	// ----------------------------------------------------------------------

	/**
	 * Find the ServletContext that has been associated with this HttpContext.
	 * If no such ServletContext is found, create one.
	 */
	public ServletContextWrapper findServletContextWrapper(
			HttpContext httpContext) {
		if (httpContext == null) {
			throw new NullPointerException("httpContext may not be null.");
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

	/**
	 * Register a Servlet or ResourceGroup at an associated alias.
	 */
	public void registerServicer(Servicer servicer) throws NamespaceException {
		String alias = servicer.getAlias();

		if (registry.containsKey(alias)) {
			throw new NamespaceException("Alias '" + alias
					+ "' is already registered.");
		}

		log.debug("register servicer: " + servicer);
		registry.put(alias, servicer);
	}

	/**
	 * Unregister a Servlet or ResourceGroup at an associated alias.
	 */
	public Servicer unregister(String alias) {
		if (registry.containsKey(alias)) {
			Servicer servicer = registry.remove(alias);
			log.debug("unregister servicer: " + servicer);
			return servicer;
		} else {
			log.debug("unregister servicer - not found at '" + alias + "'");
			return null;
		}
	}

	/**
	 * When the system shuts down, complain about any aliases that are still
	 * registered.
	 */
	public void shutdown() {
		for (Servicer servicer : registry.values()) {
			log.warn("Servicer was not unregistered before the system shut down: "
					+ servicer);
		}
	}

	// ----------------------------------------------------------------------
	// Service a request
	// ----------------------------------------------------------------------

	/**
	 * If a registered Servicer (Servlet or ResourceGroup) claims to satisfy
	 * this HTTP request, then let it. Otherwise, let the application base
	 * handle it.
	 * 
	 * When an HTTP request is received, check for a Servicer that matches that
	 * full request URI. If we find one, delegate to it. Otherwise, remove the
	 * last segment of the request URI and look again.
	 * 
	 * The last segment is a special case, since it matches "/" instead of the
	 * empty string.
	 * 
	 * If we remove all of the segments and still don't find a suitable
	 * Servicer, then return false and let the application base handle the
	 * request.
	 * 
	 * @return true if the request was serviced. false otherwise.
	 */
	public boolean serviceRequest(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		String requestPath = req.getServletPath();
		for (String path = requestPath; !path.isEmpty(); path = removeTail(path)) {
			if (registry.containsKey(path)) {
				log.debug("alias '" + path + "' matches requestPath '"
						+ requestPath + "'");
				Servicer servicer = registry.get(path);
				servicer.service(req, resp);
				return true;
			} else {
				log.debug("no match for '" + path + "'.");
			}
		}

		if (registry.containsKey("/")) {
			log.debug("alias '/' matches requestPath '" + requestPath + "'");
			Servicer servicer = registry.get("/");
			servicer.service(req, resp);
			return true;
		}

		log.debug("no match for '/'.");
		return false;
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
