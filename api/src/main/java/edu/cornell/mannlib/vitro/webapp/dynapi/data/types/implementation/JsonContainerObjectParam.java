/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonObject;

public class JsonContainerObjectParam extends JsonContainerParam {

    public JsonContainerObjectParam(String var) {
        super(var);
        getType().addInterface(JsonContainer.class);
    }

    @Override
    protected String getContainerDefaultValue() {
        return JsonObject.EMPTY_OBJECT;
    }
}
