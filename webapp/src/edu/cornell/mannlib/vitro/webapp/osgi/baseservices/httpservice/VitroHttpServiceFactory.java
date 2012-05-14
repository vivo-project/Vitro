/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.httpservice;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpContext;

/**
 * This creates an instance of VitroHttpService for each bundle that wants one,
 * and disposes of the service when it is no longer used.
 * 
 * Each instance gets its own copy of the default HttpContext, since it is
 * required to be bundle-dependent.
 * 
 * If any service instances remain at shutdown, complain.
 * 
 * TODO Figure out how collisions are handled with this structure, and whether
 * that's acceptable. If two bundles have register servlets at /foo, then we get
 * to decide which one to use by ordering the bundles by their properties. But
 * what if one bundle registers /foo and the other registers /foo/bar? Or what
 * if they both register /foo, but the URL is /foo/bar, and the first one
 * doesn't satisfy it? Need to figure out what do to in these edge cases.
 * 
 * TODO Keeping a separate registry may not work, since it means we can't tell
 * if two servlets from two different bundles are using the same HttpContext (in
 * which case they should get the same ServletContext). But is that likely?
 */
public class VitroHttpServiceFactory implements
		ServiceFactory<VitroHttpService>, HttpRequestHandler {
	private static final Log log = LogFactory
			.getLog(VitroHttpServiceFactory.class);

	/**
	 * The servlet context from Tomcat. This will be partially revealed to the
	 * registered servlets.
	 */
	private final ServletContext servletContext;

	/** Record the active service instances. */
	private final Set<VitroHttpService> serviceInstances = Collections
			.synchronizedSet(new HashSet<VitroHttpService>());

	// ----------------------------------------------------------------------
	// Factory lifecycle
	// ----------------------------------------------------------------------

	/**
	 * When created, register this as the HttpRequestHandler.
	 */
	public VitroHttpServiceFactory(ServletContext servletContext) {
		this.servletContext = servletContext;
		HttpServiceFilter.setHttpRequestHandler(this);
	}

	/**
	 * When the system shuts down, remove this as the HttpRequestHandler. Shut
	 * down each remaining service and complain that the client bundle hadn't
	 * released it.
	 */
	public void shutdown() {
		HttpServiceFilter.setHttpRequestHandler(null);
		synchronized (serviceInstances) {
			for (VitroHttpService service : serviceInstances) {
				log.warn("Service is still active at shutdown: " + service);
				service.shutdown();
			}
		}
	}

	// ----------------------------------------------------------------------
	// Service lifecycle
	// ----------------------------------------------------------------------

	/**
	 * The first time a bundle requests the service, create a service instance
	 * and remember it.
	 */
	@Override
	public VitroHttpService getService(Bundle bundle,
			ServiceRegistration<VitroHttpService> registration) {
		HttpContext defaultHttpContext = new DefaultHttpContext(bundle);
		VitroHttpService service = new VitroHttpService(bundle, servletContext,
				defaultHttpContext);
		log.debug("Created a service: " + service);
		serviceInstances.add(service);
		return service;
	}

	/**
	 * When a client bundle stops, or releases the service, shut down the
	 * service and forget about it.
	 */
	@Override
	public void ungetService(Bundle bundle,
			ServiceRegistration<VitroHttpService> registration,
			VitroHttpService service) {
		serviceInstances.remove(service);
		service.shutdown();
		log.debug("Disposed of a service: " + service);
	}

	// ----------------------------------------------------------------------
	// Registry lifecycle
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Request servicing
	// ----------------------------------------------------------------------

	@Override
	public boolean serviceRequest(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		synchronized (serviceInstances) {
			for (VitroHttpService service : serviceInstances) {
				if (service.serviceRequest(req, resp)) {
					return true;
				}
			}
		}
		return false;
	}

}
