/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.bundles.sillymodule;

import edu.cornell.mannlib.vitro.webapp.osgi.interfaces.SillyModule;

/**
 * A basic implementation of the SillyModule interface
 */
public class SillyModuleImpl implements SillyModule {

	@Override
	public String saySomething() {
		return "something";
	}

}
