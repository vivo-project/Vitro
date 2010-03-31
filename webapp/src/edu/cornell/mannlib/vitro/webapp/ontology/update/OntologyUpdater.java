/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;

/**
 * Performs knowledge base updates if necessary to align with a
 * new ontology version.
 * 
 * This class assumes that config and mapping data files are available
 * at DATA_DIR.
 * 
 * @author bjl23
 *
 */
public class OntologyUpdater {

	
	
	private final Log log = LogFactory.getLog(OntologyUpdater.class);
	
	private OntologyUpdateSettings settings;
	
	public OntologyUpdater(OntologyUpdateSettings settings) {
		this.settings = settings;
	}
	
	public void update() throws IOException {	
		// Check to see if the update is necessary.
		if (updateRequired()) {
			performUpdate();
			// add assertions to the knowledge base showing that the 
			// update was successful, so we don't need to run it again.
			assertSuccess();
		}
	}
	
	private void performUpdate() throws IOException {
		List<AtomicOntologyChange> rawChanges = getAtomicOntologyChanges();
		
		AtomicOntologyChangeLists changes = 
				new AtomicOntologyChangeLists(rawChanges, 
						settings.getOntModelSelector().getTBoxModel());
		
		//updateTBox(changes);
		//preprocessChanges(changes);
		
		updateABox(changes);
		
		updateTBoxAnnotations();
		
		// perform additional additions and retractions
	}
	
	private List<AtomicOntologyChange> getAtomicOntologyChanges() 
			throws IOException {
		return (new OntologyChangeParser()).parseFile(settings.getDiffFile());
	}
	

	
	private void updateABox(AtomicOntologyChangeLists changes) {
		// perform operations based on change objects
		// run additional SPARQL CONSTRUCTS 
	}
	
	private void updateTBoxAnnotations() {
		// Stella's code is called here
	}
	
	/**
	 * Executes a SPARQL ASK query to determine whether the knowledge base
	 * needs to be updated to conform to a new ontology version
	 */
	private boolean updateRequired() throws IOException {
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
	
	private void assertSuccess() throws FileNotFoundException {
		try {
		    Model m = settings.getOntModelSelector().getApplicationMetadataModel();
		    File successAssertionsFile = 
		    	new File(settings.getSuccessAssertionsFile()); 
		    InputStream inStream = new FileInputStream(successAssertionsFile);
		    m.enterCriticalSection(Lock.WRITE);
		    try {
		    	m.read(inStream, settings.getSuccessRDFFormat());
		    } finally {
		    	m.leaveCriticalSection();
		    }
		} catch (Exception e) {
			// TODO: log something to the error log
		}
	}
	
	private void log(String log) {
		
	}
	
	/**
	 * A class that allows to access two different ontology change lists,
	 * one for class changes and the other for property changes.  The 
	 * constructor will split a list containing both types of changes.
	 * @author bjl23
	 *
	 */
	private class AtomicOntologyChangeLists {
		
		private List<AtomicOntologyChange> atomicClassChanges;

		private List<AtomicOntologyChange> atomicPropertyChanges;
		
		public AtomicOntologyChangeLists(
				List<AtomicOntologyChange> changeList, OntModel tboxModel) {
			// TODO: split the main list of change objects into two lists 
			// depending on whether they refer to classes or properties
		}
		
		public List<AtomicOntologyChange> getAtomicClassChanges() {
			return atomicClassChanges;
		}

		public List<AtomicOntologyChange> getAtomicPropertyChanges() {
			return atomicPropertyChanges;
		}
		
	}
	
	
}
