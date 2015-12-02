/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules;

/**
 * A facade for the StartupStatus that already knows who the
 * ServletContextListener is.
 */
public interface ComponentStartupStatus {
	void info(String message);

	void info(String message, Throwable cause);

	void warning(String message);

	void warning(String message, Throwable cause);

	void fatal(String message);

	void fatal(String message, Throwable cause);
}
