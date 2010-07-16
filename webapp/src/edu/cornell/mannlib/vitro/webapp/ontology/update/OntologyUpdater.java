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

import edu.cornell.mannlib.vitro.webapp.utils.jena.JenaIngestUtils;

/**
 * Performs knowledge base updates if necessary to align with a
 * new ontology version.
 * 
 * @author bjl23
 *
 */
public class OntologyUpdater {

	//private final Log log = LogFactory.getLog(OntologyUpdater.class);
	
	private OntologyUpdateSettings settings;
	private OntologyChangeLogger logger;
	private OntologyChangeRecord record;
	
	public OntologyUpdater(OntologyUpdateSettings settings) {
		this.settings = settings;
		this.logger = new SimpleOntologyChangeLogger(settings.getLogFile(),	settings.getErrorLogFile());
		this.record = new SimpleOntologyChangeRecord(settings.getAddedDataFile(), settings.getRemovedDataFile());
	}
	
	public boolean update() throws IOException {	
		
		// Check to see if the update is necessary.
		
		boolean updateRequired = updateRequired();
		if (updateRequired) {
			
			try {
			     performUpdate();
			} catch (Exception e) {
				 logger.logError(e.getMessage());
				 e.printStackTrace();
			}

			if (!logger.errorsWritten()) {
				// add assertions to the knowledge base showing that the 
				// update was successful, so we don't need to run it again.
				// TODO improve error handling in future version.
				assertSuccess();
			}
			
			record.writeChanges();
			logger.closeLogs();

		}
		
		return updateRequired;
		
	}
	
	private void performUpdate() throws IOException {
		
		performSparqlConstructAdditions(settings.getSparqlConstructAdditionsDir(), settings.getOntModelSelector().getABoxModel());
		
		List<AtomicOntologyChange> rawChanges = getAtomicOntologyChanges();
		
		AtomicOntologyChangeLists changes = new AtomicOntologyChangeLists(rawChanges, 
						                                                  settings.getNewTBoxModel(), 
						                                                  settings.getOldTBoxModel());
		   //process the TBox before the ABox
	       updateTBoxAnnotations();

    	   updateABox(changes);
		
	}
	
	
	private void performSparqlConstructAdditions(String sparqlConstructDir, OntModel aboxModel) throws IOException {
		
		Model anonModel = performSparqlConstructs(sparqlConstructDir, aboxModel);
		
		if (anonModel == null) {
			return;
		}
		
		aboxModel.enterCriticalSection(Lock.WRITE);
		try {
			JenaIngestUtils jiu = new JenaIngestUtils();
			Model additions = jiu.renameBNodes(anonModel, settings.getDefaultNamespace() + "n", 
					aboxModel);
			Model actualAdditions = ModelFactory.createDefaultModel();
			StmtIterator stmtIt = additions.listStatements();
			while (stmtIt.hasNext()) {
				Statement stmt = stmtIt.nextStatement();
				if (!aboxModel.contains(stmt)) {
					actualAdditions.add(stmt);
				}
			}
			aboxModel.add(actualAdditions);
			if (actualAdditions.size() > 0) {
				logger.log("Constructed " + actualAdditions.size() + " new " +
						   "statement" 
						   + ((actualAdditions.size() > 1) ? "s" : "") + 
						   " using SPARQL CONSTRUCT queries.");
			}
			record.recordAdditions(actualAdditions);
		} finally {
			aboxModel.leaveCriticalSection();
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
			OntModel aboxModel) throws IOException {
		
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
				BufferedReader reader = 
					new BufferedReader(new FileReader(sparqlFile));
				StringBuffer fileContents = new StringBuffer();
				String ln;
				while ( (ln = reader.readLine()) != null) {
					fileContents.append(ln).append('\n');
				}
				try {
					Query q = QueryFactory.create(fileContents.toString(), 
							Syntax.syntaxARQ);
					aboxModel.enterCriticalSection(Lock.WRITE);
					try {
						QueryExecution qe = QueryExecutionFactory.create(q,
								aboxModel);
						qe.execConstruct(anonModel);
					} finally {
						aboxModel.leaveCriticalSection();
					}
				} catch (Exception e) {
					logger.logError(this.getClass().getName() + 
							".performSparqlConstructs() unable to execute " +
							"query at " + sparqlFile + ". Error message is: " + e.getMessage());
				}
			} catch (FileNotFoundException fnfe) {
				logger.logError(this.getClass().getName() + 
						".performSparqlConstructs() could not find " +
						" SPARQL CONSTRUCT file " + sparqlFile + ". Skipping.");
			}	
		}
		
        return anonModel;
	}
	
	
	private List<AtomicOntologyChange> getAtomicOntologyChanges() 
			throws IOException {
		return (new OntologyChangeParser(logger))
				.parseFile(settings.getDiffFile());
	}
	

	
	private void updateABox(AtomicOntologyChangeLists changes) 
			throws IOException {
		

		OntModel oldTBoxModel = settings.getOldTBoxModel();
		OntModel newTBoxModel = settings.getNewTBoxModel();
		OntModel ABoxModel = settings.getOntModelSelector().getABoxModel();
		ABoxUpdater aboxUpdater = new ABoxUpdater(
				oldTBoxModel, newTBoxModel, ABoxModel, 
				settings.getNewTBoxAnnotationsModel(), logger, record);
		aboxUpdater.processPropertyChanges(changes.getAtomicPropertyChanges());
		aboxUpdater.processClassChanges(changes.getAtomicClassChanges());
 
	}
	
	private void updateTBoxAnnotations() throws IOException {
		(new TBoxUpdater(settings.getOldTBoxAnnotationsModel(),
				settings.getNewTBoxAnnotationsModel(),
		        settings.getOntModelSelector().getABoxModel(), logger, record))
		        .updateVitroPropertyDefaultValues();
	}
	
	/**
	 * Executes a SPARQL ASK query to determine whether the knowledge base
	 * needs to be updated to conform to a new ontology version
	 */
	public boolean updateRequired() throws IOException {
		String sparqlQueryStr = loadSparqlQuery(settings.getAskQueryFile());
		if (sparqlQueryStr == null) {
			return false;
		}
		Model m = settings.getOntModelSelector().getApplicationMetadataModel();
		Query query = QueryFactory.create(sparqlQueryStr);
		QueryExecution qexec = QueryExecutionFactory.create(query, m);
		
		// if the ASK query DOES have a solution (i.e. the assertions exist
		// showing that the update has already been performed), then the update
		// is NOT required.
		return !qexec.execAsk(); 
		
	}
	
	/**
	 * loads a SPARQL ASK query from a text file
	 * @param filePath
	 * @return the query string or null if file not found
	 */
	private String loadSparqlQuery(String filePath) throws IOException {
		File file = new File(settings.getAskQueryFile());	
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
			
		    Model m = settings.getOntModelSelector().getApplicationMetadataModel();
		    File successAssertionsFile = 
		    	new File(settings.getSuccessAssertionsFile()); 
		    InputStream inStream = new FileInputStream(successAssertionsFile);
		    m.enterCriticalSection(Lock.WRITE);
		    try {
		    	m.read(inStream, null, settings.getSuccessRDFFormat());
		    	logger.log("Successfully finished processing ontology changes.");
		    } finally {
		    	m.leaveCriticalSection();
		    }
		} catch (Exception e) {
			logger.logError(" unable to make RDF assertions about successful " +
					" update to new ontology version: " + e.getMessage());
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
					}
					else if (oldTboxModel.getOntClass(changeObj.getSourceURI()) != null) {
						atomicClassChanges.add(changeObj);
					}
					else{
						logger.logError("Source URI is neither a Property" +
								" nor a Class. " + "Change Object skipped for sourceURI: " + changeObj.getSourceURI());
					}
					
				}
				else if(changeObj.getDestinationURI() != null){
					
					if (newTboxModel.getOntProperty(changeObj.getDestinationURI()) != null) {
						atomicPropertyChanges.add(changeObj);
					} else if(newTboxModel.getOntClass(changeObj.
						getDestinationURI()) != null) {
						atomicClassChanges.add(changeObj);
					} else{
						logger.logError("Destination URI is neither a Property" +
								" nor a Class. " + "Change Object skipped for destinationURI: " + changeObj.getDestinationURI());
					}
				}
				else{
					logger.logError("Source and Destination URI can't be null. " 
							+ "Change Object skipped" );
				}
			}
			//logger.log("Property and Class change Object lists separated");
		}
		
		public List<AtomicOntologyChange> getAtomicClassChanges() {
			return atomicClassChanges;
		}

		public List<AtomicOntologyChange> getAtomicPropertyChanges() {
			return atomicPropertyChanges;
		}
		
	}
	
	
}
