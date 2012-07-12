/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl;

import java.io.InputStream;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

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
