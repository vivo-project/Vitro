/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import static edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State.LOGGING_IN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.Date;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import stubs.javax.servlet.ServletConfigStub;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpServletResponseStub;
import stubs.javax.servlet.http.HttpSessionStub;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.AuthenticatorStub;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State;

/**
 */
public class AuthenticateTest extends AbstractTestClass {

	private AuthenticatorStub authenticator;
	private ServletContextStub servletContext;
	private ServletConfigStub servletConfig;
	private HttpSessionStub session;
	private HttpServletRequestStub request;
	private HttpServletResponseStub response;
	private Authenticate auth;

	// ----------------------------------------------------------------------
	// Setup and data
	// ----------------------------------------------------------------------

	/** A DBA who has never logged in (forces password change). */
	private static final String NEW_DBA_NAME = "new_dba_name";
	private static final String NEW_DBA_PW = "new_dba_pw";
	private static final UserInfo NEW_DBA = new UserInfo(NEW_DBA_NAME,
			"new_dba_uri", NEW_DBA_PW, 50, 0);

	/** A DBA who has logged in before. */
	private static final UserInfo OLD_DBA = new UserInfo("old_dba_name",
			"old_dba_uri", "old_dba_pw", 50, 5);

	/** A self-editor who has logged in before and has a profile. */
	private static final UserInfo OLD_SELF = new UserInfo("old_self_name",
			"old_self_uri", "old_self_pw", 1, 100);

	/** A self-editor who has never logged in and has no profile. */
	private static final UserInfo NEW_STRANGER = new UserInfo(
			"new_stranger_name", "new_stranger_uri", "stranger_pw", 1, 0);

	/** the login page */
	private static final String URL_LOGIN = "/vivo/login";

	/** some page with a login widget on it. */
	private static final String URL_WIDGET = "/vivo/widgetPage";

	/** a restricted page that forces a login. */
	private static final String URL_RESTRICTED = "/vivo/resrictedPage";

	/** a page with a login link. */
	private static final String URL_WITH_LINK = "/vivo/linkPage";

	// pages that we might end up on.
	private static final String URL_HOME = "/vivo";
	private static final String URL_SITE_ADMIN = "/vivo/siteAdmin";
	private static final String URL_SELF_PROFILE = "/vivo/individual?uri=old_self_associated_uri";

	// A page we will never start from or end on.
	private static final String URL_SOMEWHERE_ELSE = "/vivo/somewhereElse";

	private static final String NO_USER = "";
	private static final String NO_MSG = "";

	@Before
	public void setup() throws Exception {
		authenticator = AuthenticatorStub.setup();
		authenticator.addUser(createUserFromUserInfo(NEW_DBA));
		authenticator.addUser(createUserFromUserInfo(OLD_DBA));
		authenticator.addUser(createUserFromUserInfo(OLD_SELF));
		authenticator.addUser(createUserFromUserInfo(NEW_STRANGER));
		authenticator.setAssociatedUri(OLD_SELF.username,
				"old_self_associated_uri");

		servletContext = new ServletContextStub();

		servletConfig = new ServletConfigStub();
		servletConfig.setServletContext(servletContext);

		session = new HttpSessionStub();
		session.setServletContext(servletContext);

		request = new HttpServletRequestStub();
		request.setSession(session);
		request.setRequestUrl(new URL("http://this.that/vivo/authenticate"));
		request.setMethod("POST");

		response = new HttpServletResponseStub();

		auth = new Authenticate();
		auth.init(servletConfig);
	}

	private User createUserFromUserInfo(UserInfo userInfo) {
		User user = new User();
		user.setUsername(userInfo.username);
		user.setURI(userInfo.uri);
		user.setRoleURI(String.valueOf(userInfo.securityLevel));
		user.setMd5password(Authenticate.applyMd5Encoding(userInfo.password));
		user.setLoginCount(userInfo.loginCount);
		if (userInfo.loginCount > 0) {
			user.setFirstTime(new Date(0));
		}
		return user;
	}

	// ----------------------------------------------------------------------
	// ENTRY TESTS
	// ----------------------------------------------------------------------

	/** The "return" parameter is set, so we return to the referrer. */
	@Test
	public void enterFromALoginLink() {
		setRequestFromLoginLink(URL_WITH_LINK);

		doTheRequest();

		assertProcessBean(LOGGING_IN, NO_USER, NO_MSG, NO_MSG, URL_LOGIN,
				URL_WITH_LINK);
		assertRedirect(URL_LOGIN);
	}

	/** The "return" parameter is set, but there is no referrer. */
	@Test
	public void enterFromABookmarkOfTheLoginLink() {
		setRequestFromLoginLink(null);

		doTheRequest();

		assertProcessBean(LOGGING_IN, NO_USER, NO_MSG, NO_MSG, URL_LOGIN,
				URL_LOGIN);
		assertRedirect(URL_LOGIN);
	}

	/** The "afterLoginUrl" parameter is set, pointing to the restricted page. */
	@Test
	public void enterFromARestrictedPage() {
		setRequestFromRestrictedPage(URL_RESTRICTED);

		doTheRequest();

		assertProcessBean(LOGGING_IN, NO_USER, NO_MSG, NO_MSG, URL_LOGIN,
				URL_RESTRICTED);
		assertRedirect(URL_LOGIN);
	}

	/** The user supplies username/password but there is no process bean. */
	@Test
	public void enterFromAWidgetPage() {
		setRequestFromWidgetPage(URL_WIDGET);

		doTheRequest();

		assertProcessBean(LOGGING_IN, NO_USER, NO_MSG, NO_MSG, URL_WIDGET,
				URL_WIDGET);
		assertRedirect(URL_WIDGET);
	}

	/** A page with a widget, but treated specially. */
	@Test
	public void enterFromTheLoginPage() {
		setRequestFromWidgetPage(URL_LOGIN);

		doTheRequest();

		assertProcessBean(LOGGING_IN, NO_USER, NO_MSG, NO_MSG, URL_LOGIN,
				URL_LOGIN);
		assertRedirect(URL_LOGIN);
	}

	// ----------------------------------------------------------------------
	// RESTART LOGIN TESTS
	//
	// Each of these should "hijack" the login that was already in progress.
	// ----------------------------------------------------------------------

	/** The "return" parameter is set, so we detect the restart. */
	@Ignore
	@Test
	public void restartFromALoginLink() {
		setProcessBean(LOGGING_IN, "username", URL_LOGIN, URL_SOMEWHERE_ELSE);
		enterFromALoginLink();
	}

	/** The "return" parameter is set, so we detect the restart. */
	@Ignore
	@Test
	public void restartFromABookmarkOfTheLoginLink() {
		setProcessBean(LOGGING_IN, "username", URL_LOGIN, URL_SOMEWHERE_ELSE);
		enterFromABookmarkOfTheLoginLink();
	}

	/** The "afterLoginUrl" parameter is set, so we detect the restart. */
	@Ignore
	@Test
	public void restartFromARestrictedPage() {
		setProcessBean(LOGGING_IN, "username", URL_LOGIN, URL_SOMEWHERE_ELSE);
		enterFromARestrictedPage();
	}

	/** The referrer is not the loginProcessPage, so we detect the restart. */
	@Ignore
	@Test
	public void restartFromADifferentWidgetPage() {
		setProcessBean(LOGGING_IN, "username", URL_LOGIN, URL_SOMEWHERE_ELSE);
		enterFromAWidgetPage();
	}

	/** The referrer is not the loginProcessPage, so we detect the restart. */
	@Ignore
	@Test
	public void restartFromTheLoginPageWhenWeWereUsingAWidgetPage() {
		setProcessBean(LOGGING_IN, "username", URL_SOMEWHERE_ELSE,
				URL_SOMEWHERE_ELSE);
		enterFromTheLoginPage();
	}

	// ----------------------------------------------------------------------
	// PROCESS TESTS
	// ----------------------------------------------------------------------

	@Test
	public void loggingInNoUsername() {
		setProcessBean(LOGGING_IN, NO_USER, URL_LOGIN, URL_WITH_LINK);

		doTheRequest();

		assertProcessBean(LOGGING_IN, NO_USER, NO_MSG,
				"Please enter your email address.", URL_LOGIN, URL_WITH_LINK);
		assertRedirectToLoginProcessPage();
	}

	@Test
	public void loggingInUsernameNotRecognized() {
		setProcessBean(LOGGING_IN, NO_USER, URL_LOGIN, URL_WITH_LINK);
		setLoginNameAndPassword("unknownBozo", null);

		doTheRequest();

		assertProcessBean(LOGGING_IN, "unknownBozo", NO_MSG,
				"The email or password you entered is incorrect.", URL_LOGIN,
				URL_WITH_LINK);
		assertRedirectToLoginProcessPage();
	}

	@Test
	public void loggingInNoPassword() {
		setProcessBean(LOGGING_IN, NO_USER, URL_LOGIN, URL_WITH_LINK);
		setLoginNameAndPassword(NEW_DBA_NAME, null);

		doTheRequest();

		assertProcessBean(LOGGING_IN, NEW_DBA_NAME, NO_MSG,
				"Please enter your password.", URL_LOGIN,
				URL_WITH_LINK);
		assertRedirectToLoginProcessPage();
	}

	@Ignore
	@Test
	public void loggingInPasswordIsIncorrect() {
		fail("loggingInPasswordIsIncorrect not implemented");
	}

	@Ignore
	@Test
	public void loggingInSuccessful() {
		fail("loggingInSuccessful not implemented");
	}

	@Ignore
	@Test
	public void loggingInSuccessfulFirstTime() {
		fail("loggingInSuccessfulFirstTime not implemented");
	}

	@Ignore
	@Test
	public void changingPasswordCancel() {
		fail("changingPasswordCancel not implemented");
	}

	@Ignore
	@Test
	public void changingPasswordWrongLength() {
		fail("changingPasswordWrongLength not implemented");
	}

	@Ignore
	@Test
	public void changingPasswordDontMatch() {
		fail("changingPasswordDontMatch not implemented");
	}

	@Ignore
	@Test
	public void changingPasswordSameAsBefore() {
		fail("changingPasswordSameAsBefore not implemented");
	}

	@Ignore
	@Test
	public void changingPasswordSuccess() {
		fail("changingPasswordSuccess not implemented");
	}

	@Ignore
	@Test
	public void alreadyLoggedIn() {
		fail("alreadyLoggedIn not implemented");
	}

	// ----------------------------------------------------------------------
	// EXIT TESTS
	// ----------------------------------------------------------------------
	@Ignore
	@Test
	public void exitSelfEditor() {
		fail("exitSelfEditor not implemented");
	}

	@Ignore
	@Test
	public void exitUnrecognizedSelfEditor() {
		fail("exitUnrecognizedSelfEditor not implemented");
	}

	@Ignore
	@Test
	public void exitDba() {
		fail("exitDbaFromLoginLink not implemented");
	}

	/**
	 * TODO
	 * 
	 * <pre>
	 * INTERRUPT TESTS (RESTARTS):
	 *   Establish a specific process bean.
	 *   Set up the request with parameters and referrer.
	 *   Call the servlet.
	 *   Examine the redirect.
	 *   Examine the process bean.
	 *   
	 * PROCESS TESTS:
	 *   Establish a specific process bean.
	 *   Set up the request with parameters and referrer.
	 *   Call the servlet.
	 *   Examine the redirect.
	 *   Examine the process bean (and perhaps the status bean).
	 *   
	 * EXIT TESTS:
	 *   Mimic the simple success, but with different users.
	 *   Establish a process bean that reflects the entry.
	 *   Call the servlet.
	 *   Examine the redirect.
	 *   Confirm that the status bean is as expected, and there is no process bean.
	 * </pre>
	 */

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void setRequestFromLoginLink(String urlWithLink) {
		request.addParameter("return", "");
		request.setHeader("referer", urlWithLink);
	}

	private void setRequestFromRestrictedPage(String urlRestricted) {
		request.addParameter("afterLogin", urlRestricted);
		request.setHeader("referer", urlRestricted);
	}

	private void setRequestFromWidgetPage(String urlWidget) {
		request.setHeader("referer", urlWidget);
	}

	private void setProcessBean(State state, String username,
			String loginProcessUrl, String afterLoginUrl) {
		LoginProcessBean bean = LoginProcessBean.getBean(request);
		bean.setState(state);
		bean.setUsername(username);
		bean.setLoginPageUrl(loginProcessUrl);
		bean.setAfterLoginUrl(afterLoginUrl);
	}

	private void setLoginNameAndPassword(String loginName, String password) {
		request.addParameter("loginName", loginName);
		request.addParameter("loginPassword", password);
	}

	private void doTheRequest() {
		auth.doPost(request, response);
	}

	private void assertProcessBean(State state, String username,
			String infoMessage, String errorMessage, String loginProcessUrl,
			String afterLoginUrl) {
		if (!LoginProcessBean.isBean(request)) {
			fail("login process bean is null");
		}
		LoginProcessBean bean = LoginProcessBean.getBean(request);

		assertEquals("state", state, bean.getState());
		assertEquals("username", username, bean.getUsername());

		assertEquals("info message", infoMessage, bean.getInfoMessageAndClear());
		assertEquals("error message", errorMessage,
				bean.getErrorMessageAndClear());

		assertEquals("login process URL", loginProcessUrl,
				bean.getLoginPageUrl());
		assertEquals("after login URL", afterLoginUrl, bean.getAfterLoginUrl());
	}

	private void assertRedirect(String path) {
		if (path.startsWith("http://")) {
			assertEquals("absolute redirect", path,
					response.getRedirectLocation());
		} else {
			assertEquals("relative redirect", path,
					response.getRedirectLocation());
		}
	}

	private void assertRedirectToLoginProcessPage() {
		assertRedirect(LoginProcessBean.getBean(request).getLoginPageUrl());
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private static class UserInfo {
		final String username;
		final String uri;
		final String password;
		final int securityLevel;
		final int loginCount;

		public UserInfo(String username, String uri, String password,
				int securityLevel, int loginCount) {
			this.username = username;
			this.uri = uri;
			this.password = password;
			this.securityLevel = securityLevel;
			this.loginCount = loginCount;
		}

		@Override
		public String toString() {
			return "UserInfo[username=" + username + ", uri=" + uri
					+ ", password=" + password + ", securityLevel="
					+ securityLevel + ", loginCount=" + loginCount + "]";
		}
	}

}
