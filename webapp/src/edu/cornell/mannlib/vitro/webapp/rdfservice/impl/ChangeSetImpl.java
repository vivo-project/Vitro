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
	
	@Override
	public String getPreconditionQuery() {
	   return preconditionQuery;	
	}
	
	@Override
	public void setPreconditionQuery(String preconditionQuery) {
		this.preconditionQuery = preconditionQuery;
	}
	
	@Override
	public RDFService.SPARQLQueryType getPreconditionQueryType() {
		return queryType;
	}
	
	@Override
	public void setPreconditionQueryType(RDFService.SPARQLQueryType queryType) {
		this.queryType = queryType;
	}

	@Override
	public List<ModelChange> getModelChanges() {
	    	return modelChanges;
	}
	
	@Override
	public void addAddition(InputStream model, RDFService.ModelSerializationFormat format, String graphURI) {
		modelChanges.add(manufactureModelChange(model,format, ModelChange.Operation.ADD, graphURI));
	}
	
	@Override
	public void addRemoval(InputStream model, RDFService.ModelSerializationFormat format, String graphURI) {
		modelChanges.add(manufactureModelChange(model, format, ModelChange.Operation.REMOVE, graphURI));
	}
	
	@Override
	public ModelChange manufactureModelChange() {
		return new ModelChangeImpl(); 
	}
	
	@Override
	public ModelChange manufactureModelChange(InputStream serializedModel,
                                              RDFService.ModelSerializationFormat serializationFormat,
                                              Operation operation,
                                              String graphURI) {
		return new ModelChangeImpl(serializedModel, serializationFormat, operation, graphURI); 
	}
}
