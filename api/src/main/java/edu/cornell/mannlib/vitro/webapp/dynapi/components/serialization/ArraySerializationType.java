package edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ArraySerializationType extends SerializationType {

    private SerializationType elementsType = new PrimitiveSerializationType();

    public SerializationType getElementsType() {
        return elementsType;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasElementsOfType", maxOccurs = 1)
    public void setElementsType(SerializationType elementsType) {
        this.elementsType = elementsType;
    }

}
