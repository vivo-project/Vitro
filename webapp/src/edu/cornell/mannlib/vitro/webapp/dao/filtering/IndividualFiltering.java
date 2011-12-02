/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import static edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants.SOME_LITERAL;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.sf.jga.algorithms.Filter;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
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
		// I'd rather filter on the actual DataPropertyStatements here, but
		// Individual.getPopulatedDataPropertyList doesn't actually populate
		// the DataProperty with statements. - jblake
        List<DataProperty> outdProps = new ArrayList<DataProperty>();
        List<DataProperty> dprops = _innerIndividual.getPopulatedDataPropertyList();
		for (DataProperty dp: dprops) {
			if (_filters.getDataPropertyStatementFilter().fn(
					new DataPropertyStatementImpl(this._innerIndividual.getURI(), dp.getURI(), SOME_LITERAL))) {
				outdProps.add(dp);
			}
        }
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
		// I'd rather filter on the actual ObjectPropertyStatements here, but
		// Individual.getPopulatedObjectPropertyList doesn't actually populate
		// the ObjectProperty with statements. - jblake
        List<ObjectProperty> outOProps = new ArrayList<ObjectProperty>();
        List<ObjectProperty> oProps = _innerIndividual.getPopulatedObjectPropertyList();
		for (ObjectProperty op: oProps) {
			if (_filters.getObjectPropertyStatementFilter().fn(
					new ObjectPropertyStatementImpl(this._innerIndividual.getURI(), op.getURI(), SOME_LITERAL))) {
				outOProps.add(op);
			}
        }
        return outOProps;
    }

    /* ********************* methods that need delegated filtering *************** */
    @Override
    public List<ObjectPropertyStatement> getObjectPropertyStatements() {
        return filterObjectPropertyStatements(_innerIndividual.getObjectPropertyStatements()); 
    }

    @Override
    public List<ObjectPropertyStatement> getObjectPropertyStatements(String propertyUri) {
        return filterObjectPropertyStatements(_innerIndividual.getObjectPropertyStatements(propertyUri));       
    }
    
	private List<ObjectPropertyStatement> filterObjectPropertyStatements(List<ObjectPropertyStatement> opStmts) {
		if (opStmts == null) {
			return Collections.emptyList();
		}
		ArrayList<ObjectPropertyStatement> filtered = new ArrayList<ObjectPropertyStatement>();
		Filter.filter(opStmts, _filters.getObjectPropertyStatementFilter(),	filtered);
		return filtered;
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
    public List<DataPropertyStatement> getExternalIds() {
        return _innerIndividual.getExternalIds();
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
    public String getLocalName() {
        return _innerIndividual.getLocalName();
    }

    @Override
    public Timestamp getModTime() {
        return _innerIndividual.getModTime();
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
    public String getURI() {
        return _innerIndividual.getURI();
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
    public void setDatatypePropertyList(List<DataProperty> datatypePropertyList) {
        _innerIndividual.setDatatypePropertyList(datatypePropertyList);
    }

    @Override
    public void setPopulatedDataPropertyList(List<DataProperty> dataPropertyList) {
        _innerIndividual.setPopulatedDataPropertyList(dataPropertyList);
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
	public void setMainImageUri(String mainImageUri) {
    	_innerIndividual.setMainImageUri(mainImageUri);
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
    public void setURI(String URI) {
        _innerIndividual.setURI(URI);
    }

    @Override
    public void setVClass(VClass class1) {
        _innerIndividual.setVClass(class1);
    }

    @Override
    public void setVClassURI(String in) {
        _innerIndividual.setVClassURI(in);
    }

    @Override
    public void sortForDisplay() {
        _innerIndividual.sortForDisplay();
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
    public void setSearchSnippet(String snippet) { _innerIndividual.setSearchSnippet( snippet ); }
    @Override
    public String getSearchSnippet() {return _innerIndividual.getSearchSnippet(); }

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
}