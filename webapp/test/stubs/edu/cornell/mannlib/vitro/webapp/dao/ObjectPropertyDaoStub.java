/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;

/**
 * A minimal implementation of the ObjectPropertyDao.
 * 
 * I have only implemented the methods that I needed. Feel free to implement
 * others.
 */
public class ObjectPropertyDaoStub implements ObjectPropertyDao {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private final Map<String, ObjectProperty> opMap = new HashMap<String, ObjectProperty>();
	private final Map<String, String> configFilesMap = new HashMap<String, String>();

	public void addObjectProperty(ObjectProperty property) {
		if (property == null) {
			throw new NullPointerException("predicate may not be null.");
		}

		String uri = property.getURI();
		if (uri == null) {
			throw new NullPointerException("uri may not be null.");
		}

		opMap.put(uri, property);
	}

	public void setCustomListViewConfigFileName(ObjectProperty property,
			String filename) {
		if (property == null) {
			throw new NullPointerException("property may not be null.");
		}

		String uri = property.getURI();
		if (uri == null) {
			throw new NullPointerException("uri may not be null.");
		}

		configFilesMap.put(uri, filename);
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public List<ObjectProperty> getAllObjectProperties() {
		return new ArrayList<>(opMap.values());
	}

	@Override
	public ObjectProperty getObjectPropertyByURI(String objectPropertyURI) {
		if (objectPropertyURI == null) {
			return null;
		}
		return opMap.get(objectPropertyURI);
	}

	@Override
	public ObjectProperty getObjectPropertyByURIs(String objectPropertyURI,
			String domainURI, String rangeURI) {
		return getObjectPropertyByURI(objectPropertyURI);
	}

	@Override
	public ObjectProperty getObjectPropertyByURIs(String objectPropertyURI,
			String domainURI, String rangeURI, ObjectProperty base) {
		return getObjectPropertyByURI(objectPropertyURI);
	}

	@Override
	public String getCustomListViewConfigFileName(ObjectProperty objectProperty) {
		if (objectProperty == null) {
			return null;
		}
		String uri = objectProperty.getURI();
		if (uri == null) {
			return null;
		}
		return configFilesMap.get(uri);
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public void addSuperproperty(Property property, Property superproperty) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.addSuperproperty() not implemented.");
	}

	@Override
	public void addSuperproperty(String propertyURI, String superpropertyURI) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.addSuperproperty() not implemented.");
	}

	@Override
	public void removeSuperproperty(Property property, Property superproperty) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.removeSuperproperty() not implemented.");
	}

	@Override
	public void removeSuperproperty(String propertyURI, String superpropertyURI) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.removeSuperproperty() not implemented.");
	}

	@Override
	public void addSubproperty(Property property, Property subproperty) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.addSubproperty() not implemented.");
	}

	@Override
	public void addSubproperty(String propertyURI, String subpropertyURI) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.addSubproperty() not implemented.");
	}

	@Override
	public void removeSubproperty(Property property, Property subproperty) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.removeSubproperty() not implemented.");
	}

	@Override
	public void removeSubproperty(String propertyURI, String subpropertyURI) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.removeSubproperty() not implemented.");
	}

	@Override
	public void addEquivalentProperty(String propertyURI,
			String equivalentPropertyURI) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.addEquivalentProperty() not implemented.");
	}

	@Override
	public void addEquivalentProperty(Property property,
			Property equivalentProperty) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.addEquivalentProperty() not implemented.");
	}

	@Override
	public void removeEquivalentProperty(String propertyURI,
			String equivalentPropertyURI) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.removeEquivalentProperty() not implemented.");
	}

	@Override
	public void removeEquivalentProperty(Property property,
			Property equivalentProperty) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.removeEquivalentProperty() not implemented.");
	}

	@Override
	public List<String> getAllSubPropertyURIs(String propertyURI) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.getAllSubPropertyURIs() not implemented.");
	}

	@Override
	public List<String> getAllSuperPropertyURIs(String propertyURI) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.getAllSuperPropertyURIs() not implemented.");
	}

	@Override
	public List<String> getEquivalentPropertyURIs(String propertyURI) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.getEquivalentPropertyURIs() not implemented.");
	}

	@Override
	public List<VClass> getClassesWithRestrictionOnProperty(String propertyURI) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.getClassesWithRestrictionOnProperty() not implemented.");
	}

	@Override
	public List<ObjectProperty> getObjectPropertiesForObjectPropertyStatements(
			List objectPropertyStatements) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.getObjectPropertiesForObjectPropertyStatements() not implemented.");
	}

	@Override
	public List<String> getSuperPropertyURIs(String objectPropertyURI,
			boolean direct) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.getSuperPropertyURIs() not implemented.");
	}

	@Override
	public List<String> getSubPropertyURIs(String objectPropertyURI) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.getSubPropertyURIs() not implemented.");
	}

	@Override
	public void fillObjectPropertiesForIndividual(Individual individual) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.fillObjectPropertiesForIndividual() not implemented.");
	}

	@Override
	public int insertObjectProperty(ObjectProperty objectProperty)
			throws InsertException {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.insertObjectProperty() not implemented.");
	}

	@Override
	public void updateObjectProperty(ObjectProperty objectProperty) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.updateObjectProperty() not implemented.");
	}

	@Override
	public void deleteObjectProperty(String objectPropertyURI) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.deleteObjectProperty() not implemented.");
	}

	@Override
	public void deleteObjectProperty(ObjectProperty objectProperty) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.deleteObjectProperty() not implemented.");
	}

	@Override
	public boolean skipEditForm(String predicateURI) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.skipEditForm() not implemented.");
	}

	@Override
	public List<ObjectProperty> getRootObjectProperties() {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.getRootObjectProperties() not implemented.");
	}

	@Override
	public List<ObjectProperty> getObjectPropertyList(Individual subject) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.getObjectPropertyList() not implemented.");
	}

	@Override
	public List<ObjectProperty> getObjectPropertyList(String subjectUri) {
		throw new RuntimeException(
				"ObjectPropertyDaoStub.getObjectPropertyList() not implemented.");
	}

}
