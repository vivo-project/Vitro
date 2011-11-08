/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;

public interface ReasonerPlugin {

	public boolean isInterestedInAddedStatement(Statement stmt);
	
	public boolean isInterestedInRemovedStatement(Statement stmt);
	
	public void addedABoxStatement(Statement stmt, 
			                       Model aboxAssertionsModel, 
			                       Model aboxInferencesModel, 
			                       OntModel TBoxInferencesModel); 
	
	public void removedABoxStatement(Statement stmt, 
			                       Model aboxAssertionsModel, 
			                       Model aboxInferencesModel, 
			                       OntModel TBoxInferencesModel);
	
}
