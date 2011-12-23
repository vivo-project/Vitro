/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.factory;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import stubs.edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryStub;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpSessionStub;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.IsUser;

/**
 * The simplest of the IdentifierBundleFactory classes.
 */
public class IsUserFactoryTest extends AbstractTestClass {
	private static final String USER_URI = "http://userUri";

	private WebappDaoFactoryStub wdf;

	private ServletContextStub ctx;
	private HttpSessionStub session;
	private HttpServletRequestStub req;

	private IsUserFactory factory;

	private IdentifierBundle actualIds;
	private IdentifierBundle expectedIds;

	@Before
	public void setup() {
		wdf = new WebappDaoFactoryStub();

		ctx = new ServletContextStub();
		ctx.setAttribute("webappDaoFactory", wdf);

		session = new HttpSessionStub();
		session.setServletContext(ctx);

		req = new HttpServletRequestStub();
		req.setSession(session);

		factory = new IsUserFactory(ctx);
	}

	@Test
	public void notLoggedIn() {
		expectedIds = new ArrayIdentifierBundle();
		actualIds = factory.getIdentifierBundle(req);
		assertEquals("empty bundle", expectedIds, actualIds);
	}

	@Test
	public void loggedIn() {
		LoginStatusBean lsb = new LoginStatusBean(USER_URI,
				AuthenticationSource.EXTERNAL);
		LoginStatusBean.setBean(session, lsb);

		expectedIds = new ArrayIdentifierBundle(new IsUser(USER_URI));
		actualIds = factory.getIdentifierBundle(req);
		assertEquals("user id", expectedIds, actualIds);
	}

}
