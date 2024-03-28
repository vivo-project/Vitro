/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JacksonJsonContainer;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonArray;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonFactory;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonObject;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.LiteralParamFactory;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.URIResourceParam;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;

public class JsonContainerView {

    private static final String JSON_ARRAY = "json array";
    private static final String JSON_OBJECT = "json container";

    public static boolean isJsonContainer(Parameter param) {
        return isJsonContainer(param.getType());
    }

    public static boolean isJsonContainer(ParameterType type) {
        String canonicalName = type.getImplementationType().getClassName().getCanonicalName();
        if (JsonContainer.class.getCanonicalName().equals(canonicalName)) {
            return true;
        }
        return false;
    }

    public static Map<String, JsonArray> getJsonArrays(Parameters params, DataStore dataStore) {
        Map<String, JsonArray> jsonArrays = new HashMap<>();
        for (String name : params.getNames()) {
            Parameter param = params.get(name);
            if (JsonContainerView.isJsonContainer(param) && isJsonArray(param)) {
                JsonArray arrayNode = (JsonArray) dataStore.getData(name).getObject();
                jsonArrays.put(name, arrayNode);
            }
        }
        return jsonArrays;
    }

    public static List<String> getStringListFromJsonArrays(Parameters params, DataStore dataStore) {
        List<String> uris = new LinkedList<>();
        List<JsonArray> jsonArrays = getJsonArrayList(params, dataStore);
        for (JsonArray array : jsonArrays) {
            uris.addAll(array.getDataAsStringList());
        }
        return uris;
    }

    public static List<JsonContainer> getOutputJsonObjectList(Parameters params, DataStore dataStore) {
        List<JsonContainer> jsonObjects = new LinkedList<>();
        jsonObjects.addAll(getJsonArrayList(params, dataStore));
        for (String name : params.getNames()) {
            if (!dataStore.contains(name)) {
                JsonContainer jsonContainer = initializeJsonContainer(dataStore, params.get(name));
                jsonObjects.add(jsonContainer);
            }
        }
        return jsonObjects;
    }

    private static JacksonJsonContainer initializeJsonContainer(DataStore dataStore, Parameter param) {
        Data data = new Data(param);
        data.initializeDefault();
        dataStore.addData(param.getName(), data);
        return (JacksonJsonContainer) data.getObject();
    }

    public static List<JacksonJsonContainer> getJsonObjectList(Parameters params, DataStore dataStore) {
        List<JacksonJsonContainer> jsonArrays = new LinkedList<>();
        for (String name : params.getNames()) {
            Parameter param = params.get(name);
            if (JsonContainerView.isJsonContainer(param) && JSON_OBJECT.equals(param.getType().getName())) {
                JacksonJsonContainer objectNode = (JacksonJsonContainer) dataStore.getData(name).getObject();
                jsonArrays.add(objectNode);
            }
        }
        return jsonArrays;
    }

    public static List<JsonArray> getJsonArrayList(Parameters params, DataStore dataStore) {
        List<JsonArray> jsonArrays = new LinkedList<>();
        for (String name : params.getNames()) {
            Parameter param = params.get(name);
            if (JsonContainerView.isJsonContainer(param) && isJsonArray(param)) {
                JsonArray arrayNode = (JsonArray) dataStore.getData(name).getObject();
                jsonArrays.add(arrayNode);
            }
        }
        return jsonArrays;
    }

    public static boolean isJsonArray(Parameter param) {
        return JSON_ARRAY.equals(param.getType().getName());
    }

    public static boolean hasJsonArrays(Parameters params, DataStore dataStore) {
        for (String name : params.getNames()) {
            Parameter param = params.get(name);
            if (JsonContainerView.isJsonContainer(param) && isJsonArray(param)) {
                return true;
            }
        }
        return false;
    }

    public static JsonNode asJsonNode(Data data) {
        final JsonContainer object = (JsonContainer) data.getObject();
        return object.asJsonNode();
    }

    public static void addSolutionRow(DataStore dataStore, List<String> vars, QuerySolution solution,
            Parameters outputParams) {
        if (!hasJsonArrays(outputParams, dataStore)) {
            return;
        }
        JsonObject row = getRowMap(vars, solution);
        Map<String, JsonArray> jsonArrays = getJsonArrays(outputParams, dataStore);
        for (JsonArray array : jsonArrays.values()) {
            array.addRow(JacksonJsonContainer.PATH_ROOT_PREFIX, row);
        }
    }

    private static JsonObject getRowMap(List<String> vars, QuerySolution solution) {
        JsonObject row = JsonFactory.getEmptyObjectInstance();

        for (String var : vars) {
            RDFNode node = solution.get(var);
            if (node == null) {
                continue;
            }
            if (node.isLiteral()) {
                Literal literal = (Literal) node;
                Parameter param = LiteralParamFactory.createLiteral(literal, var);
                Data data = new Data(param);
                data.setObject(node);
                row.addKeyValue(var, data);
            } else if (node.isURIResource()) {
                Parameter param = new URIResourceParam(var);
                Data data = new Data(param);
                data.setObject(node);
                row.addKeyValue(var, data);
            }
        }
        return row;
    }

    public static JsonContainer getJsonContainer(DataStore store, Parameter param) {
        Data data = store.getData(param.getName());
        JacksonJsonContainer container = (JacksonJsonContainer) data.getObject();
        return container;
    }
}
