package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import org.apache.jena.datatypes.RDFDatatype;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.validators.Validator;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class Parameter implements Removable{

	String name;
	Validators validators = new Validators();
	ParameterType type;
	
	public String getName() {
		return name;
	}
	
	public RDFDatatype getRDFDataType() {
		return type.getRDFDataType();
	}
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasParameterType", minOccurs = 1, maxOccurs = 1)
	public void setParamType(ParameterType type) {
		this.type = type;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#paramName", minOccurs = 1, maxOccurs = 1)
	public void setName(String name) {
		this.name = name;
	}
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasValidator")
	public void addValidator(Validator validator) {
		validators.add(validator);
	}

	public boolean isValid(String name, String[] values) {
		return validators.isAllValid(name, values);
	}

	@Override
	public void dereference() {
	}

}
