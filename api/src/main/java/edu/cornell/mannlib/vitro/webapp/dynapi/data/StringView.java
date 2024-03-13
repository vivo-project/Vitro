/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.BooleanParam;

public class StringView {

    public static Data createData(String name, String value) {
        Parameter param = new BooleanParam(name);
        Data data = new Data(param);
        data.setObject(value);
        return data;
    }

    public static String getFirstStringValue(DataStore dataStore, String name) {
        Data data = dataStore.getData(name);
        if (JsonContainerView.isJsonArray(data.getParam())) {
            List array = (List) data.getObject();
            Object first = array.get(0);
            return first.toString();
        }
        return data.getObject().toString();
    }

    public static boolean isPlainString(Parameter param) {
        return param.getType().isPlainString();
    }

}
