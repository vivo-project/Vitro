/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount.Status;

/**
 * TODO
 */
public class UserAccountsDaoJenaTest extends AbstractTestClass {
	/**
	 * Where the model statements are stored for this test.
	 */
	private static final String N3_DATA_FILENAME = "resources/UserAccountsDaoJenaTest.n3";

	private static final String NS_AUTH = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#";
	private static final String NS_MINE = "http://vivo.mydomain.edu/individual/";

	private OntModel ontModel;
	private WebappDaoFactoryJena wadf;
	private UserAccountsDaoJena dao;

	@Before
	public void setup() throws IOException {
		InputStream stream = UserAccountsDaoJenaTest.class
				.getResourceAsStream(N3_DATA_FILENAME);
		Model model = ModelFactory.createDefaultModel();
		model.read(stream, null, "N3");
		stream.close();

		ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM,
				model);
		ontModel.prepare();

		wadf = new WebappDaoFactoryJena(ontModel);
		dao = new UserAccountsDaoJena(wadf);
	}

	@Test
	public void getUserAccountByUriSuccess() {
		UserAccount u = dao.getUserAccountByUri(NS_MINE + "user01");
		assertEquals("uri", NS_MINE + "user01", u.getUri());
		assertEquals("email", "email@able.edu", u.getEmailAddress());
		assertEquals("firstName", "Zack", u.getFirstName());
		assertEquals("lastName", "Roberts", u.getLastName());
		assertEquals("md5Password", "garbage", u.getMd5Password());
		assertEquals("oldPassword", "", u.getOldPassword());
		assertEquals("changeExpires", 0L, u.getPasswordLinkExpires());
		assertEquals("changeRequired", false, u.isPasswordChangeRequired());
		assertEquals("loginCount", 5, u.getLoginCount());
		assertEquals("status", Status.ACTIVE, u.getStatus());
		assertEquals("permissionSetUris",
				Collections.singleton(NS_MINE + "role1"),
				u.getPermissionSetUris());
	}

	@Test
	public void getUserAccountByUriNull() {
		UserAccount u = dao.getUserAccountByUri(null);
		assertNull("null result", u);
	}

	@Test
	public void getUserAccountByUriNotFound() {
		UserAccount u = dao.getUserAccountByUri("bogusUri");
		assertNull("null result", u);
	}

	@Test
	public void getPermissionSetByUriSuccess() {
		PermissionSet ps = dao.getPermissionSetByUri(NS_MINE + "role1");
		assertEquals("uri", NS_MINE + "role1", ps.getUri());
		assertEquals("label", "Role 1", ps.getLabel());
		assertEquals("permissionUris",
				Collections.singleton(NS_MINE + "permissionA"),
				ps.getPermissionUris());
	}

	@Test
	public void getPermissionSetByUriNull() {
		PermissionSet ps = dao.getPermissionSetByUri(null);
		assertNull("null result", ps);
	}

	@Test
	public void getPermissionSetByUriNotFound() {
		PermissionSet ps = dao.getPermissionSetByUri("bogusUri");
		assertNull("null result", ps);
	}

	@Test
	public void getAllPermissionSets() {
		setLoggerLevel(JenaBaseDao.class, Level.DEBUG);

		Set<PermissionSet> expected = new HashSet<PermissionSet>();

		PermissionSet ps1 = new PermissionSet();
		ps1.setUri(NS_MINE + "role1");
		ps1.setLabel("Role 1");
		ps1.setPermissionUris(Collections.singleton(NS_MINE + "permissionA"));
		expected.add(ps1);

		PermissionSet ps2 = new PermissionSet();
		ps2.setUri(NS_MINE + "role2");
		ps2.setLabel("Role 2");
		expected.add(ps2);

		assertCorrectPermissionSets(expected, dao.getAllPermissionSets());
	}

	private void assertCorrectPermissionSets(Set<PermissionSet> expected,
			Collection<PermissionSet> actual) {
		Set<Map<String, Object>> expectedMaps = new HashSet<Map<String, Object>>();
		for (PermissionSet ps : expected) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("uri", ps.getUri());
			map.put("label", ps.getLabel());
			map.put("permissions", ps.getPermissionUris());
			expectedMaps.add(map);
		}

		Set<Map<String, Object>> actualMaps = new HashSet<Map<String, Object>>();
		for (PermissionSet ps : actual) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("uri", ps.getUri());
			map.put("label", ps.getLabel());
			map.put("permissions", ps.getPermissionUris());
			actualMaps.add(map);
		}

		assertEquals("all permission sets", expectedMaps, actualMaps);
	}
}
