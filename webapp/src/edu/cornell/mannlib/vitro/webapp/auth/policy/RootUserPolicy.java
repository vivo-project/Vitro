/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.RDF;

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
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.AbortStartup;

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

	// ----------------------------------------------------------------------
	// Setup class
	// ----------------------------------------------------------------------

	public static class Setup implements ServletContextListener {

		@Override
		public void contextInitialized(ServletContextEvent sce) {
			ServletContext ctx = sce.getServletContext();

			if (AbortStartup.isStartupAborted(ctx)) {
				return;
			}

			try {
				UserAccountsDao uaDao = getUserAccountsDao(ctx);
				OntModel userAccountsModel = getUserAccountsModel(ctx);

				checkForWrongRootUser(ctx, uaDao);

				if (!rootUserExists(uaDao)) {
					createRootUser(ctx, uaDao, userAccountsModel);
				}

				ServletPolicyList.addPolicy(ctx, new RootUserPolicy());
			} catch (Exception e) {
				log.error("could not run " + this.getClass().getSimpleName()
						+ ": " + e);
				AbortStartup.abortStartup(ctx);
				throw new RuntimeException(e);
			}
		}

		private UserAccountsDao getUserAccountsDao(ServletContext ctx) {
			WebappDaoFactory wadf = (WebappDaoFactory) ctx
					.getAttribute("webappDaoFactory");
			if (wadf == null) {
				throw new IllegalStateException(
						"No webappDaoFactory on the servlet context");
			}
			return wadf.getUserAccountsDao();
		}

		private OntModel getUserAccountsModel(ServletContext ctx) {
			return ModelContext.getBaseOntModelSelector(ctx)
					.getUserAccountsModel();
		}

		private void checkForWrongRootUser(ServletContext ctx,
				UserAccountsDao uaDao) {
			UserAccount root = getRootUser(uaDao);
			if (root == null) {
				return;
			}
			String actualRootEmail = root.getEmailAddress();

			String configRootEmail = ConfigurationProperties.getBean(ctx)
					.getProperty(PROPERTY_ROOT_USER_EMAIL);
			if (actualRootEmail.equals(configRootEmail)) {
				return;
			}

			log.warn("Root user '" + actualRootEmail + "' already exists.");
		}

		private boolean rootUserExists(UserAccountsDao uaDao) {
			return (getRootUser(uaDao) != null);
		}

		private UserAccount getRootUser(UserAccountsDao uaDao) {
			for (UserAccount ua : uaDao.getAllUserAccounts()) {
				if (uaDao.isRootUser(ua)) {
					return ua;
				}
			}
			return null;
		}

		/**
		 * TODO The first and last name should be left blank, so the user will
		 * be forced to edit them. However, that's not in place yet.
		 */
		private void createRootUser(ServletContext ctx, UserAccountsDao uaDao,
				OntModel userAccountsModel) {
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

			uaDao.insertUserAccount(ua);

			userAccountsModel.enterCriticalSection(Lock.WRITE);
			try {
				Resource r = userAccountsModel.getResource(ua.getUri());
				Resource t = userAccountsModel
						.getResource(VitroVocabulary.USERACCOUNT_ROOT_USER);
				userAccountsModel.add(r, RDF.type, t);
			} finally {
				userAccountsModel.leaveCriticalSection();
			}

			log.info("Created root user as '" + emailAddress + "'");
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce) {
			// Nothing to destroy
		}
	}
}
