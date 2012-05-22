/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice;

import java.io.InputStream;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange.Operation;

/*
 * Input parameter to changeSetUpdate() method in RDFService.
 * Represents a precondition query and an ordered list of model changes. 
 */

public interface ChangeSet {
		
	public String getPreconditionQuery();
	
	public void setPreconditionQuery(String preconditionQuery);
	
	public RDFService.SPARQLQueryType getPreconditionQueryType();
	
	public void setPreconditionQueryType(RDFService.SPARQLQueryType queryType);

	public List<ModelChange> getModelChanges();
	
	public void addAddition(InputStream model, 
			                RDFService.ModelSerializationFormat format,
			                String graphURI);
	
	public void addRemoval(InputStream model,
			               RDFService.ModelSerializationFormat format,
			               String graphURI);
	    
	public ModelChange manufactureModelChange();
	
	public ModelChange manufactureModelChange(InputStream serializedModel,
                                              RDFService.ModelSerializationFormat serializationFormat,
                                              Operation operation,
                                              String graphURI);
}
