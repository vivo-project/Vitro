package edu.cornell.mannlib.vitro.webapp.rdfservice;

import java.io.InputStream;

/*
 * A ModelChange is one component of a ChangeSet.
 * Represents a model (collection of RDF triples), the URI
 * of a graph, and an indication of whether to add or 
 * remove the model from the graph.
 */

public interface ModelChange {

	public enum Operation {
	    ADD, REMOVE
	}
	
	/**   
	 * Getter for the serialized model
	 * 
	 * @return InputStream - a model (collection of RDF triples), serialized            
	 */
	public InputStream getSerializedModel();
	
	/**
	 * Setter for the serialized model
	 * 
	 * @param InputStream - a model (collection of RDF triples), serialized            
	 */
	public void setSerializedModel(InputStream serializedModel);
	
	/**
	 * Getter for the serialization format of the model
	 * 
	 * @return RDFService.ModelSerializationFormat - the serialization format of the model
	 */
	public RDFService.ModelSerializationFormat getSerializationFormat();
	
	/**
	 * Setter for the serialization format of the model
	 * 
	 * @param RDFService.ModelSerializationFormat - the serialization format of the model
	 */
	public void setSerializationFormat(RDFService.ModelSerializationFormat serializationFormat);
	
	/**
	 * Getter for the operation type
	 * 
	 * @return ModelChange.Operation - the operation type
	 */
	public ModelChange.Operation getOperation();

	/**
	 * Setter for the operation type
	 * 
	 * @param ModelChange.Operation - the operation type
	 */
	public void setOperation(ModelChange.Operation operation);

	/**
	 * Getter for the URI of the graph to which to apply the change
	 * 
	 * @return String - the graph URI
	 */
	public String getGraphURI();

	/**
	 * Setter for the URI of the graph to which to apply the change
	 * 
	 * @param String - the graph URI
	 */
	public void setGraphURI(String graphURI);
}
