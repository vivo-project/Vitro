/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;

import java.util.List;
import java.util.Collection;

public interface DataPropertyDao extends PropertyDao {

    public abstract List<DataProperty> getAllDataProperties();

    public abstract List<DataProperty> getAllExternalIdDataProperties();

    void fillDataPropertiesForIndividual(Individual individual);

    List<DataProperty> getDataPropertiesForVClass(String vClassURI);
    
    Collection<DataProperty> getAllPossibleDatapropsForIndividual(String individualURI);

    String getRequiredDatatypeURI(Individual individual, DataProperty dataProperty);
    
    DataProperty getDataPropertyByURI(String dataPropertyURI);

    String insertDataProperty(DataProperty dataProperty) throws InsertException;

    void updateDataProperty(DataProperty dataProperty);

    void deleteDataProperty(DataProperty dataProperty);

    void deleteDataProperty(String dataPropertyURI);

    List<DataProperty> getRootDataProperties();
    
    boolean annotateDataPropertyAsExternalIdentifier(String dataPropertyURI);
    
    public List<DataProperty> getDataPropertyList(Individual subject);
    
    public List<DataProperty> getDataPropertyList(String subjectUri);
}