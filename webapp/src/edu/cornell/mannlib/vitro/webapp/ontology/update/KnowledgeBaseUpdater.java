/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;
import edu.cornell.mannlib.vitro.webapp.utils.jena.JenaIngestUtils;

/**
 * Performs knowledge base updates necessary to align with a
 * new ontology version.
 */
public class KnowledgeBaseUpdater {

	private final Log log = LogFactory.getLog(KnowledgeBaseUpdater.class);
	
	private UpdateSettings settings;
	private ChangeLogger logger;
	private ChangeRecord record;
	
	public KnowledgeBaseUpdater(UpdateSettings settings) {
		this.settings = settings;
		this.logger = null;
		this.record = new SimpleChangeRecord(settings.getAddedDataFile(), settings.getRemovedDataFile());
	}
	
	public void update() throws IOException {	
					
		if (this.logger == null) {
			this.logger = new SimpleChangeLogger(settings.getLogFile(),	settings.getErrorLogFile());
		}
			
		long startTime = System.currentTimeMillis();
        System.out.println("Migrating the knowledge base");
        log.info("Migrating the knowledge base");
        logger.log("Started knowledge base migration");
		
		try {
		     performUpdate();
		} catch (Exception e) {
			 logger.logError(e.getMessage());
			 e.printStackTrace();
		}

		if (!logger.errorsWritten()) {
			// add assertions to the knowledge base showing that the 
			// update was successful, so we don't need to run it again.
			assertSuccess();
		}
		
		record.writeChanges();
		logger.closeLogs();

		long elapsedSecs = (System.currentTimeMillis() - startTime)/1000;		
		System.out.println("Finished knowledge base migration in " + elapsedSecs + " second" + (elapsedSecs != 1 ? "s" : ""));
		log.info("Finished knowledge base migration in " + elapsedSecs + " second" + (elapsedSecs != 1 ? "s" : ""));
		
		return;
	}
	
	
	private void performUpdate() throws IOException {
		
		log.info("\tperforming SPARQL construct additions (abox)");
		performSparqlConstructAdditions(settings.getSparqlConstructAdditionsDir(), settings.getUnionOntModelSelector().getABoxModel() , settings.getAssertionOntModelSelector().getABoxModel());
		log.info("\tperforming SPARQL construct deletions (abox)");
		performSparqlConstructRetractions(settings.getSparqlConstructDeletionsDir(), settings.getUnionOntModelSelector().getABoxModel() , settings.getAssertionOntModelSelector().getABoxModel());
		
		List<AtomicOntologyChange> rawChanges = getAtomicOntologyChanges();
		
		AtomicOntologyChangeLists changes = new AtomicOntologyChangeLists(rawChanges,settings.getNewTBoxModel(),settings.getOldTBoxModel());
		
        //process the TBox before the ABox
		
		log.info("\tupdating tbox annotations");
	    updateTBoxAnnotations();
		
		log.info("\tupdating the abox");
    	updateABox(changes);
	}
	
	private void performSparqlConstructAdditions(String sparqlConstructDir, OntModel readModel, OntModel writeModel) throws IOException {
		
		Model anonModel = performSparqlConstructs(sparqlConstructDir, readModel, true);
		
		if (anonModel == null) {
			return;
		}
		
		writeModel.enterCriticalSection(Lock.WRITE);
		try {
			JenaIngestUtils jiu = new JenaIngestUtils();
			Model additions = jiu.renameBNodes(anonModel, settings.getDefaultNamespace() + "n", writeModel);
			Model actualAdditions = ModelFactory.createDefaultModel();
			StmtIterator stmtIt = additions.listStatements();
			
			while (stmtIt.hasNext()) {
				Statement stmt = stmtIt.nextStatement();
				if (!writeModel.contains(stmt)) {
					actualAdditions.add(stmt);
				}
			}
			
			writeModel.add(actualAdditions);
			record.recordAdditions(actualAdditions);
		} finally {
			writeModel.leaveCriticalSection();
		}	
	}
	
	private void performSparqlConstructRetractions(String sparqlConstructDir, OntModel readModel, OntModel writeModel) throws IOException {
		
		Model retractions = performSparqlConstructs(sparqlConstructDir, readModel, false);
		
		if (retractions == null) {
			return;
		}
		
		writeModel.enterCriticalSection(Lock.WRITE);
		
		try {
			writeModel.remove(retractions);
			record.recordRetractions(retractions);
		} finally {
			writeModel.leaveCriticalSection();
		}
		
	}
	
	/**
	 * Performs a set of arbitrary SPARQL CONSTRUCT queries on the 
	 * data, for changes that cannot be expressed as simple property
	 * or class additions, deletions, or renamings.
	 * Blank nodes created by the queries are given random URIs.
	 * @param sparqlConstructDir
	 * @param aboxModel
	 */
	private Model performSparqlConstructs(String sparqlConstructDir, 
			                              OntModel readModel,
			                              boolean add)   throws IOException {
		
		Model anonModel = ModelFactory.createDefaultModel();
		File sparqlConstructDirectory = new File(sparqlConstructDir);
		
		if (!sparqlConstructDirectory.isDirectory()) {
			logger.logError(this.getClass().getName() + 
					"performSparqlConstructs() expected to find a directory " +
					" at " + sparqlConstructDir + ". Unable to execute " +
					" SPARQL CONSTRUCTS.");
			return null;
		}
		
		File[] sparqlFiles = sparqlConstructDirectory.listFiles();
		for (int i = 0; i < sparqlFiles.length; i ++) {
			File sparqlFile = sparqlFiles[i];			
			try {
				BufferedReader reader = new BufferedReader(new FileReader(sparqlFile));
				StringBuffer fileContents = new StringBuffer();
				String ln;
				
				while ( (ln = reader.readLine()) != null) {
					fileContents.append(ln).append('\n');
				}
				
				try {
					log.debug("\t\tprocessing SPARQL construct query from file " + sparqlFiles[i].getName());
					Query q = QueryFactory.create(fileContents.toString(), Syntax.syntaxARQ);
					readModel.enterCriticalSection(Lock.READ);
					try {
						QueryExecution qe = QueryExecutionFactory.create(q,	readModel);
						long numBefore = anonModel.size();
						qe.execConstruct(anonModel);
						long numAfter = anonModel.size();
                        long num = numAfter - numBefore;
                        
                        if (num > 0) {
						   logger.log((add ? "Added " : "Removed ") + num + 
								   " statement"  + ((num > 1) ? "s" : "") + 
								   " using the SPARQL construct query from file " + sparqlFiles[i].getParentFile().getName() + "/" + sparqlFiles[i].getName());
                        }
                        qe.close();
					} finally {
						readModel.leaveCriticalSection();
					}
				} catch (Exception e) {
					logger.logError(this.getClass().getName() + 
							".performSparqlConstructs() unable to execute " +
							"query at " + sparqlFile + ". Error message is: " + e.getMessage());
				}
			} catch (FileNotFoundException fnfe) {
				logger.log("WARNING: performSparqlConstructs() could not find " +
						   " SPARQL CONSTRUCT file " + sparqlFile + ". Skipping.");
			}	
		}
		
        return anonModel;
	}
	
	
	private List<AtomicOntologyChange> getAtomicOntologyChanges() 
			throws IOException {
		return (new OntologyChangeParser(logger)).parseFile(settings.getDiffFile());
	}
	

	
	private void updateABox(AtomicOntologyChangeLists changes) 
			throws IOException {
		
		OntModel oldTBoxModel = settings.getOldTBoxModel();
		OntModel newTBoxModel = settings.getNewTBoxModel();
		OntModel ABoxModel = settings.getAssertionOntModelSelector().getABoxModel();
		ABoxUpdater aboxUpdater = new ABoxUpdater(oldTBoxModel, newTBoxModel, ABoxModel,settings.getNewTBoxAnnotationsModel(), logger, record);
		aboxUpdater.migrateExternalConcepts();
		aboxUpdater.processPropertyChanges(changes.getAtomicPropertyChanges());
		aboxUpdater.processClassChanges(changes.getAtomicClassChanges());
	}
	
	private void updateTBoxAnnotations() throws IOException {
		
		TBoxUpdater tboxUpdater = new TBoxUpdater(settings.getOldTBoxAnnotationsModel(),
		                                          settings.getNewTBoxAnnotationsModel(),
                                                  settings.getAssertionOntModelSelector().getTBoxModel(), logger, record);
                                                  
        tboxUpdater.updateDefaultAnnotationValues();
        //tboxUpdater.updateAnnotationModel();
	}
	
	/**
	 * Executes a SPARQL ASK query to determine whether the knowledge base
	 * needs to be updated to conform to a new ontology version
	 */
	public boolean updateRequired() throws IOException {
		
		boolean required = false;
		
		String sparqlQueryStr = loadSparqlQuery(settings.getAskUpdatedQueryFile());
		if (sparqlQueryStr == null) {
			return required;
		}
				
		Model abox = settings.getAssertionOntModelSelector().getABoxModel();
		Query query = QueryFactory.create(sparqlQueryStr);
		QueryExecution isUpdated = QueryExecutionFactory.create(query, abox);
		
		// if the ASK query DOES have a solution (i.e. the assertions exist
		// showing that the update has already been performed), then the update
		// is NOT required.
		
		if (isUpdated.execAsk()) {
			required = false;
		} else {
			required = true;
			if (JenaDataSourceSetupBase.isFirstStartup()) {
				assertSuccess();  
				log.info("The application is starting with an empty database. " +
				         "An indication will be added to the database that a " +
				         "knowledge base migration to the current version is " +
				         "not required.");
			    required = false;	
			}
		}
		
		return required; 
	}
		
	/**
	 * loads a SPARQL ASK query from a text file
	 * @param filePath
	 * @return the query string or null if file not found
	 */
	public static String loadSparqlQuery(String filePath) throws IOException {
		
		File file = new File(filePath);	
		if (!file.exists()) {
			return null;
		}
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuffer fileContents = new StringBuffer();
		String ln;		
		while ((ln = reader.readLine()) != null) {
			fileContents.append(ln).append('\n');
		}
		return fileContents.toString();				
	}
	
	private void assertSuccess() throws FileNotFoundException, IOException {
		try {
			
		    //Model m = settings.getAssertionOntModelSelector().getApplicationMetadataModel();
		    Model m = settings.getAssertionOntModelSelector().getABoxModel();
		    File successAssertionsFile = new File(settings.getSuccessAssertionsFile()); 
		    InputStream inStream = new FileInputStream(successAssertionsFile);
		    m.enterCriticalSection(Lock.WRITE);
		    try {
		    	m.read(inStream, null, settings.getSuccessRDFFormat());
		    	if (logger != null) {
		    		logger.logWithDate("Finished knowledge base migration");
		    	} 
		    } finally {
		    	m.leaveCriticalSection();
		    }
		} catch (Exception e) {
			if (logger != null) {
			    logger.logError(" unable to make RDF assertions about successful " +
					" update to new ontology version: " + e.getMessage());
			}
		}
	}
	
	/**
	 * A class that allows to access two different ontology change lists,
	 * one for class changes and the other for property changes.  The 
	 * constructor will split a list containing both types of changes.
	 * @author bjl23
	 *
	 */
	private class AtomicOntologyChangeLists {
		
		private List<AtomicOntologyChange> atomicClassChanges = 
				new ArrayList<AtomicOntologyChange>();

		private List<AtomicOntologyChange> atomicPropertyChanges =
				new ArrayList<AtomicOntologyChange>();
		
		public AtomicOntologyChangeLists (
				List<AtomicOntologyChange> changeList, OntModel newTboxModel,
				OntModel oldTboxModel) throws IOException {
			
			Iterator<AtomicOntologyChange> listItr = changeList.iterator();
			
			while(listItr.hasNext()) {
				AtomicOntologyChange changeObj = listItr.next();
				if (changeObj.getSourceURI() != null){
			
					if (oldTboxModel.getOntProperty(changeObj.getSourceURI()) != null){
						 atomicPropertyChanges.add(changeObj);
					} else if (oldTboxModel.getOntClass(changeObj.getSourceURI()) != null) {
						 atomicClassChanges.add(changeObj);
					} else if ("Prop".equals(changeObj.getNotes())) {
						 atomicPropertyChanges.add(changeObj);
					} else if ("Class".equals(changeObj.getNotes())) {
						 atomicClassChanges.add(changeObj);
					} else{
						 logger.log("WARNING: Source URI is neither a Property" +
						    		" nor a Class. " + "Change Object skipped for sourceURI: " + changeObj.getSourceURI());
					}
					
				} else if(changeObj.getDestinationURI() != null){
					
					if (newTboxModel.getOntProperty(changeObj.getDestinationURI()) != null) {
						atomicPropertyChanges.add(changeObj);
					} else if(newTboxModel.getOntClass(changeObj.
						getDestinationURI()) != null) {
						atomicClassChanges.add(changeObj);
					} else{
						logger.log("WARNING: Destination URI is neither a Property" +
								" nor a Class. " + "Change Object skipped for destinationURI: " + changeObj.getDestinationURI());
					}
				} else{
					logger.log("WARNING: Source and Destination URI can't be null. " + "Change Object skipped" );
				}
			}
			//logger.log("Property and Class change Object lists have been created");
		}
		
		public List<AtomicOntologyChange> getAtomicClassChanges() {
			return atomicClassChanges;
		}

		public List<AtomicOntologyChange> getAtomicPropertyChanges() {
			return atomicPropertyChanges;
		}	
	}	
}
