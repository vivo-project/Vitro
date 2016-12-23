/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;

/**
 * Should we allow the user to add this DataPropertyStatement to this model?
 */
public class AddDataPropertyStatement extends
		AbstractDataPropertyStatementAction {

	public AddDataPropertyStatement(OntModel ontModel, String subjectUri,
			String predicateUri, String dataValue) {
		super(ontModel, subjectUri, predicateUri, dataValue);
	}

	public AddDataPropertyStatement(OntModel ontModel, DataPropertyStatement dps) {
		super(ontModel, dps);
	}

}
