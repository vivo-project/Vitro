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
import com.hp.hpl.jena.util.iterator.ClosableIterator;

/**
 * WARNING: This configuration may not reconnect to MySQL after connection timeouts.
 * @author bjl23
 *
 */
public class JenaPersistentDBOnlyDataSourceSetup extends JenaDataSourceSetupBase implements ServletContextListener {
	
	private static final Log log = LogFactory.getLog(JenaPersistentDataSourceSetup.class.getName());
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        OntModel memModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC);
		OntModel dbModel = null;
        try {
            Model dbPlainModel = makeDBModelFromConfigurationProperties(JENA_DB_MODEL, DB_ONT_MODEL_SPEC, ctx);
            dbModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC,dbPlainModel);
            boolean isEmpty = true;
            ClosableIterator stmtIt = dbModel.listStatements();
            try {
                if (stmtIt.hasNext()) {
                    isEmpty = false;
                }
            } finally {
                stmtIt.close();
            }

            if (isEmpty) {
                long startTime = System.currentTimeMillis();
                System.out.println("Reading ontology files into database");
                readOntologyFilesInPathSet(USERPATH, ctx, dbModel);
                readOntologyFilesInPathSet(AUTHPATH, ctx, dbModel);
                readOntologyFilesInPathSet(SYSTEMPATH, ctx, dbModel);
                System.out.println((System.currentTimeMillis()-startTime)/1000+" seconds to populate DB");
            }

            //readOntologyFilesInPathSet(ctx.getResourcePaths(AUTHPATH), sce, dbModel);
            //readOntologyFilesInPathSet(ctx.getResourcePaths(SYSTEMPATH), sce, dbModel);

            memModel = dbModel;
            
        } catch (Throwable t) {
			System.out.println("**** ERROR *****");
            System.out.println("Vitro unable to open Jena database model.");
			System.out.println("Check that the configuration properties file has been created in WEB-INF/classes, ");
			System.out.println("and that the database connection parameters are accurate. ");
			System.out.println("****************");
        }

		try {
	        if (dbModel==null) {
	            System.out.println("Reading ontology files");
	            readOntologyFilesInPathSet(USERPATH, ctx, dbModel);
	            readOntologyFilesInPathSet(AUTHPATH, ctx, dbModel);
	            readOntologyFilesInPathSet(SYSTEMPATH, ctx, dbModel);
	        }
		} catch (Exception f) {
			log.error(f);
		}
        
        
        // default inference graph
        try {
        	Model infDbPlainModel = makeDBModelFromConfigurationProperties(JENA_INF_MODEL, DB_ONT_MODEL_SPEC, ctx);
        	OntModel infDbModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC,infDbPlainModel);
        	ctx.setAttribute("inferenceOntModel",infDbModel);
        } catch (Throwable e) {
        	log.error(e, e);
        }
           
        ctx.setAttribute("jenaOntModel", memModel);
        ctx.setAttribute("persistentOntModel", dbModel);
        
        // BJL23: This is a funky hack until I completely rework how the models get set up in a more sane fashion
        ctx.setAttribute("useModelSynchronizers", "false");
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
        //Close the database connection
        //try {
        //    ((IDBConnection)sce.getServletContext().getAttribute("jenaConnection")).close();
        //} catch (Exception e) {
        //    //log.debug("could not close the JDBC connection.");
        //}
	}
	
}
