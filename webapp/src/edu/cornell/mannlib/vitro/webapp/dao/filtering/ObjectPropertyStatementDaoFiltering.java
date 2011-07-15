/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jga.algorithms.Filter;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;

class ObjectPropertyStatementDaoFiltering extends BaseFiltering implements ObjectPropertyStatementDao{
    final ObjectPropertyStatementDao innerObjectPropertyStatementDao;
    final VitroFilters filters;


    public ObjectPropertyStatementDaoFiltering(
            ObjectPropertyStatementDao objectPropertyStatementDao,
            VitroFilters filters) {
        super();
        this.innerObjectPropertyStatementDao = objectPropertyStatementDao;
        this.filters = filters;
    }

    
    @Override
    public void deleteObjectPropertyStatement(ObjectPropertyStatement objPropertyStmt) {
        innerObjectPropertyStatementDao.deleteObjectPropertyStatement(objPropertyStmt);
    }


    protected static List<ObjectPropertyStatement> filterAndWrapList(List<ObjectPropertyStatement> list, VitroFilters filters){        
        if( ( list ) != null ){                        
            
            ArrayList<ObjectPropertyStatement> ctemp = new ArrayList<ObjectPropertyStatement>();
            Filter.filter(list,filters.getObjectPropertyStatementFilter(),ctemp);
                        
            List<ObjectPropertyStatement> cout= new ArrayList<ObjectPropertyStatement>(list.size());
            for( ObjectPropertyStatement stmt: ctemp){
                cout.add( new ObjectPropertyStatementFiltering(stmt,filters) );
            }
            return cout;
        }else{
            return null;
        }
    }
    
    @Override
    public Individual fillExistingObjectPropertyStatements(Individual entity) {
        Individual ind = innerObjectPropertyStatementDao.fillExistingObjectPropertyStatements(entity);
        if( ind == null ) 
            return null;
        else{    
            ind.setObjectPropertyStatements( filterAndWrapList( ind.getObjectPropertyStatements(), filters) );       
            return ind;
        }
    }

    @Override
    public List<ObjectPropertyStatement> getObjectPropertyStatements(ObjectProperty objectProperty) {
    	return filterAndWrapList( innerObjectPropertyStatementDao.getObjectPropertyStatements(objectProperty), filters );
    }
    
    @Override
    public List<ObjectPropertyStatement> getObjectPropertyStatements(ObjectProperty objectProperty, int startIndex, int endIndex) {
    	return filterAndWrapList( innerObjectPropertyStatementDao.getObjectPropertyStatements(objectProperty, startIndex, endIndex) ,filters);    	
    }
    
    @Override
	public List<ObjectPropertyStatement> getObjectPropertyStatements(
			ObjectPropertyStatement objPropertyStmt) {
    	return filterAndWrapList(innerObjectPropertyStatementDao.getObjectPropertyStatements(objPropertyStmt), filters);
	}

    @Override
	public int insertNewObjectPropertyStatement(ObjectPropertyStatement objPropertyStmt) {
        return innerObjectPropertyStatementDao.insertNewObjectPropertyStatement(objPropertyStmt);
    }

    @Override
    public List<Map<String, String>> getObjectPropertyStatementsForIndividualByProperty(
            String subjectUri, String propertyUri, String objectKey, String query) {
        return getObjectPropertyStatementsForIndividualByProperty(subjectUri, propertyUri, objectKey, query, null);
    }
    
    @Override
    public List<Map<String, String>> getObjectPropertyStatementsForIndividualByProperty(
            String subjectUri, String propertyUri, String objectKey, String query, Set<String> queryStrings) {
        
        List<Map<String, String>> data = innerObjectPropertyStatementDao.
                                            getObjectPropertyStatementsForIndividualByProperty(subjectUri, propertyUri, objectKey, query, queryStrings);
        
        /* Filter the data
         * 
         * Filtering is applied to a list of ObjectPropertyStatements. Create these statements, mapped
         * to the item in data that they are built from, apply filtering to the statements, then get
         * the associated data out of the original list. Use a LinkedHashMap to preserve the ordering.
         */
        Map<ObjectPropertyStatement, Map<String, String>> stmtsToData = 
            new LinkedHashMap<ObjectPropertyStatement, Map<String, String>>(data.size());

        for (Map<String, String> map : data) {
            String objectUri = map.get(objectKey);
            ObjectPropertyStatement statement = new ObjectPropertyStatementImpl(subjectUri, propertyUri, objectUri);
            stmtsToData.put(statement, map);
        }
        
        List<ObjectPropertyStatement> stmtList = new ArrayList<ObjectPropertyStatement>(stmtsToData.keySet());
        
        // Apply the filters to the list of statements
        List<ObjectPropertyStatement> filteredStatements = filterAndWrapList(stmtList, filters);     
        
        // Get the data associated with the filtered statements out of the map
        List<Map<String, String>> filteredData = new ArrayList<Map<String, String>>(filteredStatements.size());
        for (ObjectPropertyStatement ops : filteredStatements) {        
            if (ops instanceof ObjectPropertyStatementFiltering) {
                ops = ((ObjectPropertyStatementFiltering)ops).innerStmt;
            } 
            filteredData.add(stmtsToData.get(ops));
        }       
        
        // Return the filtered list of data
        return filteredData;

    }    

    @Override 
    public Map<String, String> getMostSpecificTypesInClassgroupsForIndividual(String subjectUri) {
        return innerObjectPropertyStatementDao.getMostSpecificTypesInClassgroupsForIndividual(subjectUri);
    }
    

}