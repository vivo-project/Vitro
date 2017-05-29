/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.dao;

import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstanceIface;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;

/**
 * TODO
 */
public class PropertyInstanceDaoStub implements PropertyInstanceDao {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public void deleteObjectPropertyStatement(String subjectURI,
			String propertyURI, String objectURI) {
		throw new RuntimeException(
				"PropertyInstanceDaoStub.deleteObjectPropertyStatement() not implemented.");
	}

	@Override
	public Collection<PropertyInstance> getAllPossiblePropInstForIndividual(
			String individualURI) {
		throw new RuntimeException(
				"PropertyInstanceDaoStub.getAllPossiblePropInstForIndividual() not implemented.");
	}

	@Override
	public Collection<PropertyInstance> getAllPropInstByVClass(
			String classURI) {
		throw new RuntimeException(
				"PropertyInstanceDaoStub.getAllPropInstByVClass() not implemented.");
	}

	@Override
	public Collection<PropertyInstance> getExistingProperties(String entityURI,
			String propertyURI) {
		throw new RuntimeException(
				"PropertyInstanceDaoStub.getExistingProperties() not implemented.");
	}

	@Override
	public PropertyInstance getProperty(String subjectURI, String predicateURI,
			String objectURI) {
		throw new RuntimeException(
				"PropertyInstanceDaoStub.getProperty() not implemented.");
	}

	@Override
	public int insertProp(PropertyInstanceIface prop) {
		throw new RuntimeException(
				"PropertyInstanceDaoStub.insertProp() not implemented.");
	}

	@Override
	public void insertPropertyInstance(PropertyInstance prop) {
		throw new RuntimeException(
				"PropertyInstanceDaoStub.insertPropertyInstance() not implemented.");
	}

	@Override
	public void deletePropertyInstance(PropertyInstance prop) {
		throw new RuntimeException(
				"PropertyInstanceDaoStub.deletePropertyInstance() not implemented.");
	}

}
