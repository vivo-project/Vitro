/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

	@Override
	public List<FauxProperty> getFauxPropertiesForBaseUri(String uri) {
		List<FauxProperty> list = new ArrayList<>();
		for (FauxProperty fp : props.values()) {
			if (Objects.equals(fp.getBaseURI(), uri)) {
				list.add(fp);
			}
		}
		return list;
	}

	@Override
	public void insertFauxProperty(FauxProperty fp) {
		props.put(new FullPropertyKey(fp), fp);
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public FauxProperty getFauxPropertyFromContextUri(String contextUri) {
		throw new RuntimeException(
				"FauxPropertyDaoStub.getFauxPropertyFromContextUri() not implemented.");
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
