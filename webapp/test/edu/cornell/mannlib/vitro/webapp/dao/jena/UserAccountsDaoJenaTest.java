/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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

	private static final Set<String> EMPTY = Collections.<String> emptySet();

	/**
	 * Where the model statements are stored for this test.
	 */
	private static final String N3_DATA_FILENAME = "resources/UserAccountsDaoJenaTest.n3";

	private static final String NS_MINE = "http://vivo.mydomain.edu/individual/";

	private static final String URI_USER1 = NS_MINE + "user01";
	private static final String URI_NO_SUCH_USER = NS_MINE + "bogusUser";

	private static final String EMAIL_USER1 = "email@able.edu";
	private static final String EMAIL_NO_SUCH_USER = NS_MINE
			+ "bogus@email.com";

	private static final String URI_ROLE1 = NS_MINE + "role1";
	private static final String URI_ROLE2 = NS_MINE + "role2";
	private static final String URI_ROLE3 = NS_MINE + "role3";

	private static final String URI_PROFILE1 = NS_MINE + "profile1";
	private static final String URI_PROFILE2 = NS_MINE + "profile2";

	private OntModel ontModel;
	private WebappDaoFactoryJena wadf;
	private UserAccountsDaoJena dao;

	private UserAccount user1;
	private UserAccount userNew;

	private UserAccount userA;
	private UserAccount userB;
	private UserAccount userC;

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

	@Before
	public void createUserAccountValues() {
		user1 = userAccount(URI_USER1, "email@able.edu", "Zack", "Roberts",
				"garbage", "", 0L, false, 5, 12345678L, Status.ACTIVE, "user1",
				false, collection(URI_ROLE1), false, EMPTY);
		userNew = userAccount("", "email@here", "Joe", "Blow", "XXXX", "YYYY",
				0L, false, 1, 0L, Status.ACTIVE, "jblow", false, EMPTY, false,
				EMPTY);

		userA = userAccount("", "aahern@here", "Alf", "Ahern", "XXXX", "YYYY",
				0L, false, 1, 0L, Status.ACTIVE, "aahern", false, EMPTY, false,
				collection(URI_PROFILE1));
		userB = userAccount("", "email@here", "Betty", "Boop", "XXXX", "YYYY",
				0L, false, 1, 0L, Status.ACTIVE, "bboop", false, EMPTY, false,
				collection(URI_PROFILE1, URI_PROFILE2));
		userC = userAccount("", "ccallas@here", "Charlie", "Callas", "XXXX",
				"YYYY", 0L, false, 1, 0L, Status.ACTIVE, "ccallas", false,
				EMPTY, false, collection(URI_PROFILE2));
	}

	// ----------------------------------------------------------------------
	// Tests for UserAccount methods.
	// ----------------------------------------------------------------------

	@Test
	public void getUserAccountByUriSuccess() {
		UserAccount u = dao.getUserAccountByUri(URI_USER1);
		assertEqualAccounts(user1, u);
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
	public void getUserAccountByUriWrongType() {
		UserAccount u = dao.getUserAccountByUri(URI_ROLE1);
		// System.out.println(u);
		assertNull("null result", u);
	}

	@Test
	public void getUserAccountByEmailSuccess() {
		UserAccount u = dao.getUserAccountByEmail(EMAIL_USER1);
		assertEquals("uri", URI_USER1, u.getUri());
	}

	@Test
	public void getUserAccountByEmailNull() {
		UserAccount u = dao.getUserAccountByEmail(null);
		assertEquals("uri", null, u);
	}

	@Test
	public void getUserAccountByEmailNotFound() {
		UserAccount u = dao.getUserAccountByEmail(EMAIL_NO_SUCH_USER);
		assertEquals("uri", null, u);
	}

	@Test
	public void insertUserAccountSuccess() {
		UserAccount raw = userAccount(userNew);
		String uri = dao.insertUserAccount(raw);
		UserAccount processed = dao.getUserAccountByUri(uri);
		assertEqualAccounts(raw, processed);
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
		UserAccount orig = userAccount(URI_USER1, "updatedEmail@able.edu",
				"Ezekiel", "Roberts", "differentHash", "oldHash", 1L, false,
				43, 1020304050607080L, Status.ACTIVE, "updatedUser1", false,
				collection(URI_ROLE1, URI_ROLE3), false, EMPTY);

		dao.updateUserAccount(orig);

		UserAccount updated = dao.getUserAccountByUri(URI_USER1);
		assertEqualAccounts(orig, updated);
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
	public void insertUserAccountWithProxies() {
		userNew.setProxiedIndividualUris(collection(
				NS_MINE + "userNewProxyOne", NS_MINE + "userNewProxyTwo"));
		String userUri = dao.insertUserAccount(userNew);

		UserAccount inserted = dao.getUserAccountByUri(userUri);
		assertEqualAccounts(userNew, inserted);
	}

	@Test
	public void updateUserAccountWithProxies() {
		UserAccount beforeAccount = dao.getUserAccountByUri(URI_USER1);
		user1.setProxiedIndividualUris(collection(NS_MINE + "newProxyForUser1"));

		dao.updateUserAccount(user1);

		UserAccount updated = dao.getUserAccountByUri(URI_USER1);
		assertEqualAccounts(user1, updated);
	}

	// ----------------------------------------------------------------------
	// Tests for proxy-related methods
	// ----------------------------------------------------------------------

	@Test
	public void getProxyEditorsFirst() {
		String profileOne = NS_MINE + "userNewProxyOne";
		String profileTwo = NS_MINE + "userNewProxyTwo";
		userNew.setProxiedIndividualUris(collection(profileOne, profileTwo));

		String userUri = dao.insertUserAccount(userNew);
		UserAccount user = dao.getUserAccountByUri(userUri);

		assertExpectedAccountUris("proxy for profile one",
				Collections.singleton(user),
				dao.getUserAccountsWhoProxyForPage(profileOne));
	}

	@Test
	public void getProxyEditorsSecond() {
		String profileOne = NS_MINE + "userNewProxyOne";
		String profileTwo = NS_MINE + "userNewProxyTwo";
		userNew.setProxiedIndividualUris(collection(profileOne, profileTwo));

		String userUri = dao.insertUserAccount(userNew);
		UserAccount user = dao.getUserAccountByUri(userUri);

		assertExpectedAccountUris("proxy for profile two",
				Collections.singleton(user),
				dao.getUserAccountsWhoProxyForPage(profileTwo));
	}

	@Test
	public void getProxyEditorsBogus() {
		String profileOne = NS_MINE + "userNewProxyOne";
		String profileTwo = NS_MINE + "userNewProxyTwo";
		String bogusProfile = NS_MINE + "bogus";
		userNew.setProxiedIndividualUris(collection(profileOne, profileTwo));

		dao.insertUserAccount(userNew);

		assertExpectedAccountUris("proxy for bogus profile",
				Collections.<UserAccount> emptySet(),
				dao.getUserAccountsWhoProxyForPage(bogusProfile));
	}

	@Test
	public void setProxyEditorsOnProfile() {
		String uriA = dao.insertUserAccount(userA);
		String uriB = dao.insertUserAccount(userB);
		String uriC = dao.insertUserAccount(userC);

		dao.setProxyAccountsOnProfile(URI_PROFILE1, collection(uriB, uriC));

		assertExpectedProxies("userA", collection(),
				dao.getUserAccountByUri(uriA).getProxiedIndividualUris());
		assertExpectedProxies("userB", collection(URI_PROFILE1, URI_PROFILE2),
				dao.getUserAccountByUri(uriB).getProxiedIndividualUris());
		assertExpectedProxies("userC", collection(URI_PROFILE1, URI_PROFILE2),
				dao.getUserAccountByUri(uriC).getProxiedIndividualUris());
	}

	// ----------------------------------------------------------------------
	// Tests for PermissionSet methods
	// ----------------------------------------------------------------------

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
	public void getPermissionSetByUriWrongType() {
		PermissionSet ps = dao.getPermissionSetByUri(URI_USER1);
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

	private Collection<String> collection(String... args) {
		return Arrays.asList(args);
	}

	private UserAccount userAccount(String uri, String emailAddress,
			String firstName, String lastName, String md5Password,
			String oldPassword, long passwordLinkExpires,
			boolean passwordChangeRequired, int loginCount, long lastLoginTime,
			Status status, String externalAuthId, boolean externalAuthOnly,
			Collection<String> permissionSetUris, boolean rootUser,
			Collection<String> proxiedIndividualUris) {
		UserAccount ua = new UserAccount();
		ua.setUri(uri);
		ua.setEmailAddress(emailAddress);
		ua.setFirstName(firstName);
		ua.setLastName(lastName);
		ua.setMd5Password(md5Password);
		ua.setOldPassword(oldPassword);
		ua.setPasswordLinkExpires(passwordLinkExpires);
		ua.setPasswordChangeRequired(passwordChangeRequired);
		ua.setLoginCount(loginCount);
		ua.setLastLoginTime(lastLoginTime);
		ua.setStatus(status);
		ua.setExternalAuthId(externalAuthId);
		ua.setExternalAuthOnly(externalAuthOnly);
		ua.setPermissionSetUris(permissionSetUris);
		ua.setRootUser(rootUser);
		ua.setProxiedIndividualUris(proxiedIndividualUris);
		return ua;
	}

	private UserAccount userAccount(UserAccount in) {
		UserAccount out = new UserAccount();
		out.setUri(in.getUri());
		out.setEmailAddress(in.getEmailAddress());
		out.setFirstName(in.getFirstName());
		out.setLastName(in.getLastName());
		out.setMd5Password(in.getMd5Password());
		out.setOldPassword(in.getOldPassword());
		out.setPasswordLinkExpires(in.getPasswordLinkExpires());
		out.setPasswordChangeRequired(in.isPasswordChangeRequired());
		out.setLoginCount(in.getLoginCount());
		out.setLastLoginTime(in.getLastLoginTime());
		out.setStatus(in.getStatus());
		out.setExternalAuthId(in.getExternalAuthId());
		out.setExternalAuthOnly(in.isExternalAuthOnly());
		out.setPermissionSetUris(in.getPermissionSetUris());
		out.setRootUser(in.isRootUser());
		out.setProxiedIndividualUris(in.getProxiedIndividualUris());
		return out;
	}

	private void assertEqualAccounts(UserAccount e, UserAccount a) {
		if (!e.getUri().equals("")) {
			assertEquals("uri", e.getUri(), a.getUri());
		}
		assertEquals("email", e.getEmailAddress(), a.getEmailAddress());
		assertEquals("first name", e.getFirstName(), a.getFirstName());
		assertEquals("last name", e.getLastName(), a.getLastName());
		assertEquals("password", e.getMd5Password(), a.getMd5Password());
		assertEquals("old password", e.getOldPassword(), a.getOldPassword());
		assertEquals("link expires", e.getPasswordLinkExpires(),
				a.getPasswordLinkExpires());
		assertEquals("password change", e.isPasswordChangeRequired(),
				a.isPasswordChangeRequired());
		assertEquals("login count", e.getLoginCount(), a.getLoginCount());
		assertEquals("last login", e.getLastLoginTime(), a.getLastLoginTime());
		assertEquals("status", e.getStatus(), a.getStatus());
		assertEquals("external ID", e.getExternalAuthId(),
				a.getExternalAuthId());
		assertEquals("external only", e.isExternalAuthOnly(),
				a.isExternalAuthOnly());
		assertEquals("permission sets", e.getPermissionSetUris(),
				a.getPermissionSetUris());
		assertEquals("root user", e.isRootUser(), a.isRootUser());
		assertEquals("proxied URIs", e.getProxiedIndividualUris(),
				a.getProxiedIndividualUris());
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

	private void assertExpectedAccountUris(String label,
			Set<UserAccount> expectedUserAccounts,
			Collection<UserAccount> actualUserAccounts) {
		Set<String> expectedUris = new HashSet<String>();
		for (UserAccount ua : expectedUserAccounts) {
			expectedUris.add(ua.getUri());
		}

		Set<String> actualUris = new HashSet<String>();
		for (UserAccount ua : actualUserAccounts) {
			actualUris.add(ua.getUri());
		}

		assertEqualSets(label, expectedUris, actualUris);
	}

	private void assertExpectedProxies(String label,
			Collection<String> expected, Set<String> actual) {
		Set<String> expectedSet = new HashSet<String>(expected);
		assertEqualSets(label, expectedSet, actual);
	}

	@SuppressWarnings("unused")
	private void dumpModelStatements() {
		StmtIterator stmts = ontModel.listStatements();
		while (stmts.hasNext()) {
			Statement stmt = stmts.next();
			// System.out.println(formatStatement(stmt));
		}
	}

	private String formatStatement(Statement stmt) {
		return stmt.getSubject().getURI() + " ==> "
				+ stmt.getPredicate().getURI() + " ==> "
				+ stmt.getObject().toString();
	}

}
