package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

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
	
	public void processClassChanges(List<AtomicOntologyChange> changes) throws Exception {
		
	}

	public void renameClass(AtomicOntologyChange change) throws IOException {
     	   
	}

	public void addClass(AtomicOntologyChange change) {
			
	}

	public void deleteClass(AtomicOntologyChange change) {
		
		
	}

	public void logChange(Statement statement, boolean add) throws IOException {
		logger.log( (add ? "Added " : "Removed") + "Statement: subject = " + statement.getSubject().getURI() +
				" property = " + statement.getPredicate().getURI() +
                " object = " + (statement.getObject().isLiteral() ? ((Resource)statement.getObject()).getURI() : ((Literal)statement.getObject()).getLexicalForm()));	
	}

	public void processPropertyChanges(List<AtomicOntologyChange> changes) {
		
	}
	
	
}
