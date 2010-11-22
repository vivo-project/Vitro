/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mindswap.pellet.PelletOptions;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelectorImpl;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDBGraphGenerator;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RegeneratingGraph;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SDBGraphGenerator;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.PelletListener;
import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.ReasonerConfiguration;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasoner;
import edu.cornell.mannlib.vitro.webapp.reasoner.support.SimpleReasonerTBoxListener;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase.TripleStoreType;

public class SimpleReasonerSetup implements ServletContextListener {

	private static final Log log = LogFactory.getLog(SimpleReasonerSetup.class.getName());
	
	// Models used during a full recompute of the ABox
	static final String JENA_INF_MODEL_REBUILD = "http://vitro.mannlib.cornell.edu/default/vitro-kb-inf-rebuild";
	static final String JENA_INF_MODEL_SCRATCHPAD = "http://vitro.mannlib.cornell.edu/default/vitro-kb-inf-scratchpad";

	public void contextInitialized(ServletContextEvent sce) {
		
		try {	
		    // set up Pellet reasoning for the TBox	
			
			OntModelSelectorImpl assertionsOms = (OntModelSelectorImpl) sce.getServletContext().getAttribute("baseOntModelSelector");
			OntModelSelectorImpl inferencesOms = (OntModelSelectorImpl) sce.getServletContext().getAttribute("inferenceOntModelSelector");
			OntModelSelectorImpl unionOms = (OntModelSelectorImpl) sce.getServletContext().getAttribute("unionOntModelSelector");

			WebappDaoFactoryJena wadf = (WebappDaoFactoryJena) sce.getServletContext().getAttribute("webappDaoFactory");
			
			if (!assertionsOms.getTBoxModel().getProfile().NAMESPACE().equals(OWL.NAMESPACE.getNameSpace())) {		
				log.error("Not connecting Pellet reasoner - the TBox assertions model is not an OWL model");
				return;
			}
	        
	        // Set various Pellet options for incremental consistency checking, etc.
			PelletOptions.DL_SAFE_RULES = true;
	        PelletOptions.USE_COMPLETION_QUEUE = true;
	        PelletOptions.USE_TRACING = true;
	        PelletOptions.TRACK_BRANCH_EFFECTS = true;
	        PelletOptions.USE_INCREMENTAL_CONSISTENCY = true;
	        PelletOptions.USE_INCREMENTAL_DELETION = true;
	         
	        PelletListener pelletListener = new PelletListener(unionOms.getTBoxModel(),assertionsOms.getTBoxModel(),inferencesOms.getTBoxModel(),ReasonerConfiguration.DEFAULT);
	        sce.getServletContext().setAttribute("pelletListener",pelletListener);
	        sce.getServletContext().setAttribute("pelletOntModel", pelletListener.getPelletModel());
	        
	        if (wadf != null) {
	        	wadf.setPelletListener(pelletListener);
	        }
	        
	        log.info("Pellet reasoner connected for the TBox");
     
	       // set up simple reasoning for the ABox
	    	    	
	        BasicDataSource bds = JenaDataSourceSetupBase.getApplicationDataSource(sce.getServletContext());
            Model rebuildModel = makeDBModel(bds, JENA_INF_MODEL_REBUILD, JenaDataSourceSetupBase.DB_ONT_MODEL_SPEC, TripleStoreType.SDB);            
            Model scratchModel = makeDBModel(bds, JENA_INF_MODEL_SCRATCHPAD, JenaDataSourceSetupBase.DB_ONT_MODEL_SPEC, TripleStoreType.SDB); 
            
	        
	        // the simple reasoner will register itself as a listener to the ABox assertions
	        SimpleReasoner simpleReasoner = new SimpleReasoner(unionOms.getTBoxModel(), assertionsOms.getABoxModel(), inferencesOms.getABoxModel(), rebuildModel, scratchModel);

	        assertionsOms.getTBoxModel().register(new SimpleReasonerTBoxListener(simpleReasoner));
	        
	        sce.getServletContext().setAttribute("simpleReasoner",simpleReasoner);
	        
	        log.info("Simple reasoner connected for the ABox");
	        
		} catch (Throwable t) {
			t.printStackTrace();
		}		
	}
	
	public void contextDestroyed(ServletContextEvent arg0) {
		// nothing to do
	}
  
   protected Model makeDBModel(BasicDataSource ds, String jenaDbModelName, OntModelSpec jenaDbOntModelSpec, TripleStoreType storeType) {
	   String DB = "MySQL";    // database type
	   Model dbModel = null;
       try {
           //  open the db model
            try {
                Graph g = null;
                switch (storeType) {
                	case RDB:
                		g = new RegeneratingGraph(new RDBGraphGenerator(ds, DB, jenaDbModelName)); break;
                	case SDB:
                		StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.MySQL);
                    	g = new RegeneratingGraph(new SDBGraphGenerator(ds, desc, jenaDbModelName)); break;
                	default: throw new RuntimeException ("Unsupported store type " + storeType); 
                }
                dbModel = ModelFactory.createModelForGraph(g);
                log.debug("Using database at "+ds.getUrl());
            } catch (Throwable t) {
                t.printStackTrace();
            }
       } catch (Throwable t) {
           t.printStackTrace();
       }
       return dbModel;
   }
}
