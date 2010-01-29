package edu.cornell.mannlib.vitro.webapp.dao.filtering;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.sf.jga.algorithms.Filter;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Keyword;
import edu.cornell.mannlib.vitro.webapp.beans.Link;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;

/**
 * A Individual object that will delegate to an inner Individual
 * and then filter the results.  It also uses the specified
 * WebappDaoFactory to get filtered Statements.
 *
 * @author bdc34
 *
 */
public class IndividualFiltering implements Individual {
    private final Individual _innerIndividual;
    private final VitroFilters _filters;

    public IndividualFiltering(Individual individual, VitroFilters filters) {
        super();
        this._innerIndividual = individual;
        this._filters = filters;
    }


    /* ******************** methods that need filtering ***************** */
    public List<DataProperty> getDataPropertyList() {
        List<DataProperty> dprops =  _innerIndividual.getDataPropertyList();
        LinkedList<DataProperty> outdProps = new LinkedList<DataProperty>();
        Filter.filter(dprops,_filters.getDataPropertyFilter(), outdProps);
        
        ListIterator<DataProperty> it = outdProps.listIterator();
        while(it.hasNext()){
            DataProperty dp = it.next();
            List<DataPropertyStatement> filteredStmts = 
                new LinkedList<DataPropertyStatement>();
            Filter.filter(dp.getDataPropertyStatements(),
                    _filters.getDataPropertyStatementFilter(),filteredStmts);
            if( filteredStmts == null || filteredStmts.size() == 0 ){
                it.remove();
            }else{
                dp.setDataPropertyStatements(filteredStmts);
            }
        }
        return outdProps;
    }


    public List<DataPropertyStatement> getDataPropertyStatements() {
        List<DataPropertyStatement> dstmts = _innerIndividual.getDataPropertyStatements();
        List<DataPropertyStatement> outDstmts = new LinkedList<DataPropertyStatement>();
        Filter.filter(dstmts,_filters.getDataPropertyStatementFilter(), outDstmts);
        return outDstmts;
    }
      
    public Map<String, DataProperty> getDataPropertyMap() {
        Map<String,DataProperty> innerMap = _innerIndividual.getDataPropertyMap();
        if( innerMap == null )
            return null;
                
        Map<String,DataProperty> returnMap = new HashMap<String,DataProperty>();
        for( String key : innerMap.keySet() ){
            DataProperty dp = innerMap.get(key);
            if( _filters.getDataPropertyFilter().fn(dp) ){                
                List<DataPropertyStatement> filteredStmts = 
                    new LinkedList<DataPropertyStatement>();
                Filter.filter(dp.getDataPropertyStatements(),
                        _filters.getDataPropertyStatementFilter(),filteredStmts);
                if( filteredStmts != null && filteredStmts.size() > 0 ){
                    dp.setDataPropertyStatements(filteredStmts);
                    returnMap.put(key,dp);
                }
            }
        }            
        return returnMap;        
    }
    
    public List<ObjectProperty> getObjectPropertyList() {
        List <ObjectProperty> oprops = _innerIndividual.getObjectPropertyList();
//        List<ObjectProperty> outOProps = new LinkedList<ObjectProperty>();
//        Filter.filter(oprops, _filters.getObjectPropertyFilter(), outOProps);
        return ObjectPropertyDaoFiltering.filterAndWrap(oprops, _filters);
    }

    /* ********************* methods that need delegated filtering *************** */
    public List<ObjectPropertyStatement> getObjectPropertyStatements() {

        List<ObjectPropertyStatement> stmts = _innerIndividual.getObjectPropertyStatements();
        return ObjectPropertyStatementDaoFiltering.filterAndWrapList(stmts, _filters);
//
//         //filter ObjectPropertyStatements from inner
//        List<ObjectPropertyStatement> filteredStmts = new LinkedList<ObjectPropertyStatement>();
//        Filter.filter(stmts, _filters.getObjectPropertyStatementFilter(), filteredStmts);
//
//        //filter ObjectPropertyStatement based the related entity/individual
//        ListIterator<ObjectPropertyStatement> stmtIt = filteredStmts.listIterator();
//        while( stmtIt.hasNext() ){
//            ObjectPropertyStatement ostmt = stmtIt.next();
//            if( ostmt != null ){
//                stmtIt.remove();
//                continue;
//            } else if( ostmt.getObject() == null ){
//                continue;
//            } else if( _filters.getIndividualFilter().fn( ostmt.getObject() )){
//                    ostmt.setObject( new IndividualFiltering((Individual) ostmt.getObject(), _filters) );
//            }else{
//                    stmtIt.remove();
//            }
//        }
//        return stmts;
    }

    //TODO: may cause problems since one can get ObjectPropertyStatement list from
    // the ObjectProperty, and that won't be filtered.
    //might need to make a ObjectPropertyFiltering and a ObjectPropertyStatementFiltering    
    public Map<String, ObjectProperty> getObjectPropertyMap() {
        Map<String,ObjectProperty> innerMap = _innerIndividual.getObjectPropertyMap();
        if( innerMap == null )
            return null;
        
        
        Map<String,ObjectProperty> returnMap = new HashMap<String,ObjectProperty>();        
        for( String key : innerMap.keySet() ){
            ObjectProperty innerProp = innerMap.get(key);
            if( innerProp != null && _filters.getObjectPropertyFilter().fn( innerProp ) )
                returnMap.put(key, new ObjectPropertyFiltering(innerProp,_filters));                
        }        
        
//        Map<String,ObjectProperty> returnMap = new HashMap<String,ObjectProperty>(innerMap);
//        for( String key : returnMap.keySet() ){
//            ObjectProperty op = returnMap.get(key);
//            if( ! _filters.getObjectPropertyFilter().fn(op) )
//                returnMap.remove(key);
//        }
            
        return returnMap;
    }
    /* ************** methods that don't need filtering *********** */

    public boolean equals(Object obj) {
        return _innerIndividual.equals(obj);
    }

    public String getAnchor() {
        return _innerIndividual.getAnchor();
    }


    public String getBlurb() {
        return _innerIndividual.getBlurb();
    }

    public String getCitation() {
        return _innerIndividual.getCitation();
    }

    public String getDescription() {
        return _innerIndividual.getDescription();
    }


    public List<DataPropertyStatement> getExternalIds() {
        return _innerIndividual.getExternalIds();
    }


    public Object getField(String fieldName) throws NoSuchMethodException {
        return _innerIndividual.getField(fieldName);
    }


    public int getFlag1Numeric() {
        return _innerIndividual.getFlag1Numeric();
    }


    public String getFlag1Set() {
        return _innerIndividual.getFlag1Set();
    }


    public int getFlag2Numeric() {
        return _innerIndividual.getFlag2Numeric();
    }


    public String getFlag2Set() {
        return _innerIndividual.getFlag2Set();
    }


    public int getFlag3Numeric() {
        return _innerIndividual.getFlag3Numeric();
    }


    public String getFlag3Set() {
        return _innerIndividual.getFlag3Set();
    }


    public String getImageFile() {
        return _innerIndividual.getImageFile();
    }


    public String getImageThumb() {
        return _innerIndividual.getImageThumb();
    }


    public List<String> getKeywords() {
        return _innerIndividual.getKeywords();
    }


    public String getKeywordString() {
        return _innerIndividual.getKeywordString();
    }


    public List<Link> getLinksList() {
        return _innerIndividual.getLinksList();
    }

    public Link getPrimaryLink() {
        return _innerIndividual.getPrimaryLink();
    }

    public String getLocalName() {
        return _innerIndividual.getLocalName();
    }


    public Timestamp getModTime() {
        return _innerIndividual.getModTime();
    }


    public String getMoniker() {
        return _innerIndividual.getMoniker();
    }


    public String getName() {
        return _innerIndividual.getName();
    }


    public String getNamespace() {
        return _innerIndividual.getNamespace();
    }

    public String getStatus() {
        return _innerIndividual.getStatus();
    }


    public int getStatusId() {
        return _innerIndividual.getStatusId();
    }


    public Date getSunrise() {
        return _innerIndividual.getSunrise();
    }


    public Date getSunset() {
        return _innerIndividual.getSunset();
    }


    public Date getTimekey() {
        return _innerIndividual.getTimekey();
    }

    public String getURI() {
        return _innerIndividual.getURI();
    }

    public String getUrl() {
        return _innerIndividual.getUrl();
    }

    public VClass getVClass() {
        return _innerIndividual.getVClass();
    }


    public String getVClassURI() {
        return _innerIndividual.getVClassURI();
    }


    public int hashCode() {
        return _innerIndividual.hashCode();
    }


    public void setAnchor(String in) {
        _innerIndividual.setAnchor(in);
    }

    public void setBlurb(String in) {
        _innerIndividual.setBlurb(in);
    }

    public void setCitation(String in) {
        _innerIndividual.setCitation(in);
    }

    public void setDatatypePropertyList(List<DataProperty> datatypePropertyList) {
        _innerIndividual.setDatatypePropertyList(datatypePropertyList);
    }

    public void setDescription(String in) {
        _innerIndividual.setDescription(in);
    }

    public void setObjectPropertyStatements(List<ObjectPropertyStatement> list) {
        _innerIndividual.setObjectPropertyStatements(list);
    }

    public void setDataPropertyStatements(List<DataPropertyStatement> list) {
        _innerIndividual.setDataPropertyStatements(list);
    }


    public void setExternalIds(List<DataPropertyStatement> externalIds) {
        _innerIndividual.setExternalIds(externalIds);
    }

    public void setFlag1Numeric(int i) {
        _innerIndividual.setFlag1Numeric(i);
    }


    public void setFlag1Set(String in) {
        _innerIndividual.setFlag1Set(in);
    }


    public void setFlag2Numeric(int i) {
        _innerIndividual.setFlag2Numeric(i);
    }


    public void setFlag2Set(String in) {
        _innerIndividual.setFlag2Set(in);
    }


    public void setFlag3Numeric(int i) {
        _innerIndividual.setFlag3Numeric(i);
    }


    public void setFlag3Set(String in) {
        _innerIndividual.setFlag3Set(in);
    }


    public void setImageFile(String imageFile) {
        _innerIndividual.setImageFile(imageFile);
    }


    public void setImageThumb(String imageThumb) {
        _innerIndividual.setImageThumb(imageThumb);
    }


    public void setKeywords(List<String> keywords) {
        _innerIndividual.setKeywords(keywords);
    }


    public void setLinksList(List<Link> linksList) {
        _innerIndividual.setLinksList(linksList);
    }

    public void setPrimaryLink(Link link) {
        _innerIndividual.setPrimaryLink(link);
    }

    public void setLocalName(String localName) {
        _innerIndividual.setLocalName(localName);
    }


    public void setModTime(Timestamp in) {
        _innerIndividual.setModTime(in);
    }


    public void setMoniker(String in) {
        _innerIndividual.setMoniker(in);
    }


    public void setName(String in) {
        _innerIndividual.setName(in);
    }

    public void setNamespace(String namespace) {
        _innerIndividual.setNamespace(namespace);
    }

    public void setPropertyList(List<ObjectProperty> propertyList) {
        _innerIndividual.setPropertyList(propertyList);
    }

    public void setStatus(String s) {
        _innerIndividual.setStatus(s);
    }

    public void setStatusId(int in) {
        _innerIndividual.setStatusId(in);
    }

    public void setSunrise(Date in) {
        _innerIndividual.setSunrise(in);
    }

    public void setSunset(Date in) {
        _innerIndividual.setSunset(in);
    }

    public void setTimekey(Date in) {
        _innerIndividual.setTimekey(in);
    }

    public void setURI(String URI) {
        _innerIndividual.setURI(URI);
    }

    public void setUrl(String url) {
        _innerIndividual.setUrl(url);
    }

    public void setVClass(VClass class1) {
        _innerIndividual.setVClass(class1);
    }

    public void setVClassURI(String in) {
        _innerIndividual.setVClassURI(in);
    }

//    public void shallowCopy(Individual target) {
//        _innerIndividual.shallowCopy(target);
//    }

    public void sortForDisplay() {
        _innerIndividual.sortForDisplay();
    }

    public boolean doesFlag1Match(int flagBitMask) {
        return _innerIndividual.doesFlag1Match(flagBitMask);
    }


    public List<Keyword> getKeywordObjects() {
        return _innerIndividual.getKeywordObjects();
    }


    public List<VClass> getVClasses() {
        return _innerIndividual.getVClasses();
    }

    public List<VClass> getVClasses(boolean direct) {
        return _innerIndividual.getVClasses(direct);
    }

    public void setDataPropertyMap(Map<String, DataProperty> propertyMap) {
        _innerIndividual.setDataPropertyMap(propertyMap);
    }


    public void setKeywordObjects(List<Keyword> keywords) {
        _innerIndividual.setKeywordObjects(keywords);
    }


    public void setObjectPropertyMap(Map<String, ObjectProperty> propertyMap) {
        _innerIndividual.setObjectPropertyMap(propertyMap);
    }


    public void setVClasses(List<VClass> classList, boolean direct) {
        _innerIndividual.setVClasses(classList,direct);
    }


    public JSONObject toJSON() throws JSONException {
        return _innerIndividual.toJSON();
    }


    public RoleLevel getHiddenFromDisplayBelowRoleLevel() {
        return _innerIndividual.getHiddenFromDisplayBelowRoleLevel();
    }
    
    public void setHiddenFromDisplayBelowRoleLevel(RoleLevel eR) {
        _innerIndividual.setHiddenFromDisplayBelowRoleLevel(eR);
    }
    
    public void setHiddenFromDisplayBelowRoleLevelUsingRoleUri(String roleUri) {
        _innerIndividual.setHiddenFromDisplayBelowRoleLevel(BaseResourceBean.RoleLevel.getRoleByUri(roleUri));
    }

    public RoleLevel getProhibitedFromUpdateBelowRoleLevel() {
        return _innerIndividual.getProhibitedFromUpdateBelowRoleLevel();
    }
    
    public void setProhibitedFromUpdateBelowRoleLevel(RoleLevel eR) {
        _innerIndividual.setProhibitedFromUpdateBelowRoleLevel(eR);
    }
    
    public void setProhibitedFromUpdateBelowRoleLevelUsingRoleUri(String roleUri) {
        _innerIndividual.setProhibitedFromUpdateBelowRoleLevel(BaseResourceBean.RoleLevel.getRoleByUri(roleUri));
    }


    public boolean isAnonymous() {
        return _innerIndividual.isAnonymous();
    }
    
    public int compareTo( Individual ind2 ) {
    	return _innerIndividual.compareTo( ind2 );
    }

    public String toString() {
        return _innerIndividual.toString();  
    }

    public void setSearchBoost(Float boost) { _innerIndividual.setSearchBoost( boost ); }
    public Float getSearchBoost() {return _innerIndividual.getSearchBoost(); }
      
}