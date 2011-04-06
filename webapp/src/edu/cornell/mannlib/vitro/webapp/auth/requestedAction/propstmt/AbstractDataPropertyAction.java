/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/**
 * A base class for requestion actions that relate to data properties.
 */
public abstract class AbstractDataPropertyAction extends RequestedAction {
	private final String subjectUri;
	private final String predicateUri;

	public AbstractDataPropertyAction(String subjectUri, String predicateUri) {
		this.subjectUri = subjectUri;
		this.predicateUri = predicateUri;
	}

	public String getSubjectUri() {
		return subjectUri;
	}

	public String getPredicateUri() {
		return predicateUri;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": <" + subjectUri + "> <"
				+ predicateUri + ">";
	}
}
