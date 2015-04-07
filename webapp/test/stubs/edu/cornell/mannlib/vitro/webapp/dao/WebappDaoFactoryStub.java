/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.dao;

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
 * A minimal implementation of the WebappDaoFactory.
 * 
 * I have only implemented the methods that I needed. Feel free to implement
 * others.
 */
public class WebappDaoFactoryStub implements WebappDaoFactory {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private String defaultNamespace;
	private ApplicationDao applicationDao;
	private DataPropertyDao dataPropertyDao;
	private IndividualDao individualDao;
	private MenuDao menuDao;
	private ObjectPropertyDao objectPropertyDao;
	private ObjectPropertyStatementDao objectPropertyStatementDao;
	private FauxPropertyDao fauxPropertyDao;
	private OntologyDao ontologyDao;
	private UserAccountsDao userAccountsDao;
	private VClassDao vClassDao;

	public void setDefaultNamespace(String defaultNamespace) {
		this.defaultNamespace = defaultNamespace;
	}

	public void setApplicationDao(ApplicationDao applicationDao) {
		this.applicationDao = applicationDao;
	}

	public void setDataPropertyDao(DataPropertyDao dataPropertyDao) {
		this.dataPropertyDao = dataPropertyDao;
	}

	public void setIndividualDao(IndividualDao individualDao) {
		this.individualDao = individualDao;
	}

	public void setMenuDao(MenuDao menuDao) {
		this.menuDao = menuDao;
	}

	public void setObjectPropertyDao(ObjectPropertyDao objectPropertyDao) {
		this.objectPropertyDao = objectPropertyDao;
	}

	public void setObjectPropertyStatementDao(
			ObjectPropertyStatementDao objectPropertyStatementDao) {
		this.objectPropertyStatementDao = objectPropertyStatementDao;
	}

	public void setFauxPropertyDao(FauxPropertyDao fauxPropertyDao) {
		this.fauxPropertyDao = fauxPropertyDao;
	}

	public void setOntologyDao(OntologyDao ontologyDao) {
		this.ontologyDao = ontologyDao;
	}

	public void setUserAccountsDao(UserAccountsDao userAccountsDao) {
		this.userAccountsDao = userAccountsDao;
	}

	public void setVClassDao(VClassDao vClassDao) {
		this.vClassDao = vClassDao;
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public String getDefaultNamespace() {
		return this.defaultNamespace;
	}

	@Override
	public ApplicationDao getApplicationDao() {
		return this.applicationDao;
	}

	@Override
	public DataPropertyDao getDataPropertyDao() {
		return this.dataPropertyDao;
	}

	@Override
	public IndividualDao getIndividualDao() {
		return this.individualDao;
	}

	@Override
	public MenuDao getMenuDao() {
		return this.menuDao;
	}

	@Override
	public ObjectPropertyDao getObjectPropertyDao() {
		return this.objectPropertyDao;
	}

	@Override
	public FauxPropertyDao getFauxPropertyDao() {
		return this.fauxPropertyDao;
	}

	@Override
	public ObjectPropertyStatementDao getObjectPropertyStatementDao() {
		return this.objectPropertyStatementDao;
	}

	@Override
	public OntologyDao getOntologyDao() {
		return this.ontologyDao;
	}

	@Override
	public UserAccountsDao getUserAccountsDao() {
		return this.userAccountsDao;
	}

	@Override
	public VClassDao getVClassDao() {
		return this.vClassDao;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public String checkURI(String uriStr) {
		throw new RuntimeException(
				"WebappDaoFactory.checkURI() not implemented.");
	}

	@Override
	public String checkURIForEditableEntity(String uriStr) {
		throw new RuntimeException(
				"WebappDaoFactory.checkURIForNewEditableEntity() not implemented.");
	}

	@Override
	public boolean hasExistingURI(String uriStr) {
		throw new RuntimeException(
				"WebappDaoFactory.hasExistingURI() not implemented.");
	}

	@Override
	public Set<String> getNonuserNamespaces() {
		throw new RuntimeException(
				"WebappDaoFactory.getNonuserNamespaces() not implemented.");
	}

	@Override
	public List<String> getPreferredLanguages() {
		throw new RuntimeException(
				"WebappDaoFactory.getPreferredLanguages() not implemented.");
	}

	@Override
	public List<String> getCommentsForResource(String resourceURI) {
		throw new RuntimeException(
				"WebappDaoFactory.getCommentsForResource() not implemented.");
	}

	@Override
	public WebappDaoFactory getUserAwareDaoFactory(String userURI) {
		throw new RuntimeException(
				"WebappDaoFactory.getUserAwareDaoFactory() not implemented.");
	}

	@Override
	public String getUserURI() {
		throw new RuntimeException(
				"WebappDaoFactory.getUserURI() not implemented.");
	}

	@Override
	public DatatypeDao getDatatypeDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getDatatypeDao() not implemented.");
	}

	@Override
	public DataPropertyStatementDao getDataPropertyStatementDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getDataPropertyStatementDao() not implemented.");
	}

	@Override
	public DisplayModelDao getDisplayModelDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getDisplayModelDao() not implemented.");
	}

	@Override
	public VClassGroupDao getVClassGroupDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getVClassGroupDao() not implemented.");
	}

	@Override
	public PropertyGroupDao getPropertyGroupDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getPropertyGroupDao() not implemented.");
	}

	@Override
	public PropertyInstanceDao getPropertyInstanceDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getPropertyInstanceDao() not implemented.");
	}

	@Override
	public PageDao getPageDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getPageDao() not implemented.");
	}

	@Override
	public void close() {
		throw new RuntimeException("WebappDaoFactory.close() not implemented.");
	}

}
