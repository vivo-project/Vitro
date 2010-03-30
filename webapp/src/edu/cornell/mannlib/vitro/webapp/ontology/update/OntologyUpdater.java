/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;

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

	private final String DATA_DIR = "/WEB-INF/ontologies/update/";
	private final String ASK_QUERY_FILE = DATA_DIR + "ask.sparql";
	private final String SUCCESS_ASSERTIONS_FILE = DATA_DIR + "success.n3";
	private final String SUCCESS_RDF_FORMAT = "N3";
	private final String DIFF_FILE = DATA_DIR + "diff.tab.txt";
	
	private final Log log = LogFactory.getLog(OntologyUpdater.class);
	
	private ServletContext context;
	private OntModelSelector ontModelSelector;
	
	public OntologyUpdater(ServletContext context, 
			OntModelSelector ontModelSelector) {
		this.context = context;
		this.ontModelSelector = ontModelSelector;
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
	
	private void performUpdate() {
		List<AtomicOntologyChange> changes = getAtomicOntologyChanges();
		
		//preprocessChanges(changes);
		//updateTBox(changes);
		
		updateABox(changes);
		
		updateAnnotations();
		
		// perform additional additions and retractions
	}
	
	private List<AtomicOntologyChange> getAtomicOntologyChanges() {
		return null; //Anup's code is called here
	}
	
	private void updateABox(List<AtomicOntologyChange> changes) {
		// perform operations based on change objects
		// run additional SPARQL CONSTRUCTS 

	}
	
	private void updateAnnotations() {
		// Stella's code is called here
	}
	
	/**
	 * Executes a SPARQL ASK query to determine whether the knowledge base
	 * needs to be updated to conform to a new ontology version
	 */
	private boolean updateRequired() throws IOException {
		String sparqlQueryStr = loadSparqlQuery(ASK_QUERY_FILE);
		if (sparqlQueryStr == null) {
			return false;
		}
		Model m = ontModelSelector.getApplicationMetadataModel();
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
		File file = new File(context.getRealPath(filePath));	
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
	
	private void assertSuccess() {
	    Model m = ontModelSelector.getApplicationMetadataModel();
	    InputStream inStream = context.getResourceAsStream(SUCCESS_ASSERTIONS_FILE);
	    m.enterCriticalSection(Lock.WRITE);
	    try {
	    	m.read(inStream, SUCCESS_RDF_FORMAT);
	    } finally {
	    	m.leaveCriticalSection();
	    }
	}
	
}
