/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.modules.Application;

/**
 * Tools for working with the current Application instance.
 */
public class ApplicationUtils {
	private static final Log log = LogFactory.getLog(ApplicationUtils.class);

	private static volatile Application instance;

	public static Application instance() {
		try {
			instance.getClass();
			return instance;
		} catch (NullPointerException e) {
			log.error("Called for Application before it was available", e);
			throw new IllegalStateException(
					"Called for Application before it was available", e);
		}
	}

	static void setInstance(Application application) {
		instance = application;
	}
}
