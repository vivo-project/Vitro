/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;

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
    public List<Literal> getDataPropertyValuesForIndividualByProperty(Individual subject, DataProperty property) {
        return getDataPropertyValuesForIndividualByProperty(subject.getURI(), property.getURI());
    }

    @Override
    public List<Literal> getDataPropertyValuesForIndividualByProperty(String subjectUri, String propertyUri) {
        List<Literal> literals = innerDataPropertyStatementDao.getDataPropertyValuesForIndividualByProperty(subjectUri, propertyUri);        
        /* Filter the data
         * 
         * Filtering is applied to a list of DataPropertyStatement. Create these statements, mapped
         * to the literal that they are built from, apply filtering to the statements, then get
         * the associated literals out of the original list. Use a LinkedHashMap to preserve the ordering.
         */
        Map<DataPropertyStatement, Literal> stmtsToLiterals = 
            new LinkedHashMap<DataPropertyStatement, Literal>(literals.size());

        for (Literal literal : literals) {
            String value = literal.getLexicalForm();
            DataPropertyStatement statement = new DataPropertyStatementImpl(subjectUri, propertyUri, value);
            statement.setDatatypeURI(literal.getDatatypeURI());
            statement.setLanguage(literal.getLanguage());
            stmtsToLiterals.put(statement, literal);
        }
        
        List<DataPropertyStatement> stmtList = new ArrayList<DataPropertyStatement>(stmtsToLiterals.keySet());
        
        // Apply the filters to the list of statements
        List<DataPropertyStatement> filteredStatements = filter(stmtList, filters.getDataPropertyStatementFilter());
        
        // Get the literals associated with the filtered statements out of the original list
        List<Literal> filteredLiterals = new ArrayList<Literal>(filteredStatements.size());
        for (DataPropertyStatement dps : filteredStatements) {
            if (dps instanceof DataPropertyStatementFiltering) {
                dps = ((DataPropertyStatementFiltering)dps).innerStmt;
            }
            filteredLiterals.add(stmtsToLiterals.get(dps));
        }       
        
        // Return the filtered list of literals
        return filteredLiterals;
        
    }
    
}