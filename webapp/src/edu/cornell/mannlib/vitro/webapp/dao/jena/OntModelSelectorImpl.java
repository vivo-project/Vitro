/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import com.hp.hpl.jena.ontology.OntModel;

public class OntModelSelectorImpl implements OntModelSelector {

	private OntModel aboxModel;
	private OntModel applicationMetadataModel;
	private OntModel displayModel;
	private OntModel fullModel;
	private OntModel tboxModel;
	private OntModel userAccountsModel;
	
	public OntModel getABoxModel() {
		return this.aboxModel;
	}

	public OntModel getApplicationMetadataModel() {
		return this.applicationMetadataModel;
	}

	public OntModel getDisplayModel() {
		return this.displayModel;
	}

	public OntModel getFullModel() {
		return this.fullModel;
	}

	public OntModel getTBoxModel() {
		return this.tboxModel;
	}

	public OntModel getTBoxModel(String ontologyURI) {
		return this.tboxModel;
	}

	public OntModel getUserAccountsModel() {
		return this.userAccountsModel;
	}
	
	public void setABoxModel(OntModel m) {
		this.aboxModel = m;
	}
	
	public void setApplicationMetadataModel(OntModel m) {
		this.applicationMetadataModel = m;
	}

	public void setDisplayModel(OntModel m) {
		this.displayModel = m;
	}
	
	public void setTBoxModel(OntModel m) {
		this.tboxModel = m;
	}
	
	public void setUserAccountsModel(OntModel m) {
		this.userAccountsModel = m;
	}
	
	public void setFullModel(OntModel m) {
		this.fullModel = m;
	}
	
}
