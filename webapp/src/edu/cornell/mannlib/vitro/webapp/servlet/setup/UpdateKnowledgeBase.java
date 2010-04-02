/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDao;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SimpleOntModelSelector;
import edu.cornell.mannlib.vitro.webapp.ontology.update.OntologyUpdateSettings;
import edu.cornell.mannlib.vitro.webapp.ontology.update.OntologyUpdater;

/**
 * Invokes process to test whether the knowledge base needs any updating
 * to align with ontology changes.
 * @author bjl23
 *
 */
public class UpdateKnowledgeBase implements ServletContextListener {
	
	private final static Log log = LogFactory.getLog(UpdateKnowledgeBase.class);
	
	private final String DATA_DIR = "/WEB-INF/ontologies/update/";
	private final String LOG_DIR = "logs/";
	private final String REMOVED_DATA_DIR = "removedData/";
	private final String ASK_QUERY_FILE = DATA_DIR + "ask.sparql";
	private final String SUCCESS_ASSERTIONS_FILE = DATA_DIR + "success.n3";
	private final String SUCCESS_RDF_FORMAT = "N3";
	private final String DIFF_FILE = DATA_DIR + "diff.tab.txt";
	private final String LOG_FILE = DATA_DIR + LOG_DIR + 
									"knowledgeBaseUpdate.log";
	private final String ERROR_LOG_FILE = DATA_DIR + LOG_DIR +
									"knowledgeBaseUpdate.error.log";
	private final String REMOVED_DATA_FILE = DATA_DIR + REMOVED_DATA_DIR +
									"removedData.rdf";
	private final String SPARQL_CONSTRUCTS_DIR = DATA_DIR + "sparqlConstructs/";
	private final String MISC_REPLACEMENTS_FILE = DATA_DIR + "miscReplacements.rdf";
	private final String OLD_TBOX_MODEL_DIR = DATA_DIR + "oldVersion/";
	private final String OLD_TBOX_ANNOTATIONS_DIR = DATA_DIR + "oldAnnotations/";
	private final String NEW_TBOX_ANNOTATIONS_DIR = "/WEB-INF/ontologies/user";
	
	public void contextInitialized(ServletContextEvent sce) {
		
		// TODO remove when ready
		if (true) {
			return;
		}

		ServletContext ctx = sce.getServletContext();
		
		OntModelSelector oms = new SimpleOntModelSelector(
				(OntModel) sce.getServletContext().getAttribute(
						JenaBaseDao.ASSERTIONS_ONT_MODEL_ATTRIBUTE_NAME));
		
		OntologyUpdateSettings settings = new OntologyUpdateSettings();
		settings.setAskQueryFile(ctx.getRealPath(ASK_QUERY_FILE));
		settings.setDataDir(ctx.getRealPath(DATA_DIR));
		settings.setSparqlConstructsDir(ctx.getRealPath(SPARQL_CONSTRUCTS_DIR));
		settings.setDiffFile(ctx.getRealPath(DIFF_FILE));
		settings.setSuccessAssertionsFile(
				ctx.getRealPath(SUCCESS_ASSERTIONS_FILE));
		settings.setSuccessRDFFormat(ctx.getRealPath(SUCCESS_RDF_FORMAT));
		settings.setLogFile(ctx.getRealPath(LOG_FILE));
		settings.setErrorLogFile(ctx.getRealPath(ERROR_LOG_FILE));
		settings.setRemovedDataFile(ctx.getRealPath(REMOVED_DATA_FILE));
		WebappDaoFactory wadf = (WebappDaoFactory) ctx.getAttribute("webappDaoFactory");
		settings.setDefaultNamespace(wadf.getDefaultNamespace());
		
		OntModel oldTBoxModel = loadModelFromDirectory(ctx.getRealPath(OLD_TBOX_MODEL_DIR));
		settings.setOldTBoxModel(oldTBoxModel);
		settings.setNewTBoxModel(oms.getTBoxModel());
		OntModel oldTBoxAnnotationsModel = loadModelFromDirectory(ctx.getRealPath(OLD_TBOX_ANNOTATIONS_DIR));
		settings.setOldTBoxAnnotationsModel(oldTBoxAnnotationsModel);
		OntModel newTBoxAnnotationsModel = loadModelFromDirectory(ctx.getRealPath(NEW_TBOX_ANNOTATIONS_DIR));
		settings.setNewTBoxAnnotationsModel(newTBoxAnnotationsModel);
		
		try {
			doMiscAppMetadataReplacements(ctx.getRealPath(MISC_REPLACEMENTS_FILE), oms);
			(new OntologyUpdater(settings)).update(); 
		} catch (IOException ioe) {
			String errMsg = "IOException updating knowledge base " +
				"for ontology changes: ";
			// Tomcat doesn't always seem to print exceptions thrown from
			// context listeners
			System.out.println(errMsg);
			ioe.printStackTrace();
			throw new RuntimeException(errMsg, ioe);
		}	
		
		
		
	}	
	
	/**
	 * Replace any triple X P S in the application metadata model
	 * with X P T where P and T are specified in the input file
	 * @param filename containing replacement values
	 * @param OntModelSelector oms
	 */
	private void doMiscAppMetadataReplacements(String filename, OntModelSelector oms) {
		try {
		    Model replacementValues = ModelFactory.createDefaultModel();
		    OntModel applicationMetadataModel = oms.getApplicationMetadataModel();
		    FileInputStream fis = new FileInputStream(new File(filename));
		    replacementValues.read(fis, null);
		    StmtIterator replaceIt = replacementValues.listStatements();
		    while (replaceIt.hasNext()) {
		    	Statement replacement = replaceIt.nextStatement();
		    	applicationMetadataModel.enterCriticalSection(Lock.WRITE);
		    	try {
		    		Iterator<Resource> resIt = 
		    			    applicationMetadataModel.listSubjectsWithProperty(
		    				replacement.getPredicate(), replacement.getObject());
		    		while (resIt.hasNext()) {
		    			Resource subj = resIt.next();
		    			applicationMetadataModel.removeAll(
		    					subj, replacement.getPredicate(), (RDFNode) null);
		    			applicationMetadataModel.add(
		    					subj, replacement.getPredicate(), replacement.getObject());
		    		}
		    	} finally {
		    		
		    	}
		    }
		} catch (Exception e) {
			log.error("Error performing miscellaneous application metadata " +
					" replacements.", e);
		}
	}
	
	private OntModel loadModelFromDirectory(String directoryPath) {
		OntModel om = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		File directory = new File(directoryPath);
		if (!directory.isDirectory()) {
			throw new RuntimeException(directoryPath + " must be a directory " +
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
						" RDF from this location.");
			}
		}
		return om;
	}
	
	public void contextDestroyed(ServletContextEvent arg0) {
		// nothing to do	
	}

}
