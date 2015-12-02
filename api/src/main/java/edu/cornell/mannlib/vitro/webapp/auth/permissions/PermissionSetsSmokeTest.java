/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.permissions;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Load the initial configuration of PermissionSets and Permissions.
 * 
 * The UserAccounts model must be created before this runs.
 * 
 * The PermissionRegistry must be created before this runs.
 */
public class PermissionSetsSmokeTest implements ServletContextListener {
	@SuppressWarnings("unused")
	private static final Log log = LogFactory
			.getLog(PermissionSetsSmokeTest.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);

		try {
			new SmokeTester(this, ctx, ss).test();
		} catch (Exception e) {
			ss.fatal(this, "Found a problem while testing the PermissionSets",
					e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to tear down.
	}

	// ----------------------------------------------------------------------
	// SmokeTester class
	// ----------------------------------------------------------------------

	private static class SmokeTester {
		private ServletContextListener listener;
		private final ServletContext ctx;
		private final StartupStatus ss;

		private final UserAccountsDao uaDao;

		public SmokeTester(ServletContextListener listener, ServletContext ctx,
				StartupStatus ss) {
			this.listener = listener;
			this.ctx = ctx;
			this.ss = ss;

			this.uaDao = ModelAccess.on(ctx).getWebappDaoFactory()
					.getUserAccountsDao();
		}

		public void test() {
			checkForPermissionSetsWithoutLabels();
			checkForReferencesToNonexistentPermissionSets();
			checkForReferencesToNonexistentPermissions();
			warnIfNoPermissionSetsForNewUsers();
		}

		private void checkForPermissionSetsWithoutLabels() {
			for (PermissionSet ps : uaDao.getAllPermissionSets()) {
				if (ps.getLabel().isEmpty()) {
					ss.warning(listener, "This PermissionSet has no label: "
							+ ps.getUri());
				}
			}
		}

		private void checkForReferencesToNonexistentPermissionSets() {
			for (UserAccount user : uaDao.getAllUserAccounts()) {
				for (String psUri : user.getPermissionSetUris()) {
					if (uaDao.getPermissionSetByUri(psUri) == null) {
						ss.warning(listener, "The user '" + user.getFirstName()
								+ " " + user.getLastName()
								+ "' has the PermissionSet '" + psUri
								+ "', but the PermissionSet doesn't exist.");
					}
				}
			}
		}

		private void checkForReferencesToNonexistentPermissions() {
			PermissionRegistry registry = PermissionRegistry.getRegistry(ctx);
			for (PermissionSet ps : uaDao.getAllPermissionSets()) {
				for (String pUri : ps.getPermissionUris()) {
					if (!registry.isPermission(pUri)) {
						ss.warning(listener,
								"The PermissionSet '" + ps.getLabel()
										+ "' has the Permission '" + pUri
										+ "', but the Permission "
										+ "is not found in the registry.");
					}
				}
			}
		}

		private void warnIfNoPermissionSetsForNewUsers() {
			for (PermissionSet ps : uaDao.getAllPermissionSets()) {
				if (ps.isForNewUsers()) {
					return;
				}
			}
			ss.warning(listener, "No PermissionSet has been declared to be a "
					+ "PermissionSet for new users.");
		}

	}
}
