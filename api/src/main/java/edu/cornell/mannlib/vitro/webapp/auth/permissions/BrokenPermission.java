/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.permissions;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;

/**
 * This is what the PermissionRegistry hands out if you ask for a Permission
 * that it doesn't know about. Nothing is authorized by this Permission.
 */
public class BrokenPermission extends Permission {
	public BrokenPermission(String uri) {
		super(uri);
	}

	@Override
	public boolean isAuthorized(RequestedAction whatToAuth) {
		return false;
	}

	@Override
	public String toString() {
		return "BrokenPermission[" + uri + "]";
	}

}
