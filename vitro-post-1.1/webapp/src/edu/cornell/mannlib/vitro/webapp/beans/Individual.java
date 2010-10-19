/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;

/**
 * User: bdc34
 * Date: Oct 18, 2007
 * Time: 3:08:33 PM
 */
public interface Individual extends ResourceBean, VitroTimeWindowedResource, Comparable<Individual> {
    String getName();
    void setName(String in);

    /** 
     * Returns an rdfs:label if there is one on the individual.  Returns null
     * if none can be found.  If more than one rdfs:label can be found for the individual
     * one of the labels will be returned, which one is undefined.  
     */
    String getRdfsLabel();
    
    String getVClassURI();
    void setVClassURI(String in);

    //Date getSunrise();
    void setSunrise(Date in);

    //Date getSunset();
    void setSunset(Date in);

    Date getTimekey();
    void setTimekey(Date in);

    Timestamp getModTime();
    void setModTime(Timestamp in);

    List<ObjectProperty> getObjectPropertyList();
    void setPropertyList(List<ObjectProperty> propertyList);

    Map<String,ObjectProperty> getObjectPropertyMap();
    void setObjectPropertyMap(Map<String,ObjectProperty> propertyMap);
    
    List<DataProperty> getDataPropertyList();
    void setDatatypePropertyList(List<DataProperty> datatypePropertyList);

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
    
    public boolean isMemberOfClassProhibitedFromSearch(ProhibitedFromSearch pfs);

    void setObjectPropertyStatements(List<ObjectPropertyStatement> list);
    List<ObjectPropertyStatement> getObjectPropertyStatements();
    List<ObjectPropertyStatement> getObjectPropertyStatements(String propertyUri);
    
    List<Individual> getRelatedIndividuals(String propertyUri);
    Individual getRelatedIndividual(String propertyUri);

    List<DataPropertyStatement> getExternalIds();
    void setExternalIds(List<DataPropertyStatement> externalIds);

    String getMoniker();
    void setMoniker(String in);

    String getDescription();
    void setDescription(String in);

    String getAnchor();
    void setAnchor(String in);

    String getBlurb();
    void setBlurb(String in);

    int getStatusId();
    void setStatusId(int in);

    String getStatus();
    void setStatus(String s);

    void setMainImageUri(String mainImageUri);
    String getMainImageUri();
    
    String getImageUrl();
    String getThumbUrl();

    String getUrl();
    void setUrl(String url);

    List<Link> getLinksList();
    void setLinksList(List<Link> linksList);
    
    Link getPrimaryLink();
    void setPrimaryLink(Link link);

    String getFlag1Set();
    void setFlag1Set(String in);
    int getFlag1Numeric();
    void setFlag1Numeric(int i);

    /* Consider the flagBitMask as a mask to & with flags.
    if flagBitMask bit zero is set then return true if
    the individual is in portal 2,
    if flagBitMask bit 1 is set then return true if
    the individual is in portal 4
    etc.
     */
    boolean doesFlag1Match(int flagBitMask);

    String getFlag2Set();
    void setFlag2Set(String in);
    int getFlag2Numeric();
    void setFlag2Numeric(int i);

    String getFlag3Set();
    void setFlag3Set(String in);
    int getFlag3Numeric();
    void setFlag3Numeric(int i);

    List<String> getKeywords();
    void setKeywords(List<String> keywords);
    String getKeywordString();
    
    List<Keyword> getKeywordObjects();
    void setKeywordObjects(List<Keyword> keywords);

    void sortForDisplay();

    JSONObject toJSON() throws JSONException;

    Object getField(String fieldName) throws NoSuchMethodException;
    
    Float getSearchBoost();
    void setSearchBoost( Float boost );
}
