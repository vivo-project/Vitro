/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import org.apache.jena.ontology.OntModel;

public class OntModelSelectorImpl implements OntModelSelector {

	private OntModel aboxModel;
	private OntModel applicationMetadataModel;
	private OntModel displayModel;
	private OntModel fullModel;
	private OntModel tboxModel;
	private OntModel userAccountsModel;

	@Override
	public OntModel getABoxModel() {
		return this.aboxModel;
	}

	@Override
	public OntModel getApplicationMetadataModel() {
		return this.applicationMetadataModel;
	}

	@Override
	public OntModel getDisplayModel() {
		return this.displayModel;
	}

	@Override
	public OntModel getFullModel() {
		return this.fullModel;
	}

	@Override
	public OntModel getTBoxModel() {
		return this.tboxModel;
	}

	@Override
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

	@Override
	public String toString() {
		return "OntModelSelectorImpl[abox=" + hashHex(aboxModel) + ", tbox="
				+ hashHex(tboxModel) + ", full=" + hashHex(fullModel) + "]";
	}

	private String hashHex(Object o) {
		return (o == null) ? "00000000" : Integer.toString(o.hashCode(), 16);
	}
}
