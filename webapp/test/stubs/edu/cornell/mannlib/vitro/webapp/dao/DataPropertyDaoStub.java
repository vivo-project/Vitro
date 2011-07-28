/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;

/**
 * A minimal implementation of the DataPropertyDao.
 * 
 * I have only implemented the methods that I needed. Feel free to implement
 * others.
 */
public class DataPropertyDaoStub implements DataPropertyDao {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------
	
	private final Map<String, DataProperty> dpMap = new HashMap<String, DataProperty>();
	
	public void addDataProperty(DataProperty dataProperty) {
		if (dataProperty == null) {
			throw new NullPointerException("dataProperty may not be null.");
		}
		
		String uri = dataProperty.getURI();
		if (uri == null) {
			throw new NullPointerException("uri may not be null.");
		}
		
		dpMap.put(uri, dataProperty);
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public DataProperty getDataPropertyByURI(String dataPropertyURI) {
		return dpMap.get(dataPropertyURI);
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public void addSuperproperty(Property property, Property superproperty) {
		throw new RuntimeException(
				"PropertyDao.addSuperproperty() not implemented.");
	}

	@Override
	public void addSuperproperty(String propertyURI, String superpropertyURI) {
		throw new RuntimeException(
				"PropertyDao.addSuperproperty() not implemented.");
	}

	@Override
	public void removeSuperproperty(Property property, Property superproperty) {
		throw new RuntimeException(
				"PropertyDao.removeSuperproperty() not implemented.");
	}

	@Override
	public void removeSuperproperty(String propertyURI, String superpropertyURI) {
		throw new RuntimeException(
				"PropertyDao.removeSuperproperty() not implemented.");
	}

	@Override
	public void addSubproperty(Property property, Property subproperty) {
		throw new RuntimeException(
				"PropertyDao.addSubproperty() not implemented.");
	}

	@Override
	public void addSubproperty(String propertyURI, String subpropertyURI) {
		throw new RuntimeException(
				"PropertyDao.addSubproperty() not implemented.");
	}

	@Override
	public void removeSubproperty(Property property, Property subproperty) {
		throw new RuntimeException(
				"PropertyDao.removeSubproperty() not implemented.");
	}

	@Override
	public void removeSubproperty(String propertyURI, String subpropertyURI) {
		throw new RuntimeException(
				"PropertyDao.removeSubproperty() not implemented.");
	}

	@Override
	public void addEquivalentProperty(String propertyURI,
			String equivalentPropertyURI) {
		throw new RuntimeException(
				"PropertyDao.addEquivalentProperty() not implemented.");
	}

	@Override
	public void addEquivalentProperty(Property property,
			Property equivalentProperty) {
		throw new RuntimeException(
				"PropertyDao.addEquivalentProperty() not implemented.");
	}

	@Override
	public void removeEquivalentProperty(String propertyURI,
			String equivalentPropertyURI) {
		throw new RuntimeException(
				"PropertyDao.removeEquivalentProperty() not implemented.");
	}

	@Override
	public void removeEquivalentProperty(Property property,
			Property equivalentProperty) {
		throw new RuntimeException(
				"PropertyDao.removeEquivalentProperty() not implemented.");
	}

	@Override
	public List<String> getSubPropertyURIs(String propertyURI) {
		throw new RuntimeException(
				"PropertyDao.getSubPropertyURIs() not implemented.");
	}

	@Override
	public List<String> getAllSubPropertyURIs(String propertyURI) {
		throw new RuntimeException(
				"PropertyDao.getAllSubPropertyURIs() not implemented.");
	}

	@Override
	public List<String> getSuperPropertyURIs(String propertyURI, boolean direct) {
		throw new RuntimeException(
				"PropertyDao.getSuperPropertyURIs() not implemented.");
	}

	@Override
	public List<String> getAllSuperPropertyURIs(String propertyURI) {
		throw new RuntimeException(
				"PropertyDao.getAllSuperPropertyURIs() not implemented.");
	}

	@Override
	public List<String> getEquivalentPropertyURIs(String propertyURI) {
		throw new RuntimeException(
				"PropertyDao.getEquivalentPropertyURIs() not implemented.");
	}

	@Override
	public List<VClass> getClassesWithRestrictionOnProperty(String propertyURI) {
		throw new RuntimeException(
				"PropertyDao.getClassesWithRestrictionOnProperty() not implemented.");
	}

	@Override
	public List getAllDataProperties() {
		throw new RuntimeException(
				"DataPropertyDao.getAllDataProperties() not implemented.");
	}

	@Override
	public List getAllExternalIdDataProperties() {
		throw new RuntimeException(
				"DataPropertyDao.getAllExternalIdDataProperties() not implemented.");
	}

	@Override
	public void fillDataPropertiesForIndividual(Individual individual) {
		throw new RuntimeException(
				"DataPropertyDao.fillDataPropertiesForIndividual() not implemented.");
	}

	@Override
	public List<DataProperty> getDataPropertiesForVClass(String vClassURI) {
		throw new RuntimeException(
				"DataPropertyDao.getDataPropertiesForVClass() not implemented.");
	}

	@Override
	public Collection<DataProperty> getAllPossibleDatapropsForIndividual(
			String individualURI) {
		throw new RuntimeException(
				"DataPropertyDao.getAllPossibleDatapropsForIndividual() not implemented.");
	}

	@Override
	public String getRequiredDatatypeURI(Individual individual,
			DataProperty dataProperty) {
		throw new RuntimeException(
				"DataPropertyDao.getRequiredDatatypeURI() not implemented.");
	}

	@Override
	public String insertDataProperty(DataProperty dataProperty)
			throws InsertException {
		throw new RuntimeException(
				"DataPropertyDao.insertDataProperty() not implemented.");
	}

	@Override
	public void updateDataProperty(DataProperty dataProperty) {
		throw new RuntimeException(
				"DataPropertyDao.updateDataProperty() not implemented.");
	}

	@Override
	public void deleteDataProperty(DataProperty dataProperty) {
		throw new RuntimeException(
				"DataPropertyDao.deleteDataProperty() not implemented.");
	}

	@Override
	public void deleteDataProperty(String dataPropertyURI) {
		throw new RuntimeException(
				"DataPropertyDao.deleteDataProperty() not implemented.");
	}

	@Override
	public List<DataProperty> getRootDataProperties() {
		throw new RuntimeException(
				"DataPropertyDao.getRootDataProperties() not implemented.");
	}

	@Override
	public boolean annotateDataPropertyAsExternalIdentifier(
			String dataPropertyURI) {
		throw new RuntimeException(
				"DataPropertyDao.annotateDataPropertyAsExternalIdentifier() not implemented.");
	}

	@Override
	public List<DataProperty> getDataPropertyList(Individual subject) {
		throw new RuntimeException(
				"DataPropertyDao.getDataPropertyList() not implemented.");
	}

	@Override
	public List<DataProperty> getDataPropertyList(String subjectUri) {
		throw new RuntimeException(
				"DataPropertyDao.getDataPropertyList() not implemented.");
	}

}
