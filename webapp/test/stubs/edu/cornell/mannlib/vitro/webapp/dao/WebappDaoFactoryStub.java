/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dao.ApplicationDao;
import edu.cornell.mannlib.vitro.webapp.dao.Classes2ClassesDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.DatatypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayModelDao;
import edu.cornell.mannlib.vitro.webapp.dao.FlagDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.KeywordDao;
import edu.cornell.mannlib.vitro.webapp.dao.KeywordIndividualRelationDao;
import edu.cornell.mannlib.vitro.webapp.dao.LinksDao;
import edu.cornell.mannlib.vitro.webapp.dao.LinktypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.MenuDao;
import edu.cornell.mannlib.vitro.webapp.dao.NamespaceDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PageDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.UserDao;
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

	private IndividualDao individualDao;
	private DataPropertyDao dataPropertyDao;
	private ObjectPropertyDao objectPropertyDao;

	public void setIndividualDao(IndividualDao individualDao) {
		this.individualDao = individualDao;
	}

	public void setDataPropertyDao(DataPropertyDao dataPropertyDao) {
		this.dataPropertyDao = dataPropertyDao;
	}

	public void setObjectPropertyDao(ObjectPropertyDao objectPropertyDao) {
		this.objectPropertyDao = objectPropertyDao;
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public IndividualDao getIndividualDao() {
		return this.individualDao;
	}

	@Override
	public DataPropertyDao getDataPropertyDao() {
		return this.dataPropertyDao;
	}

	@Override
	public ObjectPropertyDao getObjectPropertyDao() {
		return this.objectPropertyDao;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public Map<String, String> getProperties() {
		throw new RuntimeException(
				"WebappDaoFactory.getProperties() not implemented.");
	}

	@Override
	public String checkURI(String uriStr) {
		throw new RuntimeException(
				"WebappDaoFactory.checkURI() not implemented.");
	}

	@Override
	public String checkURI(String uriStr, boolean checkUniqueness) {
		throw new RuntimeException(
				"WebappDaoFactory.checkURI() not implemented.");
	}

	@Override
	public int getLanguageProfile() {
		throw new RuntimeException(
				"WebappDaoFactory.getLanguageProfile() not implemented.");
	}

	@Override
	public String getDefaultNamespace() {
		throw new RuntimeException(
				"WebappDaoFactory.getDefaultNamespace() not implemented.");
	}

	@Override
	public Set<String> getNonuserNamespaces() {
		throw new RuntimeException(
				"WebappDaoFactory.getNonuserNamespaces() not implemented.");
	}

	@Override
	public String[] getPreferredLanguages() {
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
	public Classes2ClassesDao getClasses2ClassesDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getClasses2ClassesDao() not implemented.");
	}

	@Override
	public DatatypeDao getDatatypeDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getDatatypeDao() not implemented.");
	}

	@Override
	public OntologyDao getOntologyDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getOntologyDao() not implemented.");
	}

	@Override
	public VClassDao getVClassDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getVClassDao() not implemented.");
	}

	@Override
	public DataPropertyStatementDao getDataPropertyStatementDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getDataPropertyStatementDao() not implemented.");
	}

	@Override
	public ObjectPropertyStatementDao getObjectPropertyStatementDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getObjectPropertyStatementDao() not implemented.");
	}

	@Override
	public DisplayModelDao getDisplayModelDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getDisplayModelDao() not implemented.");
	}

	@Override
	public ApplicationDao getApplicationDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getApplicationDao() not implemented.");
	}

	@Override
	public KeywordIndividualRelationDao getKeys2EntsDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getKeys2EntsDao() not implemented.");
	}

	@Override
	public KeywordDao getKeywordDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getKeywordDao() not implemented.");
	}

	@Override
	public LinksDao getLinksDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getLinksDao() not implemented.");
	}

	@Override
	public LinktypeDao getLinktypeDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getLinktypeDao() not implemented.");
	}

	@Override
	public FlagDao getFlagDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getFlagDao() not implemented.");
	}

    // TODO This goes away when the UserAccounts stuff is fully implemented -- jb
	@Override
	public UserDao getUserDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getUserDao() not implemented.");
	}

	@Override
	public UserAccountsDao getUserAccountsDao() {
		throw new RuntimeException(
		"WebappDaoFactory.getUserAccountsDao() not implemented.");
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
	public NamespaceDao getNamespaceDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getNamespaceDao() not implemented.");
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
	public MenuDao getMenuDao() {
		throw new RuntimeException(
				"WebappDaoFactory.getMenuDao() not implemented.");
	}

	@Override
	public void close() {
		throw new RuntimeException("WebappDaoFactory.close() not implemented.");
	}

}
