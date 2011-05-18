/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

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
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

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

	private static final String NS_MINE = "http://vivo.mydomain.edu/individual/";

	private static final String URI_USER1 = NS_MINE + "user01";
	private static final String URI_NO_SUCH_USER = NS_MINE + "bogusUser";

	private static final String URI_ROLE1 = NS_MINE + "role1";
	private static final String URI_ROLE2 = NS_MINE + "role2";
	private static final String URI_ROLE3 = NS_MINE + "role3";

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
		UserAccount u = dao.getUserAccountByUri(URI_USER1);
		assertEquals("uri", URI_USER1, u.getUri());
		assertEquals("email", "email@able.edu", u.getEmailAddress());
		assertEquals("firstName", "Zack", u.getFirstName());
		assertEquals("lastName", "Roberts", u.getLastName());
		assertEquals("md5Password", "garbage", u.getMd5Password());
		assertEquals("oldPassword", "", u.getOldPassword());
		assertEquals("linkExpires", 0L, u.getPasswordLinkExpires());
		assertEquals("changeRequired", false, u.isPasswordChangeRequired());
		assertEquals("loginCount", 5, u.getLoginCount());
		assertEquals("status", Status.ACTIVE, u.getStatus());
		assertEquals("externalAuthId", "user1", u.getExternalAuthId());
		assertEquals("permissionSetUris", Collections.singleton(URI_ROLE1),
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
	public void insertUserAccountSuccess() {
		UserAccount in = new UserAccount();
		in.setUri("");
		in.setEmailAddress("my@email.address");
		in.setFirstName("Joe");
		in.setLastName("Bagadonuts");
		in.setMd5Password("passwordHash");
		in.setOldPassword("oldHash");
		in.setPasswordLinkExpires(999966663333L);
		in.setPasswordChangeRequired(true);
		in.setLoginCount(42);
		in.setStatus(Status.INACTIVE);
		in.setExternalAuthId("newUser");
		in.setPermissionSetUris(buildSet(URI_ROLE1, URI_ROLE2));

		String newUri = dao.insertUserAccount(in);

		UserAccount u = dao.getUserAccountByUri(newUri);
		assertEquals("uri", newUri, u.getUri());
		assertEquals("email", "my@email.address", u.getEmailAddress());
		assertEquals("firstName", "Joe", u.getFirstName());
		assertEquals("lastName", "Bagadonuts", u.getLastName());
		assertEquals("md5Password", "passwordHash", u.getMd5Password());
		assertEquals("oldPassword", "oldHash", u.getOldPassword());
		assertEquals("linkExpires", 999966663333L, u.getPasswordLinkExpires());
		assertEquals("changeRequired", true, u.isPasswordChangeRequired());
		assertEquals("loginCount", 42, u.getLoginCount());
		assertEquals("status", Status.INACTIVE, u.getStatus());
		assertEquals("externalAuthId", "newUser", u.getExternalAuthId());
		assertEquals("permissionSetUris", buildSet(URI_ROLE1, URI_ROLE2),
				u.getPermissionSetUris());
	}

	@Test(expected = NullPointerException.class)
	public void insertUserAccountNullUserAccount() {
		dao.insertUserAccount(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void insertUserAccountUriIsNotEmpty() {
		UserAccount in = new UserAccount();
		in.setUri(NS_MINE + "XXXXXX");

		dao.insertUserAccount(in);
	}

	@Test
	public void updateUserAccountSuccess() {
		UserAccount up = new UserAccount();
		up.setUri(URI_USER1);
		up.setEmailAddress("updatedEmail@able.edu");
		up.setFirstName("Ezekiel");
		up.setLastName("Roberts");
		up.setMd5Password("differentHash");
		up.setOldPassword("oldHash");
		up.setPasswordLinkExpires(1L);
		up.setPasswordChangeRequired(false);
		up.setLoginCount(43);
		up.setStatus(Status.ACTIVE);
		up.setExternalAuthId("updatedUser1");
		up.setPermissionSetUris(buildSet(URI_ROLE1, URI_ROLE3));

		dao.updateUserAccount(up);

		UserAccount u = dao.getUserAccountByUri(URI_USER1);
		assertEquals("uri", URI_USER1, u.getUri());
		assertEquals("email", "updatedEmail@able.edu", u.getEmailAddress());
		assertEquals("firstName", "Ezekiel", u.getFirstName());
		assertEquals("lastName", "Roberts", u.getLastName());
		assertEquals("md5Password", "differentHash", u.getMd5Password());
		assertEquals("oldPassword", "oldHash", u.getOldPassword());
		assertEquals("changeExpires", 1L, u.getPasswordLinkExpires());
		assertEquals("changeRequired", false, u.isPasswordChangeRequired());
		assertEquals("loginCount", 43, u.getLoginCount());
		assertEquals("status", Status.ACTIVE, u.getStatus());
		assertEquals("externalAuthId", "updatedUser1", u.getExternalAuthId());
		assertEquals("permissionSetUris", buildSet(URI_ROLE1, URI_ROLE3),
				u.getPermissionSetUris());
	}

	@Test(expected = NullPointerException.class)
	public void updateUserAccountNullUserAccount() {
		dao.updateUserAccount(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void updateUserAccountDoesNotExist() {
		UserAccount up = new UserAccount();
		up.setUri(NS_MINE + "XXXXXX");

		dao.updateUserAccount(up);
	}

	@Test
	public void deleteUserAccountSuccess() {
		dao.deleteUserAccount(URI_USER1);
		StmtIterator stmts = ontModel.listStatements(
				ontModel.getResource(URI_USER1), null, (RDFNode) null);
		if (stmts.hasNext()) {
			String message = "Expecting no statements to remain in the model, but found:\n";
			while (stmts.hasNext()) {
				message += "   " + formatStatement(stmts.next()) + "\n";
			}
			fail(message);
		}
	}

	@Test
	public void deleteUserAccountNullUri() {
		// no complaint, no action.
		dao.deleteUserAccount(null);
	}

	@Test
	public void deleteUserAccountDoesNotExist() {
		// no complaint, no action.
		dao.deleteUserAccount(URI_NO_SUCH_USER);
	}

	@Test
	public void getPermissionSetByUriSuccess() {
		PermissionSet ps = dao.getPermissionSetByUri(URI_ROLE1);
		assertEquals("uri", URI_ROLE1, ps.getUri());
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
		ps1.setUri(URI_ROLE1);
		ps1.setLabel("Role 1");
		ps1.setPermissionUris(Collections.singleton(NS_MINE + "permissionA"));
		expected.add(ps1);

		PermissionSet ps2 = new PermissionSet();
		ps2.setUri(URI_ROLE2);
		ps2.setLabel("Role 2");
		expected.add(ps2);

		assertCorrectPermissionSets(expected, dao.getAllPermissionSets());
	}

	// ----------------------------------------------------------------------
	// helper methods
	// ----------------------------------------------------------------------

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

	@SuppressWarnings("unused")
	private void dumpModelStatements() {
		StmtIterator stmts = ontModel.listStatements();
		while (stmts.hasNext()) {
			Statement stmt = stmts.next();
			System.out.println(formatStatement(stmt));
		}
	}

	private String formatStatement(Statement stmt) {
		return stmt.getSubject().getURI() + " ==> "
				+ stmt.getPredicate().getURI() + " ==> "
				+ stmt.getObject().toString();
	}

}
