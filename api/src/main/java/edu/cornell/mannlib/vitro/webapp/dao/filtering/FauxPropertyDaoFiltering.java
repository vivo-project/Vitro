/* $This file is distributed under the terms of the license in LICENSE$ */

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
		return innerFauxPropertyDao.getFauxPropertiesForBaseUri(uri);
	}

	@Override
	public FauxProperty getFauxPropertyFromContextUri(String contextUri) {
		return innerFauxPropertyDao.getFauxPropertyFromContextUri(contextUri);
	}

	@Override
	public FauxProperty getFauxPropertyByUris(String domainUri, String baseUri,
			String rangeUri) {
		return innerFauxPropertyDao.getFauxPropertyByUris(domainUri, baseUri, 
		        rangeUri);
	}

	@Override
	public void updateFauxProperty(FauxProperty fp) {
		innerFauxPropertyDao.updateFauxProperty(fp);
	}

	@Override
	public void deleteFauxProperty(FauxProperty fp) {
		innerFauxPropertyDao.deleteFauxProperty(fp);
	}

	@Override
	public void insertFauxProperty(FauxProperty fp) {
		innerFauxPropertyDao.insertFauxProperty(fp);
	}

}
