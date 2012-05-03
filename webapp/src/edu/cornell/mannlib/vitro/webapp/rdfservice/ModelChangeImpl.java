package edu.cornell.mannlib.vitro.webapp.rdfservice;

import java.io.InputStream;

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

	@Override
	public InputStream getSerializedModel() {
		return serializedModel;
	}
	
	@Override
	public void setSerializedModel(InputStream serializedModel) {
		this.serializedModel = serializedModel;
	}
	
	@Override
	public RDFService.ModelSerializationFormat getSerializationFormat() {
		return serializationFormat;
	}
	
	@Override
	public void setSerializationFormat(RDFService.ModelSerializationFormat serializationFormat) {
		this.serializationFormat = serializationFormat;
	}
	
	@Override
	public Operation getOperation() {
		return operation;
	}
	
	@Override
	public void setOperation(Operation operation) {
		this.operation = operation;
	}
	
	@Override
	public String getGraphURI() {
		return graphURI;
	}
	
	@Override
	public void setGraphURI(String graphURI) {
		this.graphURI = graphURI;
	}
}
