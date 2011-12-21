/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.permissions;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/**
 * This is what the PermissionRegistry hands out if you ask for a Permission
 * that it doesn't know about. Nothing is authorized by this Permission.
 */
public class BrokenPermission implements Permission {
	private final String uri;
	private final String localName;
	private final String namespace;

	public BrokenPermission(String uri) {
		this.uri = uri;

		int namespaceBreak = uri.lastIndexOf("#");
		if (namespaceBreak == -1) {
			namespaceBreak = uri.lastIndexOf("/");
		}

		int localNameStart = namespaceBreak + 1;

		this.namespace = uri.substring(0, localNameStart);
		this.localName = uri.substring(localNameStart);
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public String getLocalName() {
		return localName;
	}

	@Override
	public String getNamespace() {
		return namespace;
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
