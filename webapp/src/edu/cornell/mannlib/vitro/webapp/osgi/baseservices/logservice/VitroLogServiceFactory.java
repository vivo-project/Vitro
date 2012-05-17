/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.logservice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import edu.cornell.mannlib.vitro.webapp.osgi.framework.OsgiFramework;

/**
 * A factory implementation of the OSGi LogService.
 * 
 * Creates an instance of VitroLogService for each bundle that wants one, and
 * disposes of the service when it is no longer used.
 * 
 * A LogService instance can also be created by supplying the name that would
 * have been derived from the bundle. This allows us to pre-create an instance
 * that we know will be requested, and manipulate it. In particular, we can
 * create the instance which the FileInstall bundle will request and then query
 * its logging level.
 */
public class VitroLogServiceFactory implements ServiceFactory<VitroLogService> {
	private static final Log serviceFactoryLog = LogFactory
			.getLog(VitroLogServiceFactory.class);

	/** Record the active service instances, by logname. */
	private final ConcurrentMap<String, VitroLogService> serviceInstances = new ConcurrentHashMap<String, VitroLogService>();

	// ----------------------------------------------------------------------
	// Service lifecycle
	// ----------------------------------------------------------------------

	/**
	 * The first time a bundle requests the service, create a service instance
	 * and remember it.
	 * 
	 * The Apache Commons Log name will be the same as OsgiFramework, plus a
	 * suffix that is the last segment of the requesting bundle's symbolic name.
	 * 
	 * So for example, if the requesting bundle is named "foo.bar.fizzbuzz",
	 * then the log will appear as:
	 * "edu.cornell.mannlib.vitro.webapp.osgi.framework.OsgiFramework_fizzbuzz"
	 */
	@Override
	public VitroLogService getService(Bundle bundle,
			ServiceRegistration<VitroLogService> registration) {
		String name = bundle.getSymbolicName();
		if (name == null) {
			name = "bundle_" + String.valueOf(bundle.getBundleId());
		}
		String lognameSuffix = name.substring(name.lastIndexOf('.') + 1);
		return getService(lognameSuffix);
	}

	/**
	 * Create a service instance based on the suffix that is derived from the
	 * requesting bundle.
	 */
	public VitroLogService getService(String lognameSuffix) {
		String logname = getLognameForSuffix(lognameSuffix);
		VitroLogService service = new VitroLogService(logname);
		VitroLogService existingService = serviceInstances.putIfAbsent(logname,
				service);

		if (existingService == null) {
			serviceFactoryLog.debug("Creating new LogService for '" + logname
					+ "'");
			return service;
		} else {
			serviceFactoryLog.debug("LogService already existed for '"
					+ logname + "'");
			return existingService;
		}
	}

	/**
	 * When a client bundle stops, or releases the service, shut down the
	 * service and forget about it.
	 */
	@Override
	public void ungetService(Bundle bundle,
			ServiceRegistration<VitroLogService> registration,
			VitroLogService service) {
		Object logname = service.getLogname();
		serviceInstances.remove(logname);
		serviceFactoryLog.debug("Disposing of a LogService for " + logname);
	}

	/**
	 * A public utility method: If I have a bundle with the following suffix,
	 * what name will we used to fetch the logger?
	 */
	public static String getLognameForSuffix(String lognameSuffix) {
		if (StringUtils.isEmpty(lognameSuffix)) {
			return OsgiFramework.class.getName();
		} else {
			return OsgiFramework.class.getName() + "_" + lognameSuffix;
		}
	}

	/**
	 * A public utility method: What is OSGi logging level for this Apache
	 * Commons Log instance, represented as a String?
	 */
	public static String getOsgiLogLevelString(Log log) {
		return String.valueOf(getOsgiLogLevel(log));
	}

	/**
	 * A public utility method: What is OSGi logging level for this Apache
	 * Commons Log instance?
	 */
	public static int getOsgiLogLevel(Log log) {
		if (log.isDebugEnabled()) {
			return 4;
		}
		if (log.isInfoEnabled()) {
			return 3;
		}
		if (log.isWarnEnabled()) {
			return 2;
		}
		if (log.isErrorEnabled()) {
			return 1;
		}
		return 0;
	}
}
