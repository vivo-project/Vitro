/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelAuditor;

public class ModelAuditorSetup extends JenaDataSourceSetupBase implements ServletContextListener  {

	private static final Log log = LogFactory.getLog(ModelAuditorSetup.class.getName());
	
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

	
	public void contextInitialized(ServletContextEvent sce) {
        try {
        	// the events don't seem to filter down to the writable model of a dynamic union.  Bummer.  For now we have the ugly workaround of registering twice and recording some duplication.
        	OntModel ontModel = (OntModel) sce.getServletContext().getAttribute("baseOntModel");
        	Model baseModel = ontModel.getBaseModel();
        	OntModel dynamicUnionModel = (OntModel) sce.getServletContext().getAttribute("jenaOntModel");
            log.debug("Setting model auditor database...");
            OntModel auditModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC,makeDBModelFromPropertiesFile(sce.getServletContext().getRealPath(CONNECTION_PROP_LOCATION), JENA_AUDIT_MODEL, DB_ONT_MODEL_SPEC));
            sce.getServletContext().setAttribute("jenaAuditModel", auditModel);
            ModelAuditor ma = new ModelAuditor(auditModel,ontModel);
            baseModel.register(ma);
            dynamicUnionModel.getBaseModel().register(ma);
            log.debug("Successful.");
        } catch (Exception e) {
            log.error("Unable to use audit model "+JENA_AUDIT_MODEL);
        }
	}

}
