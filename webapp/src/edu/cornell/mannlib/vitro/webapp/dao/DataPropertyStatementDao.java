/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;

/**
 * Created by IntelliJ IDEA.
 * User: bdc34
 * Date: Apr 18, 2007
 * Time: 7:03:58 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DataPropertyStatementDao {

    void deleteDataPropertyStatement(DataPropertyStatement dataPropertyStmt);

    @Deprecated
    List getExistingQualifiers(String dataPropertyURI);
    
    List<DataPropertyStatement> getDataPropertyStatements(DataProperty dataProperty);
    
    List<DataPropertyStatement> getDataPropertyStatements(DataProperty dataProperty, int startIndex, int endIndex);

    @SuppressWarnings("unchecked")
    Collection<DataPropertyStatement> getDataPropertyStatementsForIndividualByDataPropertyURI(Individual entity,String datapropURI);

    Individual fillExistingDataPropertyStatementsForIndividual(Individual individual/*, boolean allowAnyNameSpace*/);

    void deleteDataPropertyStatementsForIndividualByDataProperty(String individualURI, String dataPropertyURI);

    void deleteDataPropertyStatementsForIndividualByDataProperty(Individual individual, DataProperty dataProperty);

    int insertNewDataPropertyStatement(DataPropertyStatement dataPropertyStatement );

    List<Literal> getDataPropertyValuesForIndividualByProperty(Individual subject, DataProperty property);
    
    List<Literal> getDataPropertyValuesForIndividualByProperty(String subjectUri, String propertyUri);

}

