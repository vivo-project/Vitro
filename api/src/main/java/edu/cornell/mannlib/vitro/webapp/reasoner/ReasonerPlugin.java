/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;

public interface ReasonerPlugin {

	public boolean isInterestedInAddedStatement(Statement stmt);
	
	public boolean isInterestedInRemovedStatement(Statement stmt);
	
	public boolean isConfigurationOnlyPlugin();
	
	public void addedABoxStatement(Statement stmt, 
			                       Model aboxAssertionsModel, 
			                       Model aboxInferencesModel, 
			                       OntModel TBoxInferencesModel); 
	
	public void removedABoxStatement(Statement stmt, 
			                       Model aboxAssertionsModel, 
			                       Model aboxInferencesModel, 
			                       OntModel TBoxInferencesModel);
	
	public void setSimpleReasoner(SimpleReasoner simpleReasoner);
	
	public SimpleReasoner getSimpleReasoner();
}
