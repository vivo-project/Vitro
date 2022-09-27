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
import edu.cornell.mannlib.vitro.webapp.dynapi.data.RdfView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;

public class JsonContainer {
	
	public static final String PATH_ROOT = "$";
	private static final String EMPTY_OBJECT = "{}";
	private static final String EMPTY_ARRAY = "[]";
	private static ObjectMapper mapper = new ObjectMapper();
	public static enum Type { EmptyArray, EmptyObject } ;
	
	private Map<String, Data> dataMap = new HashMap<>();
	private static final Configuration jsonPathConfig = prepareJsonPathConfig();

	private DocumentContext ctx;

	public JsonContainer(String jsonString) {
		ctx = JsonPath.using(jsonPathConfig).parse(jsonString);
	}
	
	public JsonContainer(Type type) {
		if (type.equals(Type.EmptyArray)) {
			ctx = JsonPath.using(jsonPathConfig).parse(EMPTY_ARRAY);
		} else {
			ctx = JsonPath.using(jsonPathConfig).parse(EMPTY_OBJECT);	
		}
	}
	
	public void putData(String jsonPath, Data data) {
		String randomKey = getRandomKey();
	}
	
	private String getRandomKey() {
		return UUID.randomUUID().toString();
	}
	
	public static String serialize(JsonContainer object){
		return object.jsonString();
	}
	
	public JsonNode asJsonNode() {
		JsonNode parsedModel = ctx.json();
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
				replaceKeys(entry.getKey(),entry.getValue(), objectNode);
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
		} else if (node.isValueNode() && dataMap.containsKey(node.asText())){
			Data data = dataMap.get(node.asText());
			parent.set(key, getDataValue(data));
		}
	}

	private void replaceKeys(JsonNode node, ArrayNode parent, int i) {
		if (node.isContainerNode()) {
			replaceKeys(node);
		} else if (node.isValueNode() && dataMap.containsKey(node.asText())) {
			Data data = dataMap.get(node.asText());
			parent.set(i, getDataValue(data));
		}
	}

	private JsonNode getDataValue(Data data) {
		if (RdfView.isRdfNode(data)) {
			return RdfView.getAsJsonNode(data);
		} 
		return mapper.convertValue(data.getSerializedValue(), JsonNode.class);
	}	

	private String jsonString() {
		return ctx.jsonString();
	}
	
	public static JsonContainer deserialize(String input) throws ConversionException{
		JsonContainer object = new JsonContainer(input);
		return object;
	}
	
	private static Configuration prepareJsonPathConfig() {
		return Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider(mapper))
			.mappingProvider(new JacksonMappingProvider(mapper))
			.options(
					Option.DEFAULT_PATH_LEAF_TO_NULL, 
					Option.SUPPRESS_EXCEPTIONS
					)
			.build();
	}

	public void addRow(String jsonPath, JsonContainer row) {
		dataMap.putAll(row.dataMap);
		ctx.add(jsonPath, row.ctx.json());
	}

	public void addKeyValue(String var, Data data) {
		String id = getRandomKey();
		if(dataMap.containsKey(id)) {
			//TODO:fix that
			throw new RuntimeException();
		}
		ctx.put(PATH_ROOT, var, id);
		dataMap.put(id, data);
	}
}
