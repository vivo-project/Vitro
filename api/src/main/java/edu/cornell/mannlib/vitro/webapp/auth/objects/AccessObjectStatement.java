package edu.cornell.mannlib.vitro.webapp.auth.objects;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import org.apache.jena.rdf.model.Model;

public class AccessObjectStatement {

    private Model model = null;
    private String subject = null;
    private Property predicate = null;
    private String object = null;

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Property getPredicate() {
        return predicate;
    }

    public void setPredicate(Property predicate) {
        this.predicate = predicate;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String[] getResourceUris(AccessObjectType type) {
        switch (type) {
            case DATA_PROPERTY_STATEMENT:
                return new String[] { getSubject() };
            case OBJECT_PROPERTY_STATEMENT:
                return new String[] { getSubject(), getObject() };
            case FAUX_DATA_PROPERTY_STATEMENT:
                return new String[] { getSubject(), getObject() };
            case FAUX_OBJECT_PROPERTY_STATEMENT:
                return new String[] { getSubject(), getObject() };
            default:
                return new String[0];
        }
    }
}
