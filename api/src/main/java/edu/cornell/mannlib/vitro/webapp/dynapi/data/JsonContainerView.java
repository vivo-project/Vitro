package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;

import com.fasterxml.jackson.databind.JsonNode;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer.Type;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.LiteralParamFactory;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.URIResourceParam;

public class JsonContainerView {

	private static final String JSON_ARRAY = "json array";

	public static Map<String, JsonContainer> getJsonArrays(Parameters params, DataStore dataStore) {
		Map<String, JsonContainer> jsonArrays = new HashMap<>();
		for (String name : params.getNames()) {
			Parameter param = params.get(name);
			if (param.isJsonContainer() && JSON_ARRAY.equals(param.getType().getName())) {
				JsonContainer arrayNode = (JsonContainer) dataStore.getData(name).getObject();
				jsonArrays.put(name, arrayNode);
			}
		}
		return jsonArrays;
	}
	
    public static List<String> getStringListFromJsonArrays(Parameters params, DataStore dataStore){
        List<String> uris = new LinkedList<>();
        List<JsonContainer> jsonArrays = getJsonArrayList(params, dataStore);
        for (JsonContainer array : jsonArrays) {
            uris.addAll(array.getDataAsStringList());
        }
        return uris;
    }
    
    private static List<JsonContainer> getJsonArrayList(Parameters params, DataStore dataStore) {
        List<JsonContainer> jsonArrays = new LinkedList<>();
        for (String name : params.getNames()) {
            Parameter param = params.get(name);
            if (param.isJsonContainer() && JSON_ARRAY.equals(param.getType().getName())) {
                JsonContainer arrayNode = (JsonContainer) dataStore.getData(name).getObject();
                jsonArrays.add(arrayNode);
            }
        }
        return jsonArrays;
    }

	public static boolean hasJsonArrays(Parameters params, DataStore dataStore) {
		for (String name : params.getNames()) {
			Parameter param = params.get(name);
			if (param.isJsonContainer() && JSON_ARRAY.equals(param.getType().getName())) {
				return true;
			}
		}
		return false;
	}

	public static JsonNode asJsonNode(Data data) {
		final JsonContainer object = (JsonContainer) data.getObject();
		return object.asJsonNode();
	}

	public static void addSolutionRow(DataStore dataStore, List<String> vars, QuerySolution solution, Parameters outputParams) {
		if (!hasJsonArrays(outputParams, dataStore)) {
			return;
		}
		JsonContainer row = getRowMap(vars, solution);
		Map<String, JsonContainer> jsonArrays = getJsonArrays(outputParams, dataStore);
		for ( JsonContainer array  : jsonArrays.values()) {
			array.addRow(JsonContainer.PATH_ROOT, row);
		}
	}

	private static JsonContainer getRowMap(List<String> vars, QuerySolution solution) {
		JsonContainer row = new JsonContainer(Type.EmptyObject);

		for (String var : vars) {
			RDFNode node = solution.get(var);
			if (node.isLiteral()) {
				Literal literal = (Literal) node;
				Parameter param = LiteralParamFactory.createLiteral(literal, var);
				Data data = new Data(param);
				data.setObject(node);
				row.addKeyValue(var, data);
			} else 
			if (node.isURIResource()){
				Parameter param = new URIResourceParam(var);
				Data data = new Data(param);
				data.setObject(node);
				row.addKeyValue(var, data);
			} 
		}
		return row;
	}
}
