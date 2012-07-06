/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.ontology.update.KnowledgeBaseUpdater;
import edu.cornell.mannlib.vitro.webapp.ontology.update.UpdateSettings;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ModelSerializationFormat;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

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
	private static final String OLD_TBOX_MODEL_DIR = DATA_DIR + "oldVersion/";
	private static final String NEW_TBOX_MODEL_DIR = "/WEB-INF/filegraph/tbox/";
	private static final String OLD_TBOX_ANNOTATIONS_DIR = DATA_DIR + "oldAnnotations/";
	private static final String NEW_TBOX_ANNOTATIONS_DIR = "/WEB-INF/ontologies/user/tbox/";
	//For display model migration
	private static final String OLD_DISPLAYMODEL_TBOX_PATH = DATA_DIR + "oldDisplayModel/displayTBOX.n3";
	private static final String NEW_DISPLAYMODEL_TBOX_PATH = "/WEB-INF/ontologies/app/menuload/displayTBOX.n3";
	private static final String OLD_DISPLAYMODEL_DISPLAYMETADATA_PATH = DATA_DIR + "oldDisplayModel/displayDisplay.n3";
	private static final String NEW_DISPLAYMODEL_DISPLAYMETADATA_PATH = "/WEB-INF/ontologies/app/menuload/displayDisplay.n3";
	private static final String NEW_DISPLAYMODEL_PATH = "/WEB-INF/ontologies/app/menu.n3";
	private static final String LOADED_STARTUPT_DISPLAYMODEL_DIR = "/WEB-INF/ontologies/app/loadedAtStartup/";
	private static final String OLD_DISPLAYMODEL_VIVOLISTVIEW_PATH = DATA_DIR + "oldDisplayModel/vivoListViewConfig.rdf";

	public void contextInitialized(ServletContextEvent sce) {
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
			settings.setUnionOntModelSelector(ModelContext.getUnionOntModelSelector(ctx));
			settings.setDisplayModel(ModelContext.getDisplayModel(ctx));
			try {
				OntModel oldTBoxModel = loadModelFromDirectory(ctx.getRealPath(OLD_TBOX_MODEL_DIR));
				settings.setOldTBoxModel(oldTBoxModel);
				OntModel newTBoxModel = loadModelFromDirectory(ctx.getRealPath(NEW_TBOX_MODEL_DIR));
				settings.setNewTBoxModel(newTBoxModel);
				OntModel oldTBoxAnnotationsModel = loadModelFromDirectory(ctx.getRealPath(OLD_TBOX_ANNOTATIONS_DIR));
				settings.setOldTBoxAnnotationsModel(oldTBoxAnnotationsModel);
				OntModel newTBoxAnnotationsModel = loadModelFromDirectory(ctx.getRealPath(NEW_TBOX_ANNOTATIONS_DIR));
				settings.setNewTBoxAnnotationsModel(newTBoxAnnotationsModel);
				//Display model tbox and display metadata 
				//old display model tbox model
				OntModel oldDisplayModelTboxModel = loadModelFromFile(ctx.getRealPath(OLD_DISPLAYMODEL_TBOX_PATH));
				settings.setOldDisplayModelTboxModel(oldDisplayModelTboxModel);
				//new display model tbox model
				OntModel newDisplayModelTboxModel = loadModelFromFile(ctx.getRealPath(NEW_DISPLAYMODEL_TBOX_PATH));
				settings.setNewDisplayModelTboxModel(newDisplayModelTboxModel);
				//old display model display model metadata
				OntModel oldDisplayModelDisplayMetadataModel = loadModelFromFile(ctx.getRealPath(OLD_DISPLAYMODEL_DISPLAYMETADATA_PATH));
				settings.setOldDisplayModelDisplayMetadataModel(oldDisplayModelDisplayMetadataModel);
				//new display model display model metadata
				OntModel newDisplayModelDisplayMetadataModel = loadModelFromFile(ctx.getRealPath(NEW_DISPLAYMODEL_DISPLAYMETADATA_PATH));
				settings.setNewDisplayModelDisplayMetadataModel(newDisplayModelDisplayMetadataModel);
				//Get new display model
				OntModel newDisplayModelFromFile = loadModelFromFile(ctx.getRealPath(NEW_DISPLAYMODEL_PATH));
				settings.setNewDisplayModelFromFile(newDisplayModelFromFile);
				OntModel loadedAtStartupFiles = loadModelFromDirectory(ctx.getRealPath(LOADED_STARTUPT_DISPLAYMODEL_DIR));
				settings.setLoadedAtStartupDisplayModel(loadedAtStartupFiles);
				OntModel oldDisplayModelVivoListView = loadModelFromFile(ctx.getRealPath(OLD_DISPLAYMODEL_VIVOLISTVIEW_PATH));
				settings.setVivoListViewConfigDisplayModel(oldDisplayModelVivoListView);
				
			} catch (ModelDirectoryNotFoundException e) {
				log.info("Knowledge base update directories not found.  " +
						 "No update will be performed.");
				return;
			}
				
			try {		
			   KnowledgeBaseUpdater ontologyUpdater = new KnowledgeBaseUpdater(settings);
			  
			   try {
				  if (ontologyUpdater.updateRequired(ctx)) {
					  ctx.setAttribute(KBM_REQURIED_AT_STARTUP, Boolean.TRUE);
					  ontologyUpdater.update(ctx);
					  migrateDisplayModel(ontologyUpdater);
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
	
	//Multiple changes from 1.4 to 1.5 will occur
	
  	private void migrateDisplayModel(KnowledgeBaseUpdater ontologyUpdater) {
  		ontologyUpdater.migrateDisplayModel();
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
			readFile(rdfFiles[i], om, directoryPath);
		}
		return om;
	}
	
	//load file from file path
	private OntModel loadModelFromFile(String filePath) {
		
		OntModel om = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		File file = new File(filePath);
		if (!file.isFile()) {
			throw new ModelFileNotFoundException(filePath + " must be a file " +
					"containing RDF files.");
		}
		readFile(file, om, filePath);
		return om;
	}
	
	private void readFile(File f, OntModel om, String path) {
		try {
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
			log.error(f.getName() + " not found. Unable to load" +
					" RDF from this location: " + path);
		}	
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
	
	private class ModelFileNotFoundException extends RuntimeException {
		public ModelFileNotFoundException(String msg) {
			super(msg);
		}
	}
}
