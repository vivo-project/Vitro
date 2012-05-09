/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices;

import org.apache.commons.logging.Log;
import org.apache.felix.framework.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;

/**
 * An OSGi LogService implementation that will send messages to the Vitro log
 * file. Messages will appear to come from the OSGi Framework.
 * 
 * This is a subclass the Felix Logger, because that's what the Felix framework
 * expects at startup.
 * 
 * This is also an implementation of the OSGi LogService, and will be registered
 * as such, so 3rd-party bundles can use it.
 * 
 * TODO Perhaps we should break this into two classes - one for the framework
 * itself and one as a factory for the bundles.
 */
public class OsgiFrameworkLogger extends Logger implements LogService {
	private final Log log;
	private final Activator activator;

	/**
	 * Notice that we use the Framework's log instance, so the output appears to
	 * come from the Framework and uses the Framework's log level.
	 */
	public OsgiFrameworkLogger(Log log) {
		this.log = log;
		this.activator = new Activator(this, log);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void doLog(Bundle bundle, ServiceReference sr, int level,
			String msg, Throwable throwable) {
		String s = "";

		if (sr != null) {
			s = s + "SvcRef " + sr + " ";
		} else if (bundle != null) {
			s = s + "Bundle " + bundle.toString() + " ";
		}

		s = s + msg;
		if (throwable != null) {
			s = s + " (" + throwable + ")";
		}

		switch (level) {
		case LogService.LOG_ERROR:
			if (throwable == null) {
				log.error(s);
			} else {
				log.error(s, resolve(throwable));
			}
			break;
		case LogService.LOG_INFO:
			if (throwable == null) {
				log.info(s);
			} else {
				log.info(s, resolve(throwable));
			}
			break;
		case LogService.LOG_WARNING:
			if (throwable == null) {
				log.warn(s);
			} else {
				log.warn(s, resolve(throwable));
			}
			break;
		default: // LogService.DEBUG
			if (throwable == null) {
				log.debug(s);
			} else {
				log.debug(s, resolve(throwable));
			}
			break;
		}
	}

	private Throwable resolve(Throwable t) {
		if (!(t instanceof BundleException)) {
			return t;
		}

		BundleException be = (BundleException) t;
		Throwable nested = be.getNestedException();
		if (nested != null) {
			return nested;
		} else {
			return be;
		}
	}

	public int osgiLogLevel() {
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

	public String osgiLogLevelString() {
		return String.valueOf(osgiLogLevel());
	}

	/**
	 * Create a BundleActivator to be executed when the system bundle starts.
	 */
	public Activator getActivator() {
		return this.activator;
	}

	private static class Activator implements BundleActivator {
		private final OsgiFrameworkLogger logger;
		private final Log commonsLog;
		private ServiceRegistration<?> sr;

		public Activator(OsgiFrameworkLogger logger, Log commonsLog) {
			this.logger = logger;
			this.commonsLog = commonsLog;
		}

		@Override
		public void start(BundleContext bundleContext) throws Exception {
			commonsLog.debug("Register the LogService");
			sr = bundleContext.registerService(LogService.class.getName(),
					logger, null);
		}

		@Override
		public void stop(BundleContext bundleContext) throws Exception {
			commonsLog.debug("Unregister the LogService");
			sr.unregister();
		}

	}

}
