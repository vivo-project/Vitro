/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

/**
 * Get access to a Vitro module that is implemented as an OSGi service.
 * 
 * Use isModuleAvailable() to determine whether a particular module has an
 * available implementation. If getModuleReference() is called and no
 * implementation is available, it will throw an exception.
 * 
 * When you have finished using the module, call
 * VitroModuleReference.releaseModule(). That preserves the ability of the
 * system to refresh and update bundles as desired.
 * 
 * The structure of this class differs from that of OsgiServicePublisher largely
 * because the OsgiServicePublisher.Registration is completely opaque, while the
 * OsgiModuleAccessor.VitroModuleReference is not, and requires a type
 * parameter.
 * 
 * TODO Create a proxy service to use in place of this.
 */
public class OsgiModuleAccessor {
	/*
	 * TODO
	 * 
	 * Create a shutdown that gives a limited number of complaints for
	 * unreleased services.
	 */
	private static final Log log = LogFactory.getLog(OsgiModuleAccessor.class);

	// ----------------------------------------------------------------------
	// Static methods
	// ----------------------------------------------------------------------

	/**
	 * Get the module accessor from the context and register a service.
	 * OsgiFramework acts as a facade.
	 */
	/**
	 * Convenience method.
	 */
	public static <T> VitroModuleReference<T> getModuleReference(Object caller,
			ServletContext ctx, Class<T> clazz) {
		OsgiFramework osgi = OsgiFramework.getFramework(ctx);
		OsgiModuleAccessor moduleAccessor = osgi.getModuleAccessor();
		return moduleAccessor.getModuleReference(caller, clazz);
	}

	// ----------------------------------------------------------------------
	// The accessor
	// ----------------------------------------------------------------------

	/**
	 * This synchronized Set should insure that we are thread-safe, assuming
	 * that bundleContext.getService() and bundleContext.ungetService are also
	 * thread-safe.
	 */
	private final Set<VitroModuleReference<?>> activeReferences = Collections
			.synchronizedSet(new HashSet<VitroModuleReference<?>>());

	/** The framework must set this, or we can't work. */
	private BundleContext bundleContext;

	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	/** What modules have active references? */
	public Set<VitroModuleReference<?>> getActiveReferences() {
		HashSet<VitroModuleReference<?>> result = new HashSet<VitroModuleReference<?>>();
		synchronized (activeReferences) {
			result.addAll(activeReferences);
		}
		return result;
	}

	/** Tidy up. Remove any remaining references, with complaints. */
	public void shutdown() {
		// Iterate over a copy, to avoid ConcurrentModificationException
		for (VitroModuleReference<?> r : getActiveReferences()) {
			log.warn("Module reference was not released: " + r);
			destroyReference(r);
		}
	}

	public List<String> listAvailableServices() {
		if (bundleContext == null) {
			log.warn("asking about available services, but bundleContext "
					+ "has not been set");
			return Collections.emptyList();
		}

		try {
			List<String> list = new ArrayList<String>();
			ServiceReference<?>[] allSrs = bundleContext
					.getAllServiceReferences(null, null);
			for (ServiceReference<?> sr : allSrs) {
				Object serviceClasses = sr.getProperty(Constants.OBJECTCLASS);
				if (serviceClasses instanceof String[]) {
					for (String sc : (String[]) serviceClasses) {
						list.add(sc);
					}
				}
			}
			return list;
		} catch (Exception e) {
			log.warn("asking about available services, but threw an exception",
					e);
			return Collections.emptyList();
		}
	}

	public boolean isModuleAvailable(Class<?> clazz) {
		if (bundleContext == null) {
			log.warn("asking about module: " + clazz
					+ ", but bundleContext has not been set");
			return false;
		}
		if (clazz == null) {
			throw new NullPointerException("clazz may not be null.");
		}

		return (null != bundleContext.getServiceReference(clazz));
	}

	public <T> VitroModuleReference<T> getModuleReference(Object requester,
			Class<T> moduleInterface) {
		if (requester == null) {
			throw new NullPointerException("requester may not be null.");
		}
		if (moduleInterface == null) {
			throw new NullPointerException("moduleInterface may not be null.");
		}

		return createReference(requester, moduleInterface);
	}

	private <T> VitroModuleReference<T> createReference(Object requester,
			Class<T> moduleInterface) {
		if (bundleContext == null) {
			throw new IllegalStateException("Requesting module: "
					+ moduleInterface + ", but bundleContext has not been set");
		}

		ServiceReference<T> sr = bundleContext
				.getServiceReference(moduleInterface);
		if (sr == null) {
			throw new IllegalStateException(
					"No available implementation for module: "
							+ moduleInterface);
		}

		VitroModuleReference<T> vmr = new VitroModuleReference<T>(requester,
				moduleInterface, sr);
		activeReferences.add(vmr);
		return vmr;
	}

	private void destroyReference(VitroModuleReference<?> reference) {
		if (!activeReferences.contains(reference)) {
			log.warn("Module reference has already been released: " + reference);
			return;
		}
		if (bundleContext == null) {
			log.error("BundleContext has been reset. Can't release module reference: "
					+ reference);
			return;
		}
		bundleContext.ungetService(reference.getServiceReference());
	}

	// ----------------------------------------------------------------------
	// Helper class
	// ----------------------------------------------------------------------

	/**
	 * A token that keeps track of the requested module. It allows you to access
	 * the actual module, and to release the module when finished.
	 */
	public class VitroModuleReference<T> {
		private final Object requester;
		private final Class<T> moduleInterface;
		private final ServiceReference<T> serviceReference;

		public VitroModuleReference(Object requester, Class<T> moduleInterface,
				ServiceReference<T> serviceReference) {
			this.requester = requester;
			this.moduleInterface = moduleInterface;
			this.serviceReference = serviceReference;
		}

		public T getModule() {
			if (!activeReferences.contains(this)) {
				throw new IllegalStateException(
						"The module reference has already been released.");
			}
			if (bundleContext == null) {
				throw new IllegalStateException("Requesting module: "
						+ moduleInterface
						+ ", but bundleContext has been reset.");
			}
			return bundleContext.getService(serviceReference);
		}

		public Class<T> getModuleInterface() {
			return moduleInterface;
		}

		private ServiceReference<T> getServiceReference() {
			return this.serviceReference;
		}

		public void releaseModule() {
			destroyReference(this);
		}

		@Override
		public String toString() {
			return "VitroModuleReference[" + moduleInterface
					+ ", requested by " + requester + "]";
		}
	}

}
