/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelSynchronizer;

public class JenaPersistentDataSourceSetup extends JenaDataSourceSetupBase 
                                           implements ServletContextListener {
	
	private static final Log log = LogFactory.getLog(
	        JenaPersistentDataSourceSetup.class.getName());
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
		
	    if (AbortStartup.isStartupAborted(ctx)) {
            return;
        }
        
        // user accounts Model
        try {
        	Model userAccountsDbModel = makeDBModelFromConfigurationProperties(
        	        JENA_USER_ACCOUNTS_MODEL, DB_ONT_MODEL_SPEC, ctx);
			if (userAccountsDbModel.size() == 0) {
				firstStartup = true;
				readOntologyFilesInPathSet(AUTHPATH, sce.getServletContext(),
						userAccountsDbModel);
			}
        	OntModel userAccountsModel = ModelFactory.createOntologyModel(
        	        MEM_ONT_MODEL_SPEC);
        	userAccountsModel.add(userAccountsDbModel);
        	userAccountsModel.getBaseModel().register(
        	        new ModelSynchronizer(userAccountsDbModel));
        	sce.getServletContext().setAttribute(
        	        "userAccountsOntModel", userAccountsModel);
        	if (userAccountsModel.isEmpty()) {
        		initializeUserAccounts(ctx, userAccountsModel);
        	}
        } catch (Throwable t) {
        	log.error("Unable to load user accounts model from DB", t);
        }
             
        // display, editing and navigation Model 
	    try {
	    	Model appDbModel = makeDBModelFromConfigurationProperties(
	    	        JENA_DISPLAY_METADATA_MODEL, DB_ONT_MODEL_SPEC, ctx);
			if (appDbModel.size() == 0) 
				readOntologyFilesInPathSet(
				        APPPATH, sce.getServletContext(),appDbModel);			
	    	OntModel appModel = ModelFactory.createOntologyModel(
	    	        MEM_ONT_MODEL_SPEC);
	    	appModel.add(appDbModel);
	    	appModel.getBaseModel().register(new ModelSynchronizer(appDbModel));
	    	ctx.setAttribute("displayOntModel", appModel);
	    } catch (Throwable t) {
	    	log.error("Unable to load user application configuration model from DB", t);
	    }
       
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to do.
	}
	
	private void initializeUserAccounts(ServletContext ctx, 
										Model userAccountsModel) {
		readOntologyFilesInPathSet(AUTHPATH, ctx, userAccountsModel);
	}
	
}
