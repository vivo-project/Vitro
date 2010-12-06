/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import edu.cornell.mannlib.vitro.webapp.beans.*;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;
import net.sf.jga.fn.UnaryFunctor;

import java.util.Collection;
import java.util.List;

class DataPropertyStatementDaoFiltering extends BaseFiltering implements DataPropertyStatementDao{
    final DataPropertyStatementDao innerDataPropertyStatementDao;    
    final VitroFilters filters;

    public DataPropertyStatementDaoFiltering(
            DataPropertyStatementDao dataPropertyStatementDao,
            VitroFilters filters) {
        super();
        this.innerDataPropertyStatementDao = dataPropertyStatementDao;
        this.filters = filters;
    }


    public void deleteDataPropertyStatement(DataPropertyStatement dataPropertyStatement) {
        innerDataPropertyStatementDao.deleteDataPropertyStatement(dataPropertyStatement);
    }


    public Individual fillExistingDataPropertyStatementsForIndividual(
            Individual individual/*,
            boolean allowAnyNameSpace*/) {
        if( individual == null ) return null;
        Individual ind = innerDataPropertyStatementDao.fillExistingDataPropertyStatementsForIndividual(individual/*,allowAnyNameSpace*/);
        if( ind == null ) return null;

        List<DataPropertyStatement> dprops = ind.getDataPropertyStatements();
        if( dprops != null ){
            ind.setDataPropertyStatements(filter(dprops, filters.getDataPropertyStatementFilter()));
        }
        return ind;
    }

    public void deleteDataPropertyStatementsForIndividualByDataProperty(Individual individual, DataProperty dataProperty) {
        innerDataPropertyStatementDao.deleteDataPropertyStatementsForIndividualByDataProperty(individual, dataProperty);
    }

    public void deleteDataPropertyStatementsForIndividualByDataProperty(String individualURI, String dataPropertyURI) {
        innerDataPropertyStatementDao.deleteDataPropertyStatementsForIndividualByDataProperty(individualURI, dataPropertyURI);
    }

    public Collection<DataPropertyStatement> getDataPropertyStatementsForIndividualByDataPropertyURI(
            Individual entity, String datapropURI) {
        Collection<DataPropertyStatement> col =
            innerDataPropertyStatementDao
                .getDataPropertyStatementsForIndividualByDataPropertyURI(entity, datapropURI);
        if( col != null ){
            return filter(col,filters.getDataPropertyStatementFilter());
        }else{
            return null;
        }
    }

    @Deprecated
    public List getExistingQualifiers(String dataPropertyURI) {
        return innerDataPropertyStatementDao.getExistingQualifiers(dataPropertyURI);
    }
    
    public List<DataPropertyStatement> getDataPropertyStatements(DataProperty dataProperty) {
    	List<DataPropertyStatement> dps = innerDataPropertyStatementDao.getDataPropertyStatements(dataProperty);
    	if (dps != null) {
    		return filter(dps,filters.getDataPropertyStatementFilter());
    	} else {
    		return dps;
    	}
    }
    
    public List<DataPropertyStatement> getDataPropertyStatements(DataProperty dataProperty, int startIndex, int endIndex) {
    	List<DataPropertyStatement> dps = innerDataPropertyStatementDao.getDataPropertyStatements(dataProperty, startIndex, endIndex);
    	if (dps != null) {
    		return filter(dps,filters.getDataPropertyStatementFilter());
    	} else {
    		return dps;
    	}
    }

    public int insertNewDataPropertyStatement(
            DataPropertyStatement dataPropertyStatement) {
        return innerDataPropertyStatementDao.insertNewDataPropertyStatement(dataPropertyStatement);
    }
    
    @Override
    // RY What about filtering?
    public List<DataPropertyStatement> getDataPropertyStatementsForIndividualByProperty(Individual subject, DataProperty property) {
        return innerDataPropertyStatementDao.getDataPropertyStatementsForIndividualByProperty(subject, property);
    }

}