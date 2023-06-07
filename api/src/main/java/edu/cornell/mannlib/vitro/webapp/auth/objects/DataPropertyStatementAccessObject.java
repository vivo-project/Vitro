/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.objects;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Property;

/**
 * A base class for requested actions that involve adding, editing, or dropping
 * data property statements from a model.
 */
public class DataPropertyStatementAccessObject extends AccessObject {

    public DataPropertyStatementAccessObject(OntModel ontModel, String subjectUri, String predicateUri, String dataValue) {
        setStatementOntModel(ontModel);
        setStatementSubject(subjectUri);
        setStatementPredicate(new Property(predicateUri));
        setStatementObject(dataValue);

    }
    
    public DataPropertyStatementAccessObject(OntModel ontModel, String subjectUri, Property predicate, String dataValue) {
        setStatementOntModel(ontModel);
        setStatementSubject(subjectUri);
        setStatementPredicate(predicate);
        setStatementObject(dataValue);

    }

    public DataPropertyStatementAccessObject(OntModel ontModel, DataPropertyStatement dps) {
        setStatementOntModel(ontModel);
        setStatementSubject((dps.getIndividual() == null) ? dps.getIndividualURI() : dps.getIndividual().getURI());
        setStatementPredicate(new Property(dps.getDatapropURI()));
        setStatementObject(dps.getData());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": <" + getStatementSubject() + "> <" + getStatementPredicateUri() + "> <"+ getStatementObject() + ">";
    }

    @Override
    public AccessObjectType getType() {
        return AccessObjectType.DATA_PROPERTY_STATEMENT;
    }
}
