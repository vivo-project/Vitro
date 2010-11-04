/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import static edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State.FORCED_PASSWORD_CHANGE;
import static edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State.LOGGING_IN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;

import stubs.javax.servlet.ServletConfigStub;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpServletResponseStub;
import stubs.javax.servlet.http.HttpSessionStub;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State;

/**
 * TODO
 */
public class AuthenticateTest extends AbstractTestClass {
	private static final String USER_DBA_NAME = "dbaName";
	private static final String USER_DBA_URI = "dbaURI";
	private static final String USER_DBA_PASSWORD = "dbaPassword";

	private static final String USER_OLDHAND_NAME = "oldHandName";
	private static final String USER_OLDHAND_URI = "oldHandURI";
	private static final String USER_OLDHAND_PASSWORD = "oldHandPassword";

	private static final String URL_LOGIN_PAGE = Controllers.LOGIN
			+ "?login=block";
	private static final String URL_SITE_ADMIN_PAGE = Controllers.SITE_ADMIN
			+ "?login=block";
	private static final String URL_HOME_PAGE = "";
	private static final String URL_SESSION_REDIRECT = "/sessionRedirect";
	private static final String URL_CONTEXT_REDIRECT_LOCAL = "/servletContextRedirect";
	private static final String URL_CONTEXT_REDIRECT_REMOTE = "http://servletContextRedirect";
	private static final String URL_SELF_EDITOR_PAGE = "/individual?uri=selfEditorURI";

	private static final LoginStatusBean LOGIN_STATUS_DBA = new LoginStatusBean(
			USER_DBA_URI, USER_DBA_NAME, LoginStatusBean.DBA);

	private static final LoginStatusBean LOGIN_STATUS_OLDHAND = new LoginStatusBean(
			USER_OLDHAND_URI, USER_OLDHAND_NAME, LoginStatusBean.NON_EDITOR);

	private UserDaoStub userDao;
	private WebappDaoFactoryStub webappDaoFactory;
	private ServletContextStub servletContext;
	private ServletConfigStub servletConfig;
	private HttpSessionStub session;
	private HttpServletRequestStub request;
	private HttpServletResponseStub response;
	private Authenticate auth;

	@Before
	public void setup() throws MalformedURLException, ServletException {
		User dbaUser = new User();
		dbaUser.setUsername(USER_DBA_NAME);
		dbaUser.setURI(USER_DBA_URI);
		dbaUser.setRoleURI("50");
		dbaUser.setMd5password(Authenticate.applyMd5Encoding(USER_DBA_PASSWORD));

		User ohUser = new User();
		ohUser.setUsername(USER_OLDHAND_NAME);
		ohUser.setURI(USER_OLDHAND_URI);
		ohUser.setRoleURI("1");
		ohUser.setMd5password(Authenticate
				.applyMd5Encoding(USER_OLDHAND_PASSWORD));
		ohUser.setLoginCount(100);

		userDao = new UserDaoStub();
		userDao.addUser(dbaUser);
		userDao.addUser(ohUser);

		webappDaoFactory = new WebappDaoFactoryStub();
		webappDaoFactory.setUserDao(userDao);

		servletContext = new ServletContextStub();
		servletContext.setAttribute("webappDaoFactory", webappDaoFactory);

		servletConfig = new ServletConfigStub();
		servletConfig.setServletContext(servletContext);

		session = new HttpSessionStub();
		session.setServletContext(servletContext);

		request = new HttpServletRequestStub();
		request.setSession(session);
		request.setRequestUrl(new URL("http://this.that/vivo/siteAdmin"));
		request.setMethod("POST");

		response = new HttpServletResponseStub();

		auth = new Authenticate();
		auth.init(servletConfig);
	}

	@Test
	public void alreadyLoggedIn() {
		LoginStatusBean.setBean(session, LOGIN_STATUS_DBA);

		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertNoProcessBean();
		assertExpectedStatusBean(LOGIN_STATUS_DBA);
	}

	@Test
	public void justGotHere() {
		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertNoStatusBean();

		// TODO Surprise! if no session, we get this:
		// assertNoLoginProcessBean();

		// TODO Surprise! if there is a session, we would have expected this:
		// assertExpectedLoginProcessBean(LOGGING_IN, "", "", "");

		// TODO Surprise! but we get this:
		assertExpectedProcessBean(State.NOWHERE, "", "", "");
	}

	@Test
	public void loggingInNoUsername() {
		setProcessBean(LOGGING_IN);

		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertNoStatusBean();
		assertExpectedProcessBean(LOGGING_IN, "", "",
				"Please enter your email address.");
	}

	@Test
	public void loggingInUsernameNotRecognized() {
		setProcessBean(LOGGING_IN);
		setLoginNameAndPassword("unknownBozo", null);

		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertNoStatusBean();
		assertExpectedProcessBean(LOGGING_IN, "unknownBozo", "",
				"The email or password you entered is incorrect.");
	}

	@Test
	public void loggingInNoPassword() {
		setProcessBean(LOGGING_IN);
		setLoginNameAndPassword(USER_DBA_NAME, null);

		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertNoStatusBean();
		assertExpectedProcessBean(LOGGING_IN, USER_DBA_NAME, "",
				"Please enter your password.");
	}

	@Test
	public void loggingInPasswordIsIncorrect() {
		setProcessBean(LOGGING_IN);
		setLoginNameAndPassword(USER_DBA_NAME, "bogus_password");

		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertNoStatusBean();
		assertExpectedProcessBean(LOGGING_IN, USER_DBA_NAME, "",
				"The email or password you entered is incorrect.");
	}

	@Test
	public void loggingInSuccessfulNotFirstTime() {
		setProcessBean(LOGGING_IN);
		setLoginNameAndPassword(USER_OLDHAND_NAME, USER_OLDHAND_PASSWORD);

		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertExpectedStatusBean(LOGIN_STATUS_OLDHAND);
		assertNoProcessBean();
	}

	// ----------------------------------------------------------------------
	// first-time password change
	// ----------------------------------------------------------------------

	@Test
	public void loggingInSuccessfulFirstTime() {
		setProcessBean(LOGGING_IN);
		setLoginNameAndPassword(USER_DBA_NAME, USER_DBA_PASSWORD);

		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertNoStatusBean();
		assertExpectedProcessBean(FORCED_PASSWORD_CHANGE, USER_DBA_NAME, "", "");
	}

	@Test
	public void changingPasswordCancel() {
		setProcessBean(FORCED_PASSWORD_CHANGE, USER_DBA_NAME);
		request.addParameter("cancel", "true");

		auth.doPost(request, response);

		assertExpectedRedirect(URL_HOME_PAGE);
		assertNoStatusBean();
		assertNoProcessBean();
	}

	@Test
	public void changingPasswordWrongLength() {
		setProcessBean(FORCED_PASSWORD_CHANGE, USER_DBA_NAME);
		setNewPasswordAttempt("HI", "HI");

		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertNoStatusBean();
		assertExpectedProcessBean(FORCED_PASSWORD_CHANGE, USER_DBA_NAME, "",
				"Please enter a password between 6 and 12 characters in length.");
	}

	@Test
	public void changingPasswordDontMatch() {
		setProcessBean(FORCED_PASSWORD_CHANGE, USER_DBA_NAME);
		setNewPasswordAttempt("LongEnough", "DoesNotMatch");

		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertNoStatusBean();
		assertExpectedProcessBean(FORCED_PASSWORD_CHANGE, USER_DBA_NAME, "",
				"The passwords entered do not match.");
	}

	@Test
	public void changingPasswordSameAsBefore() {
		setProcessBean(FORCED_PASSWORD_CHANGE, USER_DBA_NAME);
		setNewPasswordAttempt(USER_DBA_PASSWORD, USER_DBA_PASSWORD);

		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertNoStatusBean();
		assertExpectedProcessBean(FORCED_PASSWORD_CHANGE, USER_DBA_NAME, "",
				"Please choose a different password from the "
						+ "temporary one provided initially.");
	}

	@Test
	public void changingPasswordSuccess() {
		setProcessBean(FORCED_PASSWORD_CHANGE, USER_DBA_NAME);
		setNewPasswordAttempt("NewPassword", "NewPassword");

		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertExpectedStatusBean(LOGIN_STATUS_DBA);
		assertNoProcessBean();
	}

	// ----------------------------------------------------------------------
	// Assorted redirects: these assume a successful non-first-time login.
	// ----------------------------------------------------------------------

	@Test
	public void redirectOnSession() {
		session.setAttribute("postLoginRequest", URL_SESSION_REDIRECT);
		loginNotFirstTime();
		assertExpectedLiteralRedirect(URL_SESSION_REDIRECT);
	}

	@Test
	public void redirectOnServletContext() {
		servletContext.setAttribute("postLoginRequest",
				URL_CONTEXT_REDIRECT_LOCAL);
		loginNotFirstTime();
		assertExpectedRedirect(URL_CONTEXT_REDIRECT_LOCAL);
	}

	@Test
	public void redirectOnServletContextToExternalUrl() {
		servletContext.setAttribute("postLoginRequest",
				URL_CONTEXT_REDIRECT_REMOTE);
		loginNotFirstTime();
		assertExpectedLiteralRedirect(URL_CONTEXT_REDIRECT_REMOTE);
	}

	@Test
	public void redirectSelfEditor() {
		userDao.setIndividualsUserMayEditAs(USER_OLDHAND_URI,
				Collections.singletonList("selfEditorURI"));
		loginNotFirstTime();
		assertExpectedRedirect(URL_SELF_EDITOR_PAGE);
	}

	@Test
	public void redirectNoneOfTheAbove() {
		loginNotFirstTime();
		assertExpectedRedirect(URL_SITE_ADMIN_PAGE);
	}

	// ----------------------------------------------------------------------
	// helper methods
	// ----------------------------------------------------------------------

	private void setProcessBean(State state) {
		LoginProcessBean processBean = new LoginProcessBean();
		processBean.setState(state);
		session.setAttribute(LoginProcessBean.SESSION_ATTRIBUTE, processBean);
	}

	private void setProcessBean(State state, String username) {
		LoginProcessBean processBean = new LoginProcessBean();
		processBean.setState(state);
		processBean.setUsername(username);
		session.setAttribute(LoginProcessBean.SESSION_ATTRIBUTE, processBean);
	}

	private void setLoginNameAndPassword(String loginName, String password) {
		request.addParameter("loginName", loginName);
		request.addParameter("loginPassword", password);
	}

	private void setNewPasswordAttempt(String newPassword,
			String confirmPassword) {
		request.addParameter("newPassword", newPassword);
		request.addParameter("confirmPassword", confirmPassword);
	}

	private void assertExpectedRedirect(String path) {
		assertEquals("redirect", request.getContextPath() + path,
				response.getRedirectLocation());
	}

	/** This is for explicit redirect URLs that already include context. */
	private void assertExpectedLiteralRedirect(String path) {
		assertEquals("redirect", path, response.getRedirectLocation());
	}

	private void assertNoProcessBean() {
		assertNull(session.getAttribute(LoginProcessBean.SESSION_ATTRIBUTE));
	}

	private void assertExpectedProcessBean(State state, String username,
			String infoMessage, String errorMessage) {
		LoginProcessBean bean = (LoginProcessBean) session
				.getAttribute(LoginProcessBean.SESSION_ATTRIBUTE);
		assertNotNull("login process bean", bean);
		assertEquals("state", state, bean.getState());
		assertEquals("info message", infoMessage, bean.getInfoMessage());
		assertEquals("error message", errorMessage, bean.getErrorMessage());
		assertEquals("username", username, bean.getUsername());
	}

	private void assertNoStatusBean() {
		assertNull(session.getAttribute("loginStatus"));
	}

	private void assertExpectedStatusBean(LoginStatusBean expected) {
		LoginStatusBean bean = (LoginStatusBean) session
				.getAttribute("loginStatus");
		assertNotNull("login status bean", bean);
		assertEquals("user URI", expected.getUserURI(), bean.getUserURI());
		assertEquals("user name", expected.getUsername(), bean.getUsername());
		assertEquals("security level", expected.getSecurityLevel(),
				bean.getSecurityLevel());
	}

	/** Boilerplate login process for the rediret tests. */
	private void loginNotFirstTime() {
		setProcessBean(LOGGING_IN);
		setLoginNameAndPassword(USER_OLDHAND_NAME, USER_OLDHAND_PASSWORD);

		auth.doPost(request, response);

		assertExpectedStatusBean(LOGIN_STATUS_OLDHAND);
		assertNoProcessBean();
	}

	@SuppressWarnings("unused")
	private void showBeans() {
		LoginProcessBean processBean = (LoginProcessBean) session
				.getAttribute(LoginProcessBean.SESSION_ATTRIBUTE);
		System.out.println("LoginProcessBean=" + processBean);
		LoginStatusBean statusBean = (LoginStatusBean) session
				.getAttribute("loginStatus");
		System.out.println("LoginStatusBean=" + statusBean);
	}

}
