/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.objects;

import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.FauxObjectPropertyWrapper;

public class FauxObjectPropertyStatementAccessObject extends AccessObject {

	public FauxObjectPropertyStatementAccessObject(Model ontModel, String subjectUri, FauxObjectPropertyWrapper predicate, String objectUri) {
	    setStatementOntModel(ontModel);
        setStatementSubject(subjectUri);
        setStatementPredicate(predicate);
        setStatementObject(objectUri);
	}

    @Override
    public AccessObjectType getType() {
        return AccessObjectType.FAUX_OBJECT_PROPERTY_STATEMENT;
    }
    
    @Override
    public String getStatementPredicateUri() {
        if (statement == null || statement.getPredicate() == null) {
            return null;
        }
        Property predicate = getPredicate();
        if (predicate instanceof FauxObjectPropertyWrapper) {
            return ((FauxObjectPropertyWrapper) predicate).getConfigUri();
        }
        return predicate.getURI();
    }

    @Override
    public String toString() {
        Property predicate = getPredicate();
        return getClass().getSimpleName() + ": <" + getStatementSubject() + "> <" + ((FauxObjectPropertyWrapper) predicate).getConfigUri() + "> <"+ getStatementObject() + ">";
    }
}