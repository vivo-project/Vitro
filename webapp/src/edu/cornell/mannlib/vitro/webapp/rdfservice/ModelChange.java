/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice;

import java.io.InputStream;

/**
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
	 * @return InputStream - the serialized model (collection of RDF triples) representing a change to make           
	 */
	public InputStream getSerializedModel();
	
	/**
	 * @param serializedModel - the serialized model (collection of RDF triples) representing a change to make           
	 */
	public void setSerializedModel(InputStream serializedModel);
	
	/**
	 * @return RDFService.ModelSerializationFormat - the serialization format of the model
	 */
	public RDFService.ModelSerializationFormat getSerializationFormat();
	
	/**
	 * @param serializationFormat - the serialization format of the model
	 */
	public void setSerializationFormat(RDFService.ModelSerializationFormat serializationFormat);
	
	/**
	 * @return ModelChange.Operation - the operation to be performed
	 */
	public ModelChange.Operation getOperation();

	/**
	 * @param operation - the operation to be performed
	 */
	public void setOperation(ModelChange.Operation operation);

	/**
	 * @return String - the URI of the graph to which to apply the change
	 */
	public String getGraphURI();

	/**
	 * @param graphURI - the URI of the graph to which to apply the change
	 *                   If the graphURI is null the change applies to the
	 *                   default write graph. If this method is not used to
	 *                   set the write graph the default write graph will be used.
	 */
	public void setGraphURI(String graphURI);
}
