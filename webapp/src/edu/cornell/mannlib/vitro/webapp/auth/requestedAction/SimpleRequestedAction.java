/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction;


/**
 * A RequestedAction that can be recognized by a SimplePermission.
 */
public class SimpleRequestedAction extends RequestedAction {
	private final String uri;

	public SimpleRequestedAction(String uri) {
		if (uri == null) {
			throw new NullPointerException("uri may not be null.");
		}

		this.uri = uri;
	}

	@Override
	public String getURI() {
		return uri;
	}

	@Override
	public int hashCode() {
		return uri.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof SimpleRequestedAction) {
			SimpleRequestedAction that = (SimpleRequestedAction) o;
			return equivalent(this.uri, that.uri);
		}
		return false;
	}
	
	private boolean equivalent(Object o1, Object o2) {
		return (o1 == null) ? (o2 == null) : o1.equals(o2);
	}

	@Override
	public String toString() {
		return "SimpleRequestedAction['" + uri + "']";
	}

}
