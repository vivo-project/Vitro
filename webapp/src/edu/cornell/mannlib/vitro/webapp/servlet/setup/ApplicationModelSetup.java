/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelID;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelSynchronizer;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Setups the Application Configuration TBox and ABox.  This is sometimes
 * called the display model.
 * 
 * @author bdc34
 */

public class ApplicationModelSetup extends JenaDataSourceSetupBase 
implements ServletContextListener {

    private static final Log log = LogFactory.getLog(
            ApplicationModelSetup.class.getName());

    /**
     * Setup the application configuration model. It is frequently called the
     * display model. If this is a new DB, populate the display model with the
     * initial data.
     * 
     * Also load any files that get loaded to the display model at each tomcat
     * startup.
     * 
     * Also, at each start of tomcat, load The display TBox and the
     * display/display model.
     */
    private void setupDisplayModel(DataSource bds, ServletContext ctx,
            StartupStatus ss) {

        // display, editing and navigation Model 
        try {
            Model displayDbModel = makeDBModel(bds,
                    JENA_DISPLAY_METADATA_MODEL, DB_ONT_MODEL_SPEC, ctx);
            RDFFilesLoader.loadFirstTimeFiles(ctx, "display", displayDbModel, displayDbModel.isEmpty());
            
            OntModel displayModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC);
            displayModel.add(displayDbModel);           
            displayModel.getBaseModel().register(new ModelSynchronizer(displayDbModel));        
            ModelAccess.on(ctx).setDisplayModel(displayModel);
            
            //at each startup load all RDF files from directory to sub-models of display model
            RDFFilesLoader.loadEveryTimeFiles(ctx, "display", displayModel);
        } catch (Throwable t) {
            log.error("Unable to load user application configuration model", t);
            ss.fatal(this, "Unable to load user application configuration model", t);
        }
        
        //display tbox - currently reading in every time
        try {
            Model displayTboxModel = makeDBModel(bds,
                    JENA_DISPLAY_TBOX_MODEL, DB_ONT_MODEL_SPEC, ctx);
            
            OntModel appTBOXModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC);
            appTBOXModel.add(displayTboxModel);
            appTBOXModel.getBaseModel().register(new ModelSynchronizer(displayTboxModel));
            ModelAccess.on(ctx).setOntModel(ModelID.DISPLAY_TBOX, appTBOXModel);

            //Reading in every time, needs to be cleared/removed every time
            RDFFilesLoader.loadEveryTimeFiles(ctx, "displayTbox", appTBOXModel);
        } catch (Throwable t) {
            log.error("Unable to load user application configuration model TBOX", t);
            ss.fatal(this, "Unable to load user application configuration model TBOX", t);
        }
        
        //Display Display model, currently reading in every time
        try {
            Model displayDisplayModel = makeDBModel(bds,
                    JENA_DISPLAY_DISPLAY_MODEL, DB_ONT_MODEL_SPEC, ctx);

            OntModel appDisplayDisplayModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC);
            appDisplayDisplayModel.add(displayDisplayModel);
            appDisplayDisplayModel.getBaseModel().register(new ModelSynchronizer(displayDisplayModel));
            ModelAccess.on(ctx).setOntModel(ModelID.DISPLAY_DISPLAY, appDisplayDisplayModel);

            //Reading in every time, needs to be cleared/removed every time
            RDFFilesLoader.loadEveryTimeFiles(ctx, "displayDisplay", appDisplayDisplayModel);
        } catch (Throwable t) {
            log.error("Unable to load user application configuration model Display Model", t);
            ss.fatal(this, "Unable to load user application configuration model Display Model", t);
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        // does nothing.        
    }

    @Override   
    public void contextInitialized(ServletContextEvent sce) {       
        ServletContext ctx = sce.getServletContext();
        StartupStatus ss = StartupStatus.getBean(ctx);        
        DataSource bds = getApplicationDataSource(ctx);        
        
        setupDisplayModel(bds, ctx, ss);                        
    }
    
    

}
