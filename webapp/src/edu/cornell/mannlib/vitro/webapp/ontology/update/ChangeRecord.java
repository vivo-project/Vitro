/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import com.hp.hpl.jena.rdf.model.Model;

public interface ChangeRecord {
	
	public void recordAdditions(Model incrementalAdditions);
	
	public void recordRetractions(Model incrementalRetractions);
	
	public void writeChanges();
	
}
