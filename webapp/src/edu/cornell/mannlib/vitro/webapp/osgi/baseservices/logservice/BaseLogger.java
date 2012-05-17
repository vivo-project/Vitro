/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.logservice;

import org.apache.commons.logging.Log;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * Convert the OSGi logging calls to ApacheCommons logging calls.
 */
public class BaseLogger {
	private final Log log;

	public BaseLogger(Log log) {
		this.log = log;
	}

	@SuppressWarnings("rawtypes")
	protected void doLog(Bundle bundle, ServiceReference sr, int level,
			String msg, Throwable throwable) {
		String s = "";

		if (sr != null) {
			s = s + "SvcRef " + sr + ", ";
		}
		if (bundle != null) {
			s = s + "Bundle " + bundle + ", ";
		}

		s = s + msg;

		switch (level) {
		case LogService.LOG_ERROR:
			if (throwable == null) {
				log.error(s);
			} else {
				log.error(s, lookInside(throwable));
			}
			break;
		case LogService.LOG_INFO:
			if (throwable == null) {
				log.info(s);
			} else {
				log.info(s, lookInside(throwable));
			}
			break;
		case LogService.LOG_WARNING:
			if (throwable == null) {
				log.warn(s);
			} else {
				log.warn(s, lookInside(throwable));
			}
			break;
		default: // LogService.DEBUG
			if (throwable == null) {
				log.debug(s);
			} else {
				log.debug(s, lookInside(throwable));
			}
			break;
		}
	}

	/**
	 * If the exception is a BundleException wrapped around something else, then
	 * that something is what we really want to report.
	 */
	private Throwable lookInside(Throwable t) {
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

	/**
	 * Convert the log level of our Apache Commons Log to the Integer level that
	 * OSGi recognizes.
	 */
	public int getOsgiLogLevel() {
		return VitroLogServiceFactory.getOsgiLogLevel(log);
	}

}
