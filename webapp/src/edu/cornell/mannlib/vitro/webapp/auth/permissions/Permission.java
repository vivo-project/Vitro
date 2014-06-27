/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.permissions;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;

/**
 * Interface that describes a unit of authorization, or permission to perform
 * requested actions.
 */
public abstract class Permission implements Comparable<Permission> {
	protected final String uri;

	protected Permission(String uri) {
		if (uri == null) {
			throw new NullPointerException("uri may not be null.");
		}
		this.uri = uri;
	}

	/**
	 * Get the URI that identifies this Permission object.
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Is a user with this Permission authorized to perform this
	 * RequestedAction?
	 */
	public abstract boolean isAuthorized(RequestedAction whatToAuth);

	@Override
	public int compareTo(Permission that) {
		return this.uri.compareTo(that.uri);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!obj.getClass().equals(this.getClass())) {
			return false;
		}
		Permission that = (Permission) obj;
		return this.uri.equals(that.uri);
	}

	@Override
	public int hashCode() {
		return uri.hashCode();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "['" + uri + "']";
	}

	/**
	 * A concrete Permission instance that authorizes nothing.
	 */
	static Permission NOT_AUTHORIZED = new Permission("java:"
			+ Permission.class.getName() + "#NOT_AUTHORIZED") {

		@Override
		public boolean isAuthorized(RequestedAction whatToAuth) {
			return false;
		}

	};
}
