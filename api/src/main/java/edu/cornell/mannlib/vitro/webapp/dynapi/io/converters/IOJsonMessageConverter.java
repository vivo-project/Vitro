package edu.cornell.mannlib.vitro.webapp.dynapi.io.converters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ArrayParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ObjectParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.*;

public class IOJsonMessageConverter extends IOMessageConverter {

    private static final IOJsonMessageConverter INSTANCE = new IOJsonMessageConverter();

    public static IOJsonMessageConverter getInstance() {
        return INSTANCE;
    }

    private ObjectMapper mapper = new ObjectMapper();

    public ObjectData loadDataFromRequest(HttpServletRequest request, Parameters parameters) {
        Map<String, Data> ioDataMap = new HashMap<String, Data>();
        try {
            if (request.getReader() != null && request.getReader().lines() != null) {
                String requestData = request.getReader().lines().collect(Collectors.joining());
                JsonNode actualObj = mapper.readTree(requestData);
                Iterator<String> fieldNames = actualObj.fieldNames();
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    JsonNode value = actualObj.get(fieldName);
                    Parameter parameter = parameters.get(fieldName);
                    if (parameter != null) {
                        Data data = fromJson(value, parameter.getType());
                        if (data != null) {
                            ioDataMap.put(fieldName, data);
                        }
                    }
                }
            }
        } catch (IOException ignored) {

        }

        return new ObjectData(ioDataMap);
    }

    public String exportDataToResponseBody(ObjectData data) {
        ObjectNode objectNode = mapper.createObjectNode();
        Map<String, Data> ioDataMap = data.getContainer();
        for (String fieldName : ioDataMap.keySet()) {
            JsonNode node = toJson(ioDataMap.get(fieldName));
            if (node != null) {
                objectNode.set(fieldName, node);
            }
        }
        return objectNode.toString();
    }

    public Data fromJson(JsonNode node, ParameterType type) {
        Data retVal = null;
        if (type != null) {
            if ((node.isArray()) && (type instanceof ArrayParameterType)) {
                ArrayNode arrayNode = (ArrayNode) node;
                ParameterType innerType = ((ArrayParameterType) type).getElementsType();
                List<Data> values = new ArrayList<Data>();
                Iterator<JsonNode> itr = arrayNode.elements();
                while (itr.hasNext()) {
                    JsonNode next = itr.next();
                    values.add(fromJson(next, innerType));
                }
                retVal = new ArrayData(values);
            } else if ((node.isObject()) && (type instanceof ObjectParameterType)) {
                Map<String, Data> fields = new HashMap<String, Data>();
                Iterator<String> fieldNames = node.fieldNames();
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    JsonNode value = node.get(fieldName);
                    ParameterType innerType = ((ObjectParameterType) type).getInternalElements().get(fieldName).getType();
                    Data data = fromJson(value, innerType);
                    if (data != null) {
                        fields.put(fieldName, data);
                    }
                }
                retVal = new ObjectData(fields);
            } else
                retVal = IOMessageConverterUtils.getPrimitiveDataFromString(node.asText(), type);
        }

        return retVal;
    }

    public JsonNode toJson(Data data) {
        JsonNode retVal = null;
        if (data instanceof ArrayData) {
            ArrayNode arrayNode = mapper.createArrayNode();
            List<Data> values = ((ArrayData) data).getContainer();
            for (Data value : values) {
                JsonNode node = toJson(value);
                arrayNode.add(node);
            }
            retVal = arrayNode;
        } else if (data instanceof ObjectData) {
            ObjectNode objectNode = mapper.createObjectNode();
            Map<String, Data> fields = ((ObjectData) data).getContainer();
            for (String fieldName : fields.keySet()) {
                JsonNode node = toJson(fields.get(fieldName));
                if (node != null) {
                    objectNode.set(fieldName, node);
                }
            }
            retVal = objectNode;
        } else if (data instanceof IntegerData)
            retVal = IntNode.valueOf(((IntegerData) data).getValue());
        else if (data instanceof DecimalData)
            retVal = DoubleNode.valueOf(((DecimalData) data).getValue());
        else if (data instanceof BooleanData)
            retVal = BooleanNode.valueOf(((BooleanData) data).getValue());
        else if (data instanceof StringData)
            retVal = TextNode.valueOf(((StringData) data).getValue());
        return retVal;
    }


}
