package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.JSONConverter;

public class JsonObject {

	public static String serialize(JsonNode jsonNode){
		return jsonNode.toString();
	}
	
	public static String serialize(ArrayNode jsonNode){
		return jsonNode.toString();
	}
	
	public static JsonNode deserialize(String input) throws ConversionException{
		return JSONConverter.readJson(input);
	}
	
}
