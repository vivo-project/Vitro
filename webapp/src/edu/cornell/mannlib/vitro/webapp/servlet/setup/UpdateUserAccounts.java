/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount.Status;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.UserDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Convert any existing User resources (up to rel 1.2) in the UserAccounts Model
 * to UserAccount resources (rel 1.3 and on).
 */
public class UpdateUserAccounts implements ServletContextListener {
	private static final Log log = LogFactory.getLog(UpdateUserAccounts.class);

	private static final String URI_PERMISSION_SET_SELF_EDITOR = "http://permissionSet-1";
	private static final String URI_PERMISSION_SET_EDITOR = "http://permissionSet-2";
	private static final String URI_PERMISSION_SET_CURATOR = "http://permissionSet-3";
	private static final String URI_PERMISSION_SET_DBA = "http://permissionSet-4";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		if (AbortStartup.isStartupAborted(ctx)) {
			return;
		}

		try {
			Updater updater = new Updater(ctx);
			if (updater.isThereAnythingToDo()) {
				updater.update();
			}
		} catch (Exception e) {
			AbortStartup.abortStartup(ctx);
			log.fatal("Failed to update user accounts information", e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to destroy.
	}

	// ----------------------------------------------------------------------
	// The Updater class
	// ----------------------------------------------------------------------

	private static class Updater {
		private final ServletContext ctx;
		private final UserDao userDao;
		private final UserAccountsDao userAccountsDao;

		public Updater(ServletContext ctx) {
			this.ctx = ctx;

			WebappDaoFactory wadf = (WebappDaoFactory) ctx
					.getAttribute("webappDaoFactory");
			userDao = wadf.getUserDao();
			userAccountsDao = wadf.getUserAccountsDao();
		}

		/**
		 * If there is nothing to do, we shouldn't even create a Journal.
		 */
		public boolean isThereAnythingToDo() {
			return !userDao.getAllUsers().isEmpty();
		}

		/**
		 * We found some old User resources, so convert them.
		 */
		public void update() throws IOException {
			Journal journal = new Journal(ctx);
			log.info("Updating user accounts info. Journal is in '"
					+ journal.getPath() + "'");

			try {
				for (User user : userDao.getAllUsers()) {
					try {
						UserAccount ua = getConvertedUser(user);
						if (ua != null) {
							journal.noteAlreadyConverted(user, ua);
						} else {
							journal.writeUser(user);

							ua = convertToUserAccount(user);
							userAccountsDao.insertUserAccount(ua);
							journal.noteUserAccount(ua);

							// TODO: for now, keep the User also.
							journal.note("Not deleting User " + user.getURI());
							// userDao.deleteUser(user);
							journal.noteDeletedUser(user);
						}
					} catch (Exception e) {
						log.error(e, e);
						journal.noteException(e);
					}
				}
			} finally {
				journal.close();
			}
		}

		private UserAccount getConvertedUser(User user) {
			return userAccountsDao.getUserAccountByEmail(user.getUsername());
		}

		private UserAccount convertToUserAccount(User u) {
			UserAccount ua = new UserAccount();
			ua.setEmailAddress(nonNull(u.getUsername()));
			ua.setFirstName(nonNull(u.getFirstName()));
			ua.setLastName(nonNull(u.getLastName()));
			ua.setMd5Password(nonNull(u.getMd5password()));
			ua.setLoginCount(nonNegative(u.getLoginCount()));
			ua.setPasswordChangeRequired(u.getLoginCount() == 0);
			ua.setPasswordLinkExpires(0L);
			ua.setStatus(Status.ACTIVE);
			ua.setExternalAuthId(nonNull(u.getUsername()));
			ua.setPermissionSetUris(translateFromRoleUri(u.getRoleURI()));
			return ua;
		}

		private String nonNull(String value) {
			return (value == null) ? "" : value;
		}

		private int nonNegative(int value) {
			return Math.max(0, value);
		}

		private Set<String> translateFromRoleUri(String roleUri) {
			String permissionSetUri = URI_PERMISSION_SET_SELF_EDITOR;
			if ("4".equals(roleUri)) {
				permissionSetUri = URI_PERMISSION_SET_EDITOR;
			} else if ("5".equals(roleUri)) {
				permissionSetUri = URI_PERMISSION_SET_CURATOR;
			} else if ("50".equals(roleUri)) {
				permissionSetUri = URI_PERMISSION_SET_DBA;
			}
			return Collections.singleton(permissionSetUri);
		}
	}

	// ----------------------------------------------------------------------
	// The Journal class
	// ----------------------------------------------------------------------

	private static class Journal {
		private final File file;
		private final PrintWriter w;
		private int errorCount;

		Journal(ServletContext ctx) throws IOException {
			String homeDirectoryPath = ConfigurationProperties.getBean(ctx)
					.getProperty("vitro.home.directory");
			if (homeDirectoryPath == null) {
				throw new IllegalStateException(
						"No value found for vitro.home.directory");
			}
			File homeDirectory = new File(homeDirectoryPath);
			confirmIsDirectory(homeDirectory);

			File upgradeDirectory = createDirectory(homeDirectory, "upgrade");
			String filename = timestampedFilename("UpgradeUserAccounts", ".txt");
			this.file = new File(upgradeDirectory, filename);

			this.w = new PrintWriter(this.file);

			w.println("PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
			w.println("PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#>");
			w.println("PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>");
			w.println("PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>");
			w.println("");
		}

		public String getPath() {
			return file.getAbsolutePath();
		}

		public void note(String... notes) {
			w.println();
			for (String note : notes) {
				w.println("# " + note);
			}
		}

		public void noteAlreadyConverted(User user, UserAccount ua) {
			note("UserAccount '" + ua.getUri() + "' already exists for User '"
					+ user.getURI() + "', " + user.getFirstName() + " "
					+ user.getLastName());
		}

		public void writeUser(User u) {
			w.println();
			w.println("# converting User: ");
			w.println(u.getURI());
			w.println("    a vitro:User ;");
			w.println("    vitro:username \"" + u.getUsername() + "\" ;");
			w.println("    vitro:firstName \"" + u.getFirstName() + "\" ;");
			w.println("    vitro:lastName \"" + u.getLastName() + "\" ;");
			w.println("    vitro:md5password \"" + u.getMd5password() + "\" ;");
			w.println("    vitro:loginCount " + u.getLoginCount() + " ;");
			w.println("    vitro:roleUri \"" + u.getRoleURI() + "\" ;");
			w.println("    .");
		}

		public void noteUserAccount(UserAccount ua) {
			note("Created UserAccount '" + ua.getUri() + "' for "
					+ ua.getFirstName() + " " + ua.getLastName());
		}

		public void noteDeletedUser(User user) {
			note("Delete User: '" + user.getURI() + "'");
		}

		public void noteException(Exception e) {
			errorCount++;
			note("Exception: " + e, "    (full stack trace in Vitro log file)");
		}

		public void close() {
			w.println("upgrade complete with " + errorCount + " errors.");
			w.close();
		}

		private void confirmIsDirectory(File home) {
			if (!home.exists()) {
				throw new IllegalStateException("Vitro home directory '"
						+ home.getPath() + "' does not exist.");
			}
			if (!home.isDirectory()) {
				throw new IllegalStateException("Vitro home '" + home.getPath()
						+ "' is not a directory.");
			}
			if (!home.canWrite()) {
				throw new IllegalStateException(
						"Can't write to Vitro home directory '"
								+ home.getPath() + "'.");
			}
		}

		private File createDirectory(File home, String name) {
			File upgradeDirectory = new File(home, name);
			if (!upgradeDirectory.exists()) {
				upgradeDirectory.mkdirs();
				if (!upgradeDirectory.exists()) {
					throw new IllegalStateException(
							"Failed to create the upgrade directory '"
									+ upgradeDirectory.getPath() + "'");
				}
			}

			if (!upgradeDirectory.isDirectory()) {
				throw new IllegalStateException("Upgrade directory '"
						+ upgradeDirectory.getPath() + "' is not a directory.");
			}
			if (!upgradeDirectory.canWrite()) {
				throw new IllegalStateException(
						"Can't write to Upgrade directory '"
								+ upgradeDirectory.getPath() + "'.");
			}

			return upgradeDirectory;
		}

		private String timestampedFilename(String prefix, String suffix) {
			SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH-mm-sss");
			return prefix + "." + sdf.format(new Date()) + suffix;
		}
	}

}
