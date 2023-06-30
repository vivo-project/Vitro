package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class HTTPMethod {

	private String name;

	public String getName() {
		return name;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#name", minOccurs = 1, maxOccurs = 1)
	public void setName(String name) {
		this.name = name;
	}

}
