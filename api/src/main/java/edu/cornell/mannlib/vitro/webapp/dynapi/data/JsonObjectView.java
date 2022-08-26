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
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;

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

	public static ObjectNode createArrayObjectNode(ArrayNode node) {
		ObjectNode object = mapper.createObjectNode();
		node.add(object);
		return object;
	}

	public static void addData(DataStore dataStore, String name, Parameter arrayParam, ArrayNode node) {
		Data data = new Data(arrayParam);
		data.setObject(node);
		dataStore.addData(name, data);		
	}
	
	public static JsonNode getJsonNode(Data data) {
		return (JsonNode) data.getObject();
	}

	public static void addFromSolution(DataStore dataStore, Map<String, ArrayNode> jsonArrays, List<String> vars,
			QuerySolution solution, Parameters outputParams) throws ConversionException {
		for (String name : jsonArrays.keySet()) {
			ArrayNode node = jsonArrays.get(name);
			if (!dataStore.contains(name)) {
				final Parameter arrayParam = outputParams.get(name);
				addData(dataStore, name, arrayParam, node ); 
			}
			ObjectNode object = createArrayObjectNode(node);
			for (String var : vars) {
				RDFNode solVar = solution.get(var);
				object.put(var, solVar.toString());
			}
		}
	}
}
