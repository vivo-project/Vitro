/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.beans;

/**
 * User: bdc34
 * Date: Oct 18, 2007
 * Time: 3:41:23 PM
 */
public interface ResourceBean {
    
    String getURI();

    boolean isAnonymous();

    void setURI(String URI);

    String getNamespace();

    void setNamespace(String namespace);

    String getLocalName();

    void setLocalName(String localName);
    
    String getLabel();

    public String getPickListName();
    
}
