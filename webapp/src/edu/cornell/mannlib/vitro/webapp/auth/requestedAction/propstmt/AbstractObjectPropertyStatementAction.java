/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/**
 * A base class for requested actions that involve manipulating an object
 * property.
 */
public abstract class AbstractObjectPropertyStatementAction extends RequestedAction {
	private final String uriOfSubject;
	private final String uriOfPredicate;
	private final String uriOfObject;

	public AbstractObjectPropertyStatementAction(String uriOfSubject, String uriOfPredicate,
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
