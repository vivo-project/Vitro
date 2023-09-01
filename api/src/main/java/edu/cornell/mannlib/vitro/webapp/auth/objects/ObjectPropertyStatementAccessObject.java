/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.objects;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import org.apache.jena.rdf.model.Model;

/**
 * A base class for requested access objects that involve adding, editing, or
 * deleting object property statements from a model.
 */
public class ObjectPropertyStatementAccessObject extends AccessObject {

    public ObjectPropertyStatementAccessObject(Model ontModel, String subjectUri, Property predicate,
            String objectUri) {
        setStatementOntModel(ontModel);
        setStatementSubject(subjectUri);
        setStatementPredicate(predicate);
        setStatementObject(objectUri);
    }

    @Override
    public AccessObjectType getType() {
        return AccessObjectType.OBJECT_PROPERTY_STATEMENT;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": <" + getStatementSubject() + "> <" + getStatementPredicateUri() + "> <"
                + getStatementObject() + ">";
    }
}
