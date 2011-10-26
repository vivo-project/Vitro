/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;

public class UpdateSettings {

	private String dataDir;
	private String sparqlConstructAdditionsDir;
	private String sparqlConstructAdditionsPass2Dir;
	private String sparqlConstructDeletionsDir;
	private String askUpdatedQueryFile;
	private String successAssertionsFile;
	private String successRDFFormat = "N3";
	private String diffFile;
	private String logFile;
	private String errorLogFile;
	private String addedDataFile;
	private String removedDataFile;
	private String defaultNamespace;
	private OntModelSelector assertionOntModelSelector;
	private OntModelSelector inferenceOntModelSelector;
	private OntModelSelector unionOntModelSelector;
	private OntModel oldTBoxModel;
	private OntModel newTBoxModel;
	private OntModel oldTBoxAnnotationsModel;
	private OntModel newTBoxAnnotationsModel;
	
	public String getDataDir() {
		return dataDir;
	}
	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}
	public String getSparqlConstructAdditionsDir() {
		return sparqlConstructAdditionsDir;
	}
	public void setSparqlConstructAdditionsPass2Dir(String sparqlConstructAdditionsDir) {
		this.sparqlConstructAdditionsPass2Dir = sparqlConstructAdditionsDir;
	}
	public String getSparqlConstructAdditionsPass2Dir() {
		return sparqlConstructAdditionsPass2Dir;
	}
	public void setSparqlConstructAdditionsDir(String sparqlConstructAdditionsDir) {
		this.sparqlConstructAdditionsDir = sparqlConstructAdditionsDir;
	}
	public String getSparqlConstructDeletionsDir() {
		return sparqlConstructDeletionsDir;
	}
	public void setSparqlConstructDeletionsDir(String sparqlConstructDeletionsDir) {
		this.sparqlConstructDeletionsDir = sparqlConstructDeletionsDir;
	}
	public String getAskUpdatedQueryFile() {
		return askUpdatedQueryFile;
	}
	public void setAskUpdatedQueryFile(String askQueryFile) {
		this.askUpdatedQueryFile = askQueryFile;
	}
	public String getSuccessAssertionsFile() {
		return successAssertionsFile;
	}
	public void setSuccessAssertionsFile(String successAssertionsFile) {
		this.successAssertionsFile = successAssertionsFile;
	}
	public String getSuccessRDFFormat() {
		return successRDFFormat;
	}
	public void setSuccessRDFFormat(String successRDFFormat) {
		this.successRDFFormat = successRDFFormat;
	}
	public String getDiffFile() {
		return diffFile;
	}
	public void setDiffFile(String diffFile) {
		this.diffFile = diffFile;
	}
	public OntModelSelector getAssertionOntModelSelector() {
		return assertionOntModelSelector;
	}
	public OntModelSelector getInferenceOntModelSelector() {
		return inferenceOntModelSelector;
	}
	public OntModelSelector getUnionOntModelSelector() {
		return unionOntModelSelector;
	}
	public String getLogFile() {
		return logFile;
	}
	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}
	public String getErrorLogFile() {
		return errorLogFile;
	}
	public void setErrorLogFile(String errorLogFile) {
		this.errorLogFile = errorLogFile;
	}
	public String getAddedDataFile() {
		return addedDataFile;
	}
	public void setAddedDataFile(String addedDataFile) {
		this.addedDataFile = addedDataFile;
	}
	public String getRemovedDataFile() {
		return removedDataFile;
	}
	public void setRemovedDataFile(String removedDataFile) {
		this.removedDataFile = removedDataFile;
	}
	public String getDefaultNamespace() {
		return defaultNamespace;
	}
	public void setDefaultNamespace(String defaultNamespace) {
		this.defaultNamespace = defaultNamespace;
	}
	public void setAssertionOntModelSelector(OntModelSelector ontModelSelector) {
		this.assertionOntModelSelector = ontModelSelector;
	}
	public void setInferenceOntModelSelector(OntModelSelector ontModelSelector) {
		this.inferenceOntModelSelector = ontModelSelector;
	}
	public void setUnionOntModelSelector(OntModelSelector ontModelSelector) {
		this.unionOntModelSelector = ontModelSelector;
	}
	public OntModel getOldTBoxModel() {
		return oldTBoxModel;
	}
	public void setOldTBoxModel(OntModel oldTBoxModel) {
		this.oldTBoxModel = oldTBoxModel;
	}
	public OntModel getNewTBoxModel() {
		return newTBoxModel;
	}
	public void setNewTBoxModel(OntModel newTBoxModel) {
		this.newTBoxModel = newTBoxModel;
	}
	public OntModel getOldTBoxAnnotationsModel() {
		return oldTBoxAnnotationsModel;
	}
	public void setOldTBoxAnnotationsModel(OntModel oldTBoxAnnotationsModel) {
		this.oldTBoxAnnotationsModel = oldTBoxAnnotationsModel;
	}
	public OntModel getNewTBoxAnnotationsModel() {
		return newTBoxAnnotationsModel;
	}
	public void setNewTBoxAnnotationsModel(OntModel newTBoxAnnotationsModel) {
		this.newTBoxAnnotationsModel = newTBoxAnnotationsModel;
	}
}
