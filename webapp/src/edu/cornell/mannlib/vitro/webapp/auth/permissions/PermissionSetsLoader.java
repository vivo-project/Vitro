/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.permissions;

import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.VITRO_AUTH;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Load the initial configuration of PermissionSets and Permissions.
 * 
 * The UserAccounts model must be created before this runs.
 * 
 * The PermissionRegistry must be created before this runs.
 */
public class PermissionSetsLoader implements ServletContextListener {
	private static final Log log = LogFactory
			.getLog(PermissionSetsLoader.class);

	public static final String FILE_OF_PERMISSION_SETS_INFO = "/WEB-INF/resources/permission_config.n3";

	public static final String URI_SELF_EDITOR = VITRO_AUTH + "SELF_EDITOR";
	public static final String URI_EDITOR = VITRO_AUTH + "EDITOR";
	public static final String URI_CURATOR = VITRO_AUTH + "CURATOR";
	public static final String URI_DBA = VITRO_AUTH + "ADMIN";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);

		try {
			new Loader(this, ctx, ss).load();
			new SmokeTester(this, ctx, ss).test();
		} catch (Exception e) {
			ss.fatal(this, "Failed to load the PermissionSets", e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to tear down.
	}

	// ----------------------------------------------------------------------
	// Loader class
	// ----------------------------------------------------------------------

	private static class Loader {
		private static final int MAX_STATEMENTS_IN_WARNING = 5;

		private ServletContextListener listener;
		private final ServletContext ctx;
		private final StartupStatus ss;

		private final OntModel userAccountsModel;
		private final Property permissionSetType;

		private Model modelFromFile;
		private Model filteredModel;

		private int howManyNewPermissionSets;
		private int howManyOldPermissionSets;

		public Loader(ServletContextListener listener, ServletContext ctx,
				StartupStatus ss) {
			this.listener = listener;
			this.ctx = ctx;
			this.ss = ss;

			this.userAccountsModel = ModelAccess.on(ctx).getUserAccountsModel();
			this.permissionSetType = this.userAccountsModel
					.getProperty(VitroVocabulary.PERMISSIONSET);

		}

		public void load() {
			try {
				createModelFromFile();
				filterModelFromFile();
				checkForLeftoverStatements();
				removeExistingPermissionSetsFromUserAccountsModel();
				addNewStatementsToUserAccountsModel();

				ss.info(listener, buildInfoMessage());
			} catch (LoaderException e) {
				Throwable cause = e.getCause();
				if (cause == null) {
					ss.warning(listener, e.getMessage());
				} else {
					ss.warning(listener, e.getMessage(), e.getCause());
				}
			}
		}

		private void createModelFromFile() throws LoaderException {
			InputStream stream = ctx
					.getResourceAsStream(FILE_OF_PERMISSION_SETS_INFO);

			if (stream == null) {
				throw new LoaderException("The permission sets config file "
						+ "doesn't exist in the servlet context: '"
						+ FILE_OF_PERMISSION_SETS_INFO + "'");
			}

			try {
				modelFromFile = ModelFactory.createDefaultModel();
				modelFromFile.read(stream, null, "N3");
			} finally {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			log.debug("Loaded " + modelFromFile.size() + " statements");
		}

		/**
		 * Move all statements that relate to PermissionSets from the loaded
		 * model to the filtered model.
		 */
		private void filterModelFromFile() {
			filteredModel = ModelFactory.createDefaultModel();

			for (Resource r : iterable(modelFromFile.listResourcesWithProperty(
					RDF.type, permissionSetType))) {
				moveStatementsToFilteredModel(r);
				howManyNewPermissionSets++;
			}

			log.debug("Filtered " + filteredModel.size() + " statements for "
					+ howManyNewPermissionSets + " PermissionSets; "
					+ modelFromFile.size() + " extraneous statements.");
		}

		/**
		 * Move the statements about this PermissionSet from the loaded model to
		 * the filtered model.
		 */
		private void moveStatementsToFilteredModel(Resource ps) {
			Selector sel = new SimpleSelector(ps, null, (String) null);
			for (Statement stmt : iterable(modelFromFile.listStatements(sel))) {
				filteredModel.add(stmt);
				modelFromFile.remove(stmt);
			}
		}

		/**
		 * Complain about any statements that were not moved to the filtered
		 * model.
		 */
		private void checkForLeftoverStatements() {
			List<Statement> list = iterable(modelFromFile.listStatements());
			if (list.isEmpty()) {
				return;
			}

			String message = "The PermissionSets configuration file contained "
					+ list.size()
					+ " statements that didn't describe PermissionSets: ";
			for (int i = 0; i < Math
					.min(list.size(), MAX_STATEMENTS_IN_WARNING); i++) {
				Statement stmt = list.get(i);
				message += "(" + stmt.asTriple() + ") ";
			}
			if (list.size() > MAX_STATEMENTS_IN_WARNING) {
				message += ", ...";
			}

			ss.warning(listener, message);
		}

		private void removeExistingPermissionSetsFromUserAccountsModel() {
			userAccountsModel.enterCriticalSection(Lock.WRITE);
			try {
				for (Resource r : iterable(userAccountsModel
						.listResourcesWithProperty(RDF.type, permissionSetType))) {
					Selector sel = new SimpleSelector(r, null, (String) null);
					StmtIterator stmts = userAccountsModel.listStatements(sel);
					userAccountsModel.remove(stmts);
					howManyOldPermissionSets++;
				}
			} finally {
				userAccountsModel.leaveCriticalSection();
			}

			log.debug("Deleted " + howManyOldPermissionSets
					+ " old PermissionSets from the user model.");
		}

		private void addNewStatementsToUserAccountsModel() {
			userAccountsModel.enterCriticalSection(Lock.WRITE);
			try {
				userAccountsModel.add(filteredModel);
			} finally {
				userAccountsModel.leaveCriticalSection();
			}
		}

		private String buildInfoMessage() {
			String message = "Loaded " + howManyNewPermissionSets
					+ " PermissionSets: ";
			Selector sel = new SimpleSelector(null, RDFS.label, (String) null);
			for (Statement stmt : iterable(filteredModel.listStatements(sel))) {
				String label = stmt.getObject().asLiteral().getString();
				message += "'" + label + "' ";
			}
			return message;
		}

		private <T> List<T> iterable(ClosableIterator<T> iterator) {
			List<T> list = new ArrayList<T>();
			try {
				while (iterator.hasNext()) {
					list.add(iterator.next());
				}
			} finally {
				iterator.close();
			}
			return list;
		}

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

			WebappDaoFactory wadf = (WebappDaoFactory) ctx
					.getAttribute("webappDaoFactory");
			if (wadf == null) {
				throw new IllegalStateException(
						"No webappDaoFactory on the servlet context");
			}
			this.uaDao = wadf.getUserAccountsDao();
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

	// ----------------------------------------------------------------------
	// Handy dandy exception.
	// ----------------------------------------------------------------------

	private static class LoaderException extends Exception {

		public LoaderException(String message) {
			super(message);
		}

		public LoaderException(String message, Throwable cause) {
			super(message, cause);
		}

	}
}
