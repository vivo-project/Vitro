/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Property;

/**
 * A base class for requested actions that involve adding, editing, or dropping
 * data property statements from a model.
 */
public abstract class AbstractDataPropertyStatementAction extends
		AbstractPropertyStatementAction {
	private final String subjectUri;
	private final String predicateUri;
	private final Property predicate;
	private final String dataValue;

	public AbstractDataPropertyStatementAction(OntModel ontModel,
			String subjectUri, String predicateUri, String dataValue) {
		super(ontModel);
		this.subjectUri = subjectUri;
		this.predicateUri = predicateUri;
		Property dataProperty = new Property();
		dataProperty.setURI(predicateUri);
		this.predicate = dataProperty;
		this.dataValue = dataValue;
	}

	public AbstractDataPropertyStatementAction(OntModel ontModel,
			DataPropertyStatement dps) {
		super(ontModel);
		this.subjectUri = (dps.getIndividual() == null) ? dps
				.getIndividualURI() : dps.getIndividual().getURI();
		this.predicateUri = dps.getDatapropURI();
	    Property dataProperty = new Property();
	    dataProperty.setURI(predicateUri);
	    this.predicate = dataProperty;
	    this.dataValue = dps.getData();
	}

	public String getSubjectUri() {
		return subjectUri;
	}

	@Override
	public Property getPredicate() {
	    return predicate;
	}
	
	@Override
	public String getPredicateUri() {
		return predicateUri;
	}

	@Override
	public String[] getResourceUris() {
		return new String[] {subjectUri};
	}
	
	public String dataValue() {
		return dataValue;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": <" + subjectUri + "> <"
				+ predicateUri + ">";
	}
}
