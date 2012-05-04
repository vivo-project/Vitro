/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.bundles.sillymodule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Deactivate;

/**
 * The component activator for this module
 */
@aQute.bnd.annotation.component.Component
public class Component {
	private static final Log log = LogFactory.getLog(Component.class);
	
	
	@Activate
	public void startup() {
		log.error("Starting the silly component");
	}

	@Deactivate
	public void shutdown() {
		log.error("Stopping the silly component");
	}
	
}
