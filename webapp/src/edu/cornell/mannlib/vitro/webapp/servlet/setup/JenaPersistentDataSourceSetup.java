/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntDocumentManager;

import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/** 
 * Create connection to DB and DataSource, put them in the context.
 */
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
        
        DataSource bds = makeDataSourceFromConfigurationProperties(ctx);
        setApplicationDataSource(bds, ctx);		                          	
	}
	

    @Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to do.
	}			

}
