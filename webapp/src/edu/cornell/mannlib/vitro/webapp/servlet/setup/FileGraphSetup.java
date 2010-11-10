/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sdb.Store;

import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelectorImpl;

// This ContextListener must run after the JenaDataSourceSetup ContextListener

public class FileGraphSetup implements ServletContextListener {

	private static String ABOX = "abox";
	private static String TBOX = "tbox";
	private static String PATH_ROOT = "/WEB-INF/filegraph/";
	private static String URI_ROOT = "http://vitro.mannlib.cornell.edu/filegraph/";
	
	private static final Log log = LogFactory.getLog(FileGraphSetup.class);
		
	public void contextInitialized(ServletContextEvent sce) {
		
		try {
			OntModelSelectorImpl baseOms = (OntModelSelectorImpl) sce.getServletContext().getAttribute("baseOms");
			Store kbStore = (Store) sce.getServletContext().getAttribute("kbStore");
			
			Set<String> pathSet = sce.getServletContext().getResourcePaths(PATH_ROOT + ABOX);
			cleanupDB(kbStore, pathSet, ABOX);
			
			if (pathSet != null) {
 			   OntModel aboxBaseModel = baseOms.getABoxModel();
			   readGraphs(sce, pathSet, kbStore, ABOX, aboxBaseModel);		
			}
			
			pathSet = sce.getServletContext().getResourcePaths(PATH_ROOT + TBOX);
			cleanupDB(kbStore, pathSet,TBOX);
			
			if (pathSet != null) {
			   OntModel tboxBaseModel = baseOms.getTBoxModel();
			   readGraphs(sce, pathSet, kbStore, TBOX, tboxBaseModel);
			}
		} catch (ClassCastException cce) {
			String errMsg = "Unable to cast servlet context attribute to the appropriate type " + cce.getLocalizedMessage();
			log.error(errMsg);
			throw new ClassCastException(errMsg);
		} catch (Throwable t) {
			System.out.println("Throwable in listener " + this.getClass().getName());
			log.error(t);
			t.printStackTrace();
		}
	}
	
	/*
	 * Reads the graphs stored as files in sub-directories of 
	 * FileGraphSetup.PATH_ROOT and for each graph:
	 *   1. updates the SDB store to reflect the current contents of the graph.
	 *   2. adds the graph as an in-memory submodel of the base in-memory graph 
	 *      
	 * Note: no connection needs to be maintained between the in-memory copy of the
	 * graph and the DB copy.
	 */
	public void readGraphs(ServletContextEvent sce, Set<String> pathSet, Store kbStore, String type, OntModel baseModel) {
			
			int count = 0;
			
			// For each file graph in the target directory update or add that graph to
			// the Jena SDB, and attach the graph as a submodel of the base model
			for ( String p : pathSet ) {

				count++; // note this will count the empty files too
				File file = new File(sce.getServletContext().getRealPath(p));
				
				try {
					FileInputStream fis = new FileInputStream( file );
					try {
						Model model = ModelFactory.createDefaultModel(); 
						if ( p.endsWith(".n3") || p.endsWith(".N3") || p.endsWith(".ttl") || p.endsWith(".TTL") ) {
							model.read( fis, null, "N3" );
						} else if ( p.endsWith(".owl") || p.endsWith(".OWL") || p.endsWith(".rdf") || p.endsWith(".RDF") || p.endsWith(".xml") || p.endsWith(".XML") ) {
							model.read( fis, null, "RDF/XML" );
						} else {
							log.warn("Ignoring " + type + " file graph " + p + " because the file extension is unrecognized.");
						}
						
						if ( !model.isEmpty() ) {							
							 baseModel.addSubModel(model);
							 log.info("Attached file graph as " + type + " submodel " + p);
						} 
						
						updateGraphInDB(kbStore, model, p);
						
					} catch (Exception ioe) {
						//TODO: code had fis.close() - is that important?
						log.error("Unable to process file graph " + p, ioe);
						System.out.println("Unable to process file graph " + p);
						ioe.printStackTrace();
					}
				} catch (FileNotFoundException fnfe) {
					log.warn(p + " not found. Unable to process file graph" + 
							((fnfe.getLocalizedMessage() != null) ? 
							fnfe.getLocalizedMessage() : "") );
				}
			} // end - for
			
			System.out.println("Read " + count + " "  + type + " file graph" + ((count == 1) ? "" : "s") + " from " + PATH_ROOT + type);
		
		return;
	}
	
	
	/*
	 * If a graph with the given doesn't exist in the DB then add it.
	 * 
	 * Otherwise, if a graph with the given name is in the DB and is isomorphic with
	 * the graph that was read from the files system, then do nothing.
	 * 
	 * Otherwise, if a graph with the given name is in the DB and is not isomorphic with
	 * the graph that was read from the file system then replace the graph
	 * in the DB with the one read from the file system.
	 */
	public void updateGraphInDB(Store kbStore, Model graph, String filegraph) {
			
		
		return;
	}
	
	/*
	 * Deletes any file graphs of a given type (ABox or TBox) from the DB that
	 * are no longer present in the file system.
	 */
	public void cleanupDB(Store kbStore, Set<String> pathSet, String type) {
		
		int count = 0; 
		
		if (pathSet == null) {
			//create an empty set so the same logic below can be used in all cases
			pathSet = new HashSet<String>();
		}

		
		

		
		return;
	}
		
	public void contextDestroyed( ServletContextEvent sce ) {
		// nothing to do
	}
	
}
