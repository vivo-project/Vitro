/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange.Operation;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

/*
 * Input parameter to changeSetUpdate() method in RDFService.
 * Represents a precondition query and an ordered list of model changes. 
 */
public class ChangeSetImpl implements ChangeSet {
	
	public ChangeSetImpl() {
		modelChanges = new ArrayList<ModelChange>();
	}
	
	private String preconditionQuery;
	private RDFService.SPARQLQueryType queryType;
	private ArrayList<ModelChange> modelChanges;
	
	/**   
	 * Getter for the precondition query
	 * 
	 * @return String - a SPARQL query            
	 */	
	@Override
	public String getPreconditionQuery() {
	   return preconditionQuery;	
	}
	
	/**   
	 * Setter for the precondition query
	 * 
	 * @param String - a SPARQL query            
	 */	
	@Override
	public void setPreconditionQuery(String preconditionQuery) {
		this.preconditionQuery = preconditionQuery;
	}
	
	/**   
	 * Getter for the precondition query type
	 * 
	 * @return RDFService.SPARQLQueryType - the precondition query type           
	 */	
	@Override
	public RDFService.SPARQLQueryType getPreconditionQueryType() {
		return queryType;
	}
	
	/**   
	 * Setter for the precondition query type
	 * 
	 * @param RDFService.SPARQLQueryType - the precondition query type           
	 */	
	@Override
	public void setPreconditionQueryType(RDFService.SPARQLQueryType queryType) {
		this.queryType = queryType;
	}

	/**   
	 * Getter for the list of model changes
	 * 
	 * @return List<ModelChange> - list of model changes           
	 */	
	@Override
	public List<ModelChange> getModelChanges() {
	    	return modelChanges;
	}
	
	/**   
	 * Adds one model change representing an addition to the list of model changes
	 * 
	 * @param InputStream - a serialized RDF model (collection of triples)  
	 * @param RDFService.ModelSerializationFormat - format of the serialized RDF model
	 * @param String - URI of the graph to which the RDF model should be added         
	 */		
	@Override
	public void addAddition(InputStream model, RDFService.ModelSerializationFormat format, String graphURI) {
		modelChanges.add(manufactureModelChange(model,format, ModelChange.Operation.ADD, graphURI));
	}
	
	/**   
	 * Adds one model change representing a deletion to the list of model changes
	 * 
	 * @param InputStream - a serialized RDF model (collection of triples)  
	 * @param RDFService.ModelSerializationFormat - format of the serialized RDF model
	 * @param String - URI of the graph from which the RDF model should be removed         
	 */		
	@Override
	public void addRemoval(InputStream model, RDFService.ModelSerializationFormat format, String graphURI) {
		modelChanges.add(manufactureModelChange(model, format, ModelChange.Operation.REMOVE, graphURI));
	}
	
	/**   
	 * Creates an instance of the ModelChange class 
	 */	
	@Override
	public ModelChange manufactureModelChange() {
		return new ModelChangeImpl(); 
	}

	/**   
	 * Creates an instance of the ModelChange class
	 * 
	 * @param InputStream - a serialized RDF model (collection of triples)  
	 * @param RDFService.ModelSerializationFormat - format of the serialized RDF model
	 * @param ModelChange.Operation - the type of operation to be performed with the serialized RDF model 
	 * @param String - URI of the graph on which to apply the model change operation        
	 */		
	@Override
	public ModelChange manufactureModelChange(InputStream serializedModel,
                                              RDFService.ModelSerializationFormat serializationFormat,
                                              Operation operation,
                                              String graphURI) {
		return new ModelChangeImpl(serializedModel, serializationFormat, operation, graphURI); 
	}
}
