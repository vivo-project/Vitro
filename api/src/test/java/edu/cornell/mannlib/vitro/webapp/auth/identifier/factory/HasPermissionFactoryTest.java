/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.factory;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import stubs.edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryStub;
import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccessFactoryStub;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpSessionStub;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasPermission;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.Permission;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionRegistry;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

/**
 * Can we tell whether a user is logged in as root?
 */
public class HasPermissionFactoryTest extends AbstractTestClass {
	private static final String USER_URI = "http://userUri";
	private UserAccount user;

	private LoginStatusBean lsb;

	private PermissionSet emptyPublicPermissionSet;
	private PermissionSet publicPermissionSet1;
	private PermissionSet publicPermissionSet2;
	private PermissionSet emptyLoggedInPermissionSet;
	private PermissionSet loggedInPermissionSet1;
	private PermissionSet loggedInPermissionSet2;

	private Permission permissionP1a;
	private Permission permissionP1b;
	private Permission permissionP2;
	private Permission permissionLI1;
	private Permission permissionLI2a;
	private Permission permissionLI2b;

	private WebappDaoFactoryStub wdf;
	private UserAccountsDaoStub uaDao;

	private ServletContextStub ctx;
	private HttpSessionStub session;
	private HttpServletRequestStub req;

	private HasPermissionFactory factory;

	private IdentifierBundle actualIds;
	private IdentifierBundle expectedIds;

	@Before
	public void setup() {
		user = new UserAccount();
		user.setUri(USER_URI);

		lsb = new LoginStatusBean(USER_URI, AuthenticationSource.INTERNAL);

		uaDao = new UserAccountsDaoStub();
		uaDao.addUser(user);

		wdf = new WebappDaoFactoryStub();
		wdf.setUserAccountsDao(uaDao);

		ctx = new ServletContextStub();
		new ModelAccessFactoryStub().get(ctx).setWebappDaoFactory(wdf);

		session = new HttpSessionStub();
		session.setServletContext(ctx);

		req = new HttpServletRequestStub();
		req.setSession(session);

		factory = new HasPermissionFactory(ctx);

		preparePermissions();
		preparePermissionSets();
	}

	private void preparePermissions() {
		permissionP1a = new MyPermission("permissionP1a");
		permissionP1b = new MyPermission("permissionP1b");
		permissionP2 = new MyPermission("permissionP2");
		permissionLI1 = new MyPermission("permissionLI1");
		permissionLI2a = new MyPermission("permissionLI2a");
		permissionLI2b = new MyPermission("permissionLI2b");
		PermissionRegistry.createRegistry(
				ctx,
				list(permissionP1a, permissionP1b, permissionP2, permissionLI1,
						permissionLI2a, permissionLI2b));
	}

	private void preparePermissionSets() {
		emptyPublicPermissionSet = new PermissionSet();
		emptyPublicPermissionSet.setUri("java:emptyPS");
		emptyPublicPermissionSet.setLabel("emptyPublicPermissionSet");
		emptyPublicPermissionSet.setForPublic(true);

		publicPermissionSet1 = new PermissionSet();
		publicPermissionSet1.setUri("java:publicPS1");
		publicPermissionSet1.setLabel("publicPermissionSet1");
		publicPermissionSet1.setForPublic(true);
		setPermissions(publicPermissionSet1, permissionP1a, permissionP1b);

		publicPermissionSet2 = new PermissionSet();
		publicPermissionSet2.setUri("java:publicPS2");
		publicPermissionSet2.setLabel("publicPermissionSet2");
		publicPermissionSet2.setForPublic(true);
		setPermissions(publicPermissionSet2, permissionP2);

		emptyLoggedInPermissionSet = new PermissionSet();
		emptyLoggedInPermissionSet.setUri("java:emptyPS");
		emptyLoggedInPermissionSet.setLabel("emptyLoggedInPermissionSet");

		loggedInPermissionSet1 = new PermissionSet();
		loggedInPermissionSet1.setUri("java:loggedInPS1");
		loggedInPermissionSet1.setLabel("loggedInPermissionSet1");
		setPermissions(loggedInPermissionSet1, permissionLI1);

		loggedInPermissionSet2 = new PermissionSet();
		loggedInPermissionSet2.setUri("java:loggedInPS2");
		loggedInPermissionSet2.setLabel("loggedInPermissionSet2");
		setPermissions(loggedInPermissionSet2, permissionLI2a, permissionLI2b);

		uaDao.addPermissionSet(emptyLoggedInPermissionSet);
		uaDao.addPermissionSet(loggedInPermissionSet1);
		uaDao.addPermissionSet(loggedInPermissionSet2);
		// "public" permission sets are added for specific tests.
	}

	// ----------------------------------------------------------------------
	// the tests
	// ----------------------------------------------------------------------

	@Test
	public void notLoggedInNoPublicSets() {
		expectedIds = new ArrayIdentifierBundle();
		actualIds = factory.getIdentifierBundle(req);
		assertEquals("no public sets", expectedIds, actualIds);
	}

	@Test
	public void notLoggedInEmptyPublicSet() {
		uaDao.addPermissionSet(emptyPublicPermissionSet);

		expectedIds = new ArrayIdentifierBundle();
		actualIds = factory.getIdentifierBundle(req);
		assertEquals("empty public set", expectedIds, actualIds);
	}

	@Test
	public void notLoggedInOnePublicSet() {
		uaDao.addPermissionSet(publicPermissionSet1);

		expectedIds = new ArrayIdentifierBundle(id(permissionP1a),
				id(permissionP1b));
		actualIds = factory.getIdentifierBundle(req);
		assertEqualIds("one public set", expectedIds, actualIds);
	}

	@Test
	public void notLoggedInTwoPublicSets() {
		uaDao.addPermissionSet(publicPermissionSet1);
		uaDao.addPermissionSet(publicPermissionSet2);

		expectedIds = new ArrayIdentifierBundle(id(permissionP1a),
				id(permissionP1b), id(permissionP2));
		actualIds = factory.getIdentifierBundle(req);
		assertEqualIds("two public sets", expectedIds, actualIds);
	}

	@Test
	public void loggedInNoSets() {
		LoginStatusBean.setBean(session, lsb);

		expectedIds = new ArrayIdentifierBundle();
		actualIds = factory.getIdentifierBundle(req);
		assertEquals("no logged in sets", expectedIds, actualIds);
	}

	@Test
	public void loggedInEmptySet() {
		LoginStatusBean.setBean(session, lsb);
		user.setPermissionSetUris(list(emptyLoggedInPermissionSet.getUri()));

		expectedIds = new ArrayIdentifierBundle();
		actualIds = factory.getIdentifierBundle(req);
		assertEquals("empty logged in set", expectedIds, actualIds);
	}

	@Test
	public void loggedInOneSet() {
		LoginStatusBean.setBean(session, lsb);
		user.setPermissionSetUris(list(loggedInPermissionSet1.getUri()));

		expectedIds = new ArrayIdentifierBundle(id(permissionLI1));
		actualIds = factory.getIdentifierBundle(req);
		assertEqualIds("one logged in set", expectedIds, actualIds);
	}

	@Test
	public void loggedInTwoSets() {
		LoginStatusBean.setBean(session, lsb);
		user.setPermissionSetUris(list(loggedInPermissionSet1.getUri(),
				loggedInPermissionSet2.getUri()));

		expectedIds = new ArrayIdentifierBundle(id(permissionLI1),
				id(permissionLI2a), id(permissionLI2b));
		actualIds = factory.getIdentifierBundle(req);
		assertEqualIds("two logged in sets", expectedIds, actualIds);
	}

	// ----------------------------------------------------------------------
	// helper methods
	// ----------------------------------------------------------------------

	private void setPermissions(PermissionSet ps, Permission... permissions) {
		List<String> uris = new ArrayList<String>();
		for (Permission p : permissions) {
			uris.add(p.getUri());
		}
		ps.setPermissionUris(uris);
	}

	private HasPermission id(Permission p) {
		return new HasPermission(p);
	}

	private <T> List<T> list(T... elements) {
		List<T> l = new ArrayList<T>();
		for (T element : elements) {
			l.add(element);
		}
		return l;
	}

	private void assertEqualIds(String message, IdentifierBundle expected,
			IdentifierBundle actual) {
		Set<HasPermission> expectedSet = new HashSet<HasPermission>();
		for (Identifier id : expected) {
			expectedSet.add((HasPermission) id);
		}
		Set<HasPermission> actualSet = new HashSet<HasPermission>();
		for (Identifier id : actual) {
			actualSet.add((HasPermission) id);
		}
		assertEqualSets(message, expectedSet, actualSet);
	}

	private static class MyPermission extends Permission {
		public MyPermission(String uri) {
			super(uri);
		}

		@Override
		public boolean isAuthorized(RequestedAction whatToAuth) {
			return false;
		}

	}
}
