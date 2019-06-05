/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.beans.Property;

import javax.servlet.http.HttpServletRequest;

/**
 * Should we allow the user to edit this ObjectPropertyStatement in this model?
 */
public class EditObjectPropertyStatement extends
		AbstractObjectPropertyStatementAction {
	public EditObjectPropertyStatement(HttpServletRequest request, OntModel ontModel, String subjectUri,
									   Property keywordPred, String objectUri) {
		super(request, ontModel, subjectUri, keywordPred, objectUri);
	}
}
