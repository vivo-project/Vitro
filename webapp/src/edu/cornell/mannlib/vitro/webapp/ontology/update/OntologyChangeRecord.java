package edu.cornell.mannlib.vitro.webapp.ontology.update;

import com.hp.hpl.jena.rdf.model.Model;

public interface OntologyChangeRecord {
	
	public void recordAdditions(Model incrementalAdditions);
	
	public void recordRetractions(Model incrementalRetractions);
	
}
