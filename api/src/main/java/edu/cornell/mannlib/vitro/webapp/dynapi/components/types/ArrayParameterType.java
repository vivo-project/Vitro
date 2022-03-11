package edu.cornell.mannlib.vitro.webapp.dynapi.components.types;

import org.apache.commons.lang3.math.NumberUtils;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
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

    @Override
    public String computePrefix(String fieldName) {
        String retVal = "";
        String index = fieldName.substring(0, fieldName.indexOf("."));
        String fieldNameOtherPart = fieldName.substring(fieldName.indexOf(".") + 1);
        if (NumberUtils.isDigits(index)) {
            retVal = this.getElementsType().computePrefix(fieldNameOtherPart);
        } else {
            retVal = null;
        }

        return (retVal != null && retVal.length() > 0) ? retVal + "." : retVal;
    }

}
