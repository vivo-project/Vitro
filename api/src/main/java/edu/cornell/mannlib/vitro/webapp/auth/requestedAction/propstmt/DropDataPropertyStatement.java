/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;

/**
 * Should we allow the user to delete this DataPropertyStatement from this
 * model?
 */
public class DropDataPropertyStatement extends
		AbstractDataPropertyStatementAction {

	public DropDataPropertyStatement(OntModel ontModel, String subjectUri,
			String predicateUri, String dataValue) {
		super(ontModel, subjectUri, predicateUri, dataValue);
	}

	public DropDataPropertyStatement(OntModel ontModel,
			DataPropertyStatement dps) {
		super(ontModel, dps);
	}
}
