/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import edu.cornell.mannlib.vitro.webapp.beans.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.Collator;
import java.util.*;

/**
 * Represents a single entity record.
*/
public class IndividualImpl extends BaseResourceBean implements Individual, Comparable<Individual> {
    public String name = null;
    public String vClassURI = null;
    protected VClass vClass = null;
    protected List<VClass> directVClasses = null;
    protected List<VClass> allVClasses = null;
    protected Date sunrise = null;
    protected Date sunset = null;
    protected Date timekey = null;
    protected Timestamp modTime = null;
    protected List <ObjectProperty>propertyList = null;
    protected Map <String,ObjectProperty> objectPropertyMap = null;
    protected List <DataProperty>datatypePropertyList = null;
    protected Map <String,DataProperty> dataPropertyMap = null;
    protected List <DataPropertyStatement>dataPropertyStatements = null;
    protected List <ObjectPropertyStatement>objectPropertyStatements = null;
    protected List <ObjectPropertyStatement>rangeEnts2Ents = null;
    protected List <DataPropertyStatement>externalIds = null;

    protected String moniker = null;
    protected String url = null;
    protected String description = null;
    protected String imageFile = null;
    protected String anchor = null;
    protected String blurb = null;
    protected String imageThumb = null;
    protected String citation = null;
    protected int statusId = 0;
    protected String status = null;
    protected List <Link>linksList = null;
    protected Link primaryLink = null;
    protected List<String> keywords=null;
    protected List<Keyword> keywordObjects=null;
    protected Float searchBoost;
    
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

//     private String modTime = null;
//     public String getModtime(){return modTime;}
//     public void setModtime(String in){modTime=in;}

    public String getVClassURI(){return vClassURI;}
    public void setVClassURI(String in){vClassURI=in;}

    public Date getSunrise(){return sunrise;}
    public void setSunrise(Date in){sunrise=in;}

    public Date getSunset(){return sunset;}
    public void setSunset(Date in){sunset=in;}

    public Date getTimekey(){return timekey;}
    public void setTimekey(Date in){timekey=in;}

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
    public Map<String,DataProperty> getDataPropertyMap() {
    	return this.dataPropertyMap;
    }
    public void setDataPropertyMap( Map<String,DataProperty> propertyMap ) {
    	this.dataPropertyMap = propertyMap;
    }
    public void setDataPropertyStatements(List <DataPropertyStatement>list) {
         dataPropertyStatements = list;
    }
    public List <DataPropertyStatement>getDataPropertyStatements(){
        return dataPropertyStatements;
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

    public List <ObjectPropertyStatement>getObjectPropertyStatements(){
        return objectPropertyStatements;
    }

    public List<DataPropertyStatement> getExternalIds(){
        return externalIds;
    }
    public void setExternalIds(List<DataPropertyStatement> externalIds){
        this.externalIds = externalIds;
    }


    public String getMoniker(){return moniker;}
    public void setMoniker(String in){moniker=in;}

    public String getDescription(){return description;}
    public void setDescription(String in){description=in;}

    public String getAnchor(){return anchor;}
    public void setAnchor(String in){anchor=in;}

    public String getBlurb(){return blurb;}
    public void setBlurb(String in){blurb=in;}

    public String getCitation(){return citation;}
    public void setCitation(String in){citation=in;}

    public int getStatusId(){return statusId;}
    public void setStatusId(int in){statusId=in;}

    public String getStatus()         {return status;}
    public void   setStatus(String s) {status=s;     }

    public String getImageFile() {
        return imageFile;
    }
    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }
    public String getImageThumb() {
        return imageThumb;
    }
    public void setImageThumb(String imageThumb) {
        this.imageThumb = imageThumb;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public List<Link> getLinksList() {
        return linksList;
    }
    public void setLinksList(List <Link>linksList) {
        this.linksList = linksList;
    }
    
    public Link getPrimaryLink() {
        return primaryLink;
    }
    
    public void setPrimaryLink(Link link) {
        primaryLink = link;
    }
    
    
    /* look at PortalFlag.numeric2numerics if you want to know which
     * bits are set in a numeric flag.
     *
     * NOTICE:
     * Values set Entity.getFlagXNumeric() will NOT be saved to the model.
     *
     * Also, changes to an entity flag state using Entity.setFlagXNumeric()
     * are not reflected in Entity.getFlagXSet() and vice versa.
     */
    protected String flag1Set = null;
    public String getFlag1Set(){return flag1Set;}
    public void setFlag1Set(String in){flag1Set=in;}

    protected int flag1Numeric = -1;
    public int getFlag1Numeric(){return flag1Numeric;}
    public void setFlag1Numeric(int i){flag1Numeric=i;}

    /* Consider the flagBitMask as a mask to & with flags.
   if flagBitMask bit zero is set then return true if
   the individual is in portal 2,
   if flagBitMask bit 1 is set then return true if
   the individua is in portal 4
   etc.
    */
    public boolean doesFlag1Match(int flagBitMask) {
        return (flagBitMask & getFlag1Numeric()) != 0;
    }

    protected String flag2Set = null;
    public String getFlag2Set(){return flag2Set;}
    public void setFlag2Set(String in){flag2Set=in;}

    protected int flag2Numeric = -1;
    public int getFlag2Numeric(){return flag2Numeric;}
    public void setFlag2Numeric(int i){flag2Numeric=i;}

    protected String flag3Set = null;
    public String getFlag3Set(){return flag3Set;}
    public void setFlag3Set(String in){flag3Set=in;}

    protected int flag3Numeric = -1;
    public int getFlag3Numeric(){return flag3Numeric;}
    public void setFlag3Numeric(int i){flag3Numeric=i;}

    public List<String> getKeywords() {     return keywords;    }
    public void setKeywords(List<String> keywords) {this.keywords = keywords;}
    public String getKeywordString(){
        String rv = "";
        List keywords=getKeywords();
        if (getKeywords()!=null){
            Iterator<String> it1 = getKeywords().iterator();
            TreeSet<String> keywordSet = new TreeSet<String>(new Comparator<String>() {
                public int compare( String first, String second ) {
                    if (first==null) {
                        return 1;
                    }
                    if (second==null) {
                        return -1;
                    }
                    Collator collator = Collator.getInstance();
                    return collator.compare(first,second);
                }
            });
            while( it1.hasNext() ){
                keywordSet.add(it1.next());
            }
            Iterator<String> it2 = keywordSet.iterator();
            while (it2.hasNext()) {
                rv+= it2.next();
                if( it2.hasNext())
                    rv+=", ";
            }
        }
        return rv;
    }
    public List<Keyword> getKeywordObjects() { return keywordObjects; }
    public void setKeywordObjects(List<Keyword> keywords) {this.keywordObjects = keywords;}

    public Float getSearchBoost() { return searchBoost;  }    
    public void setSearchBoost(Float boost) { searchBoost = boost; }
    
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
         "moniker",
         "vClassId"
    };


    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObj = new JSONObject(this, INCLUDED_IN_JSON);
        return jsonObj;
    }
    /**
    *
    * @param fieldName- expected to be the field name in the format
    * @return
    * @throws NoSuchMethodException
    */
   public Object getField(String fieldName) throws NoSuchMethodException{
       if( fieldName == null || fieldName.length() == 0) return null;

       if( "name".equalsIgnoreCase(fieldName) )
           return getName();
       if( "timekey".equalsIgnoreCase(fieldName) )
           return getTimekey();

       //not one of the more common ones, try reflection

       //capitalize first letter
       String methodName = "get" + fieldName.substring(0,1).toUpperCase()
           + fieldName.substring(1,fieldName.length());

       Class cls = this.getClass();
       try {
           Method meth = cls.getMethod(methodName, (Class[]) null);
           return meth.invoke(this,(Object[])null);
       } catch (Exception e) { }
       //should never get here
       throw new NoSuchMethodException("Entity.getField() attempt to use a method called "
               + methodName +"() for field " + fieldName + " but the method doesn't exist.");
   }

   public int compareTo(Individual o2) {
       Collator collator = Collator.getInstance();
       if (o2 == null) {
       	   return 1;
       } else {
       	   return collator.compare(this.getName(),o2.getName());
       }
   }

}
