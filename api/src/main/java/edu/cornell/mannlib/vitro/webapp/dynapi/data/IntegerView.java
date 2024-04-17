/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;

public class IntegerView {

    public static boolean isInteger(Parameter param) {
        ParameterType paramType = param.getType();
        return paramType.hasInterface(Integer.class);
    }

    public static int getInteger(Data data) {
        Object object = data.getObject();
        Integer value = (Integer) object;
        return value;
    }

    public static Data createInteger(Parameter param, Integer value) {
        Data data = new Data(param);
        data.setObject(value);
        return data;
    }

}
