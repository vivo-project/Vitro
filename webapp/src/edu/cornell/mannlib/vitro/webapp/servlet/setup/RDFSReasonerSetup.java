package edu.cornell.mannlib.vitro.webapp.servlet.setup;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mindswap.pellet.PelletOptions;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.PelletListener;
import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.ReasonerConfiguration;

public class RDFSReasonerSetup implements ServletContextListener {
	
	private static final Log log = LogFactory.getLog(PelletReasonerSetupComplete.class.getName());
	
	/**
	 * This ContextListener uses the infrastructure designed for Pellet, but switches the OntModelSpec
	 * to use Jena's RDFS reasoner.  Pellet itself is not used, despite the current names of some of the
	 * classes involved.
	 */
	public void contextInitialized(ServletContextEvent sce) {
		
		try {	
			
			OntModel memoryModel = (OntModel) sce.getServletContext().getAttribute("jenaOntModel");
			OntModel baseModel = (OntModel) sce.getServletContext().getAttribute("baseOntModel");
			OntModel inferenceModel = (OntModel) sce.getServletContext().getAttribute("inferenceOntModel");
	        
	        ReasonerConfiguration configuration = ReasonerConfiguration.COMPLETE;
	        configuration.setOntModelSpec(OntModelSpec.RDFS_MEM_RDFS_INF);
	        PelletListener pelletListener = new PelletListener(memoryModel,baseModel,inferenceModel,configuration);
	        sce.getServletContext().setAttribute("pelletListener",pelletListener);
	        sce.getServletContext().setAttribute("pelletOntModel", pelletListener.getPelletModel());
	        
	        log.debug("RDFS reasoner connected");
	     
			} catch (Throwable t) {
				t.printStackTrace();
			}
	}
	
	public void contextDestroyed(ServletContextEvent arg0) {
		// 
	}

}
