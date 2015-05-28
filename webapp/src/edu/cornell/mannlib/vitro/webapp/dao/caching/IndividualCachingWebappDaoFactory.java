/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.caching;

import java.util.List;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dao.ApplicationDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.DatatypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayModelDao;
import edu.cornell.mannlib.vitro.webapp.dao.FauxPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.MenuDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PageDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * TODO
 */
public class IndividualCachingWebappDaoFactory implements WebappDaoFactory {
	private final WebappDaoFactory inner;
	private final IndividualDao cachingIndividualDao;

	public IndividualCachingWebappDaoFactory(WebappDaoFactory inner) {
		this.inner = inner;
		this.cachingIndividualDao = new IndividualDaoCaching(inner.getIndividualDao());
	}

	// ----------------------------------------------------------------------
	// return a caching wrapper around the individualDao
	// ----------------------------------------------------------------------

	@Override
	public IndividualDao getIndividualDao() {
		return this.cachingIndividualDao;
	}

	// ----------------------------------------------------------------------
	// delegated methods
	// ----------------------------------------------------------------------

	@Override
	public void close() {
		inner.close();
	}

	@Override
	public String checkURI(String uriStr) {
		return inner.checkURI(uriStr);
	}

	@Override
	public String checkURIForEditableEntity(String uriStr) {
		return inner.checkURIForEditableEntity(uriStr);
	}

	@Override
	public boolean hasExistingURI(String uriStr) {
		return inner.hasExistingURI(uriStr);
	}

	@Override
	public String getDefaultNamespace() {
		return inner.getDefaultNamespace();
	}

	@Override
	public Set<String> getNonuserNamespaces() {
		return inner.getNonuserNamespaces();
	}

	@Override
	public List<String> getPreferredLanguages() {
		return inner.getPreferredLanguages();
	}

	@Override
	public List<String> getCommentsForResource(String resourceURI) {
		return inner.getCommentsForResource(resourceURI);
	}

	@Override
	public WebappDaoFactory getUserAwareDaoFactory(String userURI) {
		return inner.getUserAwareDaoFactory(userURI);
	}

	@Override
	public String getUserURI() {
		return inner.getUserURI();
	}

	@Override
	public DataPropertyDao getDataPropertyDao() {
		return inner.getDataPropertyDao();
	}

	@Override
	public DatatypeDao getDatatypeDao() {
		return inner.getDatatypeDao();
	}

	@Override
	public ObjectPropertyDao getObjectPropertyDao() {
		return inner.getObjectPropertyDao();
	}

	@Override
	public OntologyDao getOntologyDao() {
		return inner.getOntologyDao();
	}

	@Override
	public VClassDao getVClassDao() {
		return inner.getVClassDao();
	}

	@Override
	public FauxPropertyDao getFauxPropertyDao() {
		return inner.getFauxPropertyDao();
	}

	@Override
	public DataPropertyStatementDao getDataPropertyStatementDao() {
		return inner.getDataPropertyStatementDao();
	}

	@Override
	public ObjectPropertyStatementDao getObjectPropertyStatementDao() {
		return inner.getObjectPropertyStatementDao();
	}

	@Override
	public DisplayModelDao getDisplayModelDao() {
		return inner.getDisplayModelDao();
	}

	@Override
	public ApplicationDao getApplicationDao() {
		return inner.getApplicationDao();
	}

	@Override
	public UserAccountsDao getUserAccountsDao() {
		return inner.getUserAccountsDao();
	}

	@Override
	public VClassGroupDao getVClassGroupDao() {
		return inner.getVClassGroupDao();
	}

	@Override
	public PropertyGroupDao getPropertyGroupDao() {
		return inner.getPropertyGroupDao();
	}

	@Override
	public PropertyInstanceDao getPropertyInstanceDao() {
		return inner.getPropertyInstanceDao();
	}

	@Override
	public PageDao getPageDao() {
		return inner.getPageDao();
	}

	@Override
	public MenuDao getMenuDao() {
		return inner.getMenuDao();
	}

}
