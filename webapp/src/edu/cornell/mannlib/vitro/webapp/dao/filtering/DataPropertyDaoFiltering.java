/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import static edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction.SOME_LITERAL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;

class DataPropertyDaoFiltering extends BaseFiltering implements DataPropertyDao{
    final DataPropertyDao innerDataPropertyDao;
    final VitroFilters filters;

    public DataPropertyDaoFiltering(DataPropertyDao dataPropertyDao,
            VitroFilters filters) {
        super();
        this.innerDataPropertyDao = dataPropertyDao;
        this.filters = filters;
    }

    // ----------------------------------------------------------------------
	// Filtered operations
	// ----------------------------------------------------------------------

    @Override
	public void fillDataPropertiesForIndividual(Individual individual) {
        innerDataPropertyDao.fillDataPropertiesForIndividual(individual);
        List<DataProperty> props = individual.getDataPropertyList();
        if(props != null && props.size() > 0){
            individual.setDatatypePropertyList( filter(props,filters.getDataPropertyFilter()) );
        }
    }

    @Override
    public List<DataProperty> getAllDataProperties() {
        return filter(innerDataPropertyDao.getAllDataProperties(), filters.getDataPropertyFilter());
    }

    @Override
    public List<DataProperty> getAllExternalIdDataProperties() {
        return filter(innerDataPropertyDao.getAllDataProperties(), filters.getDataPropertyFilter());
    }

    @Override
    public List<DataProperty> getDataPropertiesForVClass(String classURI) {
        return filter(innerDataPropertyDao.getDataPropertiesForVClass(classURI),
                filters.getDataPropertyFilter());
    }

    @Override
    public Collection<DataProperty> getAllPossibleDatapropsForIndividual(String individualURI) {
        List<DataProperty> filteredProps = new ArrayList<DataProperty>();
        for (DataProperty dp: innerDataPropertyDao.getAllPossibleDatapropsForIndividual(individualURI)) {
        	DataPropertyStatementImpl dps = new DataPropertyStatementImpl(individualURI, dp.getURI(), SOME_LITERAL);
			if (filters.getDataPropertyStatementFilter().fn(dps)) {
        		filteredProps.add(dp);
        	}
        }
        return filteredProps;
    }
    
	// ----------------------------------------------------------------------
	// Unfiltered operations
	// ----------------------------------------------------------------------
	
    @Override
    public void deleteDataProperty(DataProperty dataProperty) {
        innerDataPropertyDao.deleteDataProperty(dataProperty);
    }

    @Override
    public void deleteDataProperty(String dataPropertyURI) {
        innerDataPropertyDao.deleteDataProperty(dataPropertyURI);
    }

    @Override
    public boolean annotateDataPropertyAsExternalIdentifier(String dataPropertyURI) {
        return innerDataPropertyDao.annotateDataPropertyAsExternalIdentifier(dataPropertyURI);
    }

    @Override
    public String getRequiredDatatypeURI(Individual individual, DataProperty dataProperty) {
    	return innerDataPropertyDao.getRequiredDatatypeURI(individual, dataProperty);
    }
    
    @Override
    public DataProperty getDataPropertyByURI(String dataPropertyURI) {
        return innerDataPropertyDao.getDataPropertyByURI(dataPropertyURI);
    }

    @Override
    public String insertDataProperty(DataProperty dataProperty) throws InsertException {
        return innerDataPropertyDao.insertDataProperty(dataProperty);
    }

    @Override
    public void updateDataProperty(DataProperty dataProperty) {
        innerDataPropertyDao.updateDataProperty(dataProperty);
    }
    
    @Override
    public void addSuperproperty(String propertyURI, String superpropertyURI) {
    	innerDataPropertyDao.addSuperproperty(propertyURI, superpropertyURI);
    }
    
    @Override
    public void removeSuperproperty(String propertyURI, String superpropertyURI) {
    	innerDataPropertyDao.removeSuperproperty(propertyURI, superpropertyURI);
    }
    
    @Override
    public void addSubproperty(String propertyURI, String subpropertyURI) {
    	innerDataPropertyDao.addSubproperty(propertyURI, subpropertyURI);
    }
    
    @Override
    public void removeSubproperty(String propertyURI, String subpropertyURI) {
    	innerDataPropertyDao.removeSubproperty(propertyURI, subpropertyURI);
    }
    
    @Override
    public List <String> getSubPropertyURIs(String propertyURI) {
    	return innerDataPropertyDao.getSubPropertyURIs(propertyURI);
    }

    @Override
    public List <String> getAllSubPropertyURIs(String propertyURI) {
    	return innerDataPropertyDao.getAllSubPropertyURIs(propertyURI);
    }

    @Override
    public List <String> getSuperPropertyURIs(String propertyURI, boolean direct) {
    	return innerDataPropertyDao.getSuperPropertyURIs(propertyURI, direct);
    }

    @Override
    public List <String> getAllSuperPropertyURIs(String propertyURI) {
    	return innerDataPropertyDao.getAllSuperPropertyURIs(propertyURI);
    }
    
    @Override
    public List<DataProperty> getRootDataProperties() {
    	return innerDataPropertyDao.getRootDataProperties();
    }

    @Override
	public void addSubproperty(Property property, Property subproperty) {
		innerDataPropertyDao.addSubproperty(property, subproperty);
	}

    @Override
	public void addSuperproperty(Property property, Property superproperty) {
		innerDataPropertyDao.addSuperproperty(property, superproperty);	
	}

    @Override
	public void removeSubproperty(Property property, Property subproperty) {
		innerDataPropertyDao.removeSubproperty(property, subproperty);
	}

    @Override
	public void removeSuperproperty(Property property, Property superproperty) {
		innerDataPropertyDao.removeSuperproperty(property, superproperty);
	}

    @Override
	public void addEquivalentProperty(String propertyURI, String equivalentPropertyURI) {
		innerDataPropertyDao.addEquivalentProperty(propertyURI, equivalentPropertyURI);
	}

    @Override
	public void addEquivalentProperty(Property property, Property equivalentProperty) {
		innerDataPropertyDao.addEquivalentProperty(property, equivalentProperty);
	}

    @Override
	public List<String> getEquivalentPropertyURIs(String propertyURI) {
		return innerDataPropertyDao.getEquivalentPropertyURIs(propertyURI);
	}

    @Override
	public void removeEquivalentProperty(String propertyURI, String equivalentPropertyURI) {
		innerDataPropertyDao.removeEquivalentProperty(propertyURI, equivalentPropertyURI);
	}

    @Override
	public void removeEquivalentProperty(Property property, Property equivalentProperty) {
		innerDataPropertyDao.removeEquivalentProperty(property, equivalentProperty);
	}
	
    @Override
   public List <VClass> getClassesWithRestrictionOnProperty(String propertyURI) {
    	return innerDataPropertyDao.getClassesWithRestrictionOnProperty(propertyURI);
    }
    
    @Override
    // This may need to be filtered at some point.
    public List<DataProperty> getDataPropertyList(Individual subject) {
        return innerDataPropertyDao.getDataPropertyList(subject);
    }
    
    @Override
    // This may need to be filtered at some point.
    public List<DataProperty> getDataPropertyList(String subjectUri) {
        return innerDataPropertyDao.getDataPropertyList(subjectUri);
    }

    @Override
    public String getCustomListViewConfigFileName(DataProperty dataProperty) {
        return innerDataPropertyDao.getCustomListViewConfigFileName(dataProperty);
    }

}