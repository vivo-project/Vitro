/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.framework;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Make services in the application base available to the OSGi bundles.
 * 
 * The structure of this class differs from that of OsgiModuleAccessor largely
 * because the OsgiServicePublisher.Registration is completely opaque, while the
 * OsgiModuleAccessor.VitroModuleReference is not, and requires a type
 * parameter.
 * 
 * TODO does this disappear in favor of the BaseServicesActivator?
 */
public class OsgiServicePublisher {
	private static final Log log = LogFactory
			.getLog(OsgiServicePublisher.class);

	// ----------------------------------------------------------------------
	// Static methods
	// ----------------------------------------------------------------------

	/**
	 * Get the service publisher from the context and register a service.
	 * OsgiFramework acts as a facade.
	 */
	public static <T> Registration<T> registerService(
			Class<T> serviceInterface, T serviceInstance,
			Map<String, Object> properties, ServletContext ctx) {
		OsgiFramework osgi = OsgiFramework.getFramework(ctx);
		OsgiServicePublisher servicePublisher = osgi.getServicePublisher();
		return servicePublisher.registerService(serviceInterface,
				serviceInstance, properties);
	}

	// ----------------------------------------------------------------------
	// The publisher
	// ----------------------------------------------------------------------

	/**
	 * This ConcurrentMap should insure that we are thread-safe, assuming that
	 * bundleContext.registerService() is also thread-safe.
	 */
	private final ConcurrentMap<Registration<?>, ServiceRegistration<?>> activeRegistrations = new ConcurrentHashMap<Registration<?>, ServiceRegistration<?>>();

	/** The framework must set this, or we can't work. */
	private BundleContext bundleContext;

	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	/** What services are currently registered? */
	public Set<Registration<?>> getActiveRegistrations() {
		return new HashSet<Registration<?>>(activeRegistrations.keySet());
	}

	/** Tidy up. Remove any remaining registrations, with complaints. */
	public void shutdown() {
		for (Registration<?> r : activeRegistrations.keySet()) {
			log.warn("Registration was not unregistered(): " + r);
			destroyRegistration(r);
		}
	}

	/**
	 * Register a service for use by OSGi bundles. Call
	 * Registration.unregister() to remove the service.
	 */
	public <T> Registration<T> registerService(Class<T> serviceInterface,
			T serviceInstance, Map<String, Object> properties) {
		if (serviceInterface == null) {
			throw new NullPointerException("serviceInterface may not be null.");
		}
		if (serviceInstance == null) {
			throw new NullPointerException("serviceInstance may not be null.");
		}

		return createRegistration(serviceInterface, serviceInstance, properties);
	}

	private <T> Registration<T> createRegistration(Class<T> serviceInterface,
			T serviceInstance, Map<String, Object> properties) {
		if (bundleContext == null) {
			throw new IllegalStateException("Trying to create a registration: "
					+ serviceInterface + ", but bundleContext has not been set");
		}
		ServiceRegistration<T> sr = bundleContext.registerService(
				serviceInterface, serviceInstance, mapToDictionary(properties));
		Registration<T> r = new Registration<T>(serviceInterface,
				serviceInstance, properties);
		activeRegistrations.put(r, sr);
		return r;
	}

	private void destroyRegistration(Registration<?> registration) {
		if (!activeRegistrations.containsKey(registration)) {
			log.warn("Registered service has already been unregistered: "
					+ registration);
			return;
		}

		ServiceRegistration<?> sr = activeRegistrations.get(registration);
		sr.unregister();

		activeRegistrations.remove(registration);
	}

	private Dictionary<String, Object> mapToDictionary(Map<String, Object> map) {
		Dictionary<String, Object> dictionary = new Hashtable<String, Object>();
		if (map != null) {
			for (String key : map.keySet()) {
				if (key != null) {
					Object value = map.get(key);
					if (value != null) {
						dictionary.put(key, value);
					}
				}
			}
		}
		return dictionary;
	}

	// ----------------------------------------------------------------------
	// Helper class
	// ----------------------------------------------------------------------

	/**
	 * An immutable token that keeps track of this registered service. It's
	 * essentially opaque, since it only exists to unregister the service at a
	 * later time.
	 */
	public class Registration<T> {
		private final Class<T> serviceInterface;
		private final T serviceInstance;
		private final Map<String, Object> properties;

		public Registration(Class<T> serviceInterface, T serviceInstance,
				Map<String, Object> properties) {
			this.serviceInterface = serviceInterface;
			this.serviceInstance = serviceInstance;
			this.properties = (properties == null) ? Collections
					.<String, Object> emptyMap() : Collections
					.unmodifiableMap(new HashMap<String, Object>(properties));
		}

		public void unregister() {
			destroyRegistration(this);
		}

		@Override
		public String toString() {
			return "Registration[" + serviceInterface + ", " + serviceInstance
					+ ", properties=" + properties + "]";
		}
	}
}
