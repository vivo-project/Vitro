/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.startup;

import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;

/**
 * A temporary wrapper around the StartupStatus, with the ServletContextListener
 * built in.
 */
public class ComponentStartupStatusImpl implements ComponentStartupStatus {
	private final ServletContextListener listener;
	private final StartupStatus ss;

	public ComponentStartupStatusImpl(ServletContextListener listener,
			StartupStatus ss) {
		this.listener = listener;
		this.ss = ss;
	}

	@Override
	public void info(String message) {
		ss.info(listener, message);
	}

	@Override
	public void info(String message, Throwable cause) {
		ss.info(listener, message, cause);
	}

	@Override
	public void warning(String message) {
		ss.warning(listener, message);
	}

	@Override
	public void warning(String message, Throwable cause) {
		ss.warning(listener, message, cause);
	}

	@Override
	public void fatal(String message) {
		ss.fatal(listener, message);
	}

	@Override
	public void fatal(String message, Throwable cause) {
		ss.fatal(listener, message, cause);
	}

}
