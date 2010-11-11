/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.util.StoreUtils;

import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelectorImpl;

// This ContextListener must run after the JenaDataSourceSetup ContextListener

public class FileGraphSetup implements ServletContextListener {

	private static String ABOX = "abox";
	private static String TBOX = "tbox";
	private static String PATH_ROOT = "/WEB-INF/filegraph/";
	private static String URI_ROOT = "http://vitro.mannlib.cornell.edu/filegraph/";
	
	private static final Log log = LogFactory.getLog(FileGraphSetup.class);
	
	//TODO: test whether submodels need to be explicitly attached to the union model	
	public void contextInitialized(ServletContextEvent sce) {
		
		try {
			OntModelSelectorImpl baseOms = (OntModelSelectorImpl) sce.getServletContext().getAttribute("baseOms");
			Store kbStore = (Store) sce.getServletContext().getAttribute("kbStore");
			
			// ABox files
			Set<String> pathSet = sce.getServletContext().getResourcePaths(PATH_ROOT + ABOX);
			
			cleanupDB(kbStore, pathToURI(pathSet, ABOX), ABOX);
			
			if (pathSet != null) {
 			   OntModel aboxBaseModel = baseOms.getABoxModel();
			   readGraphs(sce, pathSet, kbStore, ABOX, aboxBaseModel);		
			}
			
			// TBox files
			pathSet = sce.getServletContext().getResourcePaths(PATH_ROOT + TBOX);
			
			cleanupDB(kbStore, pathToURI(pathSet, TBOX),TBOX);
			
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
					
					updateGraphInDB(kbStore, model, type, p);
					
				} catch (Exception ioe) {
					//TODO: fis.close();  // put this in a finally
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
	 * If a graph with the given name doesn't exist in the DB then add it.
	 * 
	 * Otherwise, if a graph with the given name is in the DB and is isomorphic with
	 * the graph that was read from the files system, then do nothing.
	 * 
	 * Otherwise, if a graph with the given name is in the DB and is not isomorphic with
	 * the graph that was read from the file system then replace the graph
	 * in the DB with the one read from the file system.
	 */
	public void updateGraphInDB(Store kbStore, Model fileModel, String type, String path) {
			
		String graphURI = pathToURI(path,type);
		Model dbModel = SDBFactory.connectNamedModel(kbStore, graphURI);
		
		if (dbModel.isEmpty() ) {
			dbModel.add(fileModel);
		} else if (!dbModel.isIsomorphicWith(fileModel)) {
		    dbModel.removeAll();
		    dbModel.add(fileModel);
		}
		
		return;
	}
	
	/*
	 * TODO: update these comments
	 * Deletes any file graphs that are  no longer present in the file system
	 * from the DB.
	 */
	public void cleanupDB(Store kbStore, Set<String> uriSet, String type) {
		
		Pattern graphURIPat = Pattern.compile("^" + URI_ROOT + type);   
		 
	    Iterator<Node> iter = StoreUtils.storeGraphNames(kbStore);	
	    
	    while (iter.hasNext()) {
	    	Node node = iter.next();
            Matcher matcher = graphURIPat.matcher(node.getURI());
		    
            if (matcher.find()) {
		    	if (!uriSet.contains(node.getURI())) {
		    		 Model model = SDBFactory.connectNamedModel(kbStore, node.getURI());
		    		 model.removeAll(); // delete the graph from the DB
					 log.info("Removed " + type + " file graph " + node.getURI() + " from the DB store because the file no longer exists in the file system");
		    	}
            }            
	    }
	
		return;
	}
		
	/*
	 * Takes a set of path names for file graphs and returns a set containing
	 * a graph uri for each path name in the input set. If pathSet is null
	 * returns an empty set.
	 */
	public Set<String> pathToURI (Set<String> pathSet, String type) {
		
		HashSet<String> uriSet = new HashSet<String>();
		
	    if (pathSet != null) {
			for ( String path : pathSet ) {
				uriSet.add(pathToURI(path,type));
			}
	    }
		
		return uriSet;
	}

	/*
	 * Takes a path name for a file graph and returns a uri for the graph
	 */
	public String pathToURI (String path, String type) {
		// TODO redo this to create a file and get file name
		String uri = null;
		
	    if (path != null) {
			String[] pathParts = path.split("/");
			String fileName = pathParts[pathParts.length-1].substring(0, pathParts[pathParts.length-1].lastIndexOf('.'));
			uri = URI_ROOT + type + "/" + fileName; 
	    }
		
		return uri;
	}

	public void contextDestroyed( ServletContextEvent sce ) {
		// nothing to do
	}
	
}
