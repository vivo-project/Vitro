/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice;

import java.io.InputStream;
import java.util.List;

/*
 * Input parameter to changeSetUpdate() method in RDFService.
 * Represents a precondition query and an ordered list of model changes. 
 */

public interface ChangeSet {
		
	/**   
	 * Getter for the precondition query
	 * 
	 * @return String - a SPARQL query            
	 */	
	public String getPreconditionQuery();
	
	/**   
	 * Setter for the precondition query
	 * 
	 * @param preconditionQuery - a SPARQL query            
	 */	
	public void setPreconditionQuery(String preconditionQuery);
	
	/**   
	 * Getter for the precondition query type
	 * 
	 * @return RDFService.SPARQLQueryType - the precondition query type           
	 */	
	public RDFService.SPARQLQueryType getPreconditionQueryType();
	
	/**   
	 * Setter for the precondition query type
	 * 
	 * @param queryType - the precondition query type           
	 */	
	public void setPreconditionQueryType(RDFService.SPARQLQueryType queryType);

	/**   
	 * Getter for the list of model changes
	 * 
	 * @return List<ModelChange> - list of model changes           
	 */		
	public List<ModelChange> getModelChanges();
	
	/**   
	 * Adds one model change representing an addition to the list of model changes
	 * 
	 * @param model - a serialized RDF model (collection of triples)  
	 * @param serializationFormat - format of the serialized RDF model
	 * @param graphURI - URI of the graph to which the RDF model should be added         
	 */		
	public void addAddition(InputStream model, 
			                RDFService.ModelSerializationFormat serializationFormat,
			                String graphURI);
	
	/**   
	 * Adds one model change representing a deletion to the list of model changes
	 * 
	 * @param model - a serialized RDF model (collection of triples)  
	 * @param serializationFormat - format of the serialized RDF model
	 * @param graphURI - URI of the graph from which the RDF model should be removed         
	 */		
	public void addRemoval(InputStream model,
			               RDFService.ModelSerializationFormat serializationFormat,
			               String graphURI);

	/**   
	 * Creates an instance of the ModelChange class 
	 */		
	public ModelChange manufactureModelChange();
	
	/**   
	 * Creates an instance of the ModelChange class
	 * 
	 * @param serializedModel - a serialized RDF model (collection of triples)  
	 * @param serializationFormat - format of the serialized RDF model
	 * @param operation - the type of operation to be performed with the serialized RDF model 
	 * @param graphURI - URI of the graph on which to apply the model change operation        
	 */		
	public ModelChange manufactureModelChange(InputStream serializedModel,
                                              RDFService.ModelSerializationFormat serializationFormat,
                                              ModelChange.Operation operation,
                                              String graphURI);
	
	/**
	 * Add an event that will be be passed to any change listeners in advance of
	 * the change set additions and retractions being performed.  The event
	 * will only be fired if the precondition (if any) is met.
	 * @param event
	 */
	public void addPreChangeEvent(Object event);
	    
    /**
     * Add an event that will be be passed to any change listeners after all of
     * the change set additions and retractions are performed.
     * @param event
     */
    public void addPostChangeEvent(Object event);

    /**
     * Return a list of events to pass to any change listeners in 
     * advance of the change set additions and retractions being performed.
     * @return
     */
	public List<Object> getPreChangeEvents();
	
    /**
     * Return a list of events to pass to any change listeners after 
     * the change set additions and retractions are performed.
     * @return
     */
    public List<Object> getPostChangeEvents();
    
}
