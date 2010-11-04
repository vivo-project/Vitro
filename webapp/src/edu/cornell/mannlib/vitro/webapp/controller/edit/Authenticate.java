/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
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
import edu.cornell.mannlib.vitro.webapp.auth.policy.RoleBasedPolicy.AuthRole;
import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.Message;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State;
import edu.cornell.mannlib.vitro.webapp.dao.jena.LoginLogoutEvent;

public class Authenticate extends FreemarkerHttpServlet {
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

	/**
	 * Find out where they are in the login process, and check for progress. If
	 * they succeed in logging in, record the information. Show the next page.
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) {

		VitroRequest vreq = new VitroRequest(request);

		User user = null;

		try {
			// Process any input from the login form.
			State entryState = getCurrentLoginState(vreq);
			log.debug("State on entry: " + entryState);

			switch (entryState) {
			case LOGGING_IN:
				user = checkLoginProgress(vreq);
				if (user != null) {
					whatNextForThisGuy(vreq, user);
				}
				break;
			case FORCED_PASSWORD_CHANGE:
				if (checkCancel(vreq)) {
					recordLoginCancelled(vreq);
				} else {
					user = checkChangeProgress(vreq);
					if (user != null) {
						recordSuccessfulPasswordChange(vreq, user);
					}
				}
				break;
			default:
				break;
			}

			// Figure out where they should be, and redirect.
			State exitState = getCurrentLoginState(vreq);
			log.debug("State on exit: " + exitState);

			switch (exitState) {
			case LOGGED_IN:
				redirectLoggedInUser(vreq, response);
				break;
			case CANCELLED:
				redirectCancellingUser(vreq, response);
				break;
			default:
				showLoginScreen(vreq, response);
				break;
			}
		} catch (Exception e) {
			showSystemError(e, response);
		}
	}

	/**
	 * They are logging in. Are they successful?
	 */
	private User checkLoginProgress(HttpServletRequest request) {
		String username = request.getParameter(PARAMETER_USERNAME);
		String password = request.getParameter(PARAMETER_PASSWORD);

		LoginProcessBean bean = getLoginProcessBean(request);
		bean.clearMessage();
		log.trace("username=" + username + ", password=" + password + ", bean="
				+ bean);

		if ((username == null) || username.isEmpty()) {
			bean.setMessage(Message.NO_USERNAME);
			return null;
		} else {
			bean.setUsername(username);
		}

		User user = getAuthenticator(request).getUserByUsername(username);
		log.trace("User is " + (user == null ? "null" : user.getURI()));

		if (user == null) {
			bean.setMessage(Message.UNKNOWN_USERNAME, username);
			return null;
		}

		if ((password == null) || password.isEmpty()) {
			bean.setMessage(Message.NO_PASSWORD);
			return null;
		}

		if (!getAuthenticator(request).isCurrentPassword(username, password)) {
			bean.setMessage(Message.INCORRECT_PASSWORD);
			return null;
		}

		return user;
	}

	/**
	 * Successfully applied username and password. Are we forcing a password
	 * change, or is this guy logged in?
	 */
	private void whatNextForThisGuy(HttpServletRequest request, User user) {
		if (user.getLoginCount() == 0) {
			log.debug("Forcing first-time password change");
			LoginProcessBean bean = getLoginProcessBean(request);
			bean.setState(State.FORCED_PASSWORD_CHANGE);
		} else {
			recordLoginInfo(request, user.getUsername());
		}
	}

	/**
	 * Are they cancelling the login (cancelling the first-time password
	 * change)? They are if the cancel parameter is "true" (ignoring case).
	 */
	private boolean checkCancel(HttpServletRequest request) {
		String cancel = request.getParameter(PARAMETER_CANCEL);
		log.trace("cancel=" + cancel);
		return Boolean.valueOf(cancel);
	}

	/**
	 * If they want to cancel the login, let them.
	 */
	private void recordLoginCancelled(HttpServletRequest request) {
		getLoginProcessBean(request).setState(State.CANCELLED);
	}

	/**
	 * They are changing password. Are they successful?
	 */
	private User checkChangeProgress(HttpServletRequest request) {
		String newPassword = request.getParameter(PARAMETER_NEW_PASSWORD);
		String confirm = request.getParameter(PARAMETER_CONFIRM_PASSWORD);
		LoginProcessBean bean = getLoginProcessBean(request);
		bean.clearMessage();
		log.trace("newPassword=" + newPassword + ", confirm=" + confirm
				+ ", bean=" + bean);

		if ((newPassword == null) || newPassword.isEmpty()) {
			bean.setMessage(Message.NO_NEW_PASSWORD);
			return null;
		}

		if (!newPassword.equals(confirm)) {
			bean.setMessage(Message.MISMATCH_PASSWORD);
			return null;
		}

		if ((newPassword.length() < User.MIN_PASSWORD_LENGTH)
				|| (newPassword.length() > User.MAX_PASSWORD_LENGTH)) {
			bean.setMessage(Message.PASSWORD_LENGTH, User.MIN_PASSWORD_LENGTH,
					User.MAX_PASSWORD_LENGTH);
			return null;
		}

		String username = bean.getUsername();

		if (getAuthenticator(request).isCurrentPassword(username, newPassword)) {
			bean.setMessage(Message.USING_OLD_PASSWORD);
			return null;
		}

		User user = getAuthenticator(request).getUserByUsername(username);
		log.trace("User is " + (user == null ? "null" : user.getURI()));
		return user;
	}

	/**
	 * Store the changed password. They are logged in.
	 */
	private void recordSuccessfulPasswordChange(HttpServletRequest request,
			User user) {
		String newPassword = request.getParameter(PARAMETER_NEW_PASSWORD);
		getAuthenticator(request).recordNewPassword(user, newPassword);
		log.debug("Completed first-time password change.");

		recordLoginInfo(request, user.getUsername());
	}

	/**
	 * The user provided the correct information, and changed the password if
	 * that was required. Record that they have logged in.
	 */
	private void recordLoginInfo(HttpServletRequest request, String username) {
		log.debug("Completed login.");

		// Record the login on the user record (start with a fresh copy).
		User user = getAuthenticator(request).getUserByUsername(username);
		getAuthenticator(request).recordSuccessfulLogin(user);

		// Record that a new user has logged in to this session.
		getAuthenticator(request).setLoggedIn(user);

		// Remove the login process info from the session.
		request.getSession()
				.removeAttribute(LoginProcessBean.SESSION_ATTRIBUTE);
	}

	/**
	 * User is in the login process. Show them the login screen.
	 */
	private void showLoginScreen(VitroRequest vreq, HttpServletResponse response)
			throws IOException {
		response.sendRedirect(getLoginScreenUrl(vreq));
		return;
	}

	/**
	 * User cancelled the login. Forget that they were logging in, and send them
	 * to the home page.
	 */
	private void redirectCancellingUser(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		request.getSession()
				.removeAttribute(LoginProcessBean.SESSION_ATTRIBUTE);

		log.debug("User cancelled the login. Redirect to site admin page.");
		response.sendRedirect(getHomeUrl(request));
	}

	/**
	 * User is logged in. They might go to:
	 * <ul>
	 * <li>A one-time redirect, stored in the session, if they had tried to
	 * bookmark to a page that requires login.</li>
	 * <li>An application-wide redirect, stored in the servlet context.</li>
	 * <li>Their home page, if they are a self-editor.</li>
	 * <li>The site admin page.</li>
	 * </ul>
	 */
	private void redirectLoggedInUser(HttpServletRequest request,
			HttpServletResponse response) throws IOException,
			UnsupportedEncodingException {
		// Did they have a one-time redirect stored on the session?
		String sessionRedirect = (String) request.getSession().getAttribute(
				"postLoginRequest");
		if (sessionRedirect != null) {
			request.getSession().removeAttribute("postLoginRequest");
			log.debug("User is logged in. Redirect by session to "
					+ sessionRedirect);
			response.sendRedirect(sessionRedirect);
			return;
		}

		// Is there a login-redirect stored in the application as a whole?
		// It could lead to another page in this app, or to any random URL.
		String contextRedirect = (String) getServletContext().getAttribute(
				"postLoginRequest");
		if (contextRedirect != null) {
			log.debug("User is logged in. Redirect by application to "
					+ contextRedirect);
			if (contextRedirect.indexOf(":") == -1) {
				response.sendRedirect(request.getContextPath()
						+ contextRedirect);
			} else {
				response.sendRedirect(contextRedirect);
			}
			return;
		}

		// If the user is a self-editor, send them to their home page.
		User user = getLoggedInUser(request);
		if (userIsANonEditor(user)) {
			List<String> uris = getAuthenticator(request)
					.asWhomMayThisUserEdit(user);
			if (uris != null && uris.size() > 0) {
				String userHomePage = request.getContextPath()
						+ "/individual?uri="
						+ URLEncoder.encode(uris.get(0), "UTF-8");
				log.debug("User is logged in. Redirect as self-editor to "
						+ userHomePage);
				response.sendRedirect(userHomePage);
				return;
			}
		}

		// If nothing else applies, send them to the Site Admin page.
		log.debug("User is logged in. Redirect to site admin page.");
		response.sendRedirect(getSiteAdminUrl(request));
	}

	/** Is the logged in user an AuthRole.USER? */
	private boolean userIsANonEditor(User user) {
		if (user == null) {
			return false;
		}
		String nonEditorRoleUri = Integer.toString(AuthRole.USER.level());
		return nonEditorRoleUri.equals(user.getRoleURI());
	}

	/**
	 * There has been an unexpected exception. Complain mightily.
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
	 * Where are we in the process? Logged in? Not? Somewhere in between?
	 */
	private State getCurrentLoginState(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			log.debug("no session: current state is NOWHERE");
			return State.NOWHERE;
		}

		if (LoginStatusBean.getBean(request).isLoggedIn()) {
			log.debug("found a LoginStatusBean: current state is LOGGED IN");
			return State.LOGGED_IN;
		}

		if (session.getAttribute(LoginProcessBean.SESSION_ATTRIBUTE) == null) {
			log.debug("no LoginSessionBean, no LoginProcessBean: "
					+ "current state is NOWHERE");
			return State.NOWHERE;
		}

		State state = getLoginProcessBean(request).getState();
		log.debug("state from LoginProcessBean is " + state);
		return state;
	}

	/**
	 * What user are we logged in as?
	 */
	private User getLoggedInUser(HttpServletRequest request) {
		LoginStatusBean lsb = LoginStatusBean.getBean(request);
		if (!lsb.isLoggedIn()) {
			log.debug("getLoggedInUser: not logged in");
			return null;
		}

		return getAuthenticator(request).getUserByUsername(lsb.getUsername());
	}

	/** Get a reference to the Authenticator. */
	private Authenticator getAuthenticator(HttpServletRequest request) {
		return Authenticator.getInstance(request);
	}

	/** What's the URL for the login screen? */
	private String getLoginScreenUrl(HttpServletRequest request) {
		String contextPath = request.getContextPath();
		String urlParams = "?login=block";
		return contextPath + Controllers.LOGIN + urlParams;
	}

	/** What's the URL for the site admin screen? */
	private String getSiteAdminUrl(HttpServletRequest request) {
		String contextPath = request.getContextPath();
		String urlParams = "?login=block";
		return contextPath + Controllers.SITE_ADMIN + urlParams;
	}

	/** What's the URL for the home page? */
	private String getHomeUrl(HttpServletRequest request) {
		return request.getContextPath();
	}

	/** Where do we stand in the login process? */
	private LoginProcessBean getLoginProcessBean(HttpServletRequest request) {
		return LoginProcessBean.getBeanFromSession(request);
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
