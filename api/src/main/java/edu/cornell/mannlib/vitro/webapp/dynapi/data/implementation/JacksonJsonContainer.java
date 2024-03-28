/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.JSONConverter;

public abstract class JacksonJsonContainer implements JsonContainer {

    public static final String PATH_ROOT_PREFIX = "$";
    protected static ObjectMapper mapper = new ObjectMapper();
    private static final Configuration jsonPathConfig = prepareJsonPathConfig();
    protected Map<String, Data> dataMap = new HashMap<>();
    protected DocumentContext documentContext;

    protected JacksonJsonContainer(String jsonString) {
        documentContext = JsonPath.using(jsonPathConfig).parse(jsonString);
    }

    protected String escapeKey(String key) {
        return key.replace("\\", "\\\\").replace("'", "\\'");
    }

    protected String getRandomKey() {
        String id = UUID.randomUUID().toString();
        while (dataMap.containsKey(id)) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public JsonNode asJsonNode() {
        JsonNode parsedModel = documentContext.json();
        JsonNode deepCopy = parsedModel.deepCopy();
        replaceKeys(deepCopy);
        return deepCopy;
    }

    private void replaceKeys(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Entry<String, JsonNode>> it = objectNode.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();
                replaceKeys(entry.getKey(), entry.getValue(), objectNode);
            }
        } else if (node.isArray()) {
            ArrayNode array = (ArrayNode) node;
            Iterator<JsonNode> it = array.elements();
            int i = 0;
            while (it.hasNext()) {
                JsonNode child = it.next();
                replaceKeys(child, array, i);
                i++;
            }
        }
    }

    private void replaceKeys(String key, JsonNode node, ObjectNode parent) {
        if (node.isContainerNode()) {
            replaceKeys(node);
        } else if (node.isValueNode() && dataMap.containsKey(node.asText())) {
            Data data = dataMap.get(node.asText());
            JsonNode dataValue = JSONConverter.convertDataValue(data);
            parent.set(key, dataValue);
        }
    }

    private void replaceKeys(JsonNode node, ArrayNode parent, int i) {
        if (node.isContainerNode()) {
            replaceKeys(node);
        } else if (node.isValueNode() && dataMap.containsKey(node.asText())) {
            Data data = dataMap.get(node.asText());
            parent.set(i, JSONConverter.convertDataValue(data));
        }
    }

    private static Configuration prepareJsonPathConfig() {
        return Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider(mapper)).mappingProvider(
                new JacksonMappingProvider(mapper)).options(Option.DEFAULT_PATH_LEAF_TO_NULL,
                        Option.SUPPRESS_EXCEPTIONS).build();
    }

}
