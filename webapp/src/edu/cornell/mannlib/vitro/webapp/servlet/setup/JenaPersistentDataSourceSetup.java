/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.File;
import java.io.FileOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelSynchronizer;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

public class JenaPersistentDataSourceSetup extends JenaDataSourceSetupBase 
                                           implements ServletContextListener {
	
	private static final Log log = LogFactory.getLog(
	        JenaPersistentDataSourceSetup.class.getName());
	
	@Override	
	public void contextInitialized(ServletContextEvent sce) {	    
        ServletContext ctx = sce.getServletContext();
        StartupStatus ss = StartupStatus.getBean(ctx);
        
        // we do not want to fetch imports when we wrap Models in OntModels
        OntDocumentManager.getInstance().setProcessImports(false);
        
        BasicDataSource bds = makeDataSourceFromConfigurationProperties(ctx);
        setApplicationDataSource(bds, ctx);
		
        // user accounts Model
        try {
        	Model userAccountsDbModel = makeDBModel(bds,
        	        JENA_USER_ACCOUNTS_MODEL, DB_ONT_MODEL_SPEC, ctx);
			if (userAccountsDbModel.size() == 0) {
				firstStartup = true;
				readOntologyFilesInPathSet(AUTHPATH, ctx,
						userAccountsDbModel);
			}
        	OntModel userAccountsModel = ModelFactory.createOntologyModel(
        	        MEM_ONT_MODEL_SPEC);
        	userAccountsModel.add(userAccountsDbModel);
        	userAccountsModel.getBaseModel().register(
        	        new ModelSynchronizer(userAccountsDbModel));
        	ctx.setAttribute("userAccountsOntModel", userAccountsModel);
        	
        	if (userAccountsModel.isEmpty()) {
        		initializeUserAccounts(ctx, userAccountsModel);
        	}
        } catch (Throwable t) {
        	log.error("Unable to load user accounts model from DB", t);
        	ss.fatal(this, "Unable to load user accounts model from DB", t);
        }
             
        // display, editing and navigation Model 
	    try {
	    	Model displayDbModel = makeDBModel(bds,
	    	        JENA_DISPLAY_METADATA_MODEL, DB_ONT_MODEL_SPEC, ctx);
			if (displayDbModel.size() == 0) {
				readOntologyFilesInPathSet(APPPATH, ctx,displayDbModel);
			}
	    	OntModel displayModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC);
	    	displayModel.add(displayDbModel);	    	
	    	displayModel.getBaseModel().register(new ModelSynchronizer(displayDbModel));	    	
	    	ctx.setAttribute("displayOntModel", displayModel);
	    	
	    	//at each startup load all RDF files from directory to sub-models of display model  
	    	initializeDisplayLoadedAtStartup(ctx, displayModel);		    	
	    } catch (Throwable t) {
	    	log.error("Unable to load user application configuration model", t);
	    	ss.fatal(this, "Unable to load user application configuration model", t);
	    }
	    
	    //display tbox - currently reading in every time
	    try {
	    	Model displayTboxModel = makeDBModel(bds,
	    	        JENA_DISPLAY_TBOX_MODEL, DB_ONT_MODEL_SPEC, ctx);
	    	//Reading in single file every time
	    	//TODO: Check if original needs to be cleared/removed every time?
	    	readOntologyFileFromPath(APPPATH_LOAD + "displayTBOX.n3", displayTboxModel, ctx);	
	    	OntModel appTBOXModel = ModelFactory.createOntologyModel(
	    	        MEM_ONT_MODEL_SPEC);
	    	appTBOXModel.add(displayTboxModel);
	    	appTBOXModel.getBaseModel().register(new ModelSynchronizer(displayTboxModel));
	    	ctx.setAttribute("displayOntModelTBOX", appTBOXModel);
	    	log.debug("Loaded file " + APPPATH_LOAD + "displayTBOX.n3 into display tbox model");
	    } catch (Throwable t) {
	    	log.error("Unable to load user application configuration model TBOX", t);
	    	ss.fatal(this, "Unable to load user application configuration model TBOX", t);
	    }
	    
	    //Display Display model, currently empty, create if doesn't exist but no files to load
	    try {
	    	Model displayDisplayModel = makeDBModel(bds,
	    	        JENA_DISPLAY_DISPLAY_MODEL, DB_ONT_MODEL_SPEC, ctx);
	    	//Reading in single file every time
	    	//TODO: Check if original needs to be cleared/removed every time?
	    	readOntologyFileFromPath(APPPATH_LOAD + "displayDisplay.n3", displayDisplayModel, ctx);	

	    	OntModel appDisplayDisplayModel = ModelFactory.createOntologyModel(
	    	        MEM_ONT_MODEL_SPEC);
	    	appDisplayDisplayModel.add(displayDisplayModel);
	    	appDisplayDisplayModel.getBaseModel().register(new ModelSynchronizer(displayDisplayModel));
	    	ctx.setAttribute("displayOntModelDisplayModel", appDisplayDisplayModel);
	    	log.debug("Loaded file " + APPPATH_LOAD + "displayDisplay.n3 into display display model");
	    } catch (Throwable t) {
	    	log.error("Unable to load user application configuration model Display Model", t);
	    	ss.fatal(this, "Unable to load user application configuration model Display Model", t);
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
	
	/**
	 * Load the RDF found in the directory DISPLAY_MODEL_LOAD_AT_STARTUP_DIR
	 * a sub-models of displayModel.  The RDF from thes files will not be saved 
	 * in the database and it will be reloaded each time the system starts up.
	 */
	private void initializeDisplayLoadedAtStartup(ServletContext ctx, OntModel displayModel){
	    Model displayLoadAtStartup = readInDisplayModelLoadAtStartup(ctx);
	    	    
	    if( log.isDebugEnabled() ){
	        log.debug("loaded display model from files in " + ctx.getRealPath(DISPLAY_MODEL_LOAD_AT_STARTUP_DIR) );
	        displayLoadAtStartup.write(System.out, "N3-PP");
	    }
	    	    
	    checkForOldListViews(ctx,displayModel,displayLoadAtStartup);	    
	    displayModel.addSubModel( displayLoadAtStartup );      
	}
	
	/**
	 * All of the list views should now reside in files in DISPLAY_MODEL_LOAD_AT_STARTUP_DIR.
	 * This will check for custom list view annotation statements in the displayModel, check
	 * if they exist in the files in DISPLAY_MODEL_LOAD_AT_STARTUP_DIR, and write any that don't
	 * exist there to a file in DISPLAY_MODEL_LOAD_AT_STARTUP_DIR.  After that the statements
	 * will be removed from the displayDBModel.
	 * 
	 *   returns true if there were old list view statements in the DB, returns false
	 *   if there were none.  displayLoadAlways should be reloaded from the file system
	 *   if this returns true as this method may have changed the files.
	 *   
	 *   displayLoadAtStartup and displayModel may be modified.
	 */
	private void checkForOldListViews( ServletContext ctx, OntModel displayModel, Model displayLoadAtStartup){	    
	    // run construct for old custom list view statements from displayModel
	    Model oldListViewModel = getOldListViewStatements( displayModel );
	    if( log.isDebugEnabled() ){
	        log.debug("Printing the old list view statements from the display model to System.out.");
	        oldListViewModel.write(System.out,"N3-PP");
	    }
	    
	    // find statements in old stmts that are not in loadedAtStartup and 
	    // save them in a new file in DISPLAY_MODEL_LOAD_AT_STARTUP_DIR
	    // so that in the future they will be in loadedAtStartup
	    Model stmtsInOldAndFiles = displayLoadAtStartup.intersection( displayModel );
	    Model unhandledOldListViewStmts = oldListViewModel.difference( stmtsInOldAndFiles );
	    
	    boolean saved = false;
	    boolean neededSave = false;
	    
	    if( unhandledOldListViewStmts != null && !unhandledOldListViewStmts.isEmpty() ){
	        log.debug("need to deal with old list view statements from the display model");
	        neededSave = true;
	        try{
    	        //create a file for the old statements in the loadAtStartup directory
	            String newFileName = ctx.getRealPath( 
	                    DISPLAY_MODEL_LOAD_AT_STARTUP_DIR + File.separator 
	                    + new DateTime().toString(ISODateTimeFormat.basicDateTime()) + ".n3" );	            
    	        File file = new File( newFileName );    	            	            	        
    	        file.createNewFile();
    	        
    	        log.info("Relocating " + unhandledOldListViewStmts.size() + " custom list view statements from DB and saving to " 
    	                + file.getAbsolutePath()+ File.separator + file.getName() 
    	                + ". These will be loaded from this file when the system starts up.");
    	        
    	        FileOutputStream fileOut = new FileOutputStream(file);
    	        unhandledOldListViewStmts.write(fileOut, "N3-PP");
    	        fileOut.close();
    	        saved = true;
	        }catch(Throwable th){
	            log.warn("Could not save old list view statements.  Leaving them in the DB",th);
	        }
	        
	        //need to reload displayLoadAlways because DISPLAY_MODEL_LOAD_AT_STARTUP_DIR may have changed
	        displayLoadAtStartup.removeAll().add(readInDisplayModelLoadAtStartup(ctx));
	    }
	     
	    
	    if( oldListViewModel != null && ! oldListViewModel.isEmpty() ){	        
	        //At this point, there are old list view statements in the DB but they
	        //should are all redundant with ones in DISPLAY_MODEL_LOAD_AT_STARTUP_DIR	
	        if( (neededSave && saved) || (!neededSave) ){
	            //if there was nothing to save, just remove the old stuff
	            //if there was stuff to save, only remove if it was saved.
	            log.debug("removing old statements from displayModel");
	            displayModel.remove(oldListViewModel);
	        }
	    }
	    
	}
	
	private Model getOldListViewStatements(OntModel displayModel) {
	    //run a construct on displayModel to get all list view statements
	    Query query = QueryFactory.create ( listViewQuery );
	    QueryExecution qexec = QueryExecutionFactory.create(query, displayModel) ;
	    Model oldModel = null;
	    
	    try {
	        oldModel = qexec.execConstruct();
	    } catch( Throwable th ){
	        log.error("could not check for old custom list views, query exception",th);
	    }finally { 
	        qexec.close() ; 
	    }
	    
	    if( oldModel != null)
	        return oldModel;
	    else
	        return ModelFactory.createDefaultModel();
    }

	private static final String listViewQuery = "" +
			"PREFIX d: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#>\n" +			
			"CONSTRUCT { \n" +
			"  ?a d:listViewConfigFile ?b . \n" +
			"} WHERE {\n" +			
			"  ?a d:listViewConfigFile ?b . \n" +
			"} ";
	
    protected Model readInDisplayModelLoadAtStartup( ServletContext ctx ){
	    return getModelFromDir( new File( ctx.getRealPath( DISPLAY_MODEL_LOAD_AT_STARTUP_DIR )));  
	}
}
