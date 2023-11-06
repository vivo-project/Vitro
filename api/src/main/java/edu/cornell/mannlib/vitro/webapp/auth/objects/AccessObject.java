/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.objects;

import java.util.Optional;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import org.apache.jena.rdf.model.Model;

public abstract class AccessObject {

    public static String SOME_URI = "?SOME_URI";
    public static Property SOME_PREDICATE = new Property(SOME_URI);
    public static String SOME_LITERAL = "?SOME_LITERAL";

    protected AccessObjectStatement statement;
    private DataProperty dataProperty;
    private ObjectProperty objectProperty;

    public Optional<ObjectProperty> getObjectProperty() {
        if (objectProperty != null) {
            return Optional.of(objectProperty);
        }
        return Optional.empty();
    }

    public void setObjectProperty(ObjectProperty objectProperty) {
        this.objectProperty = objectProperty;
    }

    public Optional<String> getUri() {
        return Optional.empty();
    }

    public abstract AccessObjectType getType();

    public Optional<AccessObjectStatement> getStatement() {
        if (statement == null) {
            return Optional.empty();
        }
        return Optional.of(statement);
    };

    protected void initializeStatement() {
        if (statement == null) {
            statement = new AccessObjectStatement();
        }
    }

    public void setStatementOntModel(Model ontModel) {
        initializeStatement();
        statement.setModel(ontModel);
    }

    public Model getStatementOntModel() {
        if (statement != null) {
            return statement.getModel();
        }
        return null;
    }

    public void setStatementSubject(String subject) {
        initializeStatement();
        statement.setSubject(subject);
    }

    public String getStatementSubject() {
        initializeStatement();
        return statement.getSubject();
    }

    public void setStatementPredicate(Property predicate) {
        initializeStatement();
        statement.setPredicate(predicate);
    }

    protected Property getPredicate() {
        initializeStatement();
        return statement.getPredicate();
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
        this.statement.setObject(objectUri);
    }

    public String getStatementObject() {
        initializeStatement();
        return statement.getObject();
    }

    public Optional<DataProperty> getDataProperty() {
        if (dataProperty == null) {
            return Optional.empty();
        }
        return Optional.of(dataProperty);
    }

    public void setDataProperty(DataProperty dataProperty) {
        this.dataProperty = dataProperty;
    }

    public String[] getResourceUris() {
        initializeStatement();
        return statement.getResourceUris(getType());
    }

}
