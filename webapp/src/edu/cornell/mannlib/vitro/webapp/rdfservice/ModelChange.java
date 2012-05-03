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
		
	abstract InputStream getSerializedModel();
	
	public void setSerializedModel(InputStream serializedModel);
	
	public RDFService.ModelSerializationFormat getSerializationFormat();
	
	public void setSerializationFormat(RDFService.ModelSerializationFormat serializationFormat);
	
	public Operation getOperation();
	
	public void setOperation(Operation operation);
	
	public String getGraphURI();
	
	public void setGraphURI(String graphURI);
}
