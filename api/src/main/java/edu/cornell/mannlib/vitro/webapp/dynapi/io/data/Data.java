package edu.cornell.mannlib.vitro.webapp.dynapi.io.data;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Validators;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ParameterType;

public interface Data {

    String getType();

    boolean checkType(ParameterType parameterType);

    boolean isAllValid(String name, Validators validators, ParameterType type);

    Data getElement(String fieldName);

    boolean setElement(String fieldName, Data newData);

}
