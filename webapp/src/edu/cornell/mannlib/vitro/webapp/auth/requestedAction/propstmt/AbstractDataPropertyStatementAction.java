/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;

/**
 * A base class for requested actions that involve adding, editing, or dropping
 * data property statements from a model.
 */
public abstract class AbstractDataPropertyStatementAction extends
		RequestedAction {
	private final String subjectUri;
	private final String predicateUri;

	public AbstractDataPropertyStatementAction(String subjectUri,
			String predicateUri) {
		this.subjectUri = subjectUri;
		this.predicateUri = predicateUri;
	}

	public AbstractDataPropertyStatementAction(DataPropertyStatement dps) {
		this.subjectUri = (dps.getIndividual() == null) ? dps
				.getIndividualURI() : dps.getIndividual().getURI();
		this.predicateUri = dps.getDatapropURI();
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
