package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class SimpleOntologyChangeRecord implements OntologyChangeRecord {

	private final static Log log = 
			LogFactory.getLog(SimpleOntologyChangeRecord.class);
	
	private final static String RDF_SYNTAX = "RDF/XML-ABBREV";
	
	private Model additionsModel = ModelFactory.createDefaultModel();
	private Model retractionsModel = ModelFactory.createDefaultModel();
	private File additionsFile;
	private File retractionsFile;
	
	public SimpleOntologyChangeRecord(
			String additionsFile, String retractionsFile) {
		this.additionsFile = new File(additionsFile);
		if (!this.additionsFile.exists()) {
			throw new RuntimeException(this.getClass().getName() + 
					" unable to create required file at " + additionsFile);
		}
		this.retractionsFile = new File(retractionsFile);
		if (!this.retractionsFile.exists()) {
			throw new RuntimeException(this.getClass().getName() + 
					" unable to create required file at " + retractionsFile);			
		}
	}
	
	public void recordAdditions(Model incrementalAdditions) {
		additionsModel.add(incrementalAdditions);
		write(additionsModel, additionsFile);
	}

	public void recordRetractions(Model incrementalRetractions) {
		retractionsModel.add(incrementalRetractions);
		write(retractionsModel, retractionsFile);
	}
	
	private void write(Model model, File file) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			model.write(fos, RDF_SYNTAX);
		} catch (FileNotFoundException fnfe) {
			log.error(this.getClass().getName() + 
					  " unable to write to RDF file", fnfe);
		}
	}

}
