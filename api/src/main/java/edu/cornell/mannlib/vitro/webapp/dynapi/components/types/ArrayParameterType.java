package edu.cornell.mannlib.vitro.webapp.dynapi.components.types;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ArrayParameterType extends ParameterType {

	private ParameterType elementsType = new PrimitiveParameterType();

	public ParameterType getElementsType() {
		return elementsType;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasElementsOfType", maxOccurs = 1)
	public void setElementsType(ParameterType elementsType) {
		this.elementsType = elementsType;
	}

}
