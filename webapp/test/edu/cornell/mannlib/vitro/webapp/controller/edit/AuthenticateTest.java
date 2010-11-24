/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import static edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State.FORCED_PASSWORD_CHANGE;
import static edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State.LOGGING_IN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import stubs.javax.servlet.ServletConfigStub;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpServletResponseStub;
import stubs.javax.servlet.http.HttpSessionStub;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.AuthenticatorStub;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State;

/**
 * Test the Authentate class.
 */
public class AuthenticateTest extends AbstractTestClass {
	private static final String USER_DBA_NAME = "dbaName";
	private static final String USER_DBA_URI = "dbaURI";
	private static final String USER_DBA_PASSWORD = "dbaPassword";

	private static final String USER_OLDHAND_NAME = "oldHandName";
	private static final String USER_OLDHAND_URI = "oldHandURI";
	private static final String USER_OLDHAND_PASSWORD = "oldHandPassword";
	private static final int USER_OLDHAND_LOGIN_COUNT = 100;

	private static final String URL_LOGIN_PAGE = "http://my.local.site/vivo/"
			+ Controllers.LOGIN;
	private static final String URL_SITE_ADMIN_PAGE = Controllers.SITE_ADMIN;

	private static final String URL_HOME_PAGE = "";
	private static final String URL_SESSION_REDIRECT = "/sessionRedirect";
	private static final String URL_CONTEXT_REDIRECT_LOCAL = "/servletContextRedirect";
	private static final String URL_CONTEXT_REDIRECT_REMOTE = "http://servletContextRedirect";
	private static final String URL_SELF_EDITOR_PAGE = "/individual?uri=selfEditorURI";

	private static final LoginStatusBean LOGIN_STATUS_DBA = new LoginStatusBean(
			USER_DBA_URI, USER_DBA_NAME, LoginStatusBean.DBA,
			AuthenticationSource.INTERNAL);

	private AuthenticatorStub authenticator;
	private ServletContextStub servletContext;
	private ServletConfigStub servletConfig;
	private HttpSessionStub session;
	private HttpServletRequestStub request;
	private HttpServletResponseStub response;
	private Authenticate auth;

	@Before
	public void setup() throws Exception {
		authenticator = AuthenticatorStub.setup();

		authenticator.addUser(createNewDbaUser());
		authenticator.addUser(createOldHandUser());

		servletContext = new ServletContextStub();

		servletConfig = new ServletConfigStub();
		servletConfig.setServletContext(servletContext);

		session = new HttpSessionStub();
		session.setServletContext(servletContext);

		request = new HttpServletRequestStub();
		request.setSession(session);
		request.setRequestUrl(new URL("http://this.that/vivo/siteAdmin"));
		request.setMethod("POST");
		request.setHeader("referer", URL_LOGIN_PAGE);

		response = new HttpServletResponseStub();

		auth = new Authenticate();
		auth.init(servletConfig);

	}

	private User createNewDbaUser() {
		User dbaUser = new User();
		dbaUser.setUsername(USER_DBA_NAME);
		dbaUser.setURI(USER_DBA_URI);
		dbaUser.setRoleURI("50");
		dbaUser.setMd5password(Authenticate.applyMd5Encoding(USER_DBA_PASSWORD));
		dbaUser.setFirstTime(null);
		dbaUser.setLoginCount(0);
		return dbaUser;
	}

	private User createOldHandUser() {
		User ohUser = new User();
		ohUser.setUsername(USER_OLDHAND_NAME);
		ohUser.setURI(USER_OLDHAND_URI);
		ohUser.setRoleURI("1");
		ohUser.setMd5password(Authenticate
				.applyMd5Encoding(USER_OLDHAND_PASSWORD));
		ohUser.setLoginCount(USER_OLDHAND_LOGIN_COUNT);
		ohUser.setFirstTime(new Date(0));
		return ohUser;
	}

	// ----------------------------------------------------------------------
	// the tests
	// ----------------------------------------------------------------------

	@Test
	public void alreadyLoggedIn() {
		LoginStatusBean.setBean(session, LOGIN_STATUS_DBA);

		auth.doPost(request, response);

		assertExpectedRedirect(URL_SITE_ADMIN_PAGE);
		assertNoProcessBean();
		assertExpectedLoginSessions();
	}

	@Test
	public void justGotHere() {
		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertExpectedLoginSessions();
		assertExpectedProcessBean(LOGGING_IN, "", "", "");
	}

	@Test
	public void loggingInNoUsername() {
		setProcessBean(LOGGING_IN);

		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertExpectedLoginSessions();
		assertExpectedProcessBean(LOGGING_IN, "", "",
				"Please enter your email address.");
	}

	@Test
	public void loggingInUsernameNotRecognized() {
		setProcessBean(LOGGING_IN);
		setLoginNameAndPassword("unknownBozo", null);

		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertExpectedLoginSessions();
		assertExpectedProcessBean(LOGGING_IN, "unknownBozo", "",
				"The email or password you entered is incorrect.");
	}

	@Test
	public void loggingInNoPassword() {
		setProcessBean(LOGGING_IN);
		setLoginNameAndPassword(USER_DBA_NAME, null);

		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertExpectedLoginSessions();
		assertExpectedProcessBean(LOGGING_IN, USER_DBA_NAME, "",
				"Please enter your password.");
	}

	@Test
	public void loggingInPasswordIsIncorrect() {
		setProcessBean(LOGGING_IN);
		setLoginNameAndPassword(USER_DBA_NAME, "bogus_password");

		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertExpectedLoginSessions();
		assertExpectedProcessBean(LOGGING_IN, USER_DBA_NAME, "",
				"The email or password you entered is incorrect.");
	}

	@Test
	public void loggingInSuccessfulNotFirstTime() {
		setProcessBean(LOGGING_IN);
		setLoginNameAndPassword(USER_OLDHAND_NAME, USER_OLDHAND_PASSWORD);

		auth.doPost(request, response);

		assertNoProcessBean();
		assertExpectedRedirect(URL_SITE_ADMIN_PAGE);
		assertExpectedLoginSessions(USER_OLDHAND_NAME);
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
		assertExpectedLoginSessions();
		assertExpectedProcessBean(FORCED_PASSWORD_CHANGE, USER_DBA_NAME, "", "");
	}

	@Test
	public void changingPasswordCancel() {
		setProcessBean(FORCED_PASSWORD_CHANGE, USER_DBA_NAME);
		request.addParameter("cancel", "true");

		auth.doPost(request, response);

		assertExpectedRedirect(URL_HOME_PAGE);
		assertExpectedLoginSessions();
		assertNoProcessBean();
	}

	@Test
	public void changingPasswordWrongLength() {
		setProcessBean(FORCED_PASSWORD_CHANGE, USER_DBA_NAME);
		setNewPasswordAttempt("HI", "HI");

		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertExpectedLoginSessions();
		assertExpectedProcessBean(FORCED_PASSWORD_CHANGE, USER_DBA_NAME, "",
				"Please enter a password between 6 and 12 characters in length.");
	}

	@Test
	public void changingPasswordDontMatch() {
		setProcessBean(FORCED_PASSWORD_CHANGE, USER_DBA_NAME);
		setNewPasswordAttempt("LongEnough", "DoesNotMatch");

		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertExpectedLoginSessions();
		assertExpectedProcessBean(FORCED_PASSWORD_CHANGE, USER_DBA_NAME, "",
				"The passwords entered do not match.");
	}

	@Test
	public void changingPasswordSameAsBefore() {
		setProcessBean(FORCED_PASSWORD_CHANGE, USER_DBA_NAME);
		setNewPasswordAttempt(USER_DBA_PASSWORD, USER_DBA_PASSWORD);

		auth.doPost(request, response);

		assertExpectedRedirect(URL_LOGIN_PAGE);
		assertExpectedLoginSessions();
		assertExpectedProcessBean(FORCED_PASSWORD_CHANGE, USER_DBA_NAME, "",
				"Please choose a different password from the "
						+ "temporary one provided initially.");
	}

	@Test
	public void changingPasswordSuccess() {
		setProcessBean(FORCED_PASSWORD_CHANGE, USER_DBA_NAME);
		setNewPasswordAttempt("NewPassword", "NewPassword");

		auth.doPost(request, response);

		assertNoProcessBean();
		assertExpectedRedirect(URL_SITE_ADMIN_PAGE);
		assertExpectedLoginSessions(USER_DBA_NAME);
		assertExpectedPasswordChanges(USER_DBA_NAME, "NewPassword");
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
		authenticator.addEditingPermission(USER_OLDHAND_URI, "selfEditorURI");
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
		LoginProcessBean.setBean(request, processBean);
	}

	private void setProcessBean(State state, String username) {
		LoginProcessBean processBean = new LoginProcessBean();
		processBean.setState(state);
		processBean.setUsername(username);
		LoginProcessBean.setBean(request, processBean);
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
		if (path.startsWith("http://")) {
			assertEquals("absolute redirect", path,
					response.getRedirectLocation());
		} else {
			assertEquals("relative redirect", request.getContextPath() + path,
					response.getRedirectLocation());
		}
	}

	/** This is for explicit redirect URLs that already include context. */
	private void assertExpectedLiteralRedirect(String path) {
		assertEquals("redirect", path, response.getRedirectLocation());
	}

	private void assertNoProcessBean() {
		if (LoginProcessBean.isBean(request)) {
			fail("Process bean: expected <null>, but was <"
					+ LoginProcessBean.getBean(request) + ">");
		}
	}

	private void assertExpectedProcessBean(State state, String username,
			String infoMessage, String errorMessage) {
		if (!LoginProcessBean.isBean(request)) {
			fail("login process bean is null");
		}
		LoginProcessBean bean = LoginProcessBean.getBean(request);
		assertEquals("state", state, bean.getState());
		assertEquals("info message", infoMessage, bean.getInfoMessageAndClear());
		assertEquals("error message", errorMessage, bean.getErrorMessageAndClear());
		assertEquals("username", username, bean.getUsername());
	}

	private void assertExpectedPasswordChanges(String... strings) {
		if ((strings.length % 2) != 0) {
			throw new RuntimeException(
					"supply even number of args: username and password");
		}

		Map<String, String> expected = new HashMap<String, String>();
		for (int i = 0; i < strings.length; i += 2) {
			expected.put(strings[i], strings[i + 1]);
		}

		assertEquals("password changes", expected,
				authenticator.getNewPasswordMap());
	}

	/** How many folks logged in? */
	private void assertExpectedLoginSessions(String... usernames) {
		Set<String> expected = new HashSet<String>(Arrays.asList(usernames));

		Set<String> actualRecorded = new HashSet<String>(
				authenticator.getRecordedLoginUsernames());
		assertEquals("recorded logins", expected, actualRecorded);
	}

	/** Boilerplate login process for the rediret tests. */
	private void loginNotFirstTime() {
		setProcessBean(LOGGING_IN);
		setLoginNameAndPassword(USER_OLDHAND_NAME, USER_OLDHAND_PASSWORD);

		auth.doPost(request, response);

		assertExpectedLoginSessions(USER_OLDHAND_NAME);
		assertNoProcessBean();
	}

	@SuppressWarnings("unused")
	private void showBeans() {
		LoginProcessBean processBean = (LoginProcessBean.isBean(request)) ? LoginProcessBean
				.getBean(request) : null;
		System.out.println("LoginProcessBean=" + processBean);

		LoginStatusBean statusBean = (LoginStatusBean) session
				.getAttribute("loginStatus");
		System.out.println("LoginStatusBean=" + statusBean);
	}

}
