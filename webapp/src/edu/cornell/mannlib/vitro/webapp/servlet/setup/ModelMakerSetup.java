/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroModelSource;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Sets up the content models, OntModelSelectors and webapp DAO factories.
 */
public class ModelMakerSetup extends JenaDataSourceSetupBase 
        implements javax.servlet.ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        StartupStatus ss = StartupStatus.getBean(ctx);
        
        long begin = System.currentTimeMillis();
        
        RDFServiceFactory rdfServiceFactory = RDFServiceUtils.getRDFServiceFactory(ctx);
		makeModelMakerFromConnectionProperties(TripleStoreType.RDB, ctx);
		VitroJenaModelMaker vjmm = getVitroJenaModelMaker();
		setVitroJenaModelMaker(vjmm, ctx);
		makeModelMakerFromConnectionProperties(TripleStoreType.SDB, ctx);
		RDFServiceModelMaker vsmm = new RDFServiceModelMaker(rdfServiceFactory);
		setVitroJenaSDBModelMaker(vsmm, ctx);
		        
		//bdc34: I have no reason for vsmm vs vjmm.  
		//I don't know what are the implications of this choice.        
		setVitroModelSource( new VitroModelSource(vsmm,ctx), ctx);
        
        ss.info(this, secondsSince(begin) + " seconds to set up models and DAO factories");  
    } 

    private long secondsSince(long startTime) {
		return (System.currentTimeMillis() - startTime) / 1000;
	}

    /* ===================================================================== */
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Nothing to do.
    }    
 }

