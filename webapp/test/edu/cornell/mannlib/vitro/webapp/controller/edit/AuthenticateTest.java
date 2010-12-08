/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import static edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State.FORCED_PASSWORD_CHANGE;
import static edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State.LOGGING_IN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import stubs.javax.servlet.ServletConfigStub;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpServletResponseStub;
import stubs.javax.servlet.http.HttpSessionStub;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.AuthenticatorStub;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State;

/**
 * <pre>
 * Test the Authenticate class.
 * 
 * This uses parameterized unit tests. Several sets of test data are set up, and
 * then each test is run with each set of data.
 * 
 * Each set of test data includes 
 *   information about the user who is logging in, 
 *   information about how the user began the login process
 *   information about where the user should end up
 *   
 * We run the tests with these users:
 *   A DBA who has never logged in before
 *   A DBA who has logged in before
 *   A self-editor who has logged in before
 *   A self-editor wannabe, who has never logged in and has no profile.
 * 
 * We run the tests with the assumption that the user started from:
 *   The login page
 *   A page that holds the login widget
 *   A forced login
 *   A login link on some page
 * </pre>
 */
@RunWith(value = Parameterized.class)
public class AuthenticateTest extends AbstractTestClass {

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

	private static class HowDidWeGetHere {
		final String afterLoginUrl;
		final boolean returnParameterSet;
		final String referrer;

		public HowDidWeGetHere(String afterLoginUrl,
				boolean returnParameterSet, String referrer) {
			this.afterLoginUrl = afterLoginUrl;
			this.returnParameterSet = returnParameterSet;
			this.referrer = referrer;
		}

		@Override
		public String toString() {
			return "HowDidWeGetHere[afterLoginUrl=" + afterLoginUrl
					+ ", returnParameterSet=" + returnParameterSet
					+ ", referrer=" + referrer + "]";
		}
	}

	private static class WhereTo {
		final String expectedContinueUrl;
		final String expectedCompletionUrl;
		final String expectedCancelUrl;

		public WhereTo(String expectedContinueUrl,
				String expectedCompletionUrl, String expectedCancelUrl) {
			this.expectedContinueUrl = expectedContinueUrl;
			this.expectedCompletionUrl = expectedCompletionUrl;
			this.expectedCancelUrl = expectedCancelUrl;
		}

		@Override
		public String toString() {
			return "WhereTo[expectedContinueUrl=" + expectedContinueUrl
					+ ", expectedCompletionUrl=" + expectedCompletionUrl
					+ ", expectedCancelUrl=" + expectedCancelUrl + "]";
		}
	}

	// ----------------------------------------------------------------------
	// The parameters
	// ----------------------------------------------------------------------

	// --------- Pages ----------

	/** the login page */
	private static final String URL_LOGIN = "/vivo/login";

	/** some page with a login widget on it. */
	private static final String URL_WIDGET = "/vivo/widgetPage";

	/** a restricted page that forces a login. */
	private static final String URL_RESTRICTED = "/vivo/otherPage";

	/** a page with a login link. */
	private static final String URL_LINK = "/vivo/linkPage";

	// pages that we might end up on.
	private static final String URL_HOME = "/vivo";
	private static final String URL_SITE_ADMIN = "/vivo/siteAdmin";
	private static final String URL_SELF_PROFILE = "/vivo/individual?uri=old_self_associated_uri";

	// --------- Users ----------

	/** A DBA who has never logged in (forces password change). */
	private static final UserInfo NEW_DBA = new UserInfo("new_dba_name",
			"new_dba_uri", "new_dba_pw", 50, 0);

	/** A DBA who has logged in before. */
	private static final UserInfo OLD_DBA = new UserInfo("old_dba_name",
			"old_dba_uri", "old_dba_pw", 50, 5);

	/** A self-editor who has logged in before and has a profile. */
	private static final UserInfo OLD_SELF = new UserInfo("old_self_name",
			"old_self_uri", "old_self_pw", 1, 100);

	/** A self-editor who has never logged in and has no profile. */
	private static final UserInfo NEW_STRANGER = new UserInfo(
			"new_stranger_name", "new_stranger_uri", "stranger_pw", 1, 0);

	// --------- Starting circumstances ----------

	private static final HowDidWeGetHere FROM_FORCED = new HowDidWeGetHere(
			URL_RESTRICTED, false, URL_RESTRICTED);

	private static final HowDidWeGetHere FROM_LINK = new HowDidWeGetHere(null,
			true, URL_LINK);

	private static final HowDidWeGetHere FROM_WIDGET = new HowDidWeGetHere(
			null, false, URL_WIDGET);

	private static final HowDidWeGetHere FROM_LOGIN = new HowDidWeGetHere(null,
			false, URL_LOGIN);

	// --------- All sets of test data ----------

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] {
				{ NEW_DBA, FROM_FORCED,
						new WhereTo(URL_LOGIN, URL_RESTRICTED, URL_RESTRICTED) }, // 0
				{ NEW_DBA, FROM_LINK,
						new WhereTo(URL_LOGIN, URL_LINK, URL_LINK) }, // 1
				{ NEW_DBA, FROM_WIDGET,
						new WhereTo(URL_WIDGET, URL_WIDGET, URL_WIDGET) }, // 2
				{ NEW_DBA, FROM_LOGIN,
						new WhereTo(URL_LOGIN, URL_SITE_ADMIN, URL_HOME) }, // 3
				{ OLD_DBA, FROM_FORCED,
						new WhereTo(URL_LOGIN, URL_RESTRICTED, null) }, // 4
				{ OLD_DBA, FROM_LINK, new WhereTo(URL_LOGIN, URL_LINK, null) }, // 5
				{ OLD_DBA, FROM_WIDGET,
						new WhereTo(URL_WIDGET, URL_WIDGET, null) }, // 6
				{ OLD_DBA, FROM_LOGIN,
						new WhereTo(URL_LOGIN, URL_SITE_ADMIN, null) }, // 7
				{ OLD_SELF, FROM_FORCED,
						new WhereTo(URL_LOGIN, URL_SELF_PROFILE, null) }, // 8
				{ OLD_SELF, FROM_LINK,
						new WhereTo(URL_LOGIN, URL_SELF_PROFILE, null) }, // 9
				{ OLD_SELF, FROM_WIDGET,
						new WhereTo(URL_WIDGET, URL_SELF_PROFILE, null) }, // 10
				{ OLD_SELF, FROM_LOGIN,
						new WhereTo(URL_LOGIN, URL_SELF_PROFILE, null) }, // 11
				{ NEW_STRANGER, FROM_FORCED,
						new WhereTo(URL_LOGIN, URL_HOME, URL_RESTRICTED) }, // 12
				{ NEW_STRANGER, FROM_LINK,
						new WhereTo(URL_LOGIN, URL_HOME, URL_LINK) }, // 13
				{ NEW_STRANGER, FROM_WIDGET,
						new WhereTo(URL_WIDGET, URL_HOME, URL_WIDGET) }, // 14
				{ NEW_STRANGER, FROM_LOGIN,
						new WhereTo(URL_LOGIN, URL_HOME, URL_HOME) } // 15
		};
		return Arrays.asList(data);
	}

	// ----------------------------------------------------------------------
	// Instance variables and setup
	// ----------------------------------------------------------------------

	private AuthenticatorStub authenticator;
	private ServletContextStub servletContext;
	private ServletConfigStub servletConfig;
	private HttpSessionStub session;
	private HttpServletRequestStub request;
	private HttpServletResponseStub response;
	private Authenticate auth;

	private final UserInfo userInfo;
	private final HowDidWeGetHere urlBundle;
	private final WhereTo whereTo;

	public AuthenticateTest(UserInfo userInfo, HowDidWeGetHere urlBundle,
			WhereTo whereTo) {
		this.userInfo = userInfo;
		this.urlBundle = urlBundle;
		this.whereTo = whereTo;
	}

	@Before
	public void setup() throws Exception {
		authenticator = AuthenticatorStub.setup();
		authenticator.addUser(createUserFromUserInfo());
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
		request.setHeader("referer", urlBundle.referrer);
		if (urlBundle.afterLoginUrl != null) {
			request.addParameter("afterLogin", urlBundle.afterLoginUrl);
		}
		if (urlBundle.returnParameterSet) {
			request.addParameter("return", "");
		}

		response = new HttpServletResponseStub();

		auth = new Authenticate();
		auth.init(servletConfig);
	}

	private User createUserFromUserInfo() {
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
	// The tests
	// ----------------------------------------------------------------------

	@Test
	public void justGotHere() {
		auth.doPost(request, response);

		assertProcessBean(LOGGING_IN, "", "", "");
		assertNewLoginSessions();
		assertRedirectToContinueUrl();
	}

	@Test
	public void loggingInNoUsername() {
		setProcessBean(LOGGING_IN, null);

		auth.doPost(request, response);

		assertProcessBean(LOGGING_IN, "", "",
				"Please enter your email address.");
		assertNewLoginSessions();
		assertRedirectToContinueUrl();
	}

	@Test
	public void loggingInUsernameNotRecognized() {
		setProcessBean(LOGGING_IN, null);
		setLoginNameAndPassword("unknownBozo", null);

		auth.doPost(request, response);

		assertProcessBean(LOGGING_IN, "unknownBozo", "",
				"The email or password you entered is incorrect.");
		assertNewLoginSessions();
		assertRedirectToContinueUrl();
	}

	@Test
	public void loggingInNoPassword() {
		setProcessBean(LOGGING_IN, null);
		setLoginNameAndPassword(userInfo.username, null);

		auth.doPost(request, response);

		assertProcessBean(LOGGING_IN, userInfo.username, "",
				"Please enter your password.");
		assertNewLoginSessions();
		assertRedirectToContinueUrl();
	}

	@Test
	public void loggingInPasswordIsIncorrect() {
		setProcessBean(LOGGING_IN, null);
		setLoginNameAndPassword(userInfo.username, "bogus_password");

		auth.doPost(request, response);

		assertProcessBean(LOGGING_IN, userInfo.username, "",
				"The email or password you entered is incorrect.");
		assertNewLoginSessions();
		assertRedirectToContinueUrl();
	}

	@Test
	public void loggingInSuccessful() {
		if (userInfo.loginCount == 0) {
			testLoginFirstTime();
		} else {
			testLoginNotFirstTime();
		}
	}

	private void testLoginFirstTime() {
		setProcessBean(LOGGING_IN, null);
		setLoginNameAndPassword(userInfo.username, userInfo.password);

		auth.doPost(request, response);

		assertProcessBean(FORCED_PASSWORD_CHANGE, userInfo.username, "", "");
		assertNewLoginSessions();
		assertRedirectToContinueUrl();
	}

	private void testLoginNotFirstTime() {
		setProcessBean(LOGGING_IN, null);
		setLoginNameAndPassword(userInfo.username, userInfo.password);

		auth.doPost(request, response);

		assertNoProcessBean();
		assertNewLoginSessions(userInfo.username);
		assertRedirectToCompletionUrl();
	}

	@Test
	public void changingPasswordCancel() {
		// Only valid for first-time login.
		if (userInfo.loginCount == 0) {
			setProcessBean(FORCED_PASSWORD_CHANGE, userInfo.username);
			request.addParameter("cancel", "true");

			auth.doPost(request, response);

			assertNoProcessBean();
			assertNewLoginSessions();
			assertRedirectToCancelUrl();
		}
	}

	@Test
	public void changingPasswordWrongLength() {
		// Only valid for first-time login.
		if (userInfo.loginCount == 0) {
			setProcessBean(FORCED_PASSWORD_CHANGE, userInfo.username);
			setNewPasswordAttempt("HI", "HI");

			auth.doPost(request, response);

			assertRedirectToContinueUrl();
			assertNewLoginSessions();
			assertProcessBean(FORCED_PASSWORD_CHANGE, userInfo.username, "",
					"Please enter a password between 6 and 12 characters in length.");
		}
	}

	@Test
	public void changingPasswordDontMatch() {
		// Only valid for first-time login.
		if (userInfo.loginCount == 0) {
			setProcessBean(FORCED_PASSWORD_CHANGE, userInfo.username);
			setNewPasswordAttempt("LongEnough", "DoesNotMatch");

			auth.doPost(request, response);

			assertRedirectToContinueUrl();
			assertNewLoginSessions();
			assertProcessBean(FORCED_PASSWORD_CHANGE, userInfo.username, "",
					"The passwords entered do not match.");
		}
	}

	@Test
	public void changingPasswordSameAsBefore() {
		// Only valid for first-time login.
		if (userInfo.loginCount == 0) {
			setProcessBean(FORCED_PASSWORD_CHANGE, userInfo.username);
			setNewPasswordAttempt(userInfo.password, userInfo.password);

			auth.doPost(request, response);

			assertRedirectToContinueUrl();
			assertNewLoginSessions();
			assertProcessBean(FORCED_PASSWORD_CHANGE, userInfo.username, "",
					"Please choose a different password from the "
							+ "temporary one provided initially.");
		}
	}

	@Test
	public void changingPasswordSuccess() {
		// Only valid for first-time login.
		if (userInfo.loginCount == 0) {
			setProcessBean(FORCED_PASSWORD_CHANGE, userInfo.username);
			setNewPasswordAttempt("NewPassword", "NewPassword");

			auth.doPost(request, response);

			assertRedirectToCompletionUrl();
			assertNewLoginSessions(userInfo.username);
			assertNoProcessBean();
			assertPasswordChanges(userInfo.username, "NewPassword");
		}
	}

	@Test
	public void alreadyLoggedIn() {
		LoginStatusBean statusBean = new LoginStatusBean(userInfo.uri,
				userInfo.username, userInfo.securityLevel,
				AuthenticationSource.INTERNAL);
		LoginStatusBean.setBean(session, statusBean);

		auth.doPost(request, response);

		assertRedirectToCompletionUrl();
		assertNoProcessBean();
		assertNewLoginSessions();
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void setProcessBean(State state, String username) {
		LoginProcessBean processBean = new LoginProcessBean();
		processBean.setState(state);
		if (username != null) {
			processBean.setUsername(username);
		}

		// the urls come directly from the url bundle every time.
		if (urlBundle.afterLoginUrl != null) {
			processBean.setAfterLoginUrl(urlBundle.afterLoginUrl);
			processBean.setLoginPageUrl(URL_LOGIN);
		} else if (urlBundle.returnParameterSet) {
			processBean.setAfterLoginUrl(urlBundle.referrer);
			processBean.setLoginPageUrl(URL_LOGIN);
		} else {
			processBean.setAfterLoginUrl(null);
			processBean.setLoginPageUrl(urlBundle.referrer);
		}
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

	private void assertRedirectToContinueUrl() {
		assertRedirect(whereTo.expectedContinueUrl);
	}

	private void assertRedirectToCompletionUrl() {
		assertRedirect(whereTo.expectedCompletionUrl);
	}

	private void assertRedirectToCancelUrl() {
		assertRedirect(whereTo.expectedCancelUrl);
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

	private void assertNoProcessBean() {
		if (LoginProcessBean.isBean(request)) {
			fail("Process bean: expected <null>, but was <"
					+ LoginProcessBean.getBean(request) + ">");
		}
	}

	private void assertProcessBean(State state, String username,
			String infoMessage, String errorMessage) {
		if (!LoginProcessBean.isBean(request)) {
			fail("login process bean is null");
		}
		LoginProcessBean bean = LoginProcessBean.getBean(request);
		assertEquals("state", state, bean.getState());
		assertEquals("info message", infoMessage, bean.getInfoMessageAndClear());
		assertEquals("error message", errorMessage,
				bean.getErrorMessageAndClear());
		assertEquals("username", username, bean.getUsername());

		// This should represent the URL bundle, every time.
		String expectedAfterLoginUrl = (urlBundle.returnParameterSet) ? urlBundle.referrer
				: urlBundle.afterLoginUrl;
		assertEquals("after login URL", expectedAfterLoginUrl,
				bean.getAfterLoginUrl());
	}

	/** What logins were completed in this test? */
	private void assertNewLoginSessions(String... usernames) {
		Set<String> expected = new HashSet<String>(Arrays.asList(usernames));

		Set<String> actualRecorded = new HashSet<String>(
				authenticator.getRecordedLoginUsernames());
		assertEquals("recorded logins", expected, actualRecorded);
	}

	/** What passwords were changed in this test? */
	private void assertPasswordChanges(String... strings) {
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
