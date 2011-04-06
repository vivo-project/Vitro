/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.setup;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.auth.policy.InformationResourceEditingPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.AbortStartup;

/**
 * Set up the InformationResourceEditingPolicy. This is tied to the SelfEditor
 * identifier, but has enough of its own logic to merit its own policy class.
 */
public class InformationResourceEditingPolicySetup implements
		ServletContextListener {
	private static final Log log = LogFactory
			.getLog(InformationResourceEditingPolicySetup.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();

		if (AbortStartup.isStartupAborted(ctx)) {
			return;
		}

		try {
			log.debug("Setting up InformationResourceEditingPolicy");

			// need to make a policy and add it to the ServletContext
			OntModel model = (OntModel) sce.getServletContext().getAttribute(
					"jenaOntModel");
			InformationResourceEditingPolicy irep = new InformationResourceEditingPolicy(
					ctx, model);
			ServletPolicyList.addPolicy(ctx, irep);

			// don't need an IdentifierFactory if the SelfEditingPolicy is
			// providing it.

			log.debug("Finished setting up InformationResourceEditingPolicy: "
					+ irep);
		} catch (Exception e) {
			log.error("could not run InformationResourceEditingPolicySetup: "
					+ e);
			AbortStartup.abortStartup(ctx);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to do.
	}
}
