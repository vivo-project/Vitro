/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class SimpleChangeRecord implements ChangeRecord {

	private final static Log log = 
			LogFactory.getLog(SimpleChangeRecord.class);
	
	private final static String RDF_SYNTAX = "N3";
	
	private Model additionsModel = ModelFactory.createDefaultModel();
	private Model retractionsModel = ModelFactory.createDefaultModel();
	private File additionsFile;
	private File retractionsFile;
	
	private int additionsCount = 0;
	private int retractionsCount = 0;
	
	public SimpleChangeRecord(
			String additionsFile, String retractionsFile) {
		this.additionsFile = new File(additionsFile);
		try {
			FileWriter test = new FileWriter(additionsFile);
		} catch (IOException ioe) {
				throw new RuntimeException(this.getClass().getName() + 
					" unable to create required file at " + additionsFile);
		}	
		this.retractionsFile = new File(retractionsFile);
		try { 
			FileWriter test = new FileWriter(retractionsFile);
		} catch (IOException ioe) {
			throw new RuntimeException(this.getClass().getName() + 
					" unable to create required file at " + retractionsFile);			
		}
	}
	
	public void recordAdditions(Model incrementalAdditions) {
		additionsModel.add(incrementalAdditions);
	    additionsCount += incrementalAdditions.size();
	}

	public void recordRetractions(Model incrementalRetractions) {
		retractionsModel.add(incrementalRetractions);
		retractionsCount += incrementalRetractions.size();
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
	
	public void writeChanges() {
		if (additionsModel.size() > 0) {
			write(additionsModel, additionsFile);
		}
		if (retractionsModel.size() > 0) {
			write(retractionsModel, retractionsFile);
		}
	}
	
	public boolean hasRecordedChanges() {
	    return additionsCount > 0 || retractionsCount > 0;
	}

}
