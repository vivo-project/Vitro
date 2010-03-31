package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.Statement;

public class ABoxUpdater {

	private OntModel oldTboxModel;
	private OntModel newTboxModel;
	private OntModel aboxModel;
	private OntologyChangeLogger logger;  
	private OntologyChangeRecord record;
	
	public ABoxUpdater(OntModel oldTboxModel,
			           OntModel newTboxModel,
			           OntModel aboxModel,
		               OntologyChangeLogger logger,
		               OntologyChangeRecord record) {
		this.oldTboxModel = oldTboxModel;
		this.newTboxModel = newTboxModel;
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
	
	public void processPropertyChanges(List<AtomicOntologyChange> changes) throws IOException {
		Iterator propItr = changes.iterator();
		while(propItr.hasNext()){
			AtomicOntologyChange propChangeObj = (AtomicOntologyChange)
			                                     propItr.next();
			switch (propChangeObj.getAtomicChangeType()){
			case ADD: addProperties(propChangeObj);
			break;
			case DELETE: deleteProperties(propChangeObj);
			break;
			case RENAME: renameProperties(propChangeObj);
			break;
			default: logger.logError("Property change can't be null");
			break;
		    }		
		}
	}
	
	private void addProperties(AtomicOntologyChange propObj) throws IOException{
		Statement tempStatement = null;
		OntProperty tempProperty = newTboxModel.getOntProperty
		(propObj.getDestinationURI()).getSuperProperty();
		StmtIterator stmItr = aboxModel.listStatements();
		int count = 0;
		while(stmItr.hasNext()){
			tempStatement = stmItr.nextStatement();
			if(tempStatement.getPredicate().getURI().
					equalsIgnoreCase(tempProperty.getURI())){
				count++;			
			}
		}
		logger.log("The Property " + tempProperty.getURI() + 
				"which occurs " + count + "times in database has " +
						"a new subProperty " + propObj.getDestinationURI() +
				"added to Core 1.0");
		logger.log("Please review accordingly");			
	}
	private void deleteProperties(AtomicOntologyChange propObj) throws IOException{
		OntModel deletePropModel = ModelFactory.createOntologyModel();
		Statement tempStatement = null;
		StmtIterator stmItr = aboxModel.listStatements();
		while(stmItr.hasNext()){
			tempStatement = stmItr.nextStatement();
			if(tempStatement.getPredicate().getURI().equalsIgnoreCase
					(propObj.getSourceURI())){
				deletePropModel.add(tempStatement);	
			}
		}
		aboxModel = (OntModel) aboxModel.difference(deletePropModel);	
		record.recordRetractions(deletePropModel);
		logger.log("All statements using " + propObj.getSourceURI() +
				"were removed. Please refer removed data model");
	}
	private void renameProperties(AtomicOntologyChange propObj){
		Model renamePropAddModel = ModelFactory.createDefaultModel();
		Model renamePropRetractModel = 
			ModelFactory.createDefaultModel();
		Statement tempStatement = null;
		Property tempProperty = null; 
		StmtIterator stmItr = aboxModel.listStatements();
		while(stmItr.hasNext()){
			tempStatement = stmItr.nextStatement();
			if(tempStatement.getPredicate().getURI().
					equalsIgnoreCase(propObj.getSourceURI())){
				tempProperty = ResourceFactory.createProperty(
						propObj.getDestinationURI());
				aboxModel.remove(tempStatement);
				renamePropRetractModel.add(tempStatement);
				aboxModel.add(tempStatement.getSubject(),tempProperty
						,tempStatement.getObject());
				renamePropAddModel.add(tempStatement.getSubject(),
						tempProperty,tempStatement.getObject());
			}
		}
		record.recordAdditions(renamePropAddModel);
		record.recordRetractions(renamePropRetractModel);	
	}
	
	public void logChange(Statement statement, boolean add) throws IOException {
		logger.log( (add ? "Added " : "Removed") + "Statement: subject = " + statement.getSubject().getURI() +
				" property = " + statement.getPredicate().getURI() +
                " object = " + (statement.getObject().isLiteral() ? ((Resource)statement.getObject()).getURI() : ((Literal)statement.getObject()).getLexicalForm()));	
	}

		
}
