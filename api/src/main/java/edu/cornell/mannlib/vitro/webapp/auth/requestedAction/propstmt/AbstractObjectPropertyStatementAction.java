/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.beans.Property;

/**
 * A base class for requested actions that involve adding, editing, or deleting
 * object property statements from a model.
 */
public abstract class AbstractObjectPropertyStatementAction extends
		AbstractPropertyStatementAction {
	private final String subjectUri;
	private final Property predicate;
	private final String objectUri;

	public AbstractObjectPropertyStatementAction(OntModel ontModel,
			String subjectUri, Property predicate, String objectUri) {
		super(ontModel);
		this.subjectUri = subjectUri;
		this.predicate = predicate;
		this.objectUri = objectUri;
	}

	public String getSubjectUri() {
		return subjectUri;
	}

	public String getObjectUri() {
		return objectUri;
	}

	@Override
	public Property getPredicate() {
		return predicate;
	}

	@Override
	public String getPredicateUri() {
		return predicate.getURI();
	}

	@Override
	public String[] getResourceUris() {
		return new String[] { subjectUri, objectUri };
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": <" + subjectUri + "> <"
				+ predicate.getURI() + "> <" + objectUri + ">";
	}
}
