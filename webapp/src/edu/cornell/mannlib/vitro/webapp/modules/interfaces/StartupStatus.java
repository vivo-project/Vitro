/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.interfaces;


/**
 * Provides a mechanism for Startup items and OSGi bundles to give their status where it will be seen.
 */
public interface StartupStatus {
	 void info(Object initializer, String message);

	 void info(Object initializer, String message, Throwable cause);

	 void warning(Object initializer, String message);

	 void warning(Object initializer, String message, Throwable cause);

	 void fatal(Object initializer, String message);

	 void fatal(Object initializer, String message, Throwable cause);

}
