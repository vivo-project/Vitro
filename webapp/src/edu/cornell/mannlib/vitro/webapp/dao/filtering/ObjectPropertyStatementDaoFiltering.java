/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jga.algorithms.Filter;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
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
        return innerObjectPropertyStatementDao.getObjectPropertyStatementsForIndividualByProperty(subjectUri, propertyUri, objectKey, query);
    }
    
    @Override
    public List<Map<String, String>> getObjectPropertyStatementsForIndividualByProperty(
            String subjectUri, String propertyUri, String objectKey, String query, Set<String> queryStrings) {
        return innerObjectPropertyStatementDao.getObjectPropertyStatementsForIndividualByProperty(subjectUri, propertyUri, objectKey, query, queryStrings);
    }

}