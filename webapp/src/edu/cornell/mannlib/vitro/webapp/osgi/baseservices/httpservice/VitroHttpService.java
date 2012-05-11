/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.httpservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
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

	/** Servlets and resource groups, mapped by their aliases. */
	private final Map<String, RegistrationInfo> servletMap = new HashMap<String, RegistrationInfo>();

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

		servletMap.put(alias, new ServletRegistrationInfo(alias,
				nonNull(httpContext), servlet, sc, ctxWrapper));

		log.debug("initializing servlet at '" + alias + "'");
		servlet.init(sc);
	}

	@Override
	public void registerResources(String alias, String name,
			HttpContext httpContext) throws NamespaceException {
		log.debug("registerResources alias=" + alias + ", name=" + name
				+ ", httpContext=" + httpContext);

		if (servletMap.containsKey(alias)) {
			throw new NamespaceException("Alias '" + alias
					+ "' is already registered.");
		}

		servletMap.put(alias, new ResourcesRegistrationInfo(alias,
				nonNull(httpContext), name));
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

		RegistrationInfo ri = servletMap.get(matchingAlias);
		if (ri instanceof ServletRegistrationInfo) {
			log.debug("servicing servlet request for '" + requestPath + "'");
			ServletRegistrationInfo sri = (ServletRegistrationInfo) ri;
			RequestWrapper wrappedReq = new RequestWrapper(req, matchingAlias);
			delegateToRegisteredServlet(wrappedReq, resp, sri);
			return true;
		}

		ResourcesRegistrationInfo rri = (ResourcesRegistrationInfo) ri;
		return serveResource(req, resp, rri);
	}

	private boolean serveResource(HttpServletRequest req,
			HttpServletResponse resp, ResourcesRegistrationInfo rri) throws IOException {
		String resourcePath = resolveResourcePath(rri, req);
		URL url = rri.getHttpContext().getResource(resourcePath);
		if (url == null) {
			log.debug("can't find resource for '" + resourcePath + "'");
			return false;
		}

		String mimeType = figureMimeType(rri, resourcePath);
		log.debug("mime type is " + mimeType);
		resp.setContentType(mimeType);
		
		InputStream in = url.openStream();
		OutputStream out = resp.getOutputStream();
		byte[] buffer = new byte[8192];
		int howManyBytes;
		while (-1 != (howManyBytes = in.read(buffer)) ) {
			out.write(buffer, 0, howManyBytes);
		}

		return true;
	}

	private String resolveResourcePath(ResourcesRegistrationInfo rri,
			HttpServletRequest req) {
		String alias = rri.getAlias();
		if (alias.equals("/")) {
			alias = "";
		}
		String internalName = rri.getInternalName();
		if (internalName.equals("/")) {
			internalName = "";
		}
		String uri = req.getServletPath();
		if (req.getPathInfo() != null) {
			uri += req.getPathInfo();
		}
		
		return internalName + uri.substring(alias.length());
	}

	private String figureMimeType(ResourcesRegistrationInfo rri,
			String resourcePath) {
		String mimeType = rri.getHttpContext().getMimeType(resourcePath);
		if (mimeType == null) {
			mimeType = servletContext.getMimeType(resourcePath);
		}
		return mimeType;
	}

	@Override
	public void unregister(String alias) {
		if (servletMap.containsKey(alias)) {
			log.debug("removing '" + alias + "' from servlet map");
			RegistrationInfo ri = servletMap.remove(alias);
			if (ri instanceof ServletRegistrationInfo) {
				ServletRegistrationInfo sri = (ServletRegistrationInfo) ri;
				sri.getServlet().destroy();
			}
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

		for (RegistrationInfo sri : servletMap.values()) {
			if (sri.getHttpContext().equals(httpContext)) {
				if (sri instanceof ServletRegistrationInfo) {
					return ((ServletRegistrationInfo) sri).getServletContext();
				}
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
