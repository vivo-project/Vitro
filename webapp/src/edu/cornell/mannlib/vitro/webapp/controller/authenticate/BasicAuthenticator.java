/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.IsRootUser;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfiguration;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount.Status;
import edu.cornell.mannlib.vitro.webapp.controller.edit.Authenticate;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.LoginEvent;
import edu.cornell.mannlib.vitro.webapp.dao.jena.LogoutEvent;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;

/**
 * The "standard" implementation of Authenticator.
 */
public class BasicAuthenticator extends Authenticator {
	private static final Log log = LogFactory.getLog(BasicAuthenticator.class);

	// ----------------------------------------------------------------------
	// The factory
	// ----------------------------------------------------------------------

	public static class Factory implements AuthenticatorFactory {
		@Override
		public Authenticator getInstance(HttpServletRequest req) {
			return new BasicAuthenticator(req);
		}
	}

	// ----------------------------------------------------------------------
	// The authenticator
	// ----------------------------------------------------------------------

	private final HttpServletRequest request;

	public BasicAuthenticator(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public UserAccount getAccountForInternalAuth(String emailAddress) {
		UserAccountsDao userAccountsDao = getUserAccountsDao();
		if (userAccountsDao == null) {
			return null;
		}
		return userAccountsDao.getUserAccountByEmail(emailAddress);
	}

	@Override
	public UserAccount getAccountForExternalAuth(String externalAuthId) {
		UserAccountsDao userAccountsDao = getUserAccountsDao();
		if (userAccountsDao == null) {
			return null;
		}
		return userAccountsDao.getUserAccountByExternalAuthId(externalAuthId);
	}

	@Override
	public boolean isUserPermittedToLogin(UserAccount userAccount) {
		// All users are permitted to login. If the user doesn't have an account
		// yet (userAccount is null), an account should be created for them.
		return true;
	}

	@Override
	public boolean isCurrentPassword(UserAccount userAccount,
			String clearTextPassword) {
		if (userAccount == null) {
			return false;
		}
		if (clearTextPassword == null) {
			return false;
		}
		String encodedPassword = applyMd5Encoding(clearTextPassword);
		return encodedPassword.equals(userAccount.getMd5Password());
	}

	@Override
	public void recordNewPassword(UserAccount userAccount,
			String newClearTextPassword) {
		if (userAccount == null) {
			log.error("Trying to change password on null user.");
			return;
		}
		userAccount.setMd5Password(applyMd5Encoding(newClearTextPassword));
		userAccount.setPasswordChangeRequired(false);
		userAccount.setPasswordLinkExpires(0L);
		getUserAccountsDao().updateUserAccount(userAccount);
	}

	@Override
	public boolean accountRequiresEditing(UserAccount userAccount) {
		if (userAccount == null) {
			log.error("Trying to check for valid fields on a null user.");
			return false;
		}
		if (userAccount.getFirstName().isEmpty()) {
			return true;
		}
		if (userAccount.getLastName().isEmpty()) {
			return true;
		}
		if (userAccount.getEmailAddress().isEmpty()) {
			return true;
		}
		if (!isValidEmailAddress(userAccount.getEmailAddress())) {
			return true;
		}
		return false;
	}

	@Override
	public List<String> getAssociatedIndividualUris(UserAccount userAccount) {
		List<String> uris = new ArrayList<String>();
		if (userAccount == null) {
			return uris;
		}
		uris.addAll(getUrisAssociatedBySelfEditorConfig(userAccount));
		return uris;
	}

	@Override
	public void recordLoginAgainstUserAccount(UserAccount userAccount,
			AuthenticationSource authSource) throws LoginNotPermitted {
		if (!isUserPermittedToLogin(userAccount)) {
			throw new LoginNotPermitted();
		}
		if (userAccount == null) {
			log.error("Trying to record the login of a null user. ");
			return;
		}

		recordLoginOnUserRecord(userAccount);

		HttpSession session = request.getSession();
		createLoginStatusBean(userAccount.getUri(), authSource, session);
		RequestIdentifiers.resetIdentifiers(request);
		setSessionTimeoutLimit(userAccount, session);
		recordInUserSessionMap(userAccount.getUri(), session);
		notifyOtherUsers(userAccount.getUri(), session);

		if (IsRootUser.isRootUser(RequestIdentifiers
				.getIdBundleForRequest(request))) {
			try {
				SearchEngine engine = ApplicationUtils.instance()
						.getSearchEngine();
				if (engine.documentCount() == 0) {
					log.info("Search index is empty. Running a full index rebuild.");
					ApplicationUtils.instance().getSearchIndexer()
							.rebuildIndex();
				}
			} catch (SearchEngineException e) {
				log.warn("Unable to check for search index", e);
			}
		}
	}

	/**
	 * Update the user record to record the login.
	 */
	private void recordLoginOnUserRecord(UserAccount userAccount) {
		userAccount.setLoginCount(userAccount.getLoginCount() + 1);
		userAccount.setLastLoginTime(new Date().getTime());
		userAccount.setStatus(Status.ACTIVE);
		getUserAccountsDao().updateUserAccount(userAccount);
	}

	/**
	 * Put the login bean into the session.
	 */
	private void createLoginStatusBean(String userUri,
			AuthenticationSource authSource, HttpSession session) {
		LoginStatusBean lsb = new LoginStatusBean(userUri, authSource);
		LoginStatusBean.setBean(session, lsb);
		log.debug("Adding status bean: " + lsb);
	}

	/**
	 * Editors and other privileged users get a longer timeout interval.
	 */
	private void setSessionTimeoutLimit(UserAccount userAccount,
			HttpSession session) {
		RoleLevel role = RoleLevel.getRoleFromLoginStatus(request);
		if (role == RoleLevel.EDITOR || role == RoleLevel.CURATOR
				|| role == RoleLevel.DB_ADMIN) {
			session.setMaxInactiveInterval(PRIVILEGED_TIMEOUT_INTERVAL);
		} else if (userAccount.isRootUser()) {
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
	private void recordInUserSessionMap(String userUri, HttpSession session) {
		Map<String, HttpSession> userURISessionMap = Authenticate
				.getUserURISessionMapFromContext(session.getServletContext());
		userURISessionMap.put(userUri, session);
	}

	/**
	 * Anyone listening to the model might need to know that another user is
	 * logged in.
	 */
	private void notifyOtherUsers(String userUri, HttpSession session) {
		Authenticate.sendLoginNotifyEvent(new LoginEvent(userUri),
				session.getServletContext(), session);
	}

	private List<String> getUrisAssociatedBySelfEditorConfig(UserAccount user) {
		List<String> uris = new ArrayList<String>();
		if (user == null) {
			return uris;
		}

		IndividualDao iDao = getIndividualDao();
		if (iDao == null) {
			return uris;
		}

		List<Individual> associatedIndividuals = SelfEditingConfiguration
				.getBean(request).getAssociatedIndividuals(iDao, user);
		for (Individual ind : associatedIndividuals) {
			uris.add(ind.getURI());
		}
		return uris;
	}

	@Override
	public void recordUserIsLoggedOut() {
		HttpSession session = request.getSession();
		notifyOtherUsersOfLogout(session);
		session.invalidate();
	}

	private void notifyOtherUsersOfLogout(HttpSession session) {
		String userUri = LoginStatusBean.getBean(session).getUserURI();
		if ((userUri == null) || userUri.isEmpty()) {
			return;
		}

		Authenticate.sendLoginNotifyEvent(new LogoutEvent(userUri),
				session.getServletContext(), session);
	}

	/**
	 * Get a reference to the UserAccountsDao, or null.
	 */
	private UserAccountsDao getUserAccountsDao() {
		UserAccountsDao userAccountsDao = getWebappDaoFactory()
				.getUserAccountsDao();
		if (userAccountsDao == null) {
			log.error("getUserAccountsDao: no UserAccountsDao");
		}

		return userAccountsDao;
	}

	/**
	 * Get a reference to the IndividualDao, or null.
	 */
	private IndividualDao getIndividualDao() {
		IndividualDao individualDao = getWebappDaoFactory().getIndividualDao();
		if (individualDao == null) {
			log.error("getIndividualDao: no IndividualDao");
		}

		return individualDao;
	}

	/**
	 * Get a reference to the WebappDaoFactory, or null.
	 */
	private WebappDaoFactory getWebappDaoFactory() {
		return ModelAccess.on(request).getWebappDaoFactory();
	}

	@Override
	public String toString() {
		return "BasicAuthenticator[" + request + "]";
	}

}
