/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import static edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State.FORCED_PASSWORD_CHANGE;
import static edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State.LOGGED_IN;
import static edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State.LOGGING_IN;
import static edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State.NOWHERE;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.LoginRedirector;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.Message;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State;
import edu.cornell.mannlib.vitro.webapp.dao.jena.LoginLogoutEvent;

public class Authenticate extends VitroHttpServlet {
	private static final Log log = LogFactory.getLog(Authenticate.class
			.getName());

	/** The username field on the login form. */
	private static final String PARAMETER_USERNAME = "loginName";

	/** The password field on the login form. */
	private static final String PARAMETER_PASSWORD = "loginPassword";

	/** The new password field on the password change form. */
	private static final String PARAMETER_NEW_PASSWORD = "newPassword";

	/** The confirm password field on the password change form. */
	private static final String PARAMETER_CONFIRM_PASSWORD = "confirmPassword";

	/** If this parameter is "true" (ignoring case), cancel the login. */
	private static final String PARAMETER_CANCEL = "cancel";

	/** If they are logging in, show them this form. */
	public static final String TEMPLATE_LOGIN = "login-form.ftl";

	/** If they are changing their password on first login, show them this form. */
	public static final String TEMPLATE_FORCE_PASSWORD_CHANGE = "login-forcedPasswordChange.ftl";

	public static final String BODY_LOGIN_NAME = "loginName";
	public static final String BODY_FORM_ACTION = "formAction";
	public static final String BODY_ERROR_MESSAGE = "errorMessage";

	/** Where do we find the User/Session map in the servlet context? */
	public static final String USER_SESSION_MAP_ATTR = "userURISessionMap";

	private final LoginRedirector loginRedirector = new LoginRedirector();

	/**
	 * Find out where they are in the login process, process any input, record
	 * the new state, and show the next page.
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) {

		VitroRequest vreq = new VitroRequest(request);

		try {
			// Where do we stand in the process?
			State entryState = getCurrentLoginState(vreq);
			log.debug("State on entry: " + entryState);

			// Act on any input.
			switch (entryState) {
			case NOWHERE:
				processInputNowhere(vreq);
				break;
			case LOGGING_IN:
				processInputLoggingIn(vreq);
				break;
			case FORCED_PASSWORD_CHANGE:
				processInputChangePassword(vreq);
				break;
			default: // LOGGED_IN:
				processInputLoggedIn(vreq);
				break;
			}

			// Now where do we stand?
			State exitState = getCurrentLoginState(vreq);
			log.debug("State on exit: " + exitState);

			// Send them on their way.
			switch (exitState) {
			case NOWHERE:
				redirectCancellingUser(vreq, response);
				break;
			case LOGGING_IN:
				showLoginScreen(vreq, response);
				break;
			case FORCED_PASSWORD_CHANGE:
				showLoginScreen(vreq, response);
				break;
			default: // LOGGED_IN:
				loginRedirector.redirectLoggedInUser(vreq, response);
				break;
			}
		} catch (Exception e) {
			showSystemError(e, response);
		}

	}

	/**
	 * Where are we in the process? Logged in? Not? Somewhere in between?
	 */
	private State getCurrentLoginState(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			log.debug("no session: current state is NOWHERE");
			return NOWHERE;
		}

		if (LoginStatusBean.getBean(request).isLoggedIn()) {
			log.debug("found a LoginStatusBean: current state is LOGGED IN");
			return LOGGED_IN;
		}

		if (LoginProcessBean.isBean(request)) {
			State state = LoginProcessBean.getBean(request).getState();
			log.debug("state from LoginProcessBean is " + state);
			return state;
		} else {
			log.debug("no LoginSessionBean, no LoginProcessBean: "
					+ "current state is NOWHERE");
			return NOWHERE;
		}
	}

	/**
	 * They just got here. Start the process.
	 */
	private void processInputNowhere(HttpServletRequest request) {
		transitionToLoggingIn(request);
	}

	/**
	 * They are logging in. If they get it wrong, let them know. If they get it
	 * right, record it.
	 */
	private void processInputLoggingIn(HttpServletRequest request) {
		String username = request.getParameter(PARAMETER_USERNAME);
		String password = request.getParameter(PARAMETER_PASSWORD);

		LoginProcessBean bean = LoginProcessBean.getBean(request);
		bean.clearMessage();
		log.trace("username=" + username + ", password=" + password + ", bean="
				+ bean);

		if ((username == null) || username.isEmpty()) {
			bean.setMessage(Message.NO_USERNAME);
			return;
		} else {
			bean.setUsername(username);
		}

		User user = getAuthenticator(request).getUserByUsername(username);
		log.trace("User is " + (user == null ? "null" : user.getURI()));

		if (user == null) {
			bean.setMessage(Message.UNKNOWN_USERNAME, username);
			return;
		}

		if ((password == null) || password.isEmpty()) {
			bean.setMessage(Message.NO_PASSWORD);
			return;
		}

		if (!getAuthenticator(request).isCurrentPassword(username, password)) {
			bean.setMessage(Message.INCORRECT_PASSWORD);
			return;
		}

		// Username and password are correct. What next?
		if (isFirstTimeLogin(user)) {
			transitionToForcedPasswordChange(request);
		} else {
			transitionToLoggedIn(request, username);
		}
	}

	/**
	 * <pre>
	 * They are changing passwords. 
	 * - If they cancel, let them out without checking for problems. 
	 * - Otherwise, 
	 *   - If they get it wrong, let them know. 
	 *   - If they get it right, record it.
	 * </pre>
	 */
	private void processInputChangePassword(HttpServletRequest request) {
		String newPassword = request.getParameter(PARAMETER_NEW_PASSWORD);
		String confirm = request.getParameter(PARAMETER_CONFIRM_PASSWORD);
		String cancel = request.getParameter(PARAMETER_CANCEL);

		if (Boolean.valueOf(cancel)) {
			// It's over, man. Let them go.
			transitionToNowhere(request);
			return;
		}

		LoginProcessBean bean = LoginProcessBean.getBean(request);
		bean.clearMessage();
		log.trace("newPassword=" + newPassword + ", confirm=" + confirm
				+ ", bean=" + bean);

		if ((newPassword == null) || newPassword.isEmpty()) {
			bean.setMessage(Message.NO_NEW_PASSWORD);
			return;
		}

		if (!newPassword.equals(confirm)) {
			bean.setMessage(Message.MISMATCH_PASSWORD);
			return;
		}

		if ((newPassword.length() < User.MIN_PASSWORD_LENGTH)
				|| (newPassword.length() > User.MAX_PASSWORD_LENGTH)) {
			bean.setMessage(Message.PASSWORD_LENGTH, User.MIN_PASSWORD_LENGTH,
					User.MAX_PASSWORD_LENGTH);
			return;
		}

		String username = bean.getUsername();

		if (getAuthenticator(request).isCurrentPassword(username, newPassword)) {
			bean.setMessage(Message.USING_OLD_PASSWORD);
			return;
		}

		// New password is acceptable. Store it and go on.
		transitionToLoggedIn(request, username, newPassword);
	}

	/**
	 * They are already logged in. There's nothing to do; no transition.
	 */
	@SuppressWarnings("unused")
	private void processInputLoggedIn(HttpServletRequest request) {
	}

	/**
	 * Has this user ever logged in before?
	 */
	private boolean isFirstTimeLogin(User user) {
		if (user.getLoginCount() == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * State change: they are starting the login process.
	 */
	private void transitionToLoggingIn(HttpServletRequest request) {
		log.debug("Starting the login process.");
		LoginProcessBean.getBean(request).setState(LOGGING_IN);
	}

	/**
	 * State change: username and password were correct, but now we require a
	 * new password.
	 */
	private void transitionToForcedPasswordChange(HttpServletRequest request) {
		log.debug("Forcing first-time password change");
		LoginProcessBean.getBean(request).setState(FORCED_PASSWORD_CHANGE);
	}

	/**
	 * State change: all requirements are satisfied. Log them in.
	 */
	private void transitionToLoggedIn(HttpServletRequest request,
			String username) {
		log.debug("Completed login: " + username);
		getAuthenticator(request).recordUserIsLoggedIn(username);
		LoginProcessBean.removeBean(request);
	}

	/**
	 * State change: all requirements are satisfied. Change their password and
	 * log them in.
	 */
	private void transitionToLoggedIn(HttpServletRequest request,
			String username, String newPassword) {
		log.debug("Completed login: " + username + ", password changed.");
		getAuthenticator(request).recordNewPassword(username, newPassword);
		getAuthenticator(request).recordUserIsLoggedIn(username);
		LoginProcessBean.removeBean(request);
	}

	/**
	 * State change: they decided to cancel the login.
	 */
	private void transitionToNowhere(HttpServletRequest request) {
		log.debug("Cancelling login.");
		LoginProcessBean.removeBean(request);
	}

	/**
	 * Exit: there has been an unexpected exception, so show it.
	 */
	private void showSystemError(Exception e, HttpServletResponse response) {
		log.error("Unexpected error in login process", e);
		try {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (IOException e1) {
			log.error(e1, e1);
		}
	}

	/**
	 * Exit: user is still logging in, so go back to the page they were on.
	 */
	private void showLoginScreen(VitroRequest vreq, HttpServletResponse response)
			throws IOException {
		log.debug("logging in.");
		
		String referringPage = vreq.getHeader("referer");
		if (referringPage == null) {
			log.warn("No referring page on the request");
			referringPage = getHomeUrl(vreq); 
		}
		response.sendRedirect(referringPage);
		return;
	}

	/**
	 * Exit: user cancelled the login, so show them the home page.
	 */
	private void redirectCancellingUser(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		log.debug("User cancelled the login. Redirect to site admin page.");
		LoginProcessBean.removeBean(request);
		response.sendRedirect(getHomeUrl(request));
	}

	/** Get a reference to the Authenticator. */
	private Authenticator getAuthenticator(HttpServletRequest request) {
		return Authenticator.getInstance(request);
	}

	/** What's the URL for the home page? */
	private String getHomeUrl(HttpServletRequest request) {
		return request.getContextPath();
	}

	// ----------------------------------------------------------------------
	// Public utility methods.
	// ----------------------------------------------------------------------

	/**
	 * Encode this password for storage in the database. Apply an MD5 encoding,
	 * and store the result as a string of hex digits.
	 */
	public static String applyMd5Encoding(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(password.getBytes());
			char[] hexChars = Hex.encodeHex(digest);
			return new String(hexChars).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			// This can't happen with a normal Java runtime.
			throw new RuntimeException(e);
		}
	}

	/**
	 * The servlet context should contain a map from User URIs to
	 * {@link HttpSession}s. Get a reference to it, creating it if necessary.
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, HttpSession> getUserURISessionMapFromContext(
			ServletContext ctx) {
		Map<String, HttpSession> m = (Map<String, HttpSession>) ctx
				.getAttribute(USER_SESSION_MAP_ATTR);
		if (m == null) {
			m = new HashMap<String, HttpSession>();
			ctx.setAttribute(USER_SESSION_MAP_ATTR, m);
		}
		return m;
	}

	/**
	 * Let everyone know that somebody has logged in or logged out.
	 */
	public static void sendLoginNotifyEvent(LoginLogoutEvent event,
			ServletContext context, HttpSession session) {
		if (event == null) {
			log.warn("Unable to notify audit model of login "
					+ "because a null event was passed");
			return;
		}

		OntModel jenaOntModel = (OntModel) session.getAttribute("jenaOntModel");
		if (jenaOntModel == null) {
			jenaOntModel = (OntModel) context.getAttribute("jenaOntModel");
		}
		if (jenaOntModel == null) {
			log.error("Unable to notify audit model of login event "
					+ "because no model could be found");
			return;
		}

		jenaOntModel.getBaseModel().notifyEvent(event);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		doPost(request, response);
	}

}
