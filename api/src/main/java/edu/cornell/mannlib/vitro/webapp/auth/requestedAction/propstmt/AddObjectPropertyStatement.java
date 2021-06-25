/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.beans.Property;

import javax.servlet.http.HttpServletRequest;

/**
 * Should we allow the user to add this ObjectPropertyStatement to this model?
 */
public class AddObjectPropertyStatement extends
		AbstractObjectPropertyStatementAction {
	public AddObjectPropertyStatement(HttpServletRequest request, OntModel ontModel, String uriOfSub,
									  Property predicate, String uriOfObj) {
		super(request, ontModel, uriOfSub, predicate, uriOfObj);
	}

}
