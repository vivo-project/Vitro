/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.permissions;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.AbortStartup;

/**
 * Load the initial configuration of PermissionSets and Permissions.
 * 
 * The UserAccounts model must be created before this runs.
 * 
 * For now, we just use the four hard-coded "roles".
 */
public class PermissionSetsLoader implements ServletContextListener {
	private static final Log log = LogFactory
			.getLog(PermissionSetsLoader.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();

		if (AbortStartup.isStartupAborted(ctx)) {
			return;
		}

		try {
			String ns = ConfigurationProperties.getBean(ctx).getProperty(
					"Vitro.defaultNamespace");

			OntModel model = ModelContext.getBaseOntModelSelector(ctx)
					.getUserAccountsModel();

			ModelWrapper wrapper = new ModelWrapper(model, ns);
			wrapper.createPermissionSet("1", "Self Editor");
			wrapper.createPermissionSet("2", "Editor");
			wrapper.createPermissionSet("3", "Curator");
			wrapper.createPermissionSet("4", "Site Admin");
		} catch (Exception e) {
			log.error("could not run PermissionSetsLoader" + e);
			AbortStartup.abortStartup(ctx);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to tear down.
	}

	private static class ModelWrapper {
		private final OntModel model;
		private final String defaultNamespace;

		private final Property typeProperty;
		private final Property labelProperty;
		private final Resource permissionSet;

		public ModelWrapper(OntModel model, String defaultNamespace) {
			this.model = model;
			this.defaultNamespace = defaultNamespace;

			typeProperty = model.createProperty(VitroVocabulary.RDF_TYPE);
			labelProperty = model.createProperty(VitroVocabulary.LABEL);
			permissionSet = model.createResource(VitroVocabulary.PERMISSIONSET);
		}

		public void createPermissionSet(String uriSuffix, String label) {
			String uri = defaultNamespace + "permissionSet-" + uriSuffix;

			model.enterCriticalSection(Lock.WRITE);
			try {
				Resource r = model.createResource(uri);
				model.add(r, typeProperty, permissionSet);
				model.add(r, labelProperty, label);
				log.debug("Created permission set: '" + uri + "', '" + label
						+ "'");
			} finally {
				model.leaveCriticalSection();
			}
		}
	}
}
