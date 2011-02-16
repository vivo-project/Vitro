/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.setup;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.AdministrativeUriRestrictor;
import edu.cornell.mannlib.vitro.webapp.auth.policy.InformationResourceEditingPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

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
		try {
			log.debug("Setting up InformationResourceEditingPolicy");

			OntModel model = (OntModel) sce.getServletContext().getAttribute(
					"jenaOntModel");
			replacePolicy(sce.getServletContext(), model);

			log.debug("InformationResourceEditingPolicy has been setup. ");
		} catch (Exception e) {
			log.error("could not run SelfEditingPolicySetup: " + e);
			e.printStackTrace();
		}
	}

	public static InformationResourceEditingPolicy makePolicyFromModel(
			OntModel model) {
		InformationResourceEditingPolicy policy = null;
		if (model == null)
			policy = new InformationResourceEditingPolicy(null,
					new AdministrativeUriRestrictor(null, null, null, null));
		else {
			Set<String> prohibitedProps = new HashSet<String>();

			// need to iterate through one level higher than SELF (the lowest
			// level where restrictions make sense) plus all higher levels
			for (BaseResourceBean.RoleLevel e : EnumSet.range(
					BaseResourceBean.RoleLevel.EDITOR,
					BaseResourceBean.RoleLevel.NOBODY)) {
				ResIterator it = model
						.listSubjectsWithProperty(
								model.createProperty(VitroVocabulary.PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT),
								ResourceFactory.createResource(e.getURI()));
				while (it.hasNext()) {
					Resource resource = it.nextResource();
					if (resource != null && resource.getURI() != null) {
						log.debug("adding '"
								+ resource.getURI()
								+ "' to properties prohibited from information resource editing ("
								+ e.getLabel() + ")");
						prohibitedProps.add(resource.getURI());
					}
				}
			}
			policy = new InformationResourceEditingPolicy(model,
					new AdministrativeUriRestrictor(prohibitedProps, null, null, null));
		}
		return policy;
	}

	public static void replacePolicy(ServletContext sc, OntModel model) {
		ServletPolicyList.replacePolicy(sc, makePolicyFromModel(model));
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to do.
	}

}
