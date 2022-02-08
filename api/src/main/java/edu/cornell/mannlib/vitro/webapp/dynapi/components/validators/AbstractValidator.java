package edu.cornell.mannlib.vitro.webapp.dynapi.components.validators;

public abstract class AbstractValidator implements Validator {

	
	private String name;
	
	@Override
	public void dereference() {
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

}
