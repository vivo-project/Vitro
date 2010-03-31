package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;

public class ABoxUpdater {

	private OntModel tboxModel;
	private OntModel aboxModel;
	private OntologyChangeLogger logger;  
	private OntologyChangeRecord record;
	
	public ABoxUpdater(OntModel tboxModel, OntModel aboxModel,
				OntologyChangeLogger logger, OntologyChangeRecord record) {
		this.tboxModel = tboxModel;
		this.aboxModel = aboxModel;
		this.logger = logger;
		this.record = record;
	}
	
	public void processClassChanges(List<AtomicOntologyChange> changes) {
		
	}
	
	public void processPropertyChanges(List<AtomicOntologyChange> changes) {
		
	}
	
	
}
