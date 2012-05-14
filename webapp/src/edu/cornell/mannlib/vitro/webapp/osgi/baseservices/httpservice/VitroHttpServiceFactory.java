/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.httpservice;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * A factory implementation of the OSGi HttpService.
 * 
 * Creates an instance of VitroHttpService for each bundle that wants one,
 * and disposes of the service when it is no longer used.
 * 
 * If any service instances remain at shutdown, complain.
 */
public class VitroHttpServiceFactory implements
		ServiceFactory<VitroHttpService> {
	private static final Log log = LogFactory
			.getLog(VitroHttpServiceFactory.class);

	/**
	 * The servlet context from Tomcat. This will be partially revealed to the
	 * registered servlets.
	 */
	private final ServletContext servletContext;

	private final HttpRequestHandler httpRequestHandler;

	/** Record the active service instances. */
	private final Set<VitroHttpService> serviceInstances = Collections
			.synchronizedSet(new HashSet<VitroHttpService>());

	// ----------------------------------------------------------------------
	// Factory lifecycle
	// ----------------------------------------------------------------------

	/**
	 * When created, set up an HttpRequestHandler.
	 */
	public VitroHttpServiceFactory(ServletContext servletContext) {
		this.servletContext = servletContext;
		this.httpRequestHandler = new HttpRequestHandler(servletContext);
		HttpServiceFilter.setHttpRequestHandler(this.httpRequestHandler);
	}

	/**
	 * When the system shuts down, remove the HttpRequestHandler and shut it
	 * down. Complain about any registered Servlce or ResourceGroup that had not
	 * already been unregistered.
	 * 
	 * Shut down each remaining service and complain that the client bundle
	 * hadn't released it.
	 */
	public void shutdown() {
		HttpServiceFilter.setHttpRequestHandler(null);
		this.httpRequestHandler.shutdown();

		synchronized (serviceInstances) {
			for (VitroHttpService service : serviceInstances) {
				log.warn("Service is still active at shutdown: " + service);
			}
		}
		serviceInstances.clear();
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
		VitroHttpService service = new VitroHttpService(bundle,
				httpRequestHandler, servletContext);
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
		log.debug("Disposed of a service: " + service);
	}

}
