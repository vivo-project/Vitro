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
	 * @param String - a SPARQL query            
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
	 * @param RDFService.SPARQLQueryType - the precondition query type           
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
	 * @param InputStream - a serialized RDF model (collection of triples)  
	 * @param RDFService.ModelSerializationFormat - format of the serialized RDF model
	 * @param String - URI of the graph to which the RDF model should be added         
	 */		
	public void addAddition(InputStream model, 
			                RDFService.ModelSerializationFormat format,
			                String graphURI);
	
	/**   
	 * Adds one model change representing a deletion to the list of model changes
	 * 
	 * @param InputStream - a serialized RDF model (collection of triples)  
	 * @param RDFService.ModelSerializationFormat - format of the serialized RDF model
	 * @param String - URI of the graph from which the RDF model should be removed         
	 */		
	public void addRemoval(InputStream model,
			               RDFService.ModelSerializationFormat format,
			               String graphURI);

	/**   
	 * Creates an instance of the ModelChange class 
	 */		
	public ModelChange manufactureModelChange();
	
	/**   
	 * Creates an instance of the ModelChange class
	 * 
	 * @param InputStream - a serialized RDF model (collection of triples)  
	 * @param RDFService.ModelSerializationFormat - format of the serialized RDF model
	 * @param ModelChange.Operation - the type of operation to be performed with the serialized RDF model 
	 * @param String - URI of the graph on which to apply the model change operation        
	 */		
	public ModelChange manufactureModelChange(InputStream serializedModel,
                                              RDFService.ModelSerializationFormat serializationFormat,
                                              ModelChange.Operation operation,
                                              String graphURI);
}
