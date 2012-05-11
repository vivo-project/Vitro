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

	private final ServletContext servletContext;

	private final ConcurrentMap<Long, VitroHttpService> serviceInstances = new ConcurrentHashMap<Long, VitroHttpService>();

	public VitroHttpServiceFactory(ServletContext servletContext) {
		this.servletContext = servletContext;
		HttpServiceFilter.setHttpRequestHandler(this);
	}

	@Override
	public VitroHttpService getService(Bundle bundle,
			ServiceRegistration<VitroHttpService> registration) {
		log.debug("Creating an HttpService instance for bundle "
				+ bundle.getSymbolicName());
		HttpContext defaultHttpContext = new DefaultHttpContext(bundle);
		VitroHttpService service = new VitroHttpService(servletContext,
				defaultHttpContext);
		serviceInstances.put(bundle.getBundleId(), service);
		return service;
	}

	@Override
	public void ungetService(Bundle bundle,
			ServiceRegistration<VitroHttpService> registration,
			VitroHttpService service) {
		Long bundleId = bundle.getBundleId();
		if (!serviceInstances.containsKey(bundleId)) {
			log.warn("Ungetting a service that is not active. Bundle is "
					+ bundle.getSymbolicName());
			return;
		}

		VitroHttpService storedService = serviceInstances.get(bundleId);
		if (!storedService.equals(service)) {
			log.warn("Bundle is trying to unget a service that it didn't "
					+ "get initially. Bundle is " + bundle.getSymbolicName());
			return;
		}

		log.debug("Disposing of the HttpService instance for bundle "
				+ bundle.getSymbolicName());
		serviceInstances.remove(bundleId);
		service.shutdown();
	}

	@Override
	public boolean serviceRequest(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		synchronized (serviceInstances) {
			for (VitroHttpService service : serviceInstances.values()) {
				if (service.serviceRequest(req, resp)) {
					return true;
				}
			}
		}
		return false;
	}

	public void shutdown() {
		synchronized (serviceInstances) {
			for (Long bundleId : serviceInstances.keySet()) {
				log.warn("Service is still active at shutdown for bundle"
						+ bundleId);
				VitroHttpService service = serviceInstances.get(bundleId);
				service.shutdown();
			}
		}
	}

}
