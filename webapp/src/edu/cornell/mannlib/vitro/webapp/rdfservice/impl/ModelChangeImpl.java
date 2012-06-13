/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl;

import java.io.InputStream;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

/*
 * A ModelChange is one component of a ChangeSet.
 * Represents a model (collection of RDF triples), the URI
 * of a graph, and an indication of whether to add or 
 * remove the model from the graph.
 */
public class ModelChangeImpl implements ModelChange {

	private InputStream serializedModel;
	private RDFService.ModelSerializationFormat serializationFormat;
	private Operation operation;
	private String graphURI;

	public ModelChangeImpl() {}
	
	public ModelChangeImpl(InputStream serializedModel,
                           RDFService.ModelSerializationFormat serializationFormat,
                           Operation operation,
                           String graphURI) {
		
		this.serializedModel = serializedModel;
		this.serializationFormat = serializationFormat;
		this.operation = operation;
		this.graphURI = graphURI;
	}

	/**   
	 * Getter for the serialized model
	 * 
	 * @return InputStream - a model (collection of RDF triples), serialized            
	 */
	@Override
	public InputStream getSerializedModel() {
		return serializedModel;
	}
	
	/**
	 * Setter for the serialized model
	 * 
	 * @param InputStream - a model (collection of RDF triples), serialized            
	 */
	@Override
	public void setSerializedModel(InputStream serializedModel) {
		this.serializedModel = serializedModel;
	}
	
	/**
	 * Getter for the serialization format of the model
	 * 
	 * @return RDFService.ModelSerializationFormat - the serialization format of the model
	 */
	@Override
	public RDFService.ModelSerializationFormat getSerializationFormat() {
		return serializationFormat;
	}
	
	/**
	 * Setter for the serialization format of the model
	 * 
	 * @param RDFService.ModelSerializationFormat - the serialization format of the model
	 */
	@Override
	public void setSerializationFormat(RDFService.ModelSerializationFormat serializationFormat) {
		this.serializationFormat = serializationFormat;
	}
	
	/**
	 * Getter for the operation type
	 * 
	 * @return ModelChange.Operation - the operation type
	 */
	@Override
	public Operation getOperation() {
		return operation;
	}
	
	/**
	 * Setter for the operation type
	 * 
	 * @param ModelChange.Operation - the operation type
	 */
	@Override
	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	/**
	 * Getter for the URI of the graph to which to apply the change
	 * 
	 * @return String - the graph URI
	 */
	@Override
	public String getGraphURI() {
		return graphURI;
	}

	/**
	 * Setter for the URI of the graph to which to apply the change
	 * 
	 * @param String - the graph URI
	 */
	@Override
	public void setGraphURI(String graphURI) {
		this.graphURI = graphURI;
	}
}
