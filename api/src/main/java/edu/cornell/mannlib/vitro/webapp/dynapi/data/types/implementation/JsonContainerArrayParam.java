/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonArray;

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
        return JsonArray.EMPTY_ARRAY;
    }

}
