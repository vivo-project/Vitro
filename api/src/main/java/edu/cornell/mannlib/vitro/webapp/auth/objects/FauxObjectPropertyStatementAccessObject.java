/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.objects;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import org.apache.jena.rdf.model.Model;

public class FauxObjectPropertyStatementAccessObject extends AccessObject {

    private FauxProperty predicate;

    public FauxObjectPropertyStatementAccessObject(Model ontModel, String subjectUri, FauxProperty fauxProperty,
            String objectUri) {
        setStatementOntModel(ontModel);
        setStatementSubject(subjectUri);
        predicate = fauxProperty;
        setStatementObject(objectUri);
    }

    @Override
    public AccessObjectType getType() {
        return AccessObjectType.FAUX_OBJECT_PROPERTY_STATEMENT;
    }

    @Override
    public String getStatementPredicateUri() {
        return predicate.getConfigUri();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": <" + getStatementSubject() + "> <" + predicate.getConfigUri() + "> <"
                + getStatementObject() + ">";
    }
}