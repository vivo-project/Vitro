/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
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

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.RoleBasedPolicy.AuthRole;
import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreeMarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.Message;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State;
import edu.cornell.mannlib.vitro.webapp.dao.UserDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.LoginEvent;
import edu.cornell.mannlib.vitro.webapp.dao.jena.LoginLogoutEvent;

public class Authenticate extends FreeMarkerHttpServlet {
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

	/** If no portal is specified in the request, use this one. */
	private static final int DEFAULT_PORTAL_ID = 1;

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

		User user = getUserDao(request).getUserByUsername(username);
		log.trace("User is " + (user == null ? "null" : user.getURI()));

		if (user == null) {
			bean.setMessage(Message.UNKNOWN_USERNAME, username);
			return null;
		}

		if ((password == null) || password.isEmpty()) {
			bean.setMessage(Message.NO_PASSWORD);
			return null;
		}

		String md5Password = applyMd5Encoding(password);

		if (!md5Password.equals(user.getMd5password())) {
			log.trace("Encoded passwords don't match: right="
					+ user.getMd5password() + ", wrong=" + md5Password);
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

		User user = getUserDao(request).getUserByUsername(bean.getUsername());
		log.trace("User is " + (user == null ? "null" : user.getURI()));

		if (user == null) {
			throw new IllegalStateException(
					"Changing password but bean has no user: '"
							+ bean.getUsername() + "'");
		}

		String md5NewPassword = applyMd5Encoding(newPassword);
		log.trace("Old password: " + user.getMd5password() + ", new password: "
				+ md5NewPassword);

		if (md5NewPassword.equals(user.getMd5password())) {
			bean.setMessage(Message.USING_OLD_PASSWORD);
			return null;
		}

		return user;
	}

	/**
	 * Store the changed password. They are logged in.
	 */
	private void recordSuccessfulPasswordChange(HttpServletRequest request,
			User user) {
		String newPassword = request.getParameter(PARAMETER_NEW_PASSWORD);
		String md5NewPassword = applyMd5Encoding(newPassword);
		user.setOldPassword(user.getMd5password());
		user.setMd5password(md5NewPassword);
		getUserDao(request).updateUser(user);
		log.debug("Completed first-time password change.");

		recordLoginInfo(request, user.getUsername());
	}

	/**
	 * The user provided the correct information, and changed the password if
	 * that was required. Record that they have logged in.
	 */
	private void recordLoginInfo(HttpServletRequest request, String username) {
		log.debug("Completed login.");

		// Get a fresh user object, so we know it's not stale.
		User user = getUserDao(request).getUserByUsername(username);

		HttpSession session = request.getSession();

		// Put the login info into the session.
		LoginFormBean lfb = new LoginFormBean();
		lfb.setUserURI(user.getURI());
		lfb.setLoginStatus("authenticated");
		lfb.setSessionId(session.getId());
		lfb.setLoginRole(user.getRoleURI());
		lfb.setLoginRemoteAddr(request.getRemoteAddr());
		lfb.setLoginName(user.getUsername());
		session.setAttribute("loginHandler", lfb);

		// Remove the login process info from the session.
		session.removeAttribute(LoginProcessBean.SESSION_ATTRIBUTE);

		// Record the login on the user.
		user.setLoginCount(user.getLoginCount() + 1);
		if (user.getFirstTime() == null) { // first login
			user.setFirstTime(new Date());
		}
		getUserDao(request).updateUser(user);

		// Set the timeout limit on the session - editors, etc, get more.
		session.setMaxInactiveInterval(300); // seconds, not milliseconds
		try {
			if ((int) Integer.decode(lfb.getLoginRole()) > 1) {
				session.setMaxInactiveInterval(32000);
			}
		} catch (NumberFormatException e) {
			// No problem - leave it at the default.
		}

		// Record the user in the user/Session map.
		Map<String, HttpSession> userURISessionMap = getUserURISessionMapFromContext(getServletContext());
		userURISessionMap.put(user.getURI(), request.getSession());

		// Notify the other users of this model.
		sendLoginNotifyEvent(new LoginEvent(user.getURI()),
				getServletContext(), session);

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
		// Remove the login process info from the session.
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
		if (AuthRole.USER.roleUri().equals(user.getRoleURI())) {
			UserDao userDao = getUserDao(request);
			if (userDao != null) {
				List<String> uris = userDao.getIndividualsUserMayEditAs(user
						.getURI());
				if (uris != null && uris.size() > 0) {
					log.debug("User is logged in. Redirect as self-editor to "
							+ sessionRedirect);
					String userHomePage = request.getContextPath()
							+ "/individual?uri="
							+ URLEncoder.encode(uris.get(0), "UTF-8");
					log.debug("User is logged in. Redirect as self-editor to "
							+ sessionRedirect);
					response.sendRedirect(userHomePage);
					return;
				}
			}
		}

		// If nothing else applies, send them to the Site Admin page.
		log.debug("User is logged in. Redirect to site admin page.");
		response.sendRedirect(getSiteAdminUrl(request));
	}

	/**
	 * There has been an unexpected exception. Complain mightily.
	 */
	private void showSystemError(Exception e, HttpServletResponse response) {
		log.error("Unexpected error in login process" + e);
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
			return State.NOWHERE;
		}

		LoginFormBean lfb = (LoginFormBean) session
				.getAttribute("loginHandler");
		if ((lfb != null) && (lfb.getLoginStatus().equals("authenticated"))) {
			return State.LOGGED_IN;
		}

		return getLoginProcessBean(request).getState();
	}

	/**
	 * What user are we logged in as?
	 */
	private User getLoggedInUser(HttpServletRequest request) {
		UserDao userDao = getUserDao(request);
		if (userDao == null) {
			return null;
		}

		HttpSession session = request.getSession(false);
		if (session == null) {
			return null;
		}

		LoginFormBean lfb = (LoginFormBean) session
				.getAttribute("loginHandler");
		if (lfb == null) {
			log.debug("getLoggedInUser: not logged in");
			return null;
		}

		return userDao.getUserByUsername(lfb.getLoginName());
	}

	/**
	 * Get a reference to the {@link UserDao}, or <code>null</code>.
	 */
	private UserDao getUserDao(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return null;
		}

		ServletContext servletContext = session.getServletContext();
		WebappDaoFactory wadf = (WebappDaoFactory) servletContext
				.getAttribute("webappDaoFactory");
		if (wadf == null) {
			log.error("getUserDao: no WebappDaoFactory");
			return null;
		}

		UserDao userDao = wadf.getUserDao();
		if (userDao == null) {
			log.error("getUserDao: no UserDao");
		}

		return userDao;
	}

	/** What's the URL for the login screen? */
	private String getLoginScreenUrl(HttpServletRequest request) {
		String contextPath = request.getContextPath();
		String urlParams = "?home=" + getPortalIdString(request)
				+ "&login=block";
		return contextPath + Controllers.LOGIN + urlParams;
	}

	/** What's the URL for the site admin screen? */
	private String getSiteAdminUrl(HttpServletRequest request) {
		String contextPath = request.getContextPath();
		String urlParams = "?home=" + getPortalIdString(request)
				+ "&login=block";
		return contextPath + Controllers.SITE_ADMIN + urlParams;
	}

	/** What's the URL for the home page? */
	private String getHomeUrl(HttpServletRequest request) {
		return request.getContextPath();
	}

	/**
	 * What portal are we currently in?
	 */
	private String getPortalIdString(HttpServletRequest request) {
		String portalIdParameter = request.getParameter("home");
		if (portalIdParameter == null) {
			return String.valueOf(DEFAULT_PORTAL_ID);
		} else {
			return portalIdParameter;
		}
	}

	/**
	 * How is the login process coming along?
	 */
	private LoginProcessBean getLoginProcessBean(HttpServletRequest request) {
		HttpSession session = request.getSession();

		LoginProcessBean bean = (LoginProcessBean) session
				.getAttribute(LoginProcessBean.SESSION_ATTRIBUTE);

		if (bean == null) {
			bean = new LoginProcessBean();
			session.setAttribute(LoginProcessBean.SESSION_ATTRIBUTE, bean);
		}

		return bean;
	}

	/**
	 * Encode this password for storage in the database. Apply an MD5 encoding,
	 * and store the result as a string of hex digits.
	 */
	private String applyMd5Encoding(String password) {
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

	// ----------------------------------------------------------------------
	// Public utility methods.
	// ----------------------------------------------------------------------

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
