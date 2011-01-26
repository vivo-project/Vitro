/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasoner;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupSDB;

public class SDBSetupController extends FreemarkerHttpServlet {

    private static final Log log = LogFactory.getLog(
            SimpleReasonerRecomputeController.class);
    
    private static final String SDB_SETUP_FTL = "sdbSetup.ftl";
    
    protected ResponseValues processRequest(VitroRequest vreq) { 
        // Due to requiresLoginLevel(), we don't get here unless logged in as DBA
        if (!LoginStatusBean.getBean(vreq)
                .isLoggedInAtLeast(LoginStatusBean.DBA)) {
            return new RedirectResponseValues(UrlBuilder.getUrl(Route.LOGIN));
        }
        Map<String, Object> body = new HashMap<String, Object>();
        
        String messageStr = "";
        try {
        	JenaDataSourceSetupSDB jenaDataSourceSetupSDB = new JenaDataSourceSetupSDB();
        	Boolean done = (Boolean)getServletContext().getAttribute("done");
        	String setupsignal = (String) vreq.getParameter("setupsignal");
                if (done!=null && done) {
                	 messageStr = "SDB is being setup";
                } else{
                	String sdbsetup = (String)getServletContext().getAttribute("sdbsetup");
                	if(sdbsetup == null || sdbsetup.equals("showButton") || setupsignal == null){
                		body.put("link", "show");
                    	messageStr = null;
                    	getServletContext().setAttribute("sdbsetup", "yes");
                	}
                	else if(setupsignal!=null && setupsignal.equals("setup")){
                		new Thread(new SDBSetupRunner(jenaDataSourceSetupSDB)).start();
                		messageStr = "SDB setup started";
                        getServletContext().setAttribute("sdbsetup", "showButton");
                	}	
                }
        } catch (Exception e) {
            log.error("Error setting up SDB store", e);
            body.put("errorMessage", 
                    "Error setting up SDB store: " + 
                    e.getMessage());
            return new ExceptionResponseValues(
                    SDB_SETUP_FTL, body, e);            
        }
        
        body.put("message", messageStr); 
        return new TemplateResponseValues(SDB_SETUP_FTL, body);
    }
    
    private class SDBSetupRunner implements Runnable {
        
        private JenaDataSourceSetupSDB jenaDataSourceSetupSDB;
        final OntModelSpec MEM_ONT_MODEL_SPEC = OntModelSpec.OWL_MEM;
        
        public SDBSetupRunner(JenaDataSourceSetupSDB jenaDataSourceSetupSDB) {
            this.jenaDataSourceSetupSDB = jenaDataSourceSetupSDB;
        }
        
        public void run() {
           Boolean done = true;
           getServletContext().setAttribute("done",done);
           StoreDesc storeDesc = jenaDataSourceSetupSDB.makeStoreDesc();
           BasicDataSource bds = jenaDataSourceSetupSDB.makeDataSourceFromConfigurationProperties();
           Store store = null;
		try {
			store = JenaDataSourceSetupSDB.connectStore(bds, storeDesc);
		} catch (SQLException e) {
			log.error("Error while getting the sdb store with given store description and basic data source", e);
		}
           OntModel memModel = (OntModel)getServletContext().getAttribute("jenaOntModel");
           if (memModel == null) {
               memModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC);
               log.warn("WARNING: no database connected.  Changes will disappear after context restart.");
           }  
           OntModel inferenceModel = (OntModel)getServletContext().getAttribute("inferenceOntModel");
           if(inferenceModel == null){
        	   inferenceModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC);
           }
           if(store!=null)
        	   jenaDataSourceSetupSDB.setupSDB(getServletContext(), store, memModel, inferenceModel);
           done = false;
           getServletContext().setAttribute("done",done);
        }
        
    }
    
}
