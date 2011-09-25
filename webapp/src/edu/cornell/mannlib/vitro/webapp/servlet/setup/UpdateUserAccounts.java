/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionSetsLoader;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount.Status;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Convert any existing User resources (up to rel 1.2) in the UserAccounts Model
 * to UserAccount resources (rel 1.3 and on).
 */
public class UpdateUserAccounts implements ServletContextListener {
	private static final Log log = LogFactory.getLog(UpdateUserAccounts.class);

	public static final String NS = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#";
	public static final String USER = NS + "User";
	public static final String USER_USERNAME = NS + "username";
	public static final String USER_MD5PASSWORD = NS + "md5password";
	public static final String USER_OLDPASSWORD = NS + "oldpassword";
	public static final String USER_FIRSTTIME = NS + "firstTime";
	public static final String USER_LOGINCOUNT = NS + "loginCount";
	public static final String USER_ROLE = NS + "roleURI";
	public static final String USER_LASTNAME = NS + "lastName";
	public static final String USER_FIRSTNAME = NS + "firstName";
	public static final String MAY_EDIT_AS = NS + "mayEditAs";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);
		
		try {
			Updater updater = new Updater(ctx);
			if (updater.isThereAnythingToDo()) {
				updater.update();
			}
		} catch (Exception e) {
			log.fatal("Failed to update user accounts information", e);
			ss.fatal(this, "Failed to update user accounts information", e);
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
		private final MockUserDao userDao;
		private final UserAccountsDao userAccountsDao;

		public Updater(ServletContext ctx) {
			this.ctx = ctx;

			WebappDaoFactory wadf = (WebappDaoFactory) ctx
					.getAttribute("webappDaoFactory");
			userAccountsDao = wadf.getUserAccountsDao();

			userDao = new MockUserDao(ctx);
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
				for (MockUser user : userDao.getAllUsers()) {
					try {
						UserAccount ua = getConvertedUser(user);
						if (ua != null) {
							journal.noteAlreadyConverted(user, ua);
						} else {
							journal.writeUser(user);

							ua = convertToUserAccount(user);
							userAccountsDao.insertUserAccount(ua);
							journal.noteUserAccount(ua);
						}

						userDao.deleteUser(user);
						journal.noteDeletedUser(user);
					} catch (Exception e) {
						log.error(e, e);
						journal.noteException(e);
					}
				}
			} finally {
				journal.close();
			}
		}

		private UserAccount getConvertedUser(MockUser user) {
			return userAccountsDao.getUserAccountByEmail(user.getUsername());
		}

		private UserAccount convertToUserAccount(MockUser u) {
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
			String permissionSetUri = PermissionSetsLoader.URI_SELF_EDITOR;
			if ("role:/4".equals(roleUri)) {
				permissionSetUri = PermissionSetsLoader.URI_EDITOR;
			} else if ("role:/5".equals(roleUri)) {
				permissionSetUri = PermissionSetsLoader.URI_CURATOR;
			} else if ("role:/50".equals(roleUri)) {
				permissionSetUri = PermissionSetsLoader.URI_DBA;
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

		public void noteAlreadyConverted(MockUser user, UserAccount ua) {
			note("UserAccount '" + ua.getUri() + "' already exists for User '"
					+ user.getURI() + "', " + user.getFirstName() + " "
					+ user.getLastName());
		}

		public void writeUser(MockUser u) {
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

		public void noteDeletedUser(MockUser user) {
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

	// ----------------------------------------------------------------------
	// Classes to replace the User and UserDao, just for the upgrade.
	// ----------------------------------------------------------------------

	private static class MockUser {
		private String username;
		private String roleURI;
		private int loginCount;
		private String md5password;
		private String lastName;
		private String firstName;
		private String URI;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getRoleURI() {
			return roleURI;
		}

		public void setRoleURI(String roleURI) {
			this.roleURI = roleURI;
		}

		public int getLoginCount() {
			return loginCount;
		}

		public void setLoginCount(int loginCount) {
			this.loginCount = loginCount;
		}

		public String getMd5password() {
			return md5password;
		}

		public void setMd5password(String md5password) {
			this.md5password = md5password;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getURI() {
			return URI;
		}

		public void setURI(String uRI) {
			URI = uRI;
		}

	}

	private static class MockUserDao {
		private final OntModel model;

		public MockUserDao(ServletContext ctx) {
			this.model = ModelContext.getBaseOntModelSelector(ctx)
					.getUserAccountsModel();
		}

		public Collection<MockUser> getAllUsers() {
			List<MockUser> allUsersList = new ArrayList<MockUser>();
			model.enterCriticalSection(Lock.READ);
			try {
				ClosableIterator<Statement> userStmtIt = model.listStatements(
						null, RDF.type, resource(USER));
				try {
					while (userStmtIt.hasNext()) {
						Statement stmt = userStmtIt.next();
						OntResource subjRes = stmt.getSubject().as(
								OntResource.class);
						allUsersList.add(userFromUserInd(subjRes));
					}
				} finally {
					userStmtIt.close();
				}
			} finally {
				model.leaveCriticalSection();
			}
			return allUsersList;
		}

		private MockUser userFromUserInd(OntResource userInd) {
			MockUser user = new MockUser();
			user.setURI(userInd.getURI());

			try {
				user.setUsername(getStringPropertyValue(userInd, USER_USERNAME));
			} catch (Exception e) {
				// ignore it.
			}

			try {
				user.setMd5password(getStringPropertyValue(userInd,
						USER_MD5PASSWORD));
			} catch (Exception e) {
				// ignore it.
			}

			try {
				user.setLoginCount(getIntegerPropertyValue(userInd,
						USER_LOGINCOUNT));
			} catch (Exception e) {
				user.setLoginCount(0);
			}

			try {
				user.setRoleURI(getStringPropertyValue(userInd, USER_ROLE));
			} catch (Exception e) {
				log.error("Unable to set user role\n", e);
				e.printStackTrace();
				user.setRoleURI("1");
			}
			try {
				user.setLastName(getStringPropertyValue(userInd, USER_LASTNAME));
			} catch (Exception e) {
				// ignore it.
			}

			try {
				user.setFirstName(getStringPropertyValue(userInd,
						USER_FIRSTNAME));
			} catch (Exception e) {
				// ignore it.
			}

			return user;
		}

		private String getStringPropertyValue(OntResource userInd,
				String propertyUri) {
			Property property = model.getProperty(propertyUri);
			Literal object = (Literal) userInd.getProperty(property)
					.getObject();
			return object.getString();
		}

		private int getIntegerPropertyValue(OntResource userInd,
				String propertyUri) {
			Property property = model.getProperty(propertyUri);
			Literal object = (Literal) userInd.getProperty(property)
					.getObject();
			return object.getInt();
		}

		public void deleteUser(MockUser user) {
			model.removeAll(resource(user.getURI()), null, null);
		}
		
		private Resource resource(String uri) {
			return model.getResource(uri);
		}

	}

}
