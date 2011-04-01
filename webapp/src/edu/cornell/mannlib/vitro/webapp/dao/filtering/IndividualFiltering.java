/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import java.sql.Timestamp;
import java.util.ArrayList;
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
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;

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
    @Override
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
            if( filteredStmts.size() == 0 ){
                it.remove();
            }else{
                dp.setDataPropertyStatements(filteredStmts);
            }
        }
        return outdProps;
    }

    @Override
	public List<DataProperty> getPopulatedDataPropertyList() {
        List<DataProperty> dprops =  _innerIndividual.getPopulatedDataPropertyList();
        LinkedList<DataProperty> outdProps = new LinkedList<DataProperty>();
        Filter.filter(dprops,_filters.getDataPropertyFilter(), outdProps);
        return outdProps;
    }
    
    @Override
    public List<DataPropertyStatement> getDataPropertyStatements() {
        List<DataPropertyStatement> dstmts = _innerIndividual.getDataPropertyStatements();
        return filterDataPropertyStatements(dstmts);      
    }
    
    @Override
    public List<DataPropertyStatement> getDataPropertyStatements(String propertyUri) {
        List<DataPropertyStatement> dstmts = _innerIndividual.getDataPropertyStatements(propertyUri);
        return filterDataPropertyStatements(dstmts);        
    }
    
    private List<DataPropertyStatement> filterDataPropertyStatements(List<DataPropertyStatement> dStmts) {
        List<DataPropertyStatement> outDstmts = new LinkedList<DataPropertyStatement>();
        Filter.filter(dStmts,_filters.getDataPropertyStatementFilter(), outDstmts);
        return outDstmts;          
    }    
      
    @Override
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
                if( filteredStmts.size() > 0 ){
                    dp.setDataPropertyStatements(filteredStmts);
                    returnMap.put(key,dp);
                }
            }
        }            
        return returnMap;        
    }
    
    @Override
    public List<ObjectProperty> getObjectPropertyList() {
        List <ObjectProperty> oprops = _innerIndividual.getObjectPropertyList();
//        List<ObjectProperty> outOProps = new LinkedList<ObjectProperty>();
//        Filter.filter(oprops, _filters.getObjectPropertyFilter(), outOProps);
        return ObjectPropertyDaoFiltering.filterAndWrap(oprops, _filters);
    }
    
    @Override
    public List<ObjectProperty> getPopulatedObjectPropertyList() {
        List <ObjectProperty> oprops = _innerIndividual.getPopulatedObjectPropertyList();
//        List<ObjectProperty> outOProps = new LinkedList<ObjectProperty>();
//        Filter.filter(oprops, _filters.getObjectPropertyFilter(), outOProps);
        return ObjectPropertyDaoFiltering.filterAndWrap(oprops, _filters);
    }

    /* ********************* methods that need delegated filtering *************** */
    @Override
    public List<ObjectPropertyStatement> getObjectPropertyStatements() {

        List<ObjectPropertyStatement> stmts = _innerIndividual.getObjectPropertyStatements();
        return filterObjectPropertyStatements(stmts); 
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

    @Override
    public List<ObjectPropertyStatement> getObjectPropertyStatements(String propertyUri) {
        List<ObjectPropertyStatement> stmts = _innerIndividual.getObjectPropertyStatements(propertyUri);
        return filterObjectPropertyStatements(stmts);       
    }
    
    private List<ObjectPropertyStatement> filterObjectPropertyStatements(List<ObjectPropertyStatement> opStmts) {
        return ObjectPropertyStatementDaoFiltering.filterAndWrapList(opStmts, _filters); 
    }


    
    //TODO: may cause problems since one can get ObjectPropertyStatement list from
    // the ObjectProperty, and that won't be filtered.
    //might need to make a ObjectPropertyFiltering and a ObjectPropertyStatementFiltering    
    @Override
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

    @Override
    public boolean equals(Object obj) {
        return _innerIndividual.equals(obj);
    }

    @Override
    public String getAnchor() {
        return _innerIndividual.getAnchor();
    }

    @Override
    public String getBlurb() {
        return _innerIndividual.getBlurb();
    }  
    
    @Override
    public String getDescription() {
        return _innerIndividual.getDescription();
    }

    @Override
    public List<DataPropertyStatement> getExternalIds() {
        return _innerIndividual.getExternalIds();
    }

    @Override
    public Object getField(String fieldName) throws NoSuchMethodException {
        return _innerIndividual.getField(fieldName);
    }

    @Override
    public int getFlag1Numeric() {
        return _innerIndividual.getFlag1Numeric();
    }

    @Override
    public String getFlag1Set() {
        return _innerIndividual.getFlag1Set();
    }

    @Override
    public int getFlag2Numeric() {
        return _innerIndividual.getFlag2Numeric();
    }

    @Override
    public String getFlag2Set() {
        return _innerIndividual.getFlag2Set();
    }

    @Override
    public int getFlag3Numeric() {
        return _innerIndividual.getFlag3Numeric();
    }

    @Override
    public String getFlag3Set() {
        return _innerIndividual.getFlag3Set();
    }

	@Override
	public String getMainImageUri() {
		return _innerIndividual.getMainImageUri();
	}

    @Override
	public String getImageUrl() {
    	return _innerIndividual.getImageUrl();
	}


	@Override
	public String getThumbUrl() {
		return _innerIndividual.getThumbUrl();
	}

    @Override
	public List<String> getKeywords() {
        return _innerIndividual.getKeywords();
    }

    @Override
    public String getKeywordString() {
        return _innerIndividual.getKeywordString();
    }

    @Override
    public List<Link> getLinksList() {
        return _innerIndividual.getLinksList();
    }

    @Override
    public Link getPrimaryLink() {
        return _innerIndividual.getPrimaryLink();
    }

    @Override
    public String getLocalName() {
        return _innerIndividual.getLocalName();
    }

    @Override
    public Timestamp getModTime() {
        return _innerIndividual.getModTime();
    }

    @Override
    public String getMoniker() {
        return _innerIndividual.getMoniker();
    }

    @Override
    public String getName() {
        return _innerIndividual.getName();
    }

    @Override
    public String getRdfsLabel(){
    	return _innerIndividual.getRdfsLabel();
    }

    @Override
    public String getNamespace() {
        return _innerIndividual.getNamespace();
    }

    @Override
    public String getStatus() {
        return _innerIndividual.getStatus();
    }


    @Override
    public int getStatusId() {
        return _innerIndividual.getStatusId();
    }


    @Override
    public Date getSunrise() {
        return _innerIndividual.getSunrise();
    }

    @Override
    public Date getSunset() {
        return _innerIndividual.getSunset();
    }

    @Override
    public Date getTimekey() {
        return _innerIndividual.getTimekey();
    }

    @Override
    public String getURI() {
        return _innerIndividual.getURI();
    }

    @Override
    public String getUrl() {
        return _innerIndividual.getUrl();
    }

    @Override
    public VClass getVClass() {
        return _innerIndividual.getVClass();
    }

    @Override
    public String getVClassURI() {
        return _innerIndividual.getVClassURI();
    }

    @Override
    public int hashCode() {
        return _innerIndividual.hashCode();
    }

    @Override
    public void setAnchor(String in) {
        _innerIndividual.setAnchor(in);
    }

    @Override
    public void setBlurb(String in) {
        _innerIndividual.setBlurb(in);
    }

    @Override
    public void setDatatypePropertyList(List<DataProperty> datatypePropertyList) {
        _innerIndividual.setDatatypePropertyList(datatypePropertyList);
    }

    @Override
    public void setPopulatedDataPropertyList(List<DataProperty> dataPropertyList) {
        _innerIndividual.setPopulatedDataPropertyList(dataPropertyList);
    }
    
    @Override
    public void setDescription(String in) {
        _innerIndividual.setDescription(in);
    }

    @Override
    public void setObjectPropertyStatements(List<ObjectPropertyStatement> list) {
        _innerIndividual.setObjectPropertyStatements(list);
    }

    @Override
    public void setDataPropertyStatements(List<DataPropertyStatement> list) {
        _innerIndividual.setDataPropertyStatements(list);
    }

    @Override
    public void setExternalIds(List<DataPropertyStatement> externalIds) {
        _innerIndividual.setExternalIds(externalIds);
    }

    @Override
    public void setFlag1Numeric(int i) {
        _innerIndividual.setFlag1Numeric(i);
    }

    @Override
    public void setFlag1Set(String in) {
        _innerIndividual.setFlag1Set(in);
    }

    @Override
    public void setFlag2Numeric(int i) {
        _innerIndividual.setFlag2Numeric(i);
    }

    @Override
    public void setFlag2Set(String in) {
        _innerIndividual.setFlag2Set(in);
    }

    @Override
    public void setFlag3Numeric(int i) {
        _innerIndividual.setFlag3Numeric(i);
    }

    @Override
    public void setFlag3Set(String in) {
        _innerIndividual.setFlag3Set(in);
    }

    @Override
	public void setMainImageUri(String mainImageUri) {
    	_innerIndividual.setMainImageUri(mainImageUri);
	}

    @Override
    public void setKeywords(List<String> keywords) {
        _innerIndividual.setKeywords(keywords);
    }

    @Override
    public void setLinksList(List<Link> linksList) {
        _innerIndividual.setLinksList(linksList);
    }

    @Override
    public void setPrimaryLink(Link link) {
        _innerIndividual.setPrimaryLink(link);
    }

    @Override
    public void setLocalName(String localName) {
        _innerIndividual.setLocalName(localName);
    }

    @Override
    public void setModTime(Timestamp in) {
        _innerIndividual.setModTime(in);
    }

    @Override
    public void setMoniker(String in) {
        _innerIndividual.setMoniker(in);
    }

    @Override
    public void setName(String in) {
        _innerIndividual.setName(in);
    }

    @Override
    public void setNamespace(String namespace) {
        _innerIndividual.setNamespace(namespace);
    }

    @Override
    public void setPropertyList(List<ObjectProperty> propertyList) {
        _innerIndividual.setPropertyList(propertyList);
    }

    @Override
    public void setPopulatedObjectPropertyList(List<ObjectProperty> propertyList) {
        _innerIndividual.setPopulatedObjectPropertyList(propertyList);
    }
    
    @Override
    public void setStatus(String s) {
        _innerIndividual.setStatus(s);
    }

    @Override
    public void setStatusId(int in) {
        _innerIndividual.setStatusId(in);
    }

    @Override
    public void setSunrise(Date in) {
        _innerIndividual.setSunrise(in);
    }

    @Override
    public void setSunset(Date in) {
        _innerIndividual.setSunset(in);
    }

    @Override
    public void setTimekey(Date in) {
        _innerIndividual.setTimekey(in);
    }

    @Override
    public void setURI(String URI) {
        _innerIndividual.setURI(URI);
    }

    @Override
    public void setUrl(String url) {
        _innerIndividual.setUrl(url);
    }

    @Override
    public void setVClass(VClass class1) {
        _innerIndividual.setVClass(class1);
    }

    @Override
    public void setVClassURI(String in) {
        _innerIndividual.setVClassURI(in);
    }

//    public void shallowCopy(Individual target) {
//        _innerIndividual.shallowCopy(target);
//    }

    @Override
    public void sortForDisplay() {
        _innerIndividual.sortForDisplay();
    }

    @Override
    public boolean doesFlag1Match(int flagBitMask) {
        return _innerIndividual.doesFlag1Match(flagBitMask);
    }

    @Override
    public List<Keyword> getKeywordObjects() {
        return _innerIndividual.getKeywordObjects();
    }

    @Override
    public List<VClass> getVClasses() {
        return _innerIndividual.getVClasses();
    }

    @Override
    public List<VClass> getVClasses(boolean direct) {
        return _innerIndividual.getVClasses(direct);
    }

    @Override
	public boolean isVClass(String uri) {
    	return _innerIndividual.isVClass(uri);
	}

    @Override
    public boolean isMemberOfClassProhibitedFromSearch(ProhibitedFromSearch pfs) {
    	return _innerIndividual.isMemberOfClassProhibitedFromSearch(pfs);
    }
    
    @Override
	public void setDataPropertyMap(Map<String, DataProperty> propertyMap) {
        _innerIndividual.setDataPropertyMap(propertyMap);
    }

    @Override
    public void setKeywordObjects(List<Keyword> keywords) {
        _innerIndividual.setKeywordObjects(keywords);
    }

    @Override
    public void setObjectPropertyMap(Map<String, ObjectProperty> propertyMap) {
        _innerIndividual.setObjectPropertyMap(propertyMap);
    }

    @Override
    public void setVClasses(List<VClass> classList, boolean direct) {
        _innerIndividual.setVClasses(classList,direct);
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        return _innerIndividual.toJSON();
    }

    @Override
    public RoleLevel getHiddenFromDisplayBelowRoleLevel() {
        return _innerIndividual.getHiddenFromDisplayBelowRoleLevel();
    }
    
    @Override
    public void setHiddenFromDisplayBelowRoleLevel(RoleLevel eR) {
        _innerIndividual.setHiddenFromDisplayBelowRoleLevel(eR);
    }
    
    @Override
    public void setHiddenFromDisplayBelowRoleLevelUsingRoleUri(String roleUri) {
        _innerIndividual.setHiddenFromDisplayBelowRoleLevel(BaseResourceBean.RoleLevel.getRoleByUri(roleUri));
    }

    @Override
    public RoleLevel getProhibitedFromUpdateBelowRoleLevel() {
        return _innerIndividual.getProhibitedFromUpdateBelowRoleLevel();
    }
    
    @Override
    public void setProhibitedFromUpdateBelowRoleLevel(RoleLevel eR) {
        _innerIndividual.setProhibitedFromUpdateBelowRoleLevel(eR);
    }
    
    @Override
    public void setProhibitedFromUpdateBelowRoleLevelUsingRoleUri(String roleUri) {
        _innerIndividual.setProhibitedFromUpdateBelowRoleLevel(BaseResourceBean.RoleLevel.getRoleByUri(roleUri));
    }

    @Override
    public boolean isAnonymous() {
        return _innerIndividual.isAnonymous();
    }
    
    @Override
    public int compareTo( Individual ind2 ) {
    	return _innerIndividual.compareTo( ind2 );
    }

    @Override
    public String toString() {
        return _innerIndividual.toString();  
    }

    @Override
    public void setSearchBoost(Float boost) { _innerIndividual.setSearchBoost( boost ); }
    @Override
    public Float getSearchBoost() {return _innerIndividual.getSearchBoost(); }

    @Override
    public List<String> getDataValues(String propertyUri) {
        List<DataPropertyStatement> stmts = getDataPropertyStatements(propertyUri);
        // Since the statements have been filtered, we can just take the data values without filtering.
        List<String> dataValues = new ArrayList<String>(stmts.size());
        for (DataPropertyStatement stmt : stmts) {
            dataValues.add(stmt.getData());
        }
        return dataValues;      
    }
    
    @Override
    public String getDataValue(String propertyUri) {
        List<DataPropertyStatement> stmts = getDataPropertyStatements(propertyUri);
        // Since the statements have been filtered, we can just take the first data value without filtering.
        return stmts.isEmpty() ? null : stmts.get(0).getData();
    }
    
    @Override
    public DataPropertyStatement getDataPropertyStatement(String propertyUri) {
        List<DataPropertyStatement> stmts = getDataPropertyStatements(propertyUri);
        // Since the statements have been filtered, we can just take the first data value without filtering.
        return stmts.isEmpty() ? null : stmts.get(0);       
    }
    
    @Override
    public List<Individual> getRelatedIndividuals(String propertyUri) {
        List<ObjectPropertyStatement> stmts = getObjectPropertyStatements(propertyUri);
        // Since the statements have been filtered, we can just take the individuals without filtering.
        List<Individual> relatedIndividuals = new ArrayList<Individual>(stmts.size());
        for (ObjectPropertyStatement stmt : stmts) {
            relatedIndividuals.add(stmt.getObject());
        }
        return relatedIndividuals; 
    }
    
    @Override
    public Individual getRelatedIndividual(String propertyUri) {
        List<ObjectPropertyStatement> stmts = getObjectPropertyStatements(propertyUri); 
        // Since the statements have been filtered, we can just take the first individual without filtering.
        return stmts.isEmpty() ? null : stmts.get(0).getObject();
    }

    @Override
    public boolean hasThumb() {
        return _innerIndividual.hasThumb();
    }

	@Override
	public Individual getBaseIndividual() {
		return _innerIndividual.getBaseIndividual();
	}
}