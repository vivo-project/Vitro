/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.logservice;

import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * Use the provided logname to create an Apache Commons Log with a BaseLogger
 * wrapped around it.
 * 
 * Delegate all logging calls to that BaseLogger.
 */
public class VitroLogService implements LogService {
	private final String logname;
	private final BaseLogger baseLogger;

	public VitroLogService(String logname) {
		this.logname = logname;
		this.baseLogger = new BaseLogger(LogFactory.getLog(logname));
	}

	public Object getLogname() {
		return this.logname;
	}

	@Override
	public void log(int level, String message) {
		baseLogger.doLog(null, null, level, message, null);
	}

	@Override
	public void log(int level, String message, Throwable exception) {
		baseLogger.doLog(null, null, level, message, exception);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void log(ServiceReference sr, int level, String message) {
		baseLogger.doLog(null, sr, level, message, null);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void log(ServiceReference sr, int level, String message,
			Throwable exception) {
		baseLogger.doLog(null, sr, level, message, exception);
	}

}
