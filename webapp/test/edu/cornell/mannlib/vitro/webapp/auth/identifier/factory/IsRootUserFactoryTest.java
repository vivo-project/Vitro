/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.factory;

import static org.junit.Assert.assertEquals;

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
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.IsRootUser;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

/**
 * Can we tell whether a user is logged in as root?
 */
public class IsRootUserFactoryTest extends AbstractTestClass {
	private static final String PLAIN_USER_URI = "http://userUri";
	private static final String ROOT_USER_URI = "http://rootUri";

	private WebappDaoFactoryStub wdf;
	private UserAccountsDaoStub uaDao;

	private ServletContextStub ctx;
	private HttpSessionStub session;
	private HttpServletRequestStub req;

	private IsRootUserFactory factory;

	private IdentifierBundle actualIds;
	private IdentifierBundle expectedIds;

	@Before
	public void setup() {
		UserAccount plainUser = new UserAccount();
		plainUser.setUri(PLAIN_USER_URI);
		
		UserAccount rootUser = new UserAccount();
		rootUser.setUri(ROOT_USER_URI);
		rootUser.setRootUser(true);
		
		uaDao = new UserAccountsDaoStub();
		uaDao.addUser(plainUser);
		uaDao.addUser(rootUser);
		
		wdf = new WebappDaoFactoryStub();
		wdf.setUserAccountsDao(uaDao);

		ctx = new ServletContextStub();
		new ModelAccessFactoryStub().get(ctx).setWebappDaoFactory(wdf);

		session = new HttpSessionStub();
		session.setServletContext(ctx);

		req = new HttpServletRequestStub();
		req.setSession(session);

		factory = new IsRootUserFactory(ctx);
	}

	@Test
	public void notLoggedIn() {
		expectedIds = new ArrayIdentifierBundle();
		actualIds = factory.getIdentifierBundle(req);
		assertEquals("empty bundle", expectedIds, actualIds);
	}

	@Test
	public void loggedInNotRoot() {
		LoginStatusBean lsb = new LoginStatusBean(PLAIN_USER_URI,
				AuthenticationSource.EXTERNAL);
		LoginStatusBean.setBean(session, lsb);

		expectedIds = new ArrayIdentifierBundle();
		actualIds = factory.getIdentifierBundle(req);
		assertEquals("not root", expectedIds, actualIds);
	}

	@Test
	public void loggedInAsRoot() {
		LoginStatusBean lsb = new LoginStatusBean(ROOT_USER_URI,
				AuthenticationSource.EXTERNAL);
		LoginStatusBean.setBean(session, lsb);

		expectedIds = new ArrayIdentifierBundle(IsRootUser.INSTANCE);
		actualIds = factory.getIdentifierBundle(req);
		assertEquals("root", expectedIds, actualIds);
	}

}
