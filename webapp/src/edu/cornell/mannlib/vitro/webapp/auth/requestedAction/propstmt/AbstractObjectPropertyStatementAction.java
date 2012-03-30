/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;

/**
 * A base class for requested actions that involve adding, editing, or deleting
 * object property statements from a model.
 */
public abstract class AbstractObjectPropertyStatementAction extends
		AbstractPropertyStatementAction {
	private final String subjectUri;
	private final String predicateUri;
	private final String objectUri;

	public AbstractObjectPropertyStatementAction(String subjectUri,
			String predicateUri, String objectUri) {
		this.subjectUri = subjectUri;
		this.predicateUri = predicateUri;
		this.objectUri = objectUri;
	}

	public AbstractObjectPropertyStatementAction(ObjectPropertyStatement ops) {
		this.subjectUri = (ops.getSubject() == null) ? ops.getSubjectURI()
				: ops.getSubject().getURI();
		this.predicateUri = (ops.getProperty() == null) ? ops.getPropertyURI()
				: ops.getProperty().getURI();
		this.objectUri = (ops.getObject() == null) ? ops.getObjectURI() : ops
				.getObject().getURI();
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
