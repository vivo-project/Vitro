/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;

public interface ObjectPropertyDao extends PropertyDao {

    public abstract List<ObjectProperty> getAllObjectProperties();

    public ObjectProperty getObjectPropertyByURI(String objectPropertyURI);

    public List <ObjectProperty> getObjectPropertiesForObjectPropertyStatements(List /*of ObjectPropertyStatement */ objectPropertyStatements);

    public List<String> getSuperPropertyURIs(String objectPropertyURI, boolean direct);

    public List<String> getSubPropertyURIs(String objectPropertyURI);

    public List<ObjectPropertyStatement> getStatementsUsingObjectProperty(ObjectProperty op);

    public void fillObjectPropertiesForIndividual(Individual individual);

    public int insertObjectProperty(ObjectProperty objectProperty ) throws InsertException;

    public void updateObjectProperty(ObjectProperty objectProperty );

    public void deleteObjectProperty(String objectPropertyURI);

    public void deleteObjectProperty(ObjectProperty objectProperty);
    
    public boolean skipEditForm(String predicateURI);
    

//    List /*of ObjectProperty */ getObjectPropertiesForObjectPropertyStatements(List /*of ObjectPropertyStatement */ objectPropertyStatements);
//
//    void fillObjectPropertiesForIndividual(IndividualWebapp individual);
//
//    int insertObjectProperty(ObjectProperty objectPropertyWebapp);
//
//    void updateObjectProperty(ObjectProperty objectPropertyWebapp);
//
//    void deleteObjectProperty(ObjectProperty objectPropertyWebapp);

//    ObjectProperty getObjectPropertyByURI(String objectPropertyURI);

//    List /* of ObjectProperty */ getAllObjectProperties();

    List <ObjectProperty> getRootObjectProperties();
    
    public List<ObjectProperty> getObjectPropertyList(Individual subject);
    
    public List<ObjectProperty> getObjectPropertyList(String subjectUri); 
    
    public String getCustomListViewConfigFileName(ObjectProperty objectProperty);
}
