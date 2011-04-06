/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/**
 * A base class for requested actions that involve manipulating an object
 * property.
 */
public abstract class AbstractObjectPropertyAction extends RequestedAction {
	public final String uriOfSubject;
	public final String uriOfPredicate;
	public final String uriOfObject;

	public AbstractObjectPropertyAction(String uriOfSubject, String uriOfPredicate,
			String uriOfObject) {
		this.uriOfSubject = uriOfSubject;
		this.uriOfPredicate = uriOfPredicate;
		this.uriOfObject = uriOfObject;
	}

	public String getUriOfSubject() {
		return uriOfSubject;
	}

	public String getUriOfPredicate() {
		return uriOfPredicate;
	}

	public String getUriOfObject() {
		return uriOfObject;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": <" + uriOfSubject + "> <"
				+ uriOfPredicate + "> <" + uriOfObject + ">";
	}
}
