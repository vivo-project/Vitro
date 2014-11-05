/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.dao.FauxPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;

/**
 * TODO Find out if this is really necessary. If so, what is filtered?
 */
public class FauxPropertyDaoFiltering extends BaseFiltering implements FauxPropertyDao {
	final FauxPropertyDao innerFauxPropertyDao;
	final VitroFilters filters;

	public FauxPropertyDaoFiltering(FauxPropertyDao fauxPropertyDao,
			VitroFilters filters) {
		super();
		this.innerFauxPropertyDao = fauxPropertyDao;
		this.filters = filters;
	}

	/* filtered methods */

	@Override
	public List<FauxProperty> getFauxPropertiesForBaseUri(String uri) {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"FauxPropertyDao.getFauxPropertiesForBaseUri() not implemented.");
	}

	@Override
	public FauxProperty getFauxPropertyFromContextUri(String contextUri) {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"FauxPropertyDaoFiltering.getFauxPropertyFromConfigContextUri() not implemented.");

	}

	@Override
	public FauxProperty getFauxPropertyByUris(String domainUri, String baseUri,
			String rangeUri) {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"FauxPropertyDaoFiltering.getFauxPropertyByUris() not implemented.");

	}

	@Override
	public void updateFauxProperty(FauxProperty fp) {
		// TODO Auto-generated method stub
		throw new RuntimeException("FauxPropertyDaoFiltering.updateFauxProperty() not implemented.");
		
	}

	@Override
	public void deleteFauxProperty(FauxProperty fp) {
		// TODO Auto-generated method stub
		throw new RuntimeException("FauxPropertyDao.deleteFauxProperty() not implemented.");
		
	}

	@Override
	public void insertFauxProperty(FauxProperty fp) {
		// TODO Auto-generated method stub
		throw new RuntimeException("FauxPropertyDao.insertFauxProperty() not implemented.");
		
	}

}
