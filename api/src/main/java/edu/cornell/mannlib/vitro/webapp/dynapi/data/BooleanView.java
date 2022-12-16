package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.BooleanParam;

public class BooleanView {

    public static Data createData(String name, boolean loaded) {
        Parameter param = new BooleanParam(name);
        Data data = new Data(param);
        data.setObject(Boolean.valueOf(loaded));
        return data;
    }

}
