/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.permissions;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/**
 * Interface that describes a unit of authorization, or permission to perform
 * requested actions.
 */
public interface Permission {
	/**
	 * Get the URI that identifies this Permission object.
	 */
	String getUri();

	/**
	 * Convenience method to get the localName portion of the URI.
	 */
	String getLocalName();

	/**
	 * Convenience method to get the namespace portion of the URI.
	 */
	String getNamespace();

	/**
	 * Is a user with this Permission authorized to perform this
	 * RequestedAction?
	 */
	boolean isAuthorized(RequestedAction whatToAuth);

	/**
	 * An implementation of Permission that authorizes nothing.
	 */
	static Permission NOT_AUTHORIZED = new Permission() {

		@Override
		public String getUri() {
			return "java://" + Permission.class.getName() + "#NOT_AUTHORIZED";
		}

		@Override
		public String getLocalName() {
			return "NOT_AUTHORIZED";
		}

		@Override
		public String getNamespace() {
			return "java://" + Permission.class.getName();
		}

		@Override
		public boolean isAuthorized(RequestedAction whatToAuth) {
			return false;
		}

	};
}
