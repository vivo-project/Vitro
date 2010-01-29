package edu.cornell.mannlib.vitro.webapp.dao.filtering;
/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.Date;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;

public class ObjectPropertyStatementFiltering implements ObjectPropertyStatement {
    final ObjectPropertyStatement innerStmt;
    final VitroFilters filters;
    
    public ObjectPropertyStatementFiltering( ObjectPropertyStatement stmt, VitroFilters filters){
        this.innerStmt = stmt;
        this.filters = filters;
    }
    
     /* methods that return wrapped objects */

    public Individual getObject() {
        return new IndividualFiltering(innerStmt.getObject(),filters);
    }

    //TODO: make a ObjectPropertyFiltering
    public ObjectProperty getProperty() {
        return innerStmt.getProperty();
    }

    public Individual getSubject() {
        return new IndividualFiltering(innerStmt.getSubject(), filters);
    }

    //TODO: is this in use any more?
    public PropertyInstance toPropertyInstance() {
        return innerStmt.toPropertyInstance();
    }

    /* ******** */

    public String toString() {
        return innerStmt.toString();
    }

    public String getDescription() {
        return innerStmt.getDescription();
    }

    public String getObjectURI() {
        return innerStmt.getObjectURI();
    }

    public String getPropertyURI() {
        return innerStmt.getPropertyURI();
    }

    public String getQualifier() {
        return innerStmt.getQualifier();
    }

    public String getSubjectURI() {
        return innerStmt.getSubjectURI();
    }

    public Date getSunrise() {
        return innerStmt.getSunrise();
    }

    public Date getSunset() {
        return innerStmt.getSunset();
    }

    public boolean isSubjectOriented() {
        return innerStmt.isSubjectOriented();
    }

    public void setDescription(String description) {
        innerStmt.setDescription(description);
    }

    public void setObject(Individual object) {
        innerStmt.setObject(object);
    }

    public void setObjectURI(String objectURI) {
        innerStmt.setObjectURI(objectURI);
    }

    public void setProperty(ObjectProperty property) {
        innerStmt.setProperty(property);
    }

    public void setPropertyURI(String URI) {
        innerStmt.setPropertyURI(URI);
    }

    public void setQualifier(String qualifier) {
        innerStmt.setQualifier(qualifier);
    }

    public void setSubject(Individual subject) {
        innerStmt.setSubject(subject);
    }

    public void setSubjectOriented(boolean subjectOriented) {
        innerStmt.setSubjectOriented(subjectOriented);
    }

    public void setSubjectURI(String subjectURI) {
        innerStmt.setSubjectURI(subjectURI);
    }

    public void setSunrise(Date date) {
        innerStmt.setSunrise(date);
    }

    public void setSunset(Date date) {
        innerStmt.setSunset(date);
    }

}
