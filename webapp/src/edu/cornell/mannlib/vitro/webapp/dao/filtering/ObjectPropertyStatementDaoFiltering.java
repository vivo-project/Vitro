/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import java.util.ArrayList;
import java.util.List;

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
    
    public Individual fillExistingObjectPropertyStatements(Individual entity) {
        Individual ind = innerObjectPropertyStatementDao.fillExistingObjectPropertyStatements(entity);
        if( ind == null ) 
            return null;
        else{    
            ind.setObjectPropertyStatements( filterAndWrapList( ind.getObjectPropertyStatements(), filters) );       
            return ind;
        }
    }

    public List<ObjectPropertyStatement> getObjectPropertyStatements(ObjectProperty objectProperty) {
    	return filterAndWrapList( innerObjectPropertyStatementDao.getObjectPropertyStatements(objectProperty), filters );
    }
    
    public List<ObjectPropertyStatement> getObjectPropertyStatements(ObjectProperty objectProperty, int startIndex, int endIndex) {
    	return filterAndWrapList( innerObjectPropertyStatementDao.getObjectPropertyStatements(objectProperty, startIndex, endIndex) ,filters);    	
    }
    
    public int insertNewObjectPropertyStatement(ObjectPropertyStatement objPropertyStmt) {
        return innerObjectPropertyStatementDao.insertNewObjectPropertyStatement(objPropertyStmt);
    }

}