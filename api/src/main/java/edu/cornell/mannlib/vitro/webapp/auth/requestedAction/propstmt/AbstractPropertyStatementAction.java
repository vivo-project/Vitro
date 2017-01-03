/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.Property;

/**
 * A base class for requested actions that involve adding, editing, or deleting
 * statements from a model.
 */
public abstract class AbstractPropertyStatementAction extends RequestedAction {
	private final OntModel ontModel;

	public AbstractPropertyStatementAction(OntModel ontModel) {
		this.ontModel = ontModel;
	}

	public OntModel getOntModel() {
		return ontModel;
	}

	/**
	 * Get the URI of the Resources that are involved in this statement. Those
	 * are the Subject, and the Object if this is an ObjectProperty request.
	 */
	public abstract String[] getResourceUris();

	public abstract Property getPredicate();
	
	public abstract String getPredicateUri();
}
