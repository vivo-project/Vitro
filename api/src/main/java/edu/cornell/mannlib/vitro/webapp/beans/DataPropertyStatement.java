/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.beans;

/**
 * a class representing a particular instance of a data property
 *
 */
public interface DataPropertyStatement {

    public Individual getIndividual();

    public void setIndividual(Individual individual);

    public String getIndividualURI();

    public void setIndividualURI(String individualURI);

    public String getData();

    public void setData(String data);

    public String getDatapropURI();

    public void setDatapropURI(String propertyURI);

    public String getDatatypeURI();

    public void setDatatypeURI(String datatypeURI);

    public String getLanguage();

    public void setLanguage(String language);

    public String getString();
}
