/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Test that the Jena DAOs write different types of data to the appropriate models.
 * @author bjl23
 *
 */
public class OntModelSegementationTest {

	private WebappDaoFactoryJena wadf;
	@org.junit.Before
	public void setUpWebappDaoFactoryJena() {
		wadf = new WebappDaoFactoryJena(new SimpleOntModelSelector());
	}
	
	@Test
	public void testUserAccountModel() {
		
		UserAccountsDao uadao = wadf.getUserAccountsDao();
		OntModelSelector oms = wadf.getOntModelSelector();

		UserAccount user = new UserAccount();
		user.setFirstName("Chuck");
		user.setLastName("Roast");
		user.setExternalAuthId("chuckroast");
		
		uadao.insertUserAccount(user);
		Assert.assertTrue(oms.getUserAccountsModel().size() > 0);
		Assert.assertTrue(oms.getFullModel().size() == 0);
		Assert.assertTrue(oms.getABoxModel().size() == 0);
		Assert.assertTrue(oms.getTBoxModel().size() == 0);
		Assert.assertTrue(oms.getApplicationMetadataModel().size() == 0);
		
		user.setEmailAddress("todd@somewhere");
		uadao.updateUserAccount(user);
		Assert.assertTrue(oms.getUserAccountsModel().size() > 0);
		Assert.assertTrue(oms.getFullModel().size() == 0);
		Assert.assertTrue(oms.getABoxModel().size() == 0);
		Assert.assertTrue(oms.getTBoxModel().size() == 0);
		Assert.assertTrue(oms.getApplicationMetadataModel().size() == 0);
		
		uadao.deleteUserAccount(user.getUri());
		Assert.assertTrue(oms.getUserAccountsModel().size() == 0);
		Assert.assertTrue(oms.getFullModel().size() == 0);
		Assert.assertTrue(oms.getABoxModel().size() == 0);
		Assert.assertTrue(oms.getTBoxModel().size() == 0);
		Assert.assertTrue(oms.getApplicationMetadataModel().size() == 0);
		
	}
	
	/*
	@Test
	public void testApplicationMetadataModel() throws InsertException {
		
		PortalDao pdao = wadf.getPortalDao();
		TabDao tdao = wadf.getTabDao();
		VClassGroupDao vcgdao = wadf.getVClassGroupDao();
		PropertyGroupDao pgdao = wadf.getPropertyGroupDao();
		OntModelSelector oms = wadf.getOntModelSelector();
	
		this.assertAllModelsExceptAppMetadataAreEmpty(oms);
		
		//insert a portal
		Portal portal = new Portal();
		portal.setPortalId(1);
		portal.setAppName("test portal");
		pdao.insertPortal(portal);
		this.assertMetadataModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		//insert a tab
		Tab tab = new Tab();
		tab.setTitle("test tab");
		int tabId = tdao.insertTab(tab);
		tab.setTabId(tabId);
		this.assertMetadataModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		//insert a classgroup
		VClassGroup group = new VClassGroup();
		group.setURI("http://example.org/classgroup");
		group.setPublicName("test group");
		vcgdao.insertNewVClassGroup(group);
		this.assertMetadataModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		//insert a property group
		PropertyGroup pgroup = new PropertyGroup();
		pgroup.setURI("http://example.org/propertygroup");
		pgroup.setName("test property group");
		pgdao.insertNewPropertyGroup(pgroup);
		this.assertMetadataModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		portal.setAppName("updated portal");
		tab.setTitle("updated tab");
		group.setPublicName("updated group");
		pgroup.setName("updated property group");
		
		pdao.updatePortal(portal);
		this.assertMetadataModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		tdao.updateTab(tab);
		this.assertMetadataModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		vcgdao.updateVClassGroup(group);
		this.assertMetadataModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		pgdao.updatePropertyGroup(pgroup);
		this.assertMetadataModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		tdao.deleteTab(tab);
		vcgdao.deleteVClassGroup(group);
		pgdao.deletePropertyGroup(pgroup);
		
		this.assertAllModelsExceptAppMetadataAreEmpty(oms);
		
	}
	*/
	
	@Test
	public void testTBoxModel() throws InsertException {
		
		OntModelSelector oms = wadf.getOntModelSelector();
		VClassDao vcDao = wadf.getVClassDao();
		ObjectPropertyDao opDao = wadf.getObjectPropertyDao();
		DataPropertyDao dpDao = wadf.getDataPropertyDao();
		OntologyDao oDao = wadf.getOntologyDao();
		
		VClass vclass = new VClass();
		vclass.setURI("http://example.org/vclass");
		vcDao.insertNewVClass(vclass);
		this.assertTBoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		ObjectProperty op = new ObjectProperty();
		op.setURI("http://example.org/objectProperty");
		opDao.insertObjectProperty(op);
		this.assertTBoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		DataProperty dp = new DataProperty();
		dp.setURI("http://example.org/dataProperty");
		dpDao.insertDataProperty(dp);
		this.assertTBoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		Ontology o = new Ontology();
		o.setURI("http://example.org/");
		oDao.insertNewOntology(o);
		this.assertTBoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		vclass.setName("vclass");
		op.setDomainPublic("objectProperty");
		dp.setPublicName("dataProperty");
		o.setName("ontology");
		
		vcDao.updateVClass(vclass);
		this.assertTBoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		opDao.updateObjectProperty(op);
		this.assertTBoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		dpDao.updateDataProperty(dp);
		this.assertTBoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		oDao.updateOntology(o);
		this.assertTBoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		vcDao.deleteVClass(vclass);
		opDao.deleteObjectProperty(op);
		dpDao.deleteDataProperty(dp);
		oDao.deleteOntology(o);
		
		this.assertAllModelsExceptAppMetadataAreEmpty(oms);
			
	}
	
	@Test
	public void testAboxModel() throws InsertException {
		
		OntModelSelector oms = wadf.getOntModelSelector();
		IndividualDao iDao = wadf.getIndividualDao();
		
		Individual ind = new IndividualImpl("http://example.org/individual");
		iDao.insertNewIndividual(ind);
		this.assertABoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		ind.setName("ind");
		iDao.updateIndividual(ind);
		this.assertABoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		iDao.deleteIndividual(ind);
		this.assertAllModelsExceptAppMetadataAreEmpty(oms);
		
	}

	private void assertAllModelsExceptAppMetadataAreEmpty(OntModelSelector oms) {
		//Assert.assertTrue(oms.getApplicationMetadataModel().size() == 0);
		Assert.assertTrue(oms.getFullModel().size() == 0);
		Assert.assertTrue(oms.getABoxModel().size() == 0);
		Assert.assertTrue(oms.getTBoxModel().size() == 0);
		Assert.assertTrue(oms.getUserAccountsModel().size() == 0);
	}
	
	/*
	private void assertMetadataModelNonemptyAndAllOtherModelsAreEmpty(OntModelSelector oms) {
		Assert.assertTrue(oms.getApplicationMetadataModel().size() > 0);
		Assert.assertTrue(oms.getFullModel().size() == oms.getApplicationMetadataModel().size());
		Assert.assertTrue(oms.getABoxModel().size() == 0);
		Assert.assertTrue(oms.getTBoxModel().size() == 0);
		Assert.assertTrue(oms.getUserAccountsModel().size() == 0);
	}
	*/
	
	private void assertTBoxModelNonemptyAndAllOtherModelsAreEmpty(OntModelSelector oms) {
		Assert.assertTrue(oms.getTBoxModel().size() > 0);
		Assert.assertTrue(oms.getFullModel().size() == oms.getTBoxModel().size());
		Assert.assertTrue(oms.getABoxModel().size() == 0);
		Assert.assertTrue(oms.getApplicationMetadataModel().size() == 0);
		Assert.assertTrue(oms.getUserAccountsModel().size() == 0);
	}
	
	private void assertABoxModelNonemptyAndAllOtherModelsAreEmpty(OntModelSelector oms) {
		Assert.assertTrue(oms.getABoxModel().size() > 0);
		Assert.assertTrue(oms.getFullModel().size() == oms.getABoxModel().size());
		Assert.assertTrue(oms.getTBoxModel().size() == 0);
		Assert.assertTrue(oms.getApplicationMetadataModel().size() == 0);
		Assert.assertTrue(oms.getUserAccountsModel().size() == 0);
	}
	
	@Ignore
	@Test
	public void testConcurrency() throws InsertException {
		(new Thread(new ClassLister(wadf))).start();
		(new Thread(new ClassLister(wadf))).start();
		VClass v = null;
		for (int i = 0; i < 50; i++) {
			v = new VClass();
			v.setURI("http://example.org/vclass" + i);
			wadf.getVClassDao().insertNewVClass(v);
		}
		for (int i = 0; i < 500; i++) {
			v.setName("blah " + i);
			wadf.getVClassDao().updateVClass(v);
		}
		
	}
	
	private class ClassLister implements Runnable {
		
		private WebappDaoFactory wadf;
		
		public ClassLister(WebappDaoFactory wadf) {
			this.wadf = wadf;
		}
		
		public void run() {
			
			//int vclassTotal = wadf.getVClassDao().getAllVclasses().size();
			
			for (int i = 0; i < 1500; i++) {
				
				wadf.getVClassDao().getAllVclasses().size();
				
			//	if (vclassTotal != wadf.getVClassDao().getAllVclasses().size()) {
			//		throw new RuntimeException("Inconsistent VClass list size");
			//	}
			}
			
		}
		
	}
	
}
