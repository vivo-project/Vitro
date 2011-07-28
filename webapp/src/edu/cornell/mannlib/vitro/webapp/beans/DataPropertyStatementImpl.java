/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.util.Date;

/**
 * a class representing an particular instance of a data property
 *
 */
public class DataPropertyStatementImpl implements DataPropertyStatement
{
    private Individual individual = null;
    private String individualURI = null;
    private String data = null;
    private String datapropURI = null;
    private String datatypeURI = null;
    private String language = null;
    
    public DataPropertyStatementImpl(){
    }

    public DataPropertyStatementImpl(Individual individual){
        if( individual != null ){
            this.individualURI = individual.getURI();
        }
    }
    
    public DataPropertyStatementImpl(String individualUri, String propertyUri, String data){
        individualURI = individualUri;
        datapropURI = propertyUri;
        this.data = data;
    }

    public Individual getIndividual() {
        return this.individual;
    }
    
    public void setIndividual(Individual individual) {
        this.individual = individual;
    }
    
    public String getIndividualURI() {
        return individualURI;
    }

    public void setIndividualURI(String individualURI) {
        this.individualURI = individualURI;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDatapropURI() {
        return datapropURI;
    }

    public void setDatapropURI(String datapropURI) {
        this.datapropURI = datapropURI;
    }

    public String getDatatypeURI() {
        return datatypeURI;
    }

    public void setDatatypeURI(String datatypeURI) {
        this.datatypeURI = datatypeURI;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getString(){
        String out = "instance of dataprop: " + datapropURI;

        if( data == null )
            out = out + " data is null";
        else if( data.length() > 20 )
            out = out + " data (truncated): '" + data.substring(0,19) + "'...";
        else
            out = out + " data: '" + data ;
        return out;
    }
}
