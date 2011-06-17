/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import static edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionSetsLoader.URI_DBA;
import static edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionSetsLoader.URI_SELF_EDITOR;
import static edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State.FORCED_PASSWORD_CHANGE;
import static edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State.LOGGING_IN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import stubs.edu.cornell.mannlib.vitro.webapp.config.ConfigurationPropertiesStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.IndividualDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryStub;
import stubs.javax.servlet.ServletConfigStub;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpServletResponseStub;
import stubs.javax.servlet.http.HttpSessionStub;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ActiveIdentifierBundleFactories;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.CommonIdentifierBundleFactory;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.AuthenticatorStub;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State;

/**
 */
public class AuthenticateTest extends AbstractTestClass {

	private AuthenticatorStub authenticator;
	private ServletContextStub servletContext;
	private WebappDaoFactoryStub webappDaoFactory;
	private UserAccountsDaoStub userAccountsDao;
	private IndividualDaoStub individualDao;
	private ServletConfigStub servletConfig;
	private HttpSessionStub session;
	private HttpServletRequestStub request;
	private HttpServletResponseStub response;
	private Authenticate auth;
	private LoginProcessBean initialProcessBean;

	// ----------------------------------------------------------------------
	// Setup and data
	// ----------------------------------------------------------------------

	/** A DBA who has never logged in (forces password change). */
	private static final String NEW_DBA_NAME = "new_dba_name";
	private static final String NEW_DBA_PW = "new_dba_pw";
	private static final UserInfo NEW_DBA = new UserInfo(NEW_DBA_NAME,
			"new_dba_uri", NEW_DBA_PW, URI_DBA, 0);

	/** A DBA who has logged in before. */
	private static final String OLD_DBA_NAME = "old_dba_name";
	private static final String OLD_DBA_PW = "old_dba_pw";
	private static final String OLD_DBA_URI = "old_dba_uri";
	private static final UserInfo OLD_DBA = new UserInfo(OLD_DBA_NAME,
			OLD_DBA_URI, OLD_DBA_PW, URI_DBA, 5);

	/** A self-editor who has logged in before and has a profile. */
	private static final String OLD_SELF_NAME = "old_self_name";
	private static final String OLD_SELF_PW = "old_self_pw";
	private static final UserInfo OLD_SELF = new UserInfo(OLD_SELF_NAME,
			"old_self_uri", OLD_SELF_PW, URI_SELF_EDITOR, 100);

	/** A self-editor who has logged in before but has no profile. */
	private static final String OLD_STRANGER_NAME = "old_stranger_name";
	private static final String OLD_STRANGER_PW = "stranger_pw";
	private static final UserInfo OLD_STRANGER = new UserInfo(
			OLD_STRANGER_NAME, "old_stranger_uri", OLD_STRANGER_PW,
			URI_SELF_EDITOR, 20);

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
		authenticator.addUser(createUserFromUserInfo(OLD_STRANGER));
		authenticator.setAssociatedUri(OLD_SELF.username,
				"old_self_associated_uri");

		userAccountsDao = new UserAccountsDaoStub();
		userAccountsDao.addUser(createUserFromUserInfo(NEW_DBA));
		userAccountsDao.addUser(createUserFromUserInfo(OLD_DBA));
		userAccountsDao.addUser(createUserFromUserInfo(OLD_SELF));
		userAccountsDao.addUser(createUserFromUserInfo(OLD_STRANGER));

		individualDao = new IndividualDaoStub();
		
		webappDaoFactory = new WebappDaoFactoryStub();
		webappDaoFactory.setUserAccountsDao(userAccountsDao);
		webappDaoFactory.setIndividualDao(individualDao);

		servletContext = new ServletContextStub();
		servletContext.setAttribute("webappDaoFactory", webappDaoFactory);

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

		setLoggerLevel(ConfigurationProperties.class, Level.WARN);
		new ConfigurationPropertiesStub().setBean(servletContext);
		
		ActiveIdentifierBundleFactories.addFactory(servletContext,
				new CommonIdentifierBundleFactory(servletContext));
	}

	private UserAccount createUserFromUserInfo(UserInfo userInfo) {
		UserAccount user = new UserAccount();
		user.setEmailAddress(userInfo.username);
		user.setUri(userInfo.uri);
		user.setPermissionSetUris(userInfo.permissionSetUris);
		user.setMd5Password(Authenticator.applyMd5Encoding(userInfo.password));
		user.setLoginCount(userInfo.loginCount);
		user.setPasswordChangeRequired(userInfo.loginCount == 0);
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
				"Please enter your password.", URL_LOGIN, URL_WITH_LINK);
		assertRedirectToLoginProcessPage();
	}

	@Test
	public void loggingInPasswordIsIncorrect() {
		setProcessBean(LOGGING_IN, NO_USER, URL_LOGIN, URL_WITH_LINK);
		setLoginNameAndPassword(NEW_DBA_NAME, "bogus_password");

		doTheRequest();

		assertProcessBean(LOGGING_IN, NEW_DBA_NAME, NO_MSG,
				"The email or password you entered is incorrect.", URL_LOGIN,
				URL_WITH_LINK);
		assertRedirectToLoginProcessPage();
	}

	@Test
	public void loggingInSuccessful() {
		setProcessBean(LOGGING_IN, NO_USER, URL_LOGIN, URL_WITH_LINK);
		setLoginNameAndPassword(OLD_DBA_NAME, OLD_DBA_PW);

		doTheRequest();

		assertNoProcessBean();
		assertNewLoginSessions(OLD_DBA_NAME);
		assertRedirectToAfterLoginPage();
	}

	@Test
	public void loggingInForcesPasswordChange() {
		setProcessBean(LOGGING_IN, NO_USER, URL_LOGIN, URL_WITH_LINK);
		setLoginNameAndPassword(NEW_DBA_NAME, NEW_DBA_PW);

		doTheRequest();

		assertProcessBean(FORCED_PASSWORD_CHANGE, NEW_DBA_NAME, NO_MSG, NO_MSG,
				URL_LOGIN, URL_WITH_LINK);
		assertNewLoginSessions();
		assertRedirectToLoginProcessPage();
	}

	@Test
	public void changingPasswordCancel() {
		setProcessBean(FORCED_PASSWORD_CHANGE, NEW_DBA_NAME, URL_LOGIN,
				URL_WITH_LINK);
		setCancel();

		doTheRequest();

		assertNoProcessBean();
		assertNewLoginSessions();
		assertRedirectToCancelUrl();
	}

	@Test
	public void changingPasswordWrongLength() {
		setProcessBean(FORCED_PASSWORD_CHANGE, NEW_DBA_NAME, URL_LOGIN,
				URL_WITH_LINK);
		setNewPasswordAttempt("HI", "HI");

		doTheRequest();

		assertProcessBean(FORCED_PASSWORD_CHANGE, NEW_DBA_NAME, NO_MSG,
				"Please enter a password between 6 and 12 "
						+ "characters in length.", URL_LOGIN, URL_WITH_LINK);
		assertRedirectToLoginProcessPage();
	}

	@Test
	public void changingPasswordDontMatch() {
		setProcessBean(FORCED_PASSWORD_CHANGE, NEW_DBA_NAME, URL_LOGIN,
				URL_WITH_LINK);
		setNewPasswordAttempt("LongEnough", "DoesNotMatch");

		doTheRequest();

		assertProcessBean(FORCED_PASSWORD_CHANGE, NEW_DBA_NAME, NO_MSG,
				"The passwords entered do not match.", URL_LOGIN, URL_WITH_LINK);
		assertRedirectToLoginProcessPage();
	}

	@Test
	public void changingPasswordSameAsBefore() {
		setProcessBean(FORCED_PASSWORD_CHANGE, NEW_DBA_NAME, URL_LOGIN,
				URL_WITH_LINK);
		setNewPasswordAttempt(NEW_DBA_PW, NEW_DBA_PW);

		doTheRequest();

		assertProcessBean(FORCED_PASSWORD_CHANGE, NEW_DBA_NAME, NO_MSG,
				"Your new password cannot match the current one.", URL_LOGIN,
				URL_WITH_LINK);
		assertRedirectToLoginProcessPage();
	}

	@Test
	public void changingPasswordSuccess() {
		setProcessBean(FORCED_PASSWORD_CHANGE, NEW_DBA_NAME, URL_LOGIN,
				URL_WITH_LINK);
		setNewPasswordAttempt("NewPassword", "NewPassword");

		doTheRequest();

		assertNoProcessBean();
		assertNewLoginSessions(NEW_DBA_NAME);
		assertPasswordChanges(NEW_DBA_NAME, "NewPassword");
		assertRedirectToAfterLoginPage();
	}

	@Test
	public void alreadyLoggedIn() {
		LoginStatusBean statusBean = new LoginStatusBean(OLD_DBA_URI,
				AuthenticationSource.INTERNAL);
		LoginStatusBean.setBean(session, statusBean);
		setRequestFromLoginLink(URL_WITH_LINK);

		doTheRequest();

		assertNoProcessBean();
		assertNewLoginSessions();
		assertRedirect(URL_WITH_LINK);
	}

	// ----------------------------------------------------------------------
	// EXIT TESTS
	// ----------------------------------------------------------------------

	@Test
	public void exitSelfEditor() {
		setProcessBean(LOGGING_IN, NO_USER, URL_LOGIN, URL_WITH_LINK);
		setLoginNameAndPassword(OLD_SELF_NAME, OLD_SELF_PW);

		doTheRequest();

		assertNoProcessBean();
		assertNewLoginSessions(OLD_SELF_NAME);
		assertRedirect(URL_SELF_PROFILE);
	}

	@Test
	public void exitUnrecognizedSelfEditor() {
		setProcessBean(LOGGING_IN, NO_USER, URL_LOGIN, URL_WITH_LINK);
		setLoginNameAndPassword(OLD_STRANGER_NAME, OLD_STRANGER_PW);

		doTheRequest();

		assertNoProcessBean();
		assertNewLoginSessions(OLD_STRANGER_NAME);
		assertRedirect(URL_HOME);
	}

	@Test
	public void exitDbaNormal() {
		setProcessBean(LOGGING_IN, NO_USER, URL_LOGIN, URL_RESTRICTED);
		setLoginNameAndPassword(OLD_DBA_NAME, OLD_DBA_PW);

		doTheRequest();

		assertNoProcessBean();
		assertNewLoginSessions(OLD_DBA_NAME);
		assertRedirect(URL_RESTRICTED);
	}

	@Test
	public void exitDbaFromLoginPage() {
		setProcessBean(LOGGING_IN, NO_USER, URL_LOGIN, URL_LOGIN);
		setLoginNameAndPassword(OLD_DBA_NAME, OLD_DBA_PW);

		doTheRequest();

		assertNoProcessBean();
		assertNewLoginSessions(OLD_DBA_NAME);
		assertRedirect(URL_SITE_ADMIN);
	}

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

	/** Create a LoginProcessBean in the session, and keep a reference to it. */
	private void setProcessBean(State state, String username,
			String loginProcessUrl, String afterLoginUrl) {
		LoginProcessBean bean = LoginProcessBean.getBean(request);
		bean.setState(state);
		bean.setUsername(username);
		bean.setLoginPageUrl(loginProcessUrl);
		bean.setAfterLoginUrl(afterLoginUrl);

		initialProcessBean = bean;
	}

	private void setLoginNameAndPassword(String loginName, String password) {
		request.addParameter("loginName", loginName);
		request.addParameter("loginPassword", password);
	}

	private void setCancel() {
		request.addParameter("cancel", "true");
	}

	private void setNewPasswordAttempt(String newPassword,
			String confirmPassword) {
		request.addParameter("newPassword", newPassword);
		request.addParameter("confirmPassword", confirmPassword);
	}

	private void doTheRequest() {
		auth.doPost(request, response);
	}

	private void assertNoProcessBean() {
		if (LoginProcessBean.isBean(request)) {
			fail("Process bean: expected <null>, but was <"
					+ LoginProcessBean.getBean(request) + ">");
		}
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

	private void assertRedirectToAfterLoginPage() {
		assertNotNull("No reference to the initial LoginProcessBean",
				initialProcessBean);
		assertRedirect(initialProcessBean.getAfterLoginUrl());
	}

	private void assertRedirectToCancelUrl() {
		String afterLoginUrl = initialProcessBean.getAfterLoginUrl();
		if ((afterLoginUrl == null) || (afterLoginUrl.equals(URL_LOGIN))) {
			assertRedirect(URL_HOME);
		} else {
			assertRedirect(afterLoginUrl);
		}
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

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private static class UserInfo {
		final String username;
		final String uri;
		final String password;
		final Set<String> permissionSetUris;
		final int loginCount;

		public UserInfo(String username, String uri, String password,
				String roleUri, int loginCount) {
			this.username = username;
			this.uri = uri;
			this.password = password;
			this.permissionSetUris = Collections.singleton(roleUri);
			this.loginCount = loginCount;
		}

		@Override
		public String toString() {
			return "UserInfo[username=" + username + ", uri=" + uri
					+ ", password=" + password + ", roleUri="
					+ permissionSetUris + ", loginCount=" + loginCount + "]";
		}
	}

}
