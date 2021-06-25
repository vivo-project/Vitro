/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.publish;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractDataPropertyStatementAction;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;

import javax.servlet.http.HttpServletRequest;

/**
 * Should we publish this DataPropertyStatement in a Linked Open Data request
 * from the current user?
 */
public class PublishDataPropertyStatement extends
		AbstractDataPropertyStatementAction {
	public PublishDataPropertyStatement(HttpServletRequest request, OntModel ontModel, String subjectUri,
			String predicateUri, String dataValue) {
		super(request, ontModel, subjectUri, predicateUri, dataValue);
	}

	public PublishDataPropertyStatement(HttpServletRequest request, OntModel ontModel,
										DataPropertyStatement dps) {
		super(request, ontModel, dps);
	}

}
