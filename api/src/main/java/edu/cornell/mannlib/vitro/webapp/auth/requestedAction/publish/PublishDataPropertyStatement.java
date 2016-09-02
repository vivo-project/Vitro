/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.publish;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractDataPropertyStatementAction;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;

/**
 * Should we publish this DataPropertyStatement in a Linked Open Data request
 * from the current user?
 */
public class PublishDataPropertyStatement extends
		AbstractDataPropertyStatementAction {
	public PublishDataPropertyStatement(OntModel ontModel, String subjectUri,
			String predicateUri, String dataValue) {
		super(ontModel, subjectUri, predicateUri, dataValue);
	}

	public PublishDataPropertyStatement(OntModel ontModel,
			DataPropertyStatement dps) {
		super(ontModel, dps);
	}

}
