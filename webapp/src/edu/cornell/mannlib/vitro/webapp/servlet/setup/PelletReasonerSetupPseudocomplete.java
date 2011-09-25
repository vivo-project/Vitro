/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mindswap.pellet.PelletOptions;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.PelletListener;
import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.ReasonerConfiguration;

public class PelletReasonerSetupPseudocomplete implements ServletContextListener {
	
	private static final Log log = LogFactory.getLog(PelletReasonerSetupComplete.class.getName());
	
	public void contextInitialized(ServletContextEvent sce) {
		try {	
			
    		OntModel memoryModel = (OntModel) sce.getServletContext().getAttribute("jenaOntModel");
    		OntModel baseModel = (OntModel) sce.getServletContext().getAttribute("baseOntModel");
    		OntModel inferenceModel = (OntModel) sce.getServletContext().getAttribute("inferenceOntModel");
    		
    		if (!baseModel.getProfile().NAMESPACE().equals(OWL.NAMESPACE.getNameSpace())) {		
    			log.error("Not connecting Pellet reasoner - base model is not an OWL model");
    			return;
    		}       
            
            // Set various options
    		PelletOptions.DL_SAFE_RULES = true;
            PelletOptions.USE_COMPLETION_QUEUE = true;
            PelletOptions.USE_TRACING = true;
            PelletOptions.TRACK_BRANCH_EFFECTS = true;
            PelletOptions.USE_INCREMENTAL_CONSISTENCY = true;
            PelletOptions.USE_INCREMENTAL_DELETION = true;
            
            // Pellet 2.0-RC5 is buggy with incremental reasoning through Jena
            //PelletOptions.USE_INCREMENTAL_CONSISTENCY = true;
            //PelletOptions.USE_INCREMENTAL_DELETION = true;
            ReasonerConfiguration config = ReasonerConfiguration.PSEUDOCOMPLETE;
            config.setIncrementalReasongingEnabled(false);
            
            PelletListener pelletListener = new PelletListener(memoryModel,baseModel,inferenceModel,config);
            sce.getServletContext().setAttribute("pelletListener",pelletListener);
            sce.getServletContext().setAttribute("pelletOntModel", pelletListener.getPelletModel());
            
            log.debug("Reasoner connected");
         
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public void contextDestroyed(ServletContextEvent arg0) {
		// 
	}

}
