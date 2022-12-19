package edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;

public class JsonContainerArrayParam extends JsonContainerParam {

    public JsonContainerArrayParam(String var) {
        super(var);
    }

    @Override
    public String getContainerTypeName() {
        return "json array";
    }

    @Override
    protected String getContainerDefaultValue() {
        return JsonContainer.EMPTY_ARRAY;
    }

}
