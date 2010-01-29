package edu.cornell.mannlib.vitro.webapp.beans;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.Date;

/**
 * a class representing an particular instance of a data property
 *
 */
public interface DataPropertyStatement {
    
    public String getIndividualURI();

    public void setIndividualURI(String individualURI);

    public String getData();

    public void setData(String data);

    public String getDatapropURI();

    public void setDatapropURI(String datapropURI);

    public String getDatatypeURI();

    public void setDatatypeURI(String datatypeURI);

    public String getLanguage();

    public void setLanguage(String language);

    public Date getSunrise();

    public void setSunrise(Date sunrise);

    public Date getSunset();

    public void setSunset(Date sunset);
    /*
    public String getQualifier();

    public void setQualifier(String qualifier);
    */
    public String getString();
}
