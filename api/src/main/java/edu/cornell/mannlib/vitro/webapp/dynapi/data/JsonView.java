package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

public class JsonView {

    public static boolean isJsonNode(Parameter param) {
        ParameterType type = param.getType();
        ImplementationType implType = type.getImplementationType();
        String className = implType.getClassName().getCanonicalName();
        if (JsonNode.class.getCanonicalName().equals(className)) {
            return true;
        }
        return false;
    }

    public static String getJsonString(DataStore dataStore, Parameter param) {
        Data data = dataStore.getData(param.getName());
        JsonNode node = (JsonNode) data.getObject();
        return node.toString();
    }

    public static JsonNode getJsonNode(Data data) {
        JsonNode node = (JsonNode) data.getObject();
        return node;
    }

    public static void addSparqlSelectResult(DataStore dataStore, Parameters outputParams, ResultSet results) {
        List<Data> dataList = new LinkedList<>();
        for (String name : outputParams.getNames()) {
            Parameter param = outputParams.get(name);
            if (isJsonNode(param)) {
                if (dataStore.contains(name)) {
                    Data data = dataStore.getData(name);
                    dataList.add(data);
                } else {
                    Data data = new Data(param);
                    dataStore.addData(name, data);
                    dataList.add(data);
                }
            }
        }
        if (dataList.isEmpty()) {
            // Nothing to do
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(baos, results);
        String json = new String(baos.toByteArray());
        for (Data data : dataList) {
            data.setRawString(json);
            data.initialization();
        }
    }

}
