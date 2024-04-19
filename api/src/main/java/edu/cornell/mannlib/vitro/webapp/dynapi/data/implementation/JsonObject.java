/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JsonObject extends JacksonJsonContainer {

    private static final Log log = LogFactory.getLog(JsonObject.class);

    public static final String EMPTY_OBJECT = "{}";

    protected JsonObject() {
        super(EMPTY_OBJECT);
    }

    protected JsonObject(String jsonString) {
        super(jsonString);
    }

    public Data getItem(String key, Parameter parameter) {
        JsonNode result = NullNode.getInstance();
        String jsonPath = String.format("$['%s']", escapeKey(key));
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
            String jsonPathQuery = String.format("$['%s']", escapeKey(key));
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
        documentContext.put(PATH_ROOT_PREFIX, var, id);
        dataMap.put(id, data);
    }

    public void addValue(Data data) {
        String id = getRandomKey();
        Object values = documentContext.read(PATH_ROOT_PREFIX + "." + "values");
        if (NullNode.getInstance().equals(values)) {
            ArrayNode arrayNode = mapper.createArrayNode();
            documentContext.put(PATH_ROOT_PREFIX, "values", arrayNode);
        }
        documentContext.add(PATH_ROOT_PREFIX + ".values", id);
        dataMap.put(id, data);
    }

    public List<String> getDataAsStringList() {
        List<String> result = new LinkedList<>();
        for (Data data : dataMap.values()) {
            result.add(data.getSerializedValue());
        }
        return result;
    }

}
