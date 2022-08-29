package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;

public class JsonObjectView {

	private static final ObjectMapper mapper = new ObjectMapper();
	private static final String ARRAY_OF_PAIRS = "arrayOfPairs";

	public static Map<String, ArrayNode> getJsonArrays(Parameters params, DataStore dataStore) {
		Map<String, ArrayNode> jsonArrays = new HashMap<>();
		for (String name : params.getNames()) {
			Parameter param = params.get(name);
			if (param.isJsonObject() && ARRAY_OF_PAIRS.equals(param.getType().getName())) {
				ArrayNode arrayNode = (ArrayNode) dataStore.getData(name).getObject();
				jsonArrays.put(name, arrayNode);
			}
		}
		return jsonArrays;
	}

	public static ObjectNode createArrayObjectNode(ArrayNode node) {
		ObjectNode object = mapper.createObjectNode();
		node.add(object);
		return object;
	}

	public static JsonNode getJsonNode(Data data) {
		return (JsonNode) data.getObject();
	}

	public static void addFromSolution(DataStore dataStore, List<String> vars, QuerySolution solution, Parameters outputParams) {
		Map<String, ArrayNode> jsonArrays = getJsonArrays(outputParams, dataStore);
		for (String name : jsonArrays.keySet()) {
			ArrayNode node = jsonArrays.get(name);
			ObjectNode object = createArrayObjectNode(node);
			for (String var : vars) {
				RDFNode solVar = solution.get(var);
				object.put(var, solVar.toString());
			}
		}
	}
}
