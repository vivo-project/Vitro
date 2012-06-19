/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice;

import java.io.InputStream;
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
	
	public enum ResultFormat {
	    JSON, CSV, XML, TEXT
	}
	
	/**
	 * Perform a series of additions to and or removals from specified graphs
	 * in the RDF store.  preConditionSparql will be executed against the 
	 * union of all the graphs in the knowledge base before any updates are made. 
	 * If the precondition query returns a non-empty result, no updates
	 * will be made. 
	 * 
	 * @param changeSet - a set of changes to be performed on the RDF store.
	 *    
	 * @return boolean - indicates whether the precondition was satisfied            
	 */
	public boolean changeSetUpdate(ChangeSet changeSet) throws RDFServiceException;
		
	/**
	 * If the given individual already exists in the default graph, throws an 
	 * RDFServiceException, otherwise adds one type assertion to the default
	 * graph.
	 * 
	 * @param individualURI - URI of the individual to be added
	 * @param individualTypeURI - URI of the type for the individual
	 */
	public void newIndividual(String individualURI, String individualTypeURI) throws RDFServiceException;

	/**
	 * If the given individual already exists in the given graph, throws an 
	 * RDFServiceException, otherwise adds one type assertion to the given
	 * graph.
	 *
	 * @param individualURI - URI of the individual to be added
	 * @param individualTypeURI - URI of the type for the individual
	 * @param graphURI - URI of the graph to which to add the individual
	 */
	public void newIndividual(String individualURI, String individualTypeURI, String graphURI) throws RDFServiceException;
	
	/**
	 * Performs a SPARQL construct query against the knowledge base. The query may have
	 * an embedded graph identifier.
	 * 
	 * @param query - the SPARQL query to be executed against the RDF store
	 * @param resultFormat - type of serialization for RDF result of the SPARQL query
	 * 
	 * @return InputStream - the result of the query
	 * 
	 */
	public InputStream sparqlConstructQuery(String query, RDFService.ModelSerializationFormat resultFormat) throws RDFServiceException;
	
	/**
	 * Performs a SPARQL describe query against the knowledge base. The query may have
	 * an embedded graph identifier.
	 * 
	 * @param query - the SPARQL query to be executed against the RDF store
	 * @param resultFormat - type of serialization for RDF result of the SPARQL query
	 * 
	 * @return InputStream - the result of the query
	 * 
	 */
	public InputStream sparqlDescribeQuery(String query, RDFService.ModelSerializationFormat resultFormat) throws RDFServiceException;
	
	/**
	 * Performs a SPARQL select query against the knowledge base. The query may have
	 * an embedded graph identifier.
	 * 
	 * @param query - the SPARQL query to be executed against the RDF store
	 * @param resultFormat - format for the result of the Select query
	 * 
	 * @return InputStream - the result of the query
	 * 
	 */
	public InputStream sparqlSelectQuery(String query, RDFService.ResultFormat resultFormat) throws RDFServiceException;
	
	/**
	 * Performs a SPARQL ASK query against the knowledge base. The query may have
	 * an embedded graph identifier.
	 * 
	 * @param query - the SPARQL query to be executed against the RDF store
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
	 * TBD - we need to define this method
	 */
	public void getGraphMetadata() throws RDFServiceException;
		
	/**
	 * Get the URI of the default write graph
	 * 
	 * @return String URI of default write graph
	 */
	public String getDefaultWriteGraphURI() throws RDFServiceException;
	
	/**
	 * Register a listener to listen to changes in any graph in
	 * the RDF store.
	 * 
	 * @param changeListener - the change listener
	 */
	public void registerListener(ChangeListener changeListener) throws RDFServiceException;
	
	/**
	 * Unregister a listener from listening to changes in
	 * the RDF store in any graph.
	 * 
	 * @param changeListener - the change listener
	 */
	public void unregisterListener(ChangeListener changeListener) throws RDFServiceException;

	/**
	 * Create a ChangeSet object
	 * 
	 * @return ChangeSet an empty ChangeSet object
	 */
	public ChangeSet manufactureChangeSet();	
		
	/**
     * Free any resources held by this RDFService object
     */
    public void close();
    
}
