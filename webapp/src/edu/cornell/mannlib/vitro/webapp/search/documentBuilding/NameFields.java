/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.documentBuilding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ResultFormat;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;

/**
 * Adds all labels to name fields, not just the one returned by Indivdiual.getName().
 */
public class NameFields implements DocumentModifier {
	RDFServiceFactory rsf;
	
	public static final VitroSearchTermNames term = new VitroSearchTermNames();
	public static final Log log = LogFactory.getLog(NameFields.class.getName());
	
	public NameFields( RDFServiceFactory rsf){
		this.rsf = rsf; 
	}
	
	@Override
	public void modifyDocument(Individual ind, SearchInputDocument doc) {
		if( ind == null || ind.getURI() == null ){
			return;
		}
		
		//also run SPARQL query to get rdfs:label values		
		String query = 
			"SELECT ?label WHERE {  " +
			"<" + ind.getURI() + "> " +
			"<http://www.w3.org/2000/01/rdf-schema#label> ?label  }";

		try {
			RDFService rdfService = rsf.getRDFService();
			BufferedReader stream = 
				new BufferedReader(new InputStreamReader(rdfService.sparqlSelectQuery(query, ResultFormat.CSV)));
			
			StringBuffer buffer = new StringBuffer();
			String line;

			//throw out first line since it is just a header
			stream.readLine();
			
			while( (line = stream.readLine()) != null ){
				buffer.append(line).append(' ');
			}
			
			log.debug("Adding labels for " + ind.getURI() + " \"" + buffer.toString() + "\"");
			doc.addField(term.NAME_RAW, buffer.toString());
			 
		} catch (RDFServiceException e) {
			log.error("could not get the rdfs:label for " + ind.getURI(), e);
		} catch (IOException e) {
			log.error("could not get the rdfs:label for " + ind.getURI(), e);
		}

	}
	
	@Override
	public void shutdown() { /*nothing to do */ }
}
