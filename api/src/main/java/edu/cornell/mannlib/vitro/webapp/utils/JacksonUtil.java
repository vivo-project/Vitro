package edu.cornell.mannlib.vitro.webapp.utils;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

public final class JacksonUtil {
    public static int getAsInt(JsonNode node, String name) throws JsonMappingException {
        JsonNode value = node.get(name);
        if (value == null) {
            throw new JsonMappingException("ObjectNode[" + name + "] not found.");
        }
        return value.asInt();

    }
    public static String getAsString(JsonNode node, String name) throws JsonMappingException {
        JsonNode value = node.get(name);
        if (value == null) {
            throw new JsonMappingException("ObjectNode[" + name + "] not found.");
        }
        return value.asText();
    }
}
