/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/**
 * A base class for requested actions that involve manipulating an object
 * property.
 */
public abstract class AbstractObjectPropertyStatementAction extends RequestedAction {
	private final String subjectUri;
	private final String predicateUri;
	private final String objectUri;

	public AbstractObjectPropertyStatementAction(String subjectUri, String predicateUri,
			String objectUri) {
		this.subjectUri = subjectUri;
		this.predicateUri = predicateUri;
		this.objectUri = objectUri;
	}

	public String getSubjectUri() {
		return subjectUri;
	}

	public String getPredicateUri() {
		return predicateUri;
	}

	public String getObjectUri() {
		return objectUri;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": <" + subjectUri + "> <"
				+ predicateUri + "> <" + objectUri + ">";
	}
}
