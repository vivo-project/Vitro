/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.beans;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.dao.UserDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * An immutable object that records the user's login info as a session
 * attribute.
 */
public class LoginStatusBean {
	private static final Log log = LogFactory.getLog(LoginStatusBean.class);

	/**
	 * Security level when the user has not logged in. Also used as a minimum
	 * level when we want to include every user, logged in or not.
	 */
	public static final int ANYBODY = 0;

	/** Security level when a user with no privileges is logged in. */
	public static final int NON_EDITOR = 1;

	/** Security level when an authorized editor is logged in. */
	public static final int EDITOR = 4;

	/** Security level when an authorized curator is logged in. */
	public static final int CURATOR = 5;

	/** Security level when a system administrator is logged in. */
	public static final int DBA = 50;

	/** A bean to return when the user has not logged in. */
	private static final LoginStatusBean DUMMY_BEAN = new LoginStatusBean("",
			"", ANYBODY, AuthenticationSource.UNKNOWN);

	/** The bean is attached to the session by this name. */
	private static final String ATTRIBUTE_NAME = "loginStatus";

	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

	/**
	 * Attach this bean to the session - this means you are logged in.
	 */
	public static void setBean(HttpSession session, LoginStatusBean lsb) {
		session.setAttribute(ATTRIBUTE_NAME, lsb);
	}

	/**
	 * Get the bean from this request, or a dummy bean if the user is not logged
	 * in. Never returns null.
	 */
	public static LoginStatusBean getBean(HttpServletRequest request) {
		if (request == null) {
			return DUMMY_BEAN;
		}

		HttpSession session = request.getSession(false);
		if (session == null) {
			return DUMMY_BEAN;
		}

		return getBean(session);
	}

	/**
	 * Get the bean from this session, or a dummy bean if the user is not logged
	 * in. Never returns null.
	 */
	public static LoginStatusBean getBean(HttpSession session) {
		if (session == null) {
			return DUMMY_BEAN;
		}

		Object o = session.getAttribute(ATTRIBUTE_NAME);
		if (o == null) {
			return DUMMY_BEAN;
		}

		if (!(o instanceof LoginStatusBean)) {
			log.warn("Tried to get login status bean, but found an instance of "
					+ o.getClass().getName() + ": " + o);
			return DUMMY_BEAN;
		}

		return (LoginStatusBean) o;
	}

	/**
	 * Get the current user, or null if not logged in.
	 */
	public static User getCurrentUser(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		return getCurrentUser(request.getSession(false));
	}

	/**
	 * Get the current user, or null if not logged in.
	 */
	public static User getCurrentUser(HttpSession session) {
		if (session == null) {
			return null;
		}

		ServletContext ctx = session.getServletContext();
		WebappDaoFactory wadf = (WebappDaoFactory) ctx
				.getAttribute("webappDaoFactory");
		UserDao userDao = wadf.getUserDao();

		if (getBean(session).isLoggedIn()) {
			String userUri = getBean(session).getUserURI();
			return userDao.getUserByURI(userUri);
		} else {
			return null;
		}
	}

	// ----------------------------------------------------------------------
	// the bean
	// ----------------------------------------------------------------------

	public enum AuthenticationSource {
		UNKNOWN, INTERNAL, EXTERNAL
	}

	private final String userURI;
	private final String username;
	private final int securityLevel;
	private final AuthenticationSource authenticationSource;

	public LoginStatusBean(String userURI, String username, int securityLevel,
			AuthenticationSource authenticationSource) {
		this.userURI = userURI;
		this.username = username;
		this.securityLevel = securityLevel;
		this.authenticationSource = authenticationSource;
	}

	public String getUserURI() {
		return userURI;
	}

	public String getUsername() {
		return username;
	}

	public int getSecurityLevel() {
		return securityLevel;
	}

	public AuthenticationSource getAuthenticationSource() {
		return authenticationSource;
	}

	public boolean isLoggedIn() {
		return authenticationSource != AuthenticationSource.UNKNOWN;
	}

	public boolean isLoggedInExactly(int level) {
		return securityLevel == level;
	}

	public boolean isLoggedInAtLeast(int minimumLevel) {
		return securityLevel >= minimumLevel;
	}

	public boolean hasExternalAuthentication() {
		return authenticationSource == AuthenticationSource.EXTERNAL;
	}

	@Override
	public String toString() {
		return "LoginStatusBean[userURI=" + userURI + ", username=" + username
				+ ", securityLevel=" + securityLevel
				+ ", authenticationSource=" + authenticationSource + "]";
	}

}
