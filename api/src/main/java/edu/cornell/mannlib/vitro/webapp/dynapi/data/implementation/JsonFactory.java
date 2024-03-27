/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import static edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonFactory.Type.ARRAY;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;

public class JsonFactory {

    public static enum Type {
        ARRAY,
        OBJECT
    }

    public static JsonContainer getJson(JsonFactory.Type type) {
        if (type.equals(ARRAY)) {
            return new JsonArray();
        } else {
            return new JsonObject();
        }
    }

    public static String serialize(JsonContainer object) {
        // TODO: Copy object and replace keys with values from data map
        return object.asJsonNode().toString();
    }

    public static JsonContainer deserialize(String jsonString) throws ConversionException {
        if (jsonString.startsWith("[")) {
            return new JsonArray(jsonString);
        }
        return new JsonObject(jsonString);
    }
}
