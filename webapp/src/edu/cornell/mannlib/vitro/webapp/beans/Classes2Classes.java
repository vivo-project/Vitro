/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

/**
 *
 * a class representing a direct subsumption relationship
 * between two ontology classes
 *
 */
public class Classes2Classes {

    private String superclassURI = null;
    private String superclassNamespace = null;
    private String superclassLocalName = null;

    private String subclassURI = null;
    private String subclassNamespace = null;
    private String subclassLocalName = null;

    public String getSuperclassURI(){ return superclassURI;}
    public void setSuperclassURI(String in){ superclassURI=in;}

    public String getSuperclassNamespace(){ return superclassNamespace; }
    public void setSuperclassNamespace(){ this.superclassNamespace=superclassNamespace;}

    public String getSuperclassLocalName(){ return superclassLocalName; }
    public void setSuperclassLocalName(){ this.superclassLocalName=superclassLocalName;}

    public String getSubclassURI(){ return subclassURI;}
    public void setSubclassURI(String in){ subclassURI=in;}

    public String getSubclassNamespace(){ return subclassNamespace; }
    public void setSubclassNamespace(){ this.subclassNamespace=subclassNamespace;}

    public String getSubclassLocalName(){ return subclassLocalName; }
    public void setSubclassLocalName(){ this.subclassLocalName=subclassLocalName;}

}