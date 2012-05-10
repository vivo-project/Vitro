/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/*
 * Interface for API to write, read, and update Vitro's RDF store, with support 
 * to allow listening, logging and auditing.
 */

public interface RDFService {

	public enum SPARQLQueryType {
	    SELECT, CONSTRUCT, DESCRIBE, ASK
	}

	public enum ModelSerializationFormat {
	    RDFXML, N3
	}
	
	/**
	 * Perform a series of additions to and or removals from specified graphs
	 * in the RDF store.  preConditionSparql will be executed against the 
	 * union of all the graphs in the knowledge base before any updates are made. 
	 * If the precondition query returns a non-empty result, no updates
	 * will be made. 
	 * 
	 * @param ChangeSet - a set of changes to be performed on the RDF store.
	 *    
	 * @return boolean - indicates whether the precondition was satisfied            
	 */
	public boolean changeSetUpdate(ChangeSet changeSet) throws RDFServiceException;
		
	/**
	 * If the given individual already exists in the default graph, throws an 
	 * RDFServiceException, otherwise adds one type assertion to the default
	 * graph.
	 * 
	 * @param String individualURI - URI of the individual to be added
	 * @param String individualTypeURI - URI of the type for the individual
	 */
	public void newIndividual(String individualURI, String individualTypeURI) throws RDFServiceException;

	/**
	 * If the given individual already exists in the given graph, throws an 
	 * RDFServiceException, otherwise adds one type assertion to the given
	 * graph.
	 *
	 * @param String individualURI - URI of the individual to be added
	 * @param String individualTypeURI - URI of the type for the individual
	 * @param String graphURI - URI of the graph to which to add the individual
	 */
	public void newIndividual(String individualURI, String individualTypeURI, String graphURI) throws RDFServiceException;
	
	/**
	 * Performs a SPARQL construct query against the knowledge base. The query may have
	 * an embedded graph identifier.
	 * 
	 * @param String query - the SPARQL query to be executed against the RDF store
	 * @param RDFService.ModelSerializationFormat resultFormat - type of serialization for RDF result of the SPARQL query
	 * @param OutputStream outputStream - the result of the query
	 * 
	 */
	public InputStream sparqlConstructQuery(String query, RDFService.ModelSerializationFormat resultFormat) throws RDFServiceException;
	
	/**
	 * Performs a SPARQL describe query against the knowledge base. The query may have
	 * an embedded graph identifier.
	 * 
	 * @param String query - the SPARQL query to be executed against the RDF store
	 * @param RDFService.ModelSerializationFormat resultFormat - type of serialization for RDF result of the SPARQL query
	 * 
	 * @return InputStream - the result of the query
	 * 
	 */
	public InputStream sparqlDescribeQuery(String query, RDFService.ModelSerializationFormat resultFormat) throws RDFServiceException;
	
	/**
	 * Performs a SPARQL select query against the knowledge base. The query may have
	 * an embedded graph identifier.
	 * 
	 * @param String query - the SPARQL query to be executed against the RDF store
	 * 
	 * @return InputStream - the result of the query
	 * 
	 */
	public InputStream sparqlSelectQuery(String query) throws RDFServiceException;
	
	/**
	 * Performs a SPARQL ASK query against the knowledge base. The query may have
	 * an embedded graph identifier.
	 * 
	 * @param String query - the SPARQL query to be executed against the RDF store
	 * 
	 * @return  boolean - the result of the SPARQL query 
	 */
	
	public boolean sparqlAskQuery(String query) throws RDFServiceException;
	
	/**
	 * Get a list of all the graph URIs in the RDF store.
	 * 
	 * @return  List<String> - list of all the graph URIs in the RDF store 
	 */
	public List<String> getGraphURIs() throws RDFServiceException;

	/**
	 * TODO - what is the definition of this method?
	 * @return 
	 */
	public void getGraphMetadata() throws RDFServiceException;
		
	/**
	 * Get the URI of the default write graph
	 * 
	 * @return String URI of default write graph
	 */
	public String getDefaultWriteGraphURI() throws RDFServiceException;
	
	/**
	 * Get the URI of the default read graph
	 * 
	 * @return String URI of default read graph
	 */
	public String getDefaultReadGraphURI() throws RDFServiceException;
	
	/**
	 * Register a listener to listen to changes in any graph in
	 * the RDF store.
	 * 
	 * @return String URI of default read graph
	 */
	public void registerListener(ChangeListener changeListener) throws RDFServiceException;
	
	/**
	 * Unregister a listener to listen to changes in any graph in
	 * the RDF store.
	 * 
	 * @return String URI of default read graph
	 */
	public void unregisterListener(ChangeListener changeListener) throws RDFServiceException;

	/**
	 * Create a ChangeSet object
	 * 
	 * @return a ChangeSet object
	 */
	public ChangeSet manufactureChangeSet();	
}
