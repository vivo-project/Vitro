/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.migration.rel17;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONFIGURATION;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.USER_ACCOUNTS;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.update.GraphStore;
import org.apache.jena.update.GraphStoreFactory;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * A handful of Permissions were removed between release 1.6 and 1.7. Remove
 * them from the User Accounts model.
 */
public class RemoveObsoletePermissions implements ServletContextListener {
	private static final Log log = LogFactory
			.getLog(RemoveObsoletePermissions.class);

	static final String[] OBSOLETE_PERMISSIONS = {
			"java:edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission#RebuildVClassGroupCache",
			"java:edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission#ManageTabs",
			"java:edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission#UseMiscellaneousEditorPages",
			"java:edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission#ManagePortals" };
	static final String UPDATE_TEMPLATE = "" //
			+ "DELETE WHERE { \n"
			+ "  GRAPH <http://vitro.mannlib.cornell.edu/default/vitro-kb-userAccounts> {\n"
			+ "    ?s ?p <%s> .\n" + "  } \n" + "}";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);

		try {
			Updater updater = new Updater(ctx);
			updater.update();
			if (updater.statementsRemoved() == 0L) {
				ss.info(this, "User accounts model contained no statements "
						+ "referencing obsolete permissions.");
			} else {
				ss.info(this, String.format(
						"Adjusted the user accounts model. "
								+ "Removed %s statements referencing "
								+ "%s obsolete permissions.",
						updater.statementsRemoved(),
						OBSOLETE_PERMISSIONS.length));
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

	private static class Updater {
		private final ServletContext ctx;

		private long statementsRemoved;

		public Updater(ServletContext ctx) {
			this.ctx = ctx;

		}

		public void update() {
			OntModel model = ModelAccess.on(ctx).getOntModel(USER_ACCOUNTS);
			long statementsAtStart = model.size();

			RDFService rdfService = ModelAccess.on(ctx).getRDFService(
					CONFIGURATION);
			for (String permissionUri : OBSOLETE_PERMISSIONS) {
				removeStatements(rdfService, permissionUri);
			}

			statementsRemoved = statementsAtStart - model.size();
		}

		private void removeStatements(RDFService rdfService,
				String permissionUri) {
			String updateString = String.format(UPDATE_TEMPLATE, permissionUri);
			log.debug(updateString);
			UpdateRequest parsed = UpdateFactory.create(updateString);
			Dataset ds = new RDFServiceDataset(rdfService);
			GraphStore graphStore = GraphStoreFactory.create(ds);
			UpdateAction.execute(parsed, graphStore);
		}

		public long statementsRemoved() {
			return statementsRemoved;
		}

	}
}
