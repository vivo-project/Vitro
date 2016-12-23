/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.publish;

import static edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction.SOME_URI;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractObjectPropertyStatementAction;
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

	/**
	 * We don't need to know range and domain because publishing never involves
	 * faux properties.
	 */
	public PublishObjectPropertyStatement(OntModel ontModel,
			String subjectUri,
			String predicateUri, String objectUri) {
		this(ontModel, subjectUri, populateProperty(predicateUri), objectUri);
	}

	private static Property populateProperty(String predicateUri) {
		Property prop = new Property(predicateUri);
		prop.setDomainVClassURI(SOME_URI);
		prop.setRangeVClassURI(SOME_URI);
		return prop;
	}
}
