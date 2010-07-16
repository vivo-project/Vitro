/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;

/**
 * An OntModelSelector that does not support model-per-ontology separation
 * @author bjl23
 *
 */
public class SimpleOntModelSelector implements OntModelSelector {

	private OntModel fullModel;
	private OntModel aboxModel;
	private OntModel applicationMetadataModel;
	private OntModel tboxModel;
	private OntModel userAccountsModel;
	
	private OntModelSpec DEFAULT_ONT_MODEL_SPEC = OntModelSpec.OWL_MEM;
	private OntModel displayModel;
	
	/**
	 * Construct an OntModelSelector with a bunch of empty models
	 */
	public SimpleOntModelSelector() {
		aboxModel = ModelFactory.createOntologyModel(DEFAULT_ONT_MODEL_SPEC);
		tboxModel = ModelFactory.createOntologyModel(DEFAULT_ONT_MODEL_SPEC);
		applicationMetadataModel = ModelFactory.createOntologyModel(DEFAULT_ONT_MODEL_SPEC);
		userAccountsModel = ModelFactory.createOntologyModel(DEFAULT_ONT_MODEL_SPEC);
		fullModel = ModelFactory.createOntologyModel(DEFAULT_ONT_MODEL_SPEC);
		fullModel.addSubModel(aboxModel);
		fullModel.addSubModel(tboxModel);
		fullModel.addSubModel(applicationMetadataModel);
	}
	
	/**
	 * Construct An OntModel selector that works with a single union OntModel
	 * Only for temporary backwards compatibility.
	 */	
	public SimpleOntModelSelector(OntModel ontModel) { 
		this.fullModel = ontModel;
		this.aboxModel = ontModel;
		this.applicationMetadataModel = ontModel;
		this.tboxModel = ontModel;
		this.userAccountsModel = ontModel;
	}
	
	public void setABoxModel(OntModel m) {
		fullModel.enterCriticalSection(Lock.WRITE);
		try {
			fullModel.removeSubModel(aboxModel);
			this.aboxModel = m;
			fullModel.addSubModel(aboxModel);
		} finally {
			fullModel.leaveCriticalSection();
		}
	}
	
	public void setApplicationMetadataModel(OntModel m) {
		fullModel.enterCriticalSection(Lock.WRITE);
		try {
			fullModel.removeSubModel(applicationMetadataModel);
			this.applicationMetadataModel = m;
			fullModel.addSubModel(applicationMetadataModel);
		} finally {
			fullModel.leaveCriticalSection();
		}
	}
	
	public void setTBoxModel(OntModel m) {
		fullModel.enterCriticalSection(Lock.WRITE);
		try {
			fullModel.removeSubModel(tboxModel);
			this.tboxModel = m;
			fullModel.addSubModel(tboxModel);
		} finally {
			fullModel.leaveCriticalSection();
		}
	}
	
	public void setFullModel(OntModel m) {
		m.addSubModel(tboxModel);
		m.addSubModel(aboxModel);
		m.addSubModel(applicationMetadataModel);
		this.fullModel = m;
	}
	
	public OntModel getABoxModel() {
		return aboxModel;
	}
	
	public OntModel getApplicationMetadataModel() {
		return applicationMetadataModel;
	}

	public OntModel getFullModel() {
		return fullModel;
	}

	public OntModel getTBoxModel() {
		return tboxModel;
	}

	public OntModel getTBoxModel(String ontologyURI) {
		return tboxModel;
	}

	public OntModel getUserAccountsModel() {
		return userAccountsModel;
	}
	
	public void setUserAccountsModel(OntModel userAccountsModel) {
		this.userAccountsModel = userAccountsModel;
	}

	public void setDisplayModel(OntModel displayModel) {
		this.displayModel = displayModel;		
	}
	public OntModel getDisplayModel(){
		return this.displayModel;
	}
}
