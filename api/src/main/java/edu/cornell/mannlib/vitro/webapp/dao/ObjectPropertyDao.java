/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;

public interface ObjectPropertyDao extends PropertyDao {

    public abstract List<ObjectProperty> getAllObjectProperties();

    public ObjectProperty getObjectPropertyByURI(String objectPropertyURI);
    
    public ObjectProperty getObjectPropertyByURIs(String objectPropertyURI, String domainURI, String rangeURI);
    
    /**
     * Use this method to supply a base ObjectProperty whose fields will be updated
     * as necessary to correspond to the configuration for the specified Domain
     * and Range.  
     * @param objectPropertyURI Object Property URI
     * @param domainURI Domain URI
     * @param rangeURI Range URI
     * @param base Object property
     * @return ObjectProperty
     */
    public ObjectProperty getObjectPropertyByURIs(String objectPropertyURI, String domainURI, String rangeURI, ObjectProperty base);

    public List <ObjectProperty> getObjectPropertiesForObjectPropertyStatements(List /*of ObjectPropertyStatement */ objectPropertyStatements);

    public List<String> getSuperPropertyURIs(String objectPropertyURI, boolean direct);

    public List<String> getSubPropertyURIs(String objectPropertyURI);

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
    
    /**
     * Returns a list of ObjectProperty objects for which statements exist about
     * the individual.  Note that this method now returns multiple copies of
     * a given predicate, with the rangeVClassURI changed to indicate the distinct
     * types of the related objects.  This supports finding the approriate list
     * views for the "faux" qualified properties.
     */
    public List<ObjectProperty> getObjectPropertyList(Individual subject);
    
    /**
     * Returns a list of ObjectProperty objects for which statements exist about
     * the individual.  Note that this method now returns multiple copies of
     * a given predicate, with the rangeVClassURI changed to indicate the distinct
     * types of the related objects.  This supports finding the approriate list
     * views for the "faux" qualified properties.
     */
    public List<ObjectProperty> getObjectPropertyList(String subjectUri); 
    
    public String getCustomListViewConfigFileName(ObjectProperty objectProperty);
}
