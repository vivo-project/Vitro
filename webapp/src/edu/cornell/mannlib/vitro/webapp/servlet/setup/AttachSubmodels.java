/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDao;

public class AttachSubmodels implements ServletContextListener {

	private static String PATH = "/WEB-INF/submodels";
	
	private static final Log log = LogFactory.getLog( AttachSubmodels.class );
	
	@Override
	public void contextInitialized( ServletContextEvent sce ) {
	    
	    ServletContext ctx = sce.getServletContext();
	    
        if (true) {
            (new FileGraphSetup()).contextInitialized(sce);
            return;
            // use filegraphs instead of submodels if we're running SDB
        }	    
	    
        // The future of AttachSubmodels is uncertain.
        // Presently unreachable code follows.
        
		try {
			int attachmentCount = 0;
			OntModel baseModel = (OntModel) ctx.getAttribute( JenaBaseDao.ASSERTIONS_ONT_MODEL_ATTRIBUTE_NAME );
			Set<String> pathSet = ctx.getResourcePaths( PATH );
			if (pathSet == null) { 
				return;
			}
			for ( String p : pathSet ) {
				File file = new File( ctx.getRealPath( p ) );
				try {
					FileInputStream fis = new FileInputStream( file );
					try {
						Model m = ModelFactory.createDefaultModel(); 
						if ( p.endsWith(".n3") || p.endsWith(".N3") || p.endsWith(".ttl") || p.endsWith(".TTL") ) {
							m.read( fis, null, "N3" );
						} else if ( p.endsWith(".owl") || p.endsWith(".OWL") || p.endsWith(".rdf") || p.endsWith(".RDF") || p.endsWith(".xml") || p.endsWith(".XML") ) {
							m.read( fis, null, "RDF/XML" );
						} else {
							log.warn("Ignoring submodel file " + p + " because the file extension is unrecognized.");
						}
						if ( !m.isEmpty() ) {
							baseModel.addSubModel( m );
						}
						attachmentCount++;
						log.info("Attached submodel from file " + p);
					} catch (Exception ioe) {
						log.error("Unable to attach submodel from file " + p, ioe);
						System.out.println("Unable to attach submodel from file " + p);
						ioe.printStackTrace();
					} finally {
						fis.close();
					}
				} catch (FileNotFoundException fnfe) {
					log.warn(p + " not found. Unable to attach as submodel" + 
							((fnfe.getLocalizedMessage() != null) ? 
							fnfe.getLocalizedMessage() : "") );
				}
			}
			System.out.println("Attached " + attachmentCount + " file" + ((attachmentCount == 1) ? "" : "s") + " as submodels");
		} catch (ClassCastException cce) {
			String errMsg = "Unable to cast servlet context attribute " + JenaBaseDao.ASSERTIONS_ONT_MODEL_ATTRIBUTE_NAME + " to " + OntModel.class.getName();
			// Logging this ourselves because Tomcat's tends not to log exceptions thrown in context listeners.
			log.error( errMsg );
			throw new ClassCastException( errMsg );
		} catch (Throwable t) {
			System.out.println("Throwable in listener " + this.getClass().getName());
			log.error(t);
			t.printStackTrace();
		}
		
	}
	
	@Override
	public void contextDestroyed( ServletContextEvent sce ) {
		// nothing to worry about
	}
	
}
