/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.beans.Property;

/**
 * Should we allow the user to delete this ObjectPropertyStatement from this
 * model?
 */
public class DropObjectPropertyStatement extends
		AbstractObjectPropertyStatementAction {
	public DropObjectPropertyStatement(OntModel ontModel, String sub,
			Property pred, String obj) {
		super(ontModel, sub, pred, obj);
	}
}
