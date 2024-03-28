/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import static edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer.Type.ARRAY;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;

public class JsonFactory {

    public static JsonContainer getJson(JsonContainer.Type type) {
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
            return getArrayInstance(jsonString);
        }
        return getObjectInstance(jsonString);
    }

    public static JsonObject getObjectInstance(String jsonString) {
        if (!jsonString.startsWith("{")) {
            throw new JsonIsNotObjectException(jsonString);
        }
        return new JsonObject(jsonString);
    }

    public static JsonArray getArrayInstance(String jsonString) {
        if (!jsonString.startsWith("[")) {
            throw new JsonIsNotArrayException(jsonString);
        }
        return new JsonArray(jsonString);
    }

    public static JsonObject getEmptyObjectInstance() {
        return new JsonObject();
    }

    public static JsonArray getEmptyArrayInstance() {
        return new JsonArray();
    }

    static public class JsonIsNotArrayException extends RuntimeException {
        public JsonIsNotArrayException(String json) {
            super(json);
        }
    }

    static public class JsonIsNotObjectException extends RuntimeException {
        public JsonIsNotObjectException(String json) {
            super(json);
        }
    }
}
