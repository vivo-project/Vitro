/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.dao.VitroModelProperties;

public class VitroJenaModel {

	private String graphURI = null;
	private OntModel jenaOntModel = null;
	private VitroModelProperties vitroModelProperties = null;
	
	public String getGraphURI() {
		return graphURI;
	}
	public void setGraphURI(String graphURI) {
		this.graphURI = graphURI;
	}
	
	public OntModel getJenaOntModel() {
		return this.jenaOntModel;
	}
	public void setJenaOntModel(OntModel ontModel) {
		this.jenaOntModel = ontModel;
	}
	
	public VitroModelProperties getVitroModelProperties() {
		return this.vitroModelProperties;
	}
	public void setVitroModelProperties(VitroModelProperties vitroModelProperties) {
		this.vitroModelProperties = vitroModelProperties;
	}
	
}
