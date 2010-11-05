/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.controller.edit.Authenticate;
import edu.cornell.mannlib.vitro.webapp.dao.UserDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.LoginEvent;

/**
 * The "standard" implementation of Authenticator.
 */
public class BasicAuthenticator extends Authenticator {
	/** User roles are recorded in the model like "role:/50", etc. */
	private static final String ROLE_NAMESPACE = "role:/";

	private static final Log log = LogFactory.getLog(BasicAuthenticator.class);

	private final HttpServletRequest request;

	public BasicAuthenticator(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public boolean isExistingUser(String username) {
		return getUserByUsername(username) != null;
	}

	@Override
	public User getUserByUsername(String username) {
		UserDao userDao = getUserDao(request);
		if (userDao == null) {
			return null;
		}
		return userDao.getUserByUsername(username);
	}

	@Override
	public boolean isCurrentPassword(String username, String clearTextPassword) {
		User user = getUserDao(request).getUserByUsername(username);
		if (user == null) {
			log.trace("Checking password '" + clearTextPassword
					+ "' for user '" + username + "', but user doesn't exist.");
			return false;
		}

		String md5NewPassword = Authenticate
				.applyMd5Encoding(clearTextPassword);
		return md5NewPassword.equals(user.getMd5password());
	}

	@Override
	public void recordNewPassword(String username, String newClearTextPassword) {
		User user = getUserByUsername(username);
		if (user == null) {
			log.error("Trying to change password on non-existent user: "
					+ username);
			return;
		}
		user.setOldPassword(user.getMd5password());
		user.setMd5password(Authenticate.applyMd5Encoding(newClearTextPassword));
		getUserDao(request).updateUser(user);
	}

	@Override
	public void recordUserIsLoggedIn(String username) {
		User user = getUserByUsername(username);
		if (user == null) {
			log.error("Trying to change password on non-existent user: "
					+ username);
			return;
		}

		HttpSession session = request.getSession();
		
		recordLoginOnUserRecord(user);
		createLoginFormBean(user, session);
		createLoginStatusBean(user, session);
		setSessionTimeoutLimit(session);
		recordInUserSessionMap(user, session);
		notifyOtherUsers(user, session);
	}

	/**
	 * Update the user record to record the login.
	 */
	private void recordLoginOnUserRecord(User user) {
		user.setLoginCount(user.getLoginCount() + 1);
		if (user.getFirstTime() == null) { // first login
			user.setFirstTime(new Date());
		}
		getUserDao(request).updateUser(user);
	}

	/**
	 * Put the login bean into the session.
	 * 
	 * TODO The LoginFormBean is being phased out.
	 */
	private void createLoginFormBean(User user, HttpSession session) {
		LoginFormBean lfb = new LoginFormBean();
		lfb.setUserURI(user.getURI());
		lfb.setLoginStatus("authenticated");
		lfb.setSessionId(session.getId());
		lfb.setLoginRole(user.getRoleURI());
		lfb.setLoginRemoteAddr(request.getRemoteAddr());
		lfb.setLoginName(user.getUsername());
		session.setAttribute("loginHandler", lfb);
	}

	/**
	 * Put the login bean into the session.
	 * 
	 * TODO this should eventually replace the LoginFormBean.
	 */
	private void createLoginStatusBean(User user, HttpSession session) {
		LoginStatusBean lsb = new LoginStatusBean(user.getURI(),
				user.getUsername(), parseUserSecurityLevel(user));
		LoginStatusBean.setBean(session, lsb);
		log.info("Adding status bean: " + lsb);
	}

	/**
	 * Editors and other privileged users get a longer timeout interval.
	 */
	private void setSessionTimeoutLimit(HttpSession session) {
		if (LoginStatusBean.getBean(session).isLoggedInAtLeast(
				LoginStatusBean.EDITOR)) {
			session.setMaxInactiveInterval(PRIVILEGED_TIMEOUT_INTERVAL);
		} else {
			session.setMaxInactiveInterval(LOGGED_IN_TIMEOUT_INTERVAL);
		}
	}

	/**
	 * Record the login in the user/session map.
	 * 
	 * TODO What is this map used for?
	 */
	private void recordInUserSessionMap(User user, HttpSession session) {
		Map<String, HttpSession> userURISessionMap = Authenticate
				.getUserURISessionMapFromContext(session.getServletContext());
		userURISessionMap.put(user.getURI(), session);
	}

	/**
	 * Anyone listening to themodel might need to know that another user is
	 * logged in.
	 */
	private void notifyOtherUsers(User user, HttpSession session) {
		Authenticate.sendLoginNotifyEvent(new LoginEvent(user.getURI()),
				session.getServletContext(), session);
	}

	@Override
	public List<String> asWhomMayThisUserEdit(User user) {
		if (user == null) {
			return Collections.emptyList();
		}

		UserDao userDao = getUserDao(request);
		if (userDao == null) {
			return Collections.emptyList();
		}

		String userUri = user.getURI();
		if (userUri == null) {
			return Collections.emptyList();
		}

		return userDao.getIndividualsUserMayEditAs(userUri);
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

	/**
	 * Parse the role URI from User. Don't crash if it is not valid.
	 */
	private int parseUserSecurityLevel(User user) {
		String roleURI = user.getRoleURI();
		try {
			if (roleURI.startsWith(ROLE_NAMESPACE)) {
				String roleLevel = roleURI.substring(ROLE_NAMESPACE.length());
				return Integer.parseInt(roleLevel);
			} else {
				return Integer.parseInt(roleURI);
			}
		} catch (NumberFormatException e) {
			log.warn("Invalid RoleURI '" + roleURI + "' for user '"
					+ user.getURI() + "'");
			return 1;
		}
	}

}
