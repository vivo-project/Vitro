/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.logservice;

import org.apache.commons.logging.Log;
import org.apache.felix.framework.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

/**
 * The Felix framework expects a subclass of the Felix Logger, so here is one
 * that uses a BaseLogger wrapped around the Apache Commons Log that is assigned
 * to the OsgiFramework.
 * 
 * Messages will appear to come from the OSGi Framework.
 */
public class OsgiFrameworkLogger extends Logger {
	private final Log log;
	private final BaseLogger baseLogger;

	/**
	 * Since we use the Framework's log instance, the output appears to come
	 * from the Framework and uses the Framework's log level.
	 */
	public OsgiFrameworkLogger(Log log) {
		this.log = log;
		this.baseLogger = new BaseLogger(log);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void doLog(Bundle bundle, ServiceReference sr, int level,
			String msg, Throwable throwable) {
		baseLogger.doLog(bundle, sr, level, msg, throwable);
	}

	public String osgiLogLevelString() {
		return VitroLogServiceFactory.getOsgiLogLevelString(log);
	}

}
