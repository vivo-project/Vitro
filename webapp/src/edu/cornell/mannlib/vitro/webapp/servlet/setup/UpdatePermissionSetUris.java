/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.VITRO_AUTH;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * The URIs for Admin, Curator, Editor and SelfEditor changed from 1.4 to 1.5.
 * 
 * If the old ones are still in the User Accounts Model, replace them with the
 * new ones.
 */
public class UpdatePermissionSetUris implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);
		Stats stats = new Stats();

		try {
			Updater updater = new Updater(ctx, stats);
			if (updater.isThereAnythingToDo()) {
				updater.update();
				ss.info(this, "Updated " + stats.updatedUris
						+ "URIs of PermissionSets on " + stats.updatedUsers
						+ "User Accounts, out of a total of "
						+ stats.allUserAccounts + " User Accounts.");
			} else {
				ss.info(this, "URIs of PermissionSets were up to date on all "
						+ stats.allUserAccounts + " User Accounts.");
			}
		} catch (Exception e) {
			ss.fatal(this, "Failed to update URIs of PermissionSets "
					+ "on User Accounts", e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to tear down.
	}

	// ----------------------------------------------------------------------
	// The Updater class
	// ----------------------------------------------------------------------

	private static class Updater {
		private static final String OLD_ADMIN_URI = "http://permissionSet-50";
		private static final String OLD_CURATOR_URI = "http://permissionSet-5";
		private static final String OLD_EDITOR_URI = "http://permissionSet-4";
		private static final String OLD_SELF_EDITOR_URI = "http://permissionSet-1";
		private static final String NEW_ADMIN_URI = VITRO_AUTH + "ADMIN";
		private static final String NEW_CURATOR_URI = VITRO_AUTH + "CURATOR";
		private static final String NEW_EDITOR_URI = VITRO_AUTH + "EDITOR";
		private static final String NEW_SELF_EDITOR_URI = VITRO_AUTH
				+ "SELF_EDITOR";

		private static final Map<String, String> updateMap = buildUpdateMap();

		private static Map<String, String> buildUpdateMap() {
			Map<String, String> map = new HashMap<String, String>();
			map.put(OLD_ADMIN_URI, NEW_ADMIN_URI);
			map.put(OLD_CURATOR_URI, NEW_CURATOR_URI);
			map.put(OLD_EDITOR_URI, NEW_EDITOR_URI);
			map.put(OLD_SELF_EDITOR_URI, NEW_SELF_EDITOR_URI);
			return Collections.unmodifiableMap(map);
		}

		private final ServletContext ctx;
		private final Stats stats;
		private final UserAccountsDao userAccountsDao;

		private Journal journal;

		public Updater(ServletContext ctx, Stats stats) {
			this.ctx = ctx;
			this.stats = stats;

			WebappDaoFactory wadf = ModelAccess.on(ctx).getWebappDaoFactory();
			userAccountsDao = wadf.getUserAccountsDao();
		}

		/**
		 * If none of the existing Users have Permission Sets with the obsolete
		 * URIs, then we don't do anything. We don't even create a Journal.
		 */
		public boolean isThereAnythingToDo() {
			Collection<UserAccount> allUserAccounts = userAccountsDao
					.getAllUserAccounts();
			stats.allUserAccounts = allUserAccounts.size();

			for (UserAccount user : allUserAccounts) {
				for (String psUri : user.getPermissionSetUris()) {
					if (updateMap.keySet().contains(psUri)) {
						return true;
					}
				}
			}
			return false;
		}

		public void update() throws IOException {
			journal = new Journal(ctx);
			try {
				for (UserAccount user : userAccountsDao.getAllUserAccounts()) {
					updateUserAccount(user);
				}
			} finally {
				journal.close();
			}
		}

		private void updateUserAccount(UserAccount user) {
			boolean updated = false;
			List<String> newUris = new ArrayList<String>();

			for (String oldUri : user.getPermissionSetUris()) {
				if (updateMap.keySet().contains(oldUri)) {
					String newUri = updateMap.get(oldUri);
					newUris.add(newUri);

					updated = true;
					stats.updatedUris++;
					journal.noteUpdate(user, oldUri, newUri);
				} else {
					newUris.add(oldUri);
				}
			}

			if (updated) {
				user.setPermissionSetUris(newUris);
				userAccountsDao.updateUserAccount(user);

				stats.updatedUsers++;
			}
		}

	}

	// ----------------------------------------------------------------------
	// The Stats class
	// ----------------------------------------------------------------------

	private static class Stats {
		int allUserAccounts;
		int updatedUsers;
		int updatedUris;
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
					.getProperty("vitro.home");
			if (homeDirectoryPath == null) {
				throw new IllegalStateException(
						"No value found for vitro.home");
			}
			File homeDirectory = new File(homeDirectoryPath);
			confirmIsDirectory(homeDirectory);

			File upgradeDirectory = createDirectory(homeDirectory, "upgrade/permissions");
			String filename = timestampedFilename("UpgradePermissionSetUris",
					".txt");
			this.file = new File(upgradeDirectory, filename);

			this.w = new PrintWriter(this.file);
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

		public void noteUpdate(UserAccount user, String oldPermissionSetUri,
				String newPermissionSetUri) {
			note(String.format("For user %1$s, replaced '%2$s' with '%3$s'",
					user.getUri(), oldPermissionSetUri, newPermissionSetUri));
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
			File newDir = new File(home, name);
			if (!newDir.exists()) {
				newDir.mkdirs();
				if (!newDir.exists()) {
					throw new IllegalStateException(
							"Failed to create the upgrade directory '"
									+ newDir.getPath() + "'");
				}
			}

			if (!newDir.isDirectory()) {
				throw new IllegalStateException("Upgrade directory '"
						+ newDir.getPath() + "' is not a directory.");
			}
			if (!newDir.canWrite()) {
				throw new IllegalStateException(
						"Can't write to Upgrade directory '" + newDir.getPath()
								+ "'.");
			}

			return newDir;
		}

		private String timestampedFilename(String prefix, String suffix) {
			SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH-mm-sss");
			return prefix + "." + sdf.format(new Date()) + suffix;
		}
	}

}
