/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.IsRootUser;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount.Status;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * If the user has an IsRootUser identifier, they can do anything!
 * 
 * On setup, check to see that there is a root user. If not, create one. If we
 * can't create one, abort.
 */
public class RootUserPolicy implements PolicyIface {
	private static final Log log = LogFactory.getLog(RootUserPolicy.class);

	private static final String PROPERTY_ROOT_USER_EMAIL = "rootUser.emailAddress";
	private static final String ROOT_USER_INITIAL_PASSWORD = "rootPassword";

	/**
	 * This is the entire policy. If you are a root user, you are authorized.
	 */
	@Override
	public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
			RequestedAction whatToAuth) {
		if (IsRootUser.isRootUser(whoToAuth)) {
			return new BasicPolicyDecision(Authorization.AUTHORIZED,
					"RootUserPolicy: approved");
		} else {
			return new BasicPolicyDecision(Authorization.INCONCLUSIVE,
					"not root user");
		}
	}

	@Override
	public String toString() {
		return "RootUserPolicy - " + hashCode();
	}

	// ----------------------------------------------------------------------
	// Setup class
	// ----------------------------------------------------------------------

	public static class Setup implements ServletContextListener {
		private ServletContext ctx;
		private StartupStatus ss;
		private String configRootEmail;
		private UserAccountsDao uaDao;

		@Override
		public void contextInitialized(ServletContextEvent sce) {
			ctx = sce.getServletContext();
			ss = StartupStatus.getBean(ctx);

			try {
				uaDao = getUserAccountsDao();
				configRootEmail = getRootEmailFromConfig();

				checkForWrongRootUser();

				if (rootUserExists()) {
					ss.info(this, "root user is " + configRootEmail);
				} else {
					createRootUser();
				}

				ServletPolicyList.addPolicy(ctx, new RootUserPolicy());
			} catch (Exception e) {
				ss.fatal(this, "Failed to set up the RootUserPolicy", e);
			}
		}

		private UserAccountsDao getUserAccountsDao() {
			WebappDaoFactory wadf = (WebappDaoFactory) ctx
					.getAttribute("webappDaoFactory");
			if (wadf == null) {
				throw new IllegalStateException(
						"No webappDaoFactory on the servlet context");
			}
			return wadf.getUserAccountsDao();
		}

		private String getRootEmailFromConfig() {
			String email = ConfigurationProperties.getBean(ctx).getProperty(
					PROPERTY_ROOT_USER_EMAIL);
			if (email == null) {
				throw new IllegalStateException(
						"deploy.properties must contain a value for '"
								+ PROPERTY_ROOT_USER_EMAIL + "'");
			} else {
				return email;
			}
		}

		private void checkForWrongRootUser() {
			UserAccount root = getRootUser();
			if (root == null) {
				return;
			}

			String actualRootEmail = root.getEmailAddress();
			if (actualRootEmail.equals(configRootEmail)) {
				return;
			}

			ss.warning(
					this,
					"The deploy.properties file specifies a root user of '"
							+ configRootEmail
							+ "', but the system already contains a root user named '"
							+ actualRootEmail + "'. The user '"
							+ configRootEmail + "' will not be created.");
		}

		private boolean rootUserExists() {
			return (getRootUser() != null);
		}

		private UserAccount getRootUser() {
			for (UserAccount ua : uaDao.getAllUserAccounts()) {
				if (ua.isRootUser()) {
					return ua;
				}
			}
			return null;
		}

		/**
		 * TODO The first and last name should be left blank, so the user will
		 * be forced to edit them. However, that's not in place yet.
		 */
		private void createRootUser() {
			String emailAddress = ConfigurationProperties.getBean(ctx)
					.getProperty(PROPERTY_ROOT_USER_EMAIL);
			if (emailAddress == null) {
				throw new IllegalStateException(
						"deploy.properties must contain a value for '"
								+ PROPERTY_ROOT_USER_EMAIL + "'");
			}

			if (!Authenticator.isValidEmailAddress(emailAddress)) {
				throw new IllegalStateException("Value for '"
						+ PROPERTY_ROOT_USER_EMAIL
						+ "' is not a valid email address: '" + emailAddress
						+ "'");
			}

			if (null != uaDao.getUserAccountByEmail(emailAddress)) {
				throw new IllegalStateException("Can't create root user - "
						+ "an account already exists with email address '"
						+ emailAddress + "'");
			}

			UserAccount ua = new UserAccount();
			ua.setEmailAddress(emailAddress);
			ua.setFirstName("root");
			ua.setLastName("user");
			ua.setMd5Password(Authenticator
					.applyMd5Encoding(ROOT_USER_INITIAL_PASSWORD));
			ua.setPasswordChangeRequired(true);
			ua.setStatus(Status.ACTIVE);
			ua.setRootUser(true);

			uaDao.insertUserAccount(ua);

			StartupStatus.getBean(ctx).info(this,
					"Created root user as '" + emailAddress + "'");
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce) {
			// Nothing to destroy
		}
	}
}
