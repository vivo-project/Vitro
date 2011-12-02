/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.filestorage.model.ImageInfo;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;

/**
 * Represents a single entity record.
*/
public class IndividualImpl extends BaseResourceBean implements Individual, Comparable<Individual> {
	/**
	 * This can be used as a "not initialized" indicator for a property that
	 * could validly be set to <code>null</code>. If <code>get()</code> is
	 * called on such a property, and the property has this value, the correct
	 * value can be fetched and cached.
	 */
	protected static final String NOT_INITIALIZED = "__%NOT_INITIALIZED%__";

	public String name = null;
	protected String rdfsLabel = null;
    public String vClassURI = null;
    protected VClass vClass = null;
    protected List<VClass> directVClasses = null;
    protected List<VClass> allVClasses = null;
    protected Timestamp modTime = null;
    protected List <ObjectProperty>propertyList = null;
    protected List<ObjectProperty> populatedObjectPropertyList = null;
    protected Map <String,ObjectProperty> objectPropertyMap = null;
    protected List <DataProperty>datatypePropertyList = null;
    protected List<DataProperty> populatedDataPropertyList = null;
    protected Map <String,DataProperty> dataPropertyMap = null;
    protected List <DataPropertyStatement>dataPropertyStatements = null;
    protected List <ObjectPropertyStatement>objectPropertyStatements = null;
    protected List <ObjectPropertyStatement>rangeEnts2Ents = null;
    protected List <DataPropertyStatement>externalIds = null;

    protected String mainImageUri = NOT_INITIALIZED;
    protected ImageInfo imageInfo = null;
    protected Float searchBoost;
    protected String searchSnippet;
    
    /** indicates if sortForDisplay has been called  */
    protected boolean sorted = false;
    protected boolean DIRECT = true;
    protected boolean ALL = false;
    
    public IndividualImpl() {
    }

    public IndividualImpl(String URI) {
        this.setURI(URI);
        this.setVClasses(new ArrayList<VClass>(), DIRECT);
        this.setVClasses(new ArrayList<VClass>(), ALL);
        this.setObjectPropertyStatements(new ArrayList<ObjectPropertyStatement>());
        this.setObjectPropertyMap(new HashMap<String, ObjectProperty>());
        this.setDataPropertyStatements(new ArrayList<DataPropertyStatement>());
        this.setDataPropertyMap(new HashMap<String, DataProperty>());
        this.setPropertyList(new ArrayList<ObjectProperty>());
        this.setDatatypePropertyList(new ArrayList<DataProperty>());
    }

    public String getName(){return name;}
    public void setName(String in){name=in;}

    public String getRdfsLabel(){ return rdfsLabel; }
    public void setRdfsLabel(String s){ rdfsLabel = s; }    	

    public String getVClassURI(){return vClassURI;}
    public void setVClassURI(String in){vClassURI=in;}

    /**
     * Returns the last time this object was changed in the model.
     * Notice Java API craziness: Timestamp is a subclass of Date
     * but there are notes in the Javadoc that you should not pretend
     * that a Timestamp is a Date.  (Crazy ya?)  In particular,
     * Timestamp.equals(Date) will never return true because of
     * the 'nanos.'
     */
    public Timestamp getModTime(){return modTime;}
    public void setModTime(Timestamp in){modTime=in;}

    public List<ObjectProperty> getObjectPropertyList() {
        return propertyList;
    }
    public void setPropertyList(List <ObjectProperty>propertyList) {
        this.propertyList = propertyList;
    }
    public List<ObjectProperty> getPopulatedObjectPropertyList() {
        return populatedObjectPropertyList;
    }
    public void setPopulatedObjectPropertyList(List<ObjectProperty> propertyList) {
        populatedObjectPropertyList = propertyList;
    }
    public Map<String,ObjectProperty> getObjectPropertyMap() {
    	return this.objectPropertyMap;
    }
    public void setObjectPropertyMap( Map<String,ObjectProperty> propertyMap ) {
    	this.objectPropertyMap = propertyMap;
    }
    public List <DataProperty>getDataPropertyList() {
        return datatypePropertyList;
    }
    public void setDatatypePropertyList(List <DataProperty>datatypePropertyList) {
        this.datatypePropertyList = datatypePropertyList;
    }
    public List<DataProperty> getPopulatedDataPropertyList() {
        return populatedDataPropertyList;
    }
    public void setPopulatedDataPropertyList(List<DataProperty> propertyList) {
        populatedDataPropertyList = propertyList;
    }
    public Map<String,DataProperty> getDataPropertyMap() {
    	return this.dataPropertyMap;
    }
    public void setDataPropertyMap( Map<String,DataProperty> propertyMap ) {
    	this.dataPropertyMap = propertyMap;
    }
    public void setDataPropertyStatements(List <DataPropertyStatement>list) {
         dataPropertyStatements = list;
    }
    public List<DataPropertyStatement> getDataPropertyStatements(){
        return dataPropertyStatements;
    }
    
    public List<DataPropertyStatement> getDataPropertyStatements(String propertyUri) {
        List<DataPropertyStatement> stmts = getDataPropertyStatements();
        List<DataPropertyStatement> stmtsForProp = new ArrayList<DataPropertyStatement>();
        for (DataPropertyStatement stmt : stmts) {
            if (stmt.getDatapropURI().equals(propertyUri)) {
                stmtsForProp.add(stmt);
            }
        }
        return stmtsForProp;        
    }

    public DataPropertyStatement getDataPropertyStatement(String propertyUri) {
        List<DataPropertyStatement> stmts = getDataPropertyStatements(propertyUri);
        return stmts.isEmpty() ? null : stmts.get(0);       
    }
    
    public List<String> getDataValues(String propertyUri) {     
        List<DataPropertyStatement> stmts = getDataPropertyStatements(propertyUri);
        List<String> dataValues = new ArrayList<String>(stmts.size());
        for (DataPropertyStatement stmt : stmts) {
            dataValues.add(stmt.getData());
        }
        return dataValues;
    }
 
    public String getDataValue(String propertyUri) {
        List<DataPropertyStatement> stmts = getDataPropertyStatements(propertyUri);
        return stmts.isEmpty() ? null : stmts.get(0).getData();
    }

    public VClass getVClass() {
        return vClass;
    }
    public void setVClass(VClass class1) {
        vClass = class1;
    }
    
    public List<VClass> getVClasses() {
    	return allVClasses;
    }
    
    @Override
	public boolean isVClass(String uri) {
    	if (uri == null) {
    		return false;
    	}
		for (VClass vClass : getVClasses()) {
			if (uri.equals(vClass.getURI())) {
				return true;
			}
		}
		return false;
	}
    
    public boolean isMemberOfClassProhibitedFromSearch(ProhibitedFromSearch pfs) {
    	throw new UnsupportedOperationException(this.getClass().getName() +
    		".isMemberOfClassProhibitedFromSearch must be overriden by a subclass");
    }

	public List<VClass> getVClasses(boolean direct) {
    	if (direct) {
    		return directVClasses;
    	} else {
    		return allVClasses;
    	}
    }
    
    public void setVClasses(List<VClass> vClassList, boolean direct) {
    	if (direct) {
    		this.directVClasses = vClassList; 
    	} else {
    		this.allVClasses = vClassList;
    	}
    }

    public void setObjectPropertyStatements(List<ObjectPropertyStatement> list) {
         objectPropertyStatements = list;
    }

    public List <ObjectPropertyStatement> getObjectPropertyStatements(){
        return objectPropertyStatements;
    }
    
    public List<ObjectPropertyStatement> getObjectPropertyStatements(String propertyUri) {
        List<ObjectPropertyStatement> stmts = getObjectPropertyStatements();
        List<ObjectPropertyStatement> stmtsForProp = new ArrayList<ObjectPropertyStatement>();
        for (ObjectPropertyStatement stmt : stmts) {
            if (stmt.getPropertyURI().equals(propertyUri)) {
                stmtsForProp.add(stmt);
            }
        }
        return stmtsForProp;
    }
    
    public List<Individual> getRelatedIndividuals(String propertyUri) {
        List<ObjectPropertyStatement> stmts = getObjectPropertyStatements(propertyUri);
        List<Individual> relatedIndividuals = new ArrayList<Individual>(stmts.size());
        for (ObjectPropertyStatement stmt : stmts) {
            relatedIndividuals.add(stmt.getObject());
        }
        return relatedIndividuals;       
    }
    
    public Individual getRelatedIndividual(String propertyUri) {
        List<ObjectPropertyStatement> stmts = getObjectPropertyStatements(propertyUri);    
        return stmts.isEmpty() ? null : stmts.get(0).getObject();
    }

    public List<DataPropertyStatement> getExternalIds(){
        return externalIds;
    }
    public void setExternalIds(List<DataPropertyStatement> externalIds){
        this.externalIds = externalIds;
    }
    
	@Override
	public String getMainImageUri() {
		return (mainImageUri == NOT_INITIALIZED) ? null : mainImageUri;
	}

	@Override
	public void setMainImageUri(String mainImageUri) {
		this.mainImageUri = mainImageUri;
		this.imageInfo = null;
	}

	@Override
	public String getImageUrl() {
		return "imageUrl";
	}

	@Override
	public String getThumbUrl() {
		return "thumbUrl";
	}

    public Float getSearchBoost() { return searchBoost;  }    
    public void setSearchBoost(Float boost) { searchBoost = boost; }
    
    public String getSearchSnippet() { return searchSnippet; }
    public void setSearchSnippet(String snippet) { searchSnippet = snippet; }
    
    /**
     * Sorts the ents2ents records into the proper order for display.
     *
     */
    public void sortForDisplay(){
        if( sorted ) return;
        if( getObjectPropertyList() == null ) return;
        sortPropertiesForDisplay();
        sortEnts2EntsForDisplay();
        sorted = true;
    }

    protected void sortEnts2EntsForDisplay(){
        if( getObjectPropertyList() == null ) return;

        Iterator it = getObjectPropertyList().iterator();
        while(it.hasNext()){
            ObjectProperty prop = (ObjectProperty)it.next();
            prop.sortObjectPropertyStatementsForDisplay(prop,prop.getObjectPropertyStatements());
        }
    }

    protected void sortPropertiesForDisplay( ){
        //here we sort the Property objects
        Collections.sort(getObjectPropertyList(), new ObjectProperty.DisplayComparator());
    }

    public static final String [] INCLUDED_IN_JSON = {
         "URI",
         "name",
         "vClassId"
    };


    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObj = new JSONObject(this, INCLUDED_IN_JSON);
        return jsonObj;
    }

   public int compareTo(Individual o2) {
       Collator collator = Collator.getInstance();
       if (o2 == null) {
       	   return 1;
       } else {
       	   return collator.compare(this.getName(),o2.getName());
       }
   }

   public String toString(){
       if( getURI() == null ){
           return "uninitialized, null URI";
       }else{
           return getURI() + " " + getName();
       }
   }
    
    public boolean hasThumb() {
        return getThumbUrl() != null && ! getThumbUrl().isEmpty();
    }
}
