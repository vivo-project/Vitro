/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.publish;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractObjectPropertyStatementAction;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Property;

/**
 * Should we publish this ObjectPropertyStatement in a Linked Open Data request
 * from the current user?
 */

public class PublishObjectPropertyStatement extends
		AbstractObjectPropertyStatementAction {
	public PublishObjectPropertyStatement(OntModel ontModel, String subjectUri,
			Property keywordPred, String objectUri) {
		super(ontModel, subjectUri, keywordPred, objectUri);
	}

	public PublishObjectPropertyStatement(OntModel ontModel,
			ObjectPropertyStatement ops) {
		super(ontModel, ops);
	}

}
