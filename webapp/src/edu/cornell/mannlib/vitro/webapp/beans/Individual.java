/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * User: bdc34
 * Date: Oct 18, 2007
 * Time: 3:08:33 PM
 */
public interface Individual extends ResourceBean, Comparable<Individual> {
    String getName();
    void setName(String in);

    /** 
     * Returns an rdfs:label if there is one on the individual.  Returns null
     * if none can be found.  If more than one rdfs:label can be found for the individual
     * one of the labels will be returned, which one is undefined.  
     */
    String getRdfsLabel();
    void setRdfsLabel(String in);

    String getVClassURI();
    void setVClassURI(String in);

    Timestamp getModTime();
    void setModTime(Timestamp in);

    List<ObjectProperty> getObjectPropertyList();
    void setPropertyList(List<ObjectProperty> propertyList);

    /**
     * Returns a list of ObjectProperty objects for which statements exist about
     * the individual.  Note that this method now returns multiple copies of
     * a given predicate, with the rangeVClassURI changed to indicate the distinct
     * types of the related objects.  This supports finding the approriate list
     * views for the "faux" qualified properties.
     */
    List<ObjectProperty> getPopulatedObjectPropertyList();
    void setPopulatedObjectPropertyList(List<ObjectProperty> propertyList);
    
    Map<String,ObjectProperty> getObjectPropertyMap();
    void setObjectPropertyMap(Map<String,ObjectProperty> propertyMap);
    
    List<DataProperty> getDataPropertyList();
    void setDatatypePropertyList(List<DataProperty> datatypePropertyList);

    List<DataProperty> getPopulatedDataPropertyList();
    void setPopulatedDataPropertyList(List<DataProperty> dataPropertyList);
    
    Map<String,DataProperty> getDataPropertyMap();
    void setDataPropertyMap(Map<String,DataProperty> propertyMap);
    
    void setDataPropertyStatements(List<DataPropertyStatement> list);
    List<DataPropertyStatement> getDataPropertyStatements();
    List<DataPropertyStatement> getDataPropertyStatements(String propertyUri);
    DataPropertyStatement getDataPropertyStatement(String propertyUri);
    
    List<String> getDataValues(String propertyUri);
    String getDataValue(String propertyUri);

    VClass getVClass();
    void setVClass(VClass class1);
    
    List<VClass> getVClasses();
    
    List<VClass> getVClasses(boolean direct);
    void setVClasses(List<VClass> vClassList, boolean direct);
    
    /** Does the individual belong to this class? */
    boolean isVClass(String uri);
    
    List<String> getMostSpecificTypeURIs();

    void setObjectPropertyStatements(List<ObjectPropertyStatement> list);
    List<ObjectPropertyStatement> getObjectPropertyStatements();
    List<ObjectPropertyStatement> getObjectPropertyStatements(String propertyUri);
    
    List<Individual> getRelatedIndividuals(String propertyUri);
    Individual getRelatedIndividual(String propertyUri);

    List<DataPropertyStatement> getExternalIds();
    void setExternalIds(List<DataPropertyStatement> externalIds);

    void setMainImageUri(String mainImageUri);
    String getMainImageUri();
    
    String getImageUrl();
    String getThumbUrl();
    boolean hasThumb();

    void sortForDisplay();

    JSONObject toJSON() throws JSONException;
    
    Float getSearchBoost();
    void setSearchBoost( Float boost );
    
    String getSearchSnippet();
    void setSearchSnippet( String snippet );
    
    /**
     * This is crap. It was put in so IndividualFiltering could filter object properties properly,
     * but what we really need is either: filters have a reference to a webappDaoFactory, or
     * all ObjectPropertyStatements are resolved when they are created. See VIVO-946 and VIVO-984.
     */
	void resolveAsFauxPropertyStatement(ObjectPropertyStatement stmt);
}
