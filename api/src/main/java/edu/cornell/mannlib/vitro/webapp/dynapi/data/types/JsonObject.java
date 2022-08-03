package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import java.util.ArrayList;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.fasterxml.jackson.databind.JsonNode;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.JSONConverter;

public class JsonObject {

	public static String serialize(JsonNode jsonNode){
		return jsonNode.toString();
	}
	
	public static JsonNode deserialize(String input) throws ConversionException{
		return JSONConverter.readJson(input);
	}
	
}
