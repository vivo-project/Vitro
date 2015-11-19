/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange.Operation;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

public class ChangeSetImpl implements ChangeSet {
	
	public ChangeSetImpl() {
		modelChanges = new ArrayList<ModelChange>();
	}
	
	private String preconditionQuery;
	private RDFService.SPARQLQueryType queryType;
	private ArrayList<ModelChange> modelChanges = new ArrayList<ModelChange>();
	private ArrayList<Object> preChangeEvents = new ArrayList<Object>();
	private ArrayList<Object> postChangeEvents = new ArrayList<Object>();
 	
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
	
	@Override 
	public void addPreChangeEvent(Object o) {
	    this.preChangeEvents.add(o);
	}
	
	@Override 
    public void addPostChangeEvent(Object o) {
        this.postChangeEvents.add(o);
    }
	
	@Override
	public List<Object> getPreChangeEvents() {
	    return this.preChangeEvents;
	}
	
	@Override
    public List<Object> getPostChangeEvents() {
        return this.postChangeEvents;
    }

	@Override
	public String toString() {
		return "ChangeSetImpl [preconditionQuery=" + preconditionQuery
				+ ", queryType=" + queryType + ", modelChanges=" + modelChanges
				+ ", preChangeEvents=" + preChangeEvents
				+ ", postChangeEvents=" + postChangeEvents + "]";
	}	
	
}
