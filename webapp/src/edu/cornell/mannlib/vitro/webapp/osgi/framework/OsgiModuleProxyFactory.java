/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.framework;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import edu.cornell.mannlib.vitro.webapp.modules.interfaces.FileStorage;

/**
 * Create and manage proxy objects that provide access to the Vitro modules
 * implemented as services.
 */
public class OsgiModuleProxyFactory {
	private static final Log log = LogFactory
			.getLog(OsgiModuleProxyFactory.class);

	/**
	 * What proxies have been obtained and not released, and who asked for them?
	 */
	private final ConcurrentMap<BaseOsgiModuleProxy<?>, Object> activeProxies = new ConcurrentHashMap<BaseOsgiModuleProxy<?>, Object>();

	private BundleContext bundleContext;

	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	@SuppressWarnings("unchecked")
	public <T> T getProxyForModule(Object requester, Class<T> interfaceClass) {
		if (FileStorage.class.equals(interfaceClass)) {
			BaseOsgiModuleProxy<FileStorage> proxy = new FileStorageOsgiModuleProxy();
			log.debug("get a FileStorage proxy: " + proxy);
			activeProxies.put(proxy, requester);
			return (T) proxy;
		} else {
			throw new OsgiProxyException(
					"Unrecognized class for module interface: "
							+ interfaceClass);
		}
	}

	public <T> void releaseProxyForModule(T proxy) {
		if (!activeProxies.containsKey(proxy)) {
			log.warn("Attempt to release proxy that is not active: " + proxy);
		}
		activeProxies.remove(proxy);
	}

	/**
	 * All proxies should have been released by the time this is called.
	 */
	public void shutdown() {
		for (BaseOsgiModuleProxy<?> proxy : activeProxies.keySet()) {
			Object requester = activeProxies.get(proxy);
			log.warn("Proxy for '" + proxy.getShortClassName()
					+ "' has not been released; requested by '" + requester
					+ "'");
		}
		activeProxies.clear();
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	public static class OsgiProxyException extends RuntimeException {
		public OsgiProxyException() {
			super();
		}

		public OsgiProxyException(String message, Throwable cause) {
			super(message, cause);
		}

		public OsgiProxyException(String message) {
			super(message);
		}

		public OsgiProxyException(Throwable cause) {
			super(cause);
		}
	}

	/**
	 * The important aspect of the proxy is that it doesn't hold on to the
	 * service. So the service is free to go away, or be replaced.
	 */
	private class BaseOsgiModuleProxy<T> {
		private final Class<T> interfaceClass;

		protected BaseOsgiModuleProxy(Class<T> interfaceClass) {
			this.interfaceClass = interfaceClass;
		}

		protected ServiceReference<T> getServiceReference() {
			if (!activeProxies.containsKey(this)) {
				throw new OsgiProxyException("This proxy is no longer active.");
			}
			if (bundleContext == null) {
				throw new OsgiProxyException("The BundleContext is not set.");
			}

			ServiceReference<T> sr = bundleContext
					.getServiceReference(interfaceClass);
			if (sr == null) {
				throw new OsgiProxyException(
						"There is no registered service for "
								+ FileStorage.class);
			}

			return sr;
		}

		protected T getService(ServiceReference<T> sr) {
			T service = bundleContext.getService(sr);
			if (service == null) {
				throw new OsgiProxyException(
						"Service from reference was null: " + sr);
			}
			return service;
		}

		public String getShortClassName() {
			return interfaceClass.getSimpleName();
		}

	}

	private class FileStorageOsgiModuleProxy extends
			BaseOsgiModuleProxy<FileStorage> implements FileStorage {

		protected FileStorageOsgiModuleProxy() {
			super(FileStorage.class);
		}

		@Override
		public void createFile(String id, String filename, InputStream bytes)
				throws FileAlreadyExistsException, IOException {
			ServiceReference<FileStorage> sr = getServiceReference();
			FileStorage fs = getService(sr);
			try {
				fs.createFile(id, filename, bytes);
			} finally {
				bundleContext.ungetService(sr);
			}
		}

		@Override
		public String getFilename(String id) throws IOException {
			ServiceReference<FileStorage> sr = getServiceReference();
			FileStorage fs = getService(sr);
			try {
				return fs.getFilename(id);
			} finally {
				bundleContext.ungetService(sr);
			}
		}

		@Override
		public InputStream getInputStream(String id, String filename)
				throws FileNotFoundException, IOException {
			ServiceReference<FileStorage> sr = getServiceReference();
			FileStorage fs = getService(sr);
			try {
				return fs.getInputStream(id, filename);
			} finally {
				bundleContext.ungetService(sr);
			}
		}

		@Override
		public boolean deleteFile(String id) throws IOException {
			ServiceReference<FileStorage> sr = getServiceReference();
			FileStorage fs = getService(sr);
			try {
				return fs.deleteFile(id);
			} finally {
				bundleContext.ungetService(sr);
			}
		}

	}
}
