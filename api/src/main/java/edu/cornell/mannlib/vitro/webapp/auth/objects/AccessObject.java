/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.objects;

import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;

public abstract class AccessObject {

    public static String SOME_URI = "?SOME_URI";
    public static Property SOME_PREDICATE = new Property(SOME_URI);
    public static String SOME_LITERAL = "?SOME_LITERAL";
    
    private AccessObjectStatement statement;
    private DataProperty dataProperty;
    private ObjectProperty objectProperty;
    
    public ObjectProperty getObjectProperty() {
        return objectProperty;
    }

    public void setObjectProperty(ObjectProperty objectProperty) {
        this.objectProperty = objectProperty;
    }

    public String getUri() {
        return null;
    }
    
    public AccessObjectType getType() {
        return AccessObjectType.ANY;
    };
    
    public AccessObjectStatement getStatement() {
        return statement;
    };
    
    protected void initializeStatement() {
        if (statement == null) {
            statement = new AccessObjectStatement();
        }
    }

    public void setStatementOntModel(Model ontModel) {
        initializeStatement();
        getStatement().setModel(ontModel);
    }

    public Model getStatementOntModel() {
        initializeStatement();
        return getStatement().getModel();
    }
    
    public void setStatementSubject(String subject) {
        initializeStatement();
        getStatement().setSubject(subject);
    }

    public String getStatementSubject() {
        initializeStatement();
        return getStatement().getSubject();
    }
    
    public void setStatementPredicate(Property predicate) {
        initializeStatement();
        getStatement().setPredicate(predicate);
    }
    
    private Property getPredicate() {
        initializeStatement();
        return getStatement().getPredicate();
    }

    public String getStatementPredicateUri() {
        if (statement == null || statement.getPredicate() == null) {
            return null;
        }
        Property predicate = getPredicate();
        return predicate.getURI();
    }
    
    public void setStatementObject(String objectUri) {
        initializeStatement();
        this.getStatement().setObject(objectUri);
    }

    public String getStatementObject() {
        initializeStatement();
        return getStatement().getObject();
    }
    
    public DataProperty getDataProperty() {
        return dataProperty;
    }

    public void setDataProperty(DataProperty dataProperty) {
        this.dataProperty = dataProperty;
    }
    
    public String[] getResourceUris() {
        initializeStatement();
        return getStatement().getResourceUris(getType());
    }
    
}
