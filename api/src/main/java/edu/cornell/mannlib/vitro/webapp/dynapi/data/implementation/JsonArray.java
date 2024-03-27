/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JsonArray extends JacksonJsonContainer {

    private static final Log log = LogFactory.getLog(JsonArray.class);

    public static final String EMPTY_ARRAY = "[]";

    public JsonArray() {
        super(EMPTY_ARRAY);
    }

    public JsonArray(String jsonString) {
        super(jsonString);
    }

    public Data getItem(String key, Parameter parameter) {
        JsonNode result = NullNode.getInstance();
        String jsonPath = String.format("$[%s]", escapeKey(key));
        result = documentContext.read(jsonPath);
        if (!result.isNull() && result.isValueNode()) {
            String localKey = result.asText();
            if (dataMap.containsKey(localKey)) {
                return dataMap.get(localKey);
            }
            Data data = new Data(parameter);
            data.setRawString(result.asText());
            data.initializeFromString();
            return data;
        }
        if (result.isContainerNode()) {
            Data data = new Data(parameter);
            data.setRawString(result.toString());
            data.initializeFromString();
            return data;
        }
        return new Data(parameter);
    }

    public boolean contains(String key) {
        JsonNode result = NullNode.getInstance();
        try {
            String jsonPathQuery = String.format("$[%s]", escapeKey(key));
            result = documentContext.read(jsonPathQuery);
        } catch (Exception e) {
            log.error(e, e);
            return false;
        }
        if (result == null || result.isNull()) {
            return false;
        }
        return true;
    }

    public void addKeyValue(String var, Data data) {
        String id = getRandomKey();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put(var, id);
        documentContext.add(PATH_ROOT_PREFIX, objectNode);
        dataMap.put(id, data);
    }

    public void addValue(Data data) {
        String id = getRandomKey();
        documentContext.add(PATH_ROOT_PREFIX, id);
        dataMap.put(id, data);
    }

    public void addRow(String jsonPath, JacksonJsonContainer row) {
        dataMap.putAll(row.dataMap);
        documentContext.add(jsonPath, row.documentContext.json());
    }

}
