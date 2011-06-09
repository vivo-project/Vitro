/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.beans;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * An immutable object that records the user's login info as a session
 * attribute.
 */
public class LoginStatusBean {
	private static final Log log = LogFactory.getLog(LoginStatusBean.class);

	/** A bean to return when the user has not logged in. */
	private static final LoginStatusBean DUMMY_BEAN = new LoginStatusBean("",
			AuthenticationSource.UNKNOWN);

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
	public static UserAccount getCurrentUser(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		return getCurrentUser(request.getSession(false));
	}

	/**
	 * Get the current user, or null if not logged in.
	 */
	public static UserAccount getCurrentUser(HttpSession session) {
		if (session == null) {
			return null;
		}

		if (!getBean(session).isLoggedIn()) {
			return null;
		}

		ServletContext ctx = session.getServletContext();
		WebappDaoFactory wadf = (WebappDaoFactory) ctx
				.getAttribute("webappDaoFactory");
		if (wadf == null) {
			log.error("No WebappDaoFactory");
			return null;
		}

		UserAccountsDao userAccountsDao = wadf.getUserAccountsDao();
		if (userAccountsDao == null) {
			log.error("No UserAccountsDao");
			return null;
		}

		String userUri = getBean(session).getUserURI();
		return userAccountsDao.getUserAccountByUri(userUri);
	}

	// ----------------------------------------------------------------------
	// the bean
	// ----------------------------------------------------------------------

	public enum AuthenticationSource {
		UNKNOWN, INTERNAL, EXTERNAL
	}

	private final String userURI;
	private final AuthenticationSource authenticationSource;

	public LoginStatusBean(String userURI,
			AuthenticationSource authenticationSource) {
		this.userURI = userURI;
		this.authenticationSource = authenticationSource;
	}

	public String getUserURI() {
		return userURI;
	}

	public AuthenticationSource getAuthenticationSource() {
		return authenticationSource;
	}

	public boolean isLoggedIn() {
		return authenticationSource != AuthenticationSource.UNKNOWN;
	}

	public boolean hasExternalAuthentication() {
		return authenticationSource == AuthenticationSource.EXTERNAL;
	}

	@Override
	public String toString() {
		return "LoginStatusBean[userURI=" + userURI + ", authenticationSource="
				+ authenticationSource + "]";
	}

}
