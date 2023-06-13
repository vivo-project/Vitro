/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.objects;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;

public class FauxDataPropertyStatementAccessObject extends AccessObject {

    private FauxProperty predicate;

    public FauxDataPropertyStatementAccessObject(OntModel ontModel, String subjectUri, FauxProperty predicate, String dataValue) {
        setStatementOntModel(ontModel);
        setStatementSubject(subjectUri);
        this.predicate = predicate;
        setStatementObject(dataValue);
    }

    @Override
    public AccessObjectType getType() {
        return AccessObjectType.FAUX_DATA_PROPERTY_STATEMENT;
    }
    
    @Override
    public String getStatementPredicateUri() {
        return predicate.getConfigUri();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": <" + getStatementSubject() + "> <" + predicate.getConfigUri() + "> <"+ getStatementObject() + ">";
    }
}
