/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelChangedListener;
import org.apache.jena.rdf.model.RDFNode;

/**
 * Interface for API to write, read, and update Vitro's RDF store, with support 
 * to allow listening, logging and auditing.
 */

public interface RDFService {

	public enum SPARQLQueryType {
	    SELECT, CONSTRUCT, DESCRIBE, ASK
	}

	public enum ModelSerializationFormat {
	    RDFXML, N3, NTRIPLE
	}
	
	public enum ResultFormat {
	    JSON, CSV, XML, TEXT
	}
	
	/**
	 * Performs a series of additions to and or removals from specified graphs
	 * in the RDF store.  preConditionSparql is executed against the 
	 * union of all the graphs in the knowledge base before any updates are made. 
	 * If the precondition query returns a non-empty result, no updates
	 * are made made. 
	 * 
	 * @param changeSet - a set of changes to be performed on the RDF store.
	 *    
	 * @return boolean - indicates whether the precondition was satisfied            
	 */
	public boolean changeSetUpdate(ChangeSet changeSet) throws RDFServiceException;
		
	/**
	 * If the given individual already exists in the default write graph, throws an 
	 * RDFServiceException, otherwise adds one type assertion to the default write
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
	 * an embedded graph identifier. If the query does not contain a graph identifier
	 * the query is executed against the union of all named and unnamed graphs in the 
	 * store.
	 * 
	 * @param query - the SPARQL query to be executed against the RDF store
	 * @param resultFormat - type of serialization for RDF result of the SPARQL query
	 * 
	 * @return InputStream - the result of the query
	 * 
	 */
	public InputStream sparqlConstructQuery(String query, RDFService.ModelSerializationFormat resultFormat) throws RDFServiceException;

	/**
	 * Performs a SPARQL construct query against the knowledge base. The query may have
	 * an embedded graph identifier. If the query does not contain a graph identifier
	 * the query is executed against the union of all named and unnamed graphs in the
	 * store.
	 *
	 * @param query - the SPARQL query to be executed against the RDF store
	 * @param model - the Model to add the statements to
	 */
	public void sparqlConstructQuery(String query, Model model) throws RDFServiceException;

	/**
	 * Performs a SPARQL describe query against the knowledge base. The query may have
	 * an embedded graph identifier. If the query does not contain a graph identifier
	 * the query is executed against the union of all named and unnamed graphs in the 
	 * store.
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
	 * an embedded graph identifier. If the query does not contain a graph identifier
	 * the query is executed against the union of all named and unnamed graphs in the 
	 * store.
	 * 
	 * @param query - the SPARQL query to be executed against the RDF store
	 * @param resultFormat - format for the result of the Select query
	 * 
	 * @return InputStream - the result of the query
	 * 
	 */
	public InputStream sparqlSelectQuery(String query, RDFService.ResultFormat resultFormat) throws RDFServiceException;

	/**
	 * Performs a SPARQL select query against the knowledge base. The query may have
	 * an embedded graph identifier. If the query does not contain a graph identifier
	 * the query is executed against the union of all named and unnamed graphs in the
	 * store.
	 *
	 * @param query - the SPARQL query to be executed against the RDF store
	 * @param consumer - the class to consume the results of the query
	 */
	public void sparqlSelectQuery(String query, ResultSetConsumer consumer) throws RDFServiceException;

	/**
	 * Performs a SPARQL ASK query against the knowledge base. The query may have
	 * an embedded graph identifier. If the query does not contain a graph identifier
	 * the query is executed against the union of all named and unnamed graphs in the 
	 * store.
	 * 
	 * @param query - the SPARQL ASK query to be executed against the RDF store
	 * 
	 * @return  boolean - the result of the SPARQL ASK query 
	 */
	public boolean sparqlAskQuery(String query) throws RDFServiceException;
	
	/**
	 * Returns a list of all the graph URIs in the RDF store.
	 * 
	 * @return  list of all the named graph URIs in the RDF store.
	 *          Return an empty list of there no named graphs in
	 *                         the store. 
	 */
	public List<String> getGraphURIs() throws RDFServiceException;

	/**
	 * To be determined. This is a place holder and is not implemented
	 * in current implementations.
	 */
	public void getGraphMetadata() throws RDFServiceException;
		
	/**
	 * Returns the URI of the default write graph
	 * 
	 * @return String URI of default write graph. Returns null if no
	 *         default write graph has been set.
	 */
	public String getDefaultWriteGraphURI() throws RDFServiceException;
	
	/**
	 * Serializes the union of all named and unnamed graphs in the store to the
	 * supplied OutputStream, in N-Quads format. This method is designed for 
	 * exporting data from VIVO, so any filters should be bypassed. If possible, 
	 * this should be done without buffering in memory, so arbitrarily large 
	 * graphs can be exported.
	 * 
	 * @param outputStream - receives the serialized result.
	 */
	public void serializeAll(OutputStream outputStream) throws RDFServiceException;

	/**
	 * Serializes the contents of the named graph to the supplied OutputStream,
	 * in N-Triples format. This method is designed for exporting data from 
	 * VIVO, so any filters should be bypassed. If possible, this should be 
	 * done without buffering in memory, so arbitrarily large graphs can be 
	 * exported.
	 * 
	 * @param graphURI - the URI of the desired graph. May not be null.
	 * @param outputStream - receives the serialized result.
	 */
	public void serializeGraph(String graphURI, OutputStream outputStream) throws RDFServiceException;

	/**
	 * Tests to see whether the supplied serialization is equivalent to the 
	 * named graph, as it exists in the store. Equivalence means that if this 
	 * serialization were written to the store, the resulting graph would be 
	 * isomorphic to the existing named graph.
	 * 
	 * @param graphURI - the URI of the graph to test against. May not be null.
	 * @param serializedGraph - the contents to be compared with the existing graph. May not be null.
	 * @param serializationFormat - May not be null.
	 */
	public boolean isEquivalentGraph(String graphURI, InputStream serializedGraph, 
			ModelSerializationFormat serializationFormat) throws RDFServiceException;

	/**
	 * Tests to see whether the supplied serialization is equivalent to the
	 * named graph, as it exists in the store. Equivalence means that if this
	 * serialization were written to the store, the resulting graph would be
	 * isomorphic to the existing named graph.
	 *
	 * @param graphURI - the URI of the graph to test against. May not be null.
	 * @param graph - the contents to be compared with the existing graph. May not be null.
	 */
	public boolean isEquivalentGraph(String graphURI, Model graph) throws RDFServiceException;

	/**
	 * Registers a Jena listener to listen to changes in any graph in
	 * the RDF store.
	 * 
	 * @param changeListener - the change listener
	 */
	public void registerJenaModelChangedListener(ModelChangedListener changeListener) 
	        throws RDFServiceException;
	
	/**
	 * Unregisters a Jena listener from listening to changes in
	 * any graph in the RDF store
	 * 
	 * @param changeListener - the change listener
	 */
	public void unregisterJenaModelChangedListener(ModelChangedListener changeListener) 
	        throws RDFServiceException;
	
	/**
     * Registers a listener to listen to changes in any graph in
     * the RDF store.
     * 
     * @param changeListener - the change listener
     */
    public void registerListener(ChangeListener changeListener) throws RDFServiceException;
    
    /**
     * Unregisters a listener from listening to changes in
     * any graph in the RDF store
     * 
     * @param changeListener - the change listener
     */
    public void unregisterListener(ChangeListener changeListener) throws RDFServiceException;

	/**
	 * Creates a ChangeSet object
	 * 
	 * @return ChangeSet an empty ChangeSet object
	 */
	public ChangeSet manufactureChangeSet();

	public long countTriples(RDFNode subject, RDFNode predicate, RDFNode object) throws RDFServiceException;

	public Model getTriples(RDFNode subject, RDFNode predicate, RDFNode object, long limit, long offset) throws RDFServiceException;
		
	/**
     * Frees any resources held by this RDFService object
     * 
     * The implementation of this method should be idempotent so that
     * multiple invocations do not cause an error.
     */
    public void close();
}
