/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.ontology.update.KnowledgeBaseUpdater;
import edu.cornell.mannlib.vitro.webapp.ontology.update.UpdateSettings;
import edu.cornell.mannlib.vitro.webapp.search.IndexConstants;

/**
 * Invokes process to test whether the knowledge base needs any updating
 * to align with ontology changes.
 * @author bjl23
 *
 */
public class UpdateKnowledgeBase implements ServletContextListener {
	
    public static final String KBM_REQURIED_AT_STARTUP = "KNOWLEDGE_BASE_MIGRATION_REQUIRED_AT_STARTUP";
	private final static Log log = LogFactory.getLog(UpdateKnowledgeBase.class);
	
	private static final String DATA_DIR = "/WEB-INF/ontologies/update/";
	private static final String LOG_DIR = "logs/";
	private static final String CHANGED_DATA_DIR = "changedData/";
	private static final String ASK_QUERY_FILE = DATA_DIR + "askUpdated.sparql";
	private static final String SUCCESS_ASSERTIONS_FILE = DATA_DIR + "success.n3";
	private static final String SUCCESS_RDF_FORMAT = "N3";
	private static final String DIFF_FILE = DATA_DIR + "diff.tab.txt";
	private static final String REMOVED_DATA_FILE = DATA_DIR + CHANGED_DATA_DIR + 	"removedData.n3";
	private static final String ADDED_DATA_FILE = DATA_DIR + CHANGED_DATA_DIR + "addedData.n3";
	private static final String SPARQL_CONSTRUCT_ADDITIONS_DIR = DATA_DIR + "sparqlConstructs/additions/";
	private static final String SPARQL_CONSTRUCT_DELETIONS_DIR = DATA_DIR + "sparqlConstructs/deletions/";
	private static final String MISC_REPLACEMENTS_FILE = DATA_DIR + "miscReplacements.rdf";
	private static final String OLD_TBOX_MODEL_DIR = DATA_DIR + "oldVersion/";
	private static final String NEW_TBOX_MODEL_DIR = "/WEB-INF/filegraph/tbox/";
	private static final String OLD_TBOX_ANNOTATIONS_DIR = DATA_DIR + "oldAnnotations/";
	private static final String NEW_TBOX_ANNOTATIONS_DIR = "/WEB-INF/ontologies/user/tbox/";
	
	public void contextInitialized(ServletContextEvent sce) {
				
	    if (AbortStartup.isStartupAborted(sce.getServletContext())) {
            return;
        }
	    
		try {

			ServletContext ctx = sce.getServletContext();
			
			String logFileName =  DATA_DIR + LOG_DIR + timestampedFileName("knowledgeBaseUpdate", "log");
			String errorLogFileName = DATA_DIR + LOG_DIR + 	timestampedFileName("knowledgeBaseUpdate.error", "log");
						
			UpdateSettings settings = new UpdateSettings();
			settings.setAskUpdatedQueryFile(getAskUpdatedQueryPath(ctx));
			settings.setDataDir(ctx.getRealPath(DATA_DIR));
			settings.setSparqlConstructAdditionsDir(ctx.getRealPath(SPARQL_CONSTRUCT_ADDITIONS_DIR));
			settings.setSparqlConstructDeletionsDir(ctx.getRealPath(SPARQL_CONSTRUCT_DELETIONS_DIR));
			settings.setDiffFile(ctx.getRealPath(DIFF_FILE));
			settings.setSuccessAssertionsFile(ctx.getRealPath(SUCCESS_ASSERTIONS_FILE));
			settings.setSuccessRDFFormat(SUCCESS_RDF_FORMAT);
			settings.setLogFile(ctx.getRealPath(logFileName));
			settings.setErrorLogFile(ctx.getRealPath(errorLogFileName));
			settings.setAddedDataFile(ctx.getRealPath(ADDED_DATA_FILE));
			settings.setRemovedDataFile(ctx.getRealPath(REMOVED_DATA_FILE));
			WebappDaoFactory wadf = (WebappDaoFactory) ctx.getAttribute("webappDaoFactory");
			settings.setDefaultNamespace(wadf.getDefaultNamespace());
			settings.setAssertionOntModelSelector(ModelContext.getBaseOntModelSelector(ctx));
			settings.setInferenceOntModelSelector(ModelContext.getInferenceOntModelSelector(ctx));
			
			try {
				OntModel oldTBoxModel = loadModelFromDirectory(ctx.getRealPath(OLD_TBOX_MODEL_DIR));
				settings.setOldTBoxModel(oldTBoxModel);
				OntModel newTBoxModel = loadModelFromDirectory(ctx.getRealPath(NEW_TBOX_MODEL_DIR));
				settings.setNewTBoxModel(newTBoxModel);
				OntModel oldTBoxAnnotationsModel = loadModelFromDirectory(ctx.getRealPath(OLD_TBOX_ANNOTATIONS_DIR));
				settings.setOldTBoxAnnotationsModel(oldTBoxAnnotationsModel);
				OntModel newTBoxAnnotationsModel = loadModelFromDirectory(ctx.getRealPath(NEW_TBOX_ANNOTATIONS_DIR));
				settings.setNewTBoxAnnotationsModel(newTBoxAnnotationsModel);
			} catch (ModelDirectoryNotFoundException e) {
				log.info("Knowledge base update directories not found.  " +
						 "No update will be performed.");
				return;
			}
				
			try {		
			   KnowledgeBaseUpdater ontologyUpdater = new KnowledgeBaseUpdater(settings);
			  
			   try {
				  if (ontologyUpdater.updateRequired()) {
					  ctx.setAttribute(IndexConstants.INDEX_REBUILD_REQUESTED_AT_STARTUP, Boolean.TRUE);
					  ctx.setAttribute(KBM_REQURIED_AT_STARTUP, Boolean.TRUE);
					  //doMiscAppMetadataReplacements(ctx.getRealPath(MISC_REPLACEMENTS_FILE), oms);
					  reloadDisplayModel(ctx);
					  ontologyUpdater.update();
				  }
			   } catch (IOException ioe) {
					String errMsg = "IOException updating knowledge base " +
						"for ontology changes: ";
					// Tomcat doesn't always seem to print exceptions thrown from
					// context listeners
					System.out.println(errMsg);
					ioe.printStackTrace();
					throw new RuntimeException(errMsg, ioe);
			   }	
			} catch (Throwable t){
				  log.warn("warning", t);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}	
	
	/**
	 * 
	 * Behavior changed from 1.0
	 * 
	 * Replace any triple X P S in the application metadata model
	 * with X P T where X, P, and T are specified in the input file
	 * @param filename containing replacement values
	 * @param OntModelSelector oms
	 */
	private void doMiscAppMetadataReplacements(String filename, OntModelSelector oms) {
		try {
		    Model replacementValues = ModelFactory.createDefaultModel();
		    OntModel applicationMetadataModel = oms.getApplicationMetadataModel();
		    FileInputStream fis = new FileInputStream(new File(filename));
		    replacementValues.read(fis, null);
		    Model retractions = ModelFactory.createDefaultModel();
		    Model additions = ModelFactory.createDefaultModel();
		    StmtIterator replaceIt = replacementValues.listStatements();
		    while (replaceIt.hasNext()) {
		    	Statement replacement = replaceIt.nextStatement();
		    	applicationMetadataModel.enterCriticalSection(Lock.WRITE);
		    	try {
		    		StmtIterator stmtIt = 
		    			    applicationMetadataModel.listStatements( 
		    			    		replacement.getSubject(), 
		    			    		replacement.getPredicate(),
		    			    		(RDFNode) null);
		    		while (stmtIt.hasNext()) {
		    			Statement stmt = stmtIt.nextStatement();
		    			retractions.add(stmt);
		    			additions.add(stmt.getSubject(),
		    					replacement.getPredicate(), 
		    					replacement.getObject());
		    		}
		    		applicationMetadataModel.remove(retractions);
		    		applicationMetadataModel.add(additions);
		    	} finally {
		    		applicationMetadataModel.leaveCriticalSection();
		    	}
		    }
		} catch (FileNotFoundException fnfe) {
			log.warn("Couldn't find miscellaneous application metadata replacement file: " + filename);
		
		} catch (Exception e) {
			log.error("Error performing miscellaneous application metadata " +
					" replacements.", e);
		}
	}
	
	private void reloadDisplayModel(ServletContext ctx) {
	    log.info("Reloading display model");
	    Object o = ctx.getAttribute("displayOntModel");
	    if (o instanceof OntModel) {
	        OntModel displayModel = (OntModel) o;
	        displayModel.removeAll((Resource) null, (Property) null, (RDFNode) null);
	        if (displayModel.size() != 0) {
	            log.error("Display model not cleared successfully");
	        }
            JenaPersistentDataSourceSetup.readOntologyFilesInPathSet(
                    JenaPersistentDataSourceSetup.APPPATH, ctx, displayModel);
            log.info("Display model reloaded");
            if (displayModel.size() == 0) {
                log.warn("Display model empty after reloading");
            }
	    } else {
	        log.error("No display model found in context");
	    }
	}
	
	private OntModel loadModelFromDirectory(String directoryPath) {
		
		OntModel om = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		File directory = new File(directoryPath);
		if (!directory.isDirectory()) {
			throw new ModelDirectoryNotFoundException(directoryPath + " must be a directory " +
					"containing RDF files.");
		}
		File[] rdfFiles = directory.listFiles();
		for (int i = 0; i < rdfFiles.length; i++) {
			try {
				File f = rdfFiles[i];
				FileInputStream fis = new FileInputStream(f);
				try {
					if (f.getName().endsWith(".n3")) {
						om.read(fis, null, "N3");
					} else {
						om.read(fis, null, "RDF/XML");
					}
				} catch (Exception e) {
					log.error("Unable to load RDF from " + f.getName(), e); 
				}
			} catch (FileNotFoundException fnfe) {
				log.error(rdfFiles[i].getName() + " not found. Unable to load" +
						" RDF from this location: " + directoryPath);
			}
		}
		return om;
	}
	
	public void contextDestroyed(ServletContextEvent arg0) {
		// nothing to do	
	}
	
	public static String getAskUpdatedQueryPath(ServletContext ctx) {
		return ctx.getRealPath(ASK_QUERY_FILE);
	
    }
	
	private static String timestampedFileName(String prefix, String suffix) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-sss");
		return prefix + "." + sdf.format(new Date()) + "." + suffix;
	}
	
	private class ModelDirectoryNotFoundException extends RuntimeException {
		public ModelDirectoryNotFoundException(String msg) {
			super(msg);
		}
	}
}
