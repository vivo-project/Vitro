package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;

public class JsonFasterxmlNode {
    private static final Log log = LogFactory.getLog(JsonFasterxmlNode.class.getName());
    private static ObjectMapper mapper = new ObjectMapper();
    
    public static JsonNode deserialize(String jsonString) throws ConversionException{
        JsonNode node = null;
        try {
            node = mapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            log.error(e, e);
        }
        if (node == null) {
            String message = "Error reading json:\n" + jsonString;
            throw new ConversionException(message);
        }
        return node;
    }
    
    public static String serialize(JsonNode object){
        return object.toString();
    }
}
