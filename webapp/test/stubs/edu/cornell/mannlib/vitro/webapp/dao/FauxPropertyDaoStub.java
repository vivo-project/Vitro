/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.dao.FauxPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyDao.FullPropertyKey;

/**
 * TODO
 */
public class FauxPropertyDaoStub implements FauxPropertyDao {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private final Map<FullPropertyKey, FauxProperty> props = new HashMap<>();
	
	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public FauxProperty getFauxPropertyByUris(String domainUri, String baseUri,
			String rangeUri) {
		return props.get(new FullPropertyKey(domainUri, baseUri, rangeUri));
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public List<FauxProperty> getFauxPropertiesForBaseUri(String uri) {
		throw new RuntimeException(
				"FauxPropertyDaoStub.getFauxPropertiesForBaseUri() not implemented.");
	}

	@Override
	public FauxProperty getFauxPropertyFromContextUri(String contextUri) {
		throw new RuntimeException(
				"FauxPropertyDaoStub.getFauxPropertyFromContextUri() not implemented.");
	}

	@Override
	public void insertFauxProperty(FauxProperty fp) {
		throw new RuntimeException(
				"FauxPropertyDaoStub.insertFauxProperty() not implemented.");
	}

	@Override
	public void updateFauxProperty(FauxProperty fp) {
		throw new RuntimeException(
				"FauxPropertyDaoStub.updateFauxProperty() not implemented.");
	}

	@Override
	public void deleteFauxProperty(FauxProperty fp) {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"FauxPropertyDaoStub.deleteFauxProperty() not implemented.");
	}
}
