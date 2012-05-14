/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.httpservice;

import java.util.Dictionary;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * A thin implementation of the OSGi HttpService API.
 * 
 * When a client registers a Servlet or a ResourceGroup, this service creates a
 * Servicer instance and passes it on to the HttpRequestHandler to manage.
 * 
 * The Servicer instance contains a reference to the client bundle, so we can
 * assign proper context objects, and so we can more easily diagnose problems.
 * 
 * As per the spec, a servlet that is registered with no HttpContext gets a
 * default implementation, and if two servlets are registered with the same
 * HttpContext, they will share a single ServletContext.
 * 
 * The default implementation of HttpContext is bundle-specific, since it will
 * use Bundle.getResource().
 */
public class VitroHttpService implements HttpService {
	private static final Log log = LogFactory.getLog(VitroHttpService.class);

	private final Bundle bundle;
	private final HttpRequestHandler registry;
	private final ServletContext servletContext;
	private final HttpContext defaultHttpContext;

	public VitroHttpService(Bundle bundle, HttpRequestHandler registry,
			ServletContext servletContext) {
		this.bundle = bundle;
		this.registry = registry;
		this.servletContext = servletContext;
		this.defaultHttpContext = new DefaultHttpContext(bundle);
	}

	@Override
	public HttpContext createDefaultHttpContext() {
		log.debug("createDefaultHttpContext for " + formatBundleInfo());
		return defaultHttpContext;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void registerServlet(String alias, Servlet servlet,
			Dictionary initparams, HttpContext httpContext)
			throws ServletException, NamespaceException {
		HttpContext nonNullHttpContext = nonNull(httpContext);
		ServletContextWrapper servletContextWrapper = registry
				.findServletContextWrapper(nonNullHttpContext);
		Servicer servicer = new ServletServicer(alias, bundle,
				nonNullHttpContext, servlet, initparams, servletContextWrapper);

		registry.registerServicer(servicer);
		servicer.init();
	}

	@Override
	public void registerResources(String alias, String name,
			HttpContext httpContext) throws NamespaceException {
		HttpContext nonNullHttpContext = nonNull(httpContext);
		Servicer servicer = new ResourceGroupServicer(alias, bundle,
				nonNullHttpContext, servletContext, name);

		registry.registerServicer(servicer);
	}

	@Override
	public void unregister(String alias) {
		Servicer servicer = registry.unregister(alias);
		if (servicer != null) {
			servicer.dispose();
		}
	}

	/**
	 * If an HttpContext is not provided with the "register" request, use the
	 * default HttpContext for this service instance.
	 */
	private HttpContext nonNull(HttpContext httpContext) {
		if (httpContext == null) {
			return createDefaultHttpContext();
		} else {
			return httpContext;
		}
	}

	private String formatBundleInfo() {
		return bundle.getSymbolicName() + " - [" + bundle.getBundleId() + "]";
	}

	@Override
	public String toString() {
		return "VitroHttpService[" + formatBundleInfo() + "]";
	}

}
