package edu.cornell.mannlib.vitro.webapp.dynapi.components.types;

public class PrimitiveParameterType extends ParameterType {

    @Override
    public String computePrefix(String fieldName, String parameterName) {
        String retVal = "";
        retVal = (parameterName.equals(fieldName)) ? "" : null;
        return (retVal != null && retVal.length() > 0) ? retVal += "." : retVal;
    }

}
