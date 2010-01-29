package edu.cornell.mannlib.vitro.webapp.dao.filtering;
/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.Date;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;

public class DataPropertyStatementFiltering implements DataPropertyStatement {
    final DataPropertyStatement innerStmt;
    final VitroFilters filters;
    
    public DataPropertyStatementFiltering( DataPropertyStatement stmt, VitroFilters filters){
        this.innerStmt = stmt;
        this.filters = filters;
    }
    
    /***** methods that return wrapped objects *****/
    /*
    public String getIndividual() {
        return new IndividualFiltering(innerStmt.getIndividual(),filters);
    } */

    /* ******** */

    public String toString() {
        return innerStmt.toString();
    }

    public String getIndividualURI() {
        return innerStmt.getIndividualURI();
    }

    public String getLanguage() {
        return innerStmt.getLanguage();
    }

    public String getData() {
        return innerStmt.getData();
    }
    
    public String getDatatypeURI() {
        return innerStmt.getDatatypeURI();
    }

    public String getDatapropURI() {
        return innerStmt.getDatapropURI();
    }

    public Date getSunrise() {
        return innerStmt.getSunrise();
    }

    public Date getSunset() {
        return innerStmt.getSunset();
    }
    
    public String getString() {
        return innerStmt.getString();
    }
    
    public void setIndividualURI(String individualURI) {
        innerStmt.setIndividualURI(individualURI);
    }
    
    public void setData(String data) {
        innerStmt.setData(data);
    }

    public void setLanguage(String language) {
        innerStmt.setLanguage(language);
    }

    public void setDatatypeURI(String URI) {
        innerStmt.setDatatypeURI(URI);
    }

    public void setDatapropURI(String datapropURI) {
        innerStmt.setDatapropURI(datapropURI);
    }

    public void setSunrise(Date date) {
        innerStmt.setSunrise(date);
    }

    public void setSunset(Date date) {
        innerStmt.setSunset(date);
    }
    
}
