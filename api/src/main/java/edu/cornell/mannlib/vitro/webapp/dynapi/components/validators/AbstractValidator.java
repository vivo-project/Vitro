package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public abstract class AbstractValidator implements Validator {


	private String name;
	
	@Override
	public void dereference() {
	}

	@Override
	public String getName() {
		return name;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#validatorName", minOccurs = 0, maxOccurs = 1)
	public void setName(String name) {
		this.name = name;
	}

}
