package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;

import com.fasterxml.jackson.databind.JsonNode;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.DynapiJsonObject;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.DynapiJsonObject.Type;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.LiteralParamFactory;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.URIResourceParam;

public class JsonObjectView {

	private static final String JSON_ARRAY = "jsonArray";

	public static Map<String, DynapiJsonObject> getJsonArrays(Parameters params, DataStore dataStore) {
		Map<String, DynapiJsonObject> jsonArrays = new HashMap<>();
		for (String name : params.getNames()) {
			Parameter param = params.get(name);
			if (param.isJsonObject() && JSON_ARRAY.equals(param.getType().getName())) {
				DynapiJsonObject arrayNode = (DynapiJsonObject) dataStore.getData(name).getObject();
				jsonArrays.put(name, arrayNode);
			}
		}
		return jsonArrays;
	}
	
	public static boolean hasJsonArrays(Parameters params, DataStore dataStore) {
		for (String name : params.getNames()) {
			Parameter param = params.get(name);
			if (param.isJsonObject() && JSON_ARRAY.equals(param.getType().getName())) {
				return true;
			}
		}
		return false;
	}

	public static JsonNode asJsonNode(Data data) {
		final DynapiJsonObject object = (DynapiJsonObject) data.getObject();
		return object.asJsonNode();
	}

	public static void addSolutionRow(DataStore dataStore, List<String> vars, QuerySolution solution, Parameters outputParams) {
		if (!hasJsonArrays(outputParams, dataStore)) {
			return;
		}
		DynapiJsonObject row = getRowMap(vars, solution);
		Map<String, DynapiJsonObject> jsonArrays = getJsonArrays(outputParams, dataStore);
		for ( DynapiJsonObject array  : jsonArrays.values()) {
			array.addRow(DynapiJsonObject.PATH_ROOT, row);
		}
	}

	private static DynapiJsonObject getRowMap(List<String> vars, QuerySolution solution) {
		DynapiJsonObject row = new DynapiJsonObject(Type.EmptyObject);

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
