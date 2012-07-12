/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/**
 * A RequestedAction that can be recognized by a SimplePermission.
 */
public class SimpleRequestedAction extends RequestedAction {
	private final String localName;

	public SimpleRequestedAction(String localName) {
		if (localName == null) {
			throw new NullPointerException("localName may not be null.");
		}

		this.localName = localName;
	}

	@Override
	public String getURI() {
		return "java:" + this.getClass().getName() + "#" + localName;
	}

	@Override
	public int hashCode() {
		return (localName == null) ? 0 : localName.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof SimpleRequestedAction) {
			SimpleRequestedAction that = (SimpleRequestedAction) o;
			return equivalent(this.localName, that.localName);
		}
		return false;
	}
	
	private boolean equivalent(Object o1, Object o2) {
		return (o1 == null) ? (o2 == null) : o1.equals(o2);
	}

	@Override
	public String toString() {
		return "SimpleRequestedAction['" + localName + "']";
	}

}
