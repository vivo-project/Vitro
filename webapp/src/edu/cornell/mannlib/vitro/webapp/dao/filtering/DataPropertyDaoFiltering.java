/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import static edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants.SOME_LITERAL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.jga.fn.UnaryFunctor;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
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

    public void deleteDataProperty(DataProperty dataProperty) {
        innerDataPropertyDao.deleteDataProperty(dataProperty);
    }


    public void deleteDataProperty(String dataPropertyURI) {
        innerDataPropertyDao.deleteDataProperty(dataPropertyURI);
    }

    public boolean annotateDataPropertyAsExternalIdentifier(String dataPropertyURI) {
        return innerDataPropertyDao.annotateDataPropertyAsExternalIdentifier(dataPropertyURI);
    }


    public void fillDataPropertiesForIndividual(Individual individual) {
        innerDataPropertyDao.fillDataPropertiesForIndividual(individual);
        List<DataProperty> props = individual.getDataPropertyList();
        if(props != null && props.size() > 0){
            individual.setDatatypePropertyList( filter(props,filters.getDataPropertyFilter()) );
        }
    }


    public List getAllDataProperties() {
        return filter(innerDataPropertyDao.getAllDataProperties(), filters.getDataPropertyFilter());
    }

    public List getAllExternalIdDataProperties() {
        return filter(innerDataPropertyDao.getAllDataProperties(), filters.getDataPropertyFilter());
    }


    public List<DataProperty> getDataPropertiesForVClass(String classURI) {
        return filter(innerDataPropertyDao.getDataPropertiesForVClass(classURI),
                filters.getDataPropertyFilter());
    }

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
    
    public String getRequiredDatatypeURI(Individual individual, DataProperty dataProperty) {
    	return innerDataPropertyDao.getRequiredDatatypeURI(individual, dataProperty);
    }
    
    public DataProperty getDataPropertyByURI(String dataPropertyURI) {
        DataProperty prop = innerDataPropertyDao.getDataPropertyByURI(dataPropertyURI);
        if( prop != null ){
            Boolean acceptable = filters.getDataPropertyFilter().fn(prop);
            if( acceptable == Boolean.TRUE )
                return prop;
            else
                return null;
        }
        return null;
    }


    public String insertDataProperty(DataProperty dataProperty) throws InsertException {
        return innerDataPropertyDao.insertDataProperty(dataProperty);
    }


    public void updateDataProperty(DataProperty dataProperty) {
        innerDataPropertyDao.updateDataProperty(dataProperty);
    }
    
    public void addSuperproperty(ObjectProperty property, ObjectProperty superproperty) {
    	innerDataPropertyDao.addSuperproperty(property, superproperty);
    }
    
    public void addSuperproperty(String propertyURI, String superpropertyURI) {
    	innerDataPropertyDao.addSuperproperty(propertyURI, superpropertyURI);
    }
    
    public void removeSuperproperty(ObjectProperty property, ObjectProperty superproperty) {
    	innerDataPropertyDao.removeSuperproperty(property, superproperty);
    }
    
    public void removeSuperproperty(String propertyURI, String superpropertyURI) {
    	innerDataPropertyDao.removeSuperproperty(propertyURI, superpropertyURI);
    }
    
    public void addSubproperty(ObjectProperty property, ObjectProperty subproperty) {
    	innerDataPropertyDao.addSubproperty(property, subproperty);
    }
    
    public void addSubproperty(String propertyURI, String subpropertyURI) {
    	innerDataPropertyDao.addSubproperty(propertyURI, subpropertyURI);
    }
    
    public void removeSubproperty(ObjectProperty property, ObjectProperty subproperty) {
    	innerDataPropertyDao.removeSubproperty(property, subproperty);
    }
    
    public void removeSubproperty(String propertyURI, String subpropertyURI) {
    	innerDataPropertyDao.removeSubproperty(propertyURI, subpropertyURI);
    }
    
    public List <String> getSubPropertyURIs(String propertyURI) {
    	return innerDataPropertyDao.getSubPropertyURIs(propertyURI);
    }

    public List <String> getAllSubPropertyURIs(String propertyURI) {
    	return innerDataPropertyDao.getAllSubPropertyURIs(propertyURI);
    }

    public List <String> getSuperPropertyURIs(String propertyURI, boolean direct) {
    	return innerDataPropertyDao.getSuperPropertyURIs(propertyURI, direct);
    }

    public List <String> getAllSuperPropertyURIs(String propertyURI) {
    	return innerDataPropertyDao.getAllSuperPropertyURIs(propertyURI);
    }
    
    public List<DataProperty> getRootDataProperties() {
    	return innerDataPropertyDao.getRootDataProperties();
    }

	public void addSubproperty(Property property, Property subproperty) {
		innerDataPropertyDao.addSubproperty(property, subproperty);
	}

	public void addSuperproperty(Property property, Property superproperty) {
		innerDataPropertyDao.addSuperproperty(property, superproperty);	
	}

	public void removeSubproperty(Property property, Property subproperty) {
		innerDataPropertyDao.removeSubproperty(property, subproperty);
	}

	public void removeSuperproperty(Property property, Property superproperty) {
		innerDataPropertyDao.removeSuperproperty(property, superproperty);
	}

	public void addEquivalentProperty(String propertyURI,
			String equivalentPropertyURI) {
		innerDataPropertyDao.addEquivalentProperty(propertyURI, equivalentPropertyURI);
	}

	public void addEquivalentProperty(Property property,
			Property equivalentProperty) {
		innerDataPropertyDao.addEquivalentProperty(property, equivalentProperty);
	}

	public List<String> getEquivalentPropertyURIs(String propertyURI) {
		return innerDataPropertyDao.getEquivalentPropertyURIs(propertyURI);
	}

	public void removeEquivalentProperty(String propertyURI,
			String equivalentPropertyURI) {
		innerDataPropertyDao.removeEquivalentProperty(propertyURI, equivalentPropertyURI);
	}

	public void removeEquivalentProperty(Property property,
			Property equivalentProperty) {
		innerDataPropertyDao.removeEquivalentProperty(property, equivalentProperty);
	}
	
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

}