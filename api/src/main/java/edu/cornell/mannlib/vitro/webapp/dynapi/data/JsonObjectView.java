package edu.cornell.mannlib.vitro.webapp.dynapi.data;


import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;

public class JsonObjectView {

	private static final ObjectMapper mapper = new ObjectMapper();
	private static final String ARRAY_OF_PAIRS = "arrayOfPairs";

	public static Map<String, ArrayNode> getJsonArrays(Parameters params){
		Map<String, ArrayNode> result = new HashMap<>();
		for (String name : params.getNames()) {
			Parameter param = params.get(name);
			if (param.isJsonObject() && ARRAY_OF_PAIRS.equals(param.getType().getName())) {
				ArrayNode arrayNode = mapper.createArrayNode();
				result.put(name, arrayNode);
			}
		}
		return result;
	}

	public static ObjectNode getObject(ArrayNode node) {
		ObjectNode object = mapper.createObjectNode();
		node.add(object);
		return object;
	}
}
