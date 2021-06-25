/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.beans.Property;

import javax.servlet.http.HttpServletRequest;

/**
 * Should we allow the user to delete this ObjectPropertyStatement from this
 * model?
 */
public class DropObjectPropertyStatement extends
		AbstractObjectPropertyStatementAction {
	public DropObjectPropertyStatement(HttpServletRequest request, OntModel ontModel, String sub,
									   Property pred, String obj) {
		super(request, ontModel, sub, pred, obj);
	}
}
