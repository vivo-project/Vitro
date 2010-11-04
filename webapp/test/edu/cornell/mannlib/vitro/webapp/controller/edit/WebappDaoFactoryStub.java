/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dao.ApplicationDao;
import edu.cornell.mannlib.vitro.webapp.dao.Classes2ClassesDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.DatatypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.FlagDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.KeywordDao;
import edu.cornell.mannlib.vitro.webapp.dao.KeywordIndividualRelationDao;
import edu.cornell.mannlib.vitro.webapp.dao.LinksDao;
import edu.cornell.mannlib.vitro.webapp.dao.LinktypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.NamespaceDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PortalDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.TabDao;
import edu.cornell.mannlib.vitro.webapp.dao.TabIndividualRelationDao;
import edu.cornell.mannlib.vitro.webapp.dao.TabVClassRelationDao;
import edu.cornell.mannlib.vitro.webapp.dao.UserDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * A simple stub for the WebappDaoFactory.
 */
public class WebappDaoFactoryStub implements WebappDaoFactory {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private UserDao userDao;

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public UserDao getUserDao() {
		return userDao;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public Map<String, String> getProperties() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getProperties() not implemented.");
	}

	@Override
	public String checkURI(String uriStr) {
		throw new RuntimeException(
				"WebappDaoFactoryStub.checkURI() not implemented.");
	}

	@Override
	public String checkURI(String uriStr, boolean checkUniqueness) {
		throw new RuntimeException(
				"WebappDaoFactoryStub.checkURI() not implemented.");
	}

	@Override
	public int getLanguageProfile() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getLanguageProfile() not implemented.");
	}

	@Override
	public String getDefaultNamespace() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getDefaultNamespace() not implemented.");
	}

	@Override
	public Set<String> getNonuserNamespaces() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getNonuserNamespaces() not implemented.");
	}

	@Override
	public String[] getPreferredLanguages() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getPreferredLanguages() not implemented.");
	}

	@Override
	public List<String> getCommentsForResource(String resourceURI) {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getCommentsForResource() not implemented.");
	}

	@Override
	public WebappDaoFactory getUserAwareDaoFactory(String userURI) {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getUserAwareDaoFactory() not implemented.");
	}

	@Override
	public String getUserURI() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getUserURI() not implemented.");
	}

	@Override
	public Classes2ClassesDao getClasses2ClassesDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getClasses2ClassesDao() not implemented.");
	}

	@Override
	public DataPropertyDao getDataPropertyDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getDataPropertyDao() not implemented.");
	}

	@Override
	public DatatypeDao getDatatypeDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getDatatypeDao() not implemented.");
	}

	@Override
	public ObjectPropertyDao getObjectPropertyDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getObjectPropertyDao() not implemented.");
	}

	@Override
	public OntologyDao getOntologyDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getOntologyDao() not implemented.");
	}

	@Override
	public VClassDao getVClassDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getVClassDao() not implemented.");
	}

	@Override
	public DataPropertyStatementDao getDataPropertyStatementDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getDataPropertyStatementDao() not implemented.");
	}

	@Override
	public IndividualDao getIndividualDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getIndividualDao() not implemented.");
	}

	@Override
	public ObjectPropertyStatementDao getObjectPropertyStatementDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getObjectPropertyStatementDao() not implemented.");
	}

	@Override
	public ApplicationDao getApplicationDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getApplicationDao() not implemented.");
	}

	@Override
	public PortalDao getPortalDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getPortalDao() not implemented.");
	}

	@Override
	public TabDao getTabDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getTabDao() not implemented.");
	}

	@Override
	public TabIndividualRelationDao getTabs2EntsDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getTabs2EntsDao() not implemented.");
	}

	@Override
	public TabVClassRelationDao getTabs2TypesDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getTabs2TypesDao() not implemented.");
	}

	@Override
	public KeywordIndividualRelationDao getKeys2EntsDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getKeys2EntsDao() not implemented.");
	}

	@Override
	public KeywordDao getKeywordDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getKeywordDao() not implemented.");
	}

	@Override
	public LinksDao getLinksDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getLinksDao() not implemented.");
	}

	@Override
	public LinktypeDao getLinktypeDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getLinktypeDao() not implemented.");
	}

	@Override
	public FlagDao getFlagDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getFlagDao() not implemented.");
	}

	@Override
	public VClassGroupDao getVClassGroupDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getVClassGroupDao() not implemented.");
	}

	@Override
	public PropertyGroupDao getPropertyGroupDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getPropertyGroupDao() not implemented.");
	}

	@Override
	public NamespaceDao getNamespaceDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getNamespaceDao() not implemented.");
	}

	@Override
	public PropertyInstanceDao getPropertyInstanceDao() {
		throw new RuntimeException(
				"WebappDaoFactoryStub.getPropertyInstanceDao() not implemented.");
	}

}
