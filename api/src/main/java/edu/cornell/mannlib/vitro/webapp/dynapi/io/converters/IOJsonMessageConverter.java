package edu.cornell.mannlib.vitro.webapp.dynapi.io.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import edu.cornell.mannlib.vitro.webapp.dynapi.ActionPool;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.*;
import edu.cornell.mannlib.vitro.webapp.dynapi.request.RequestPath;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class IOJsonMessageConverter implements IOMessageConverter {

    private final static int IOUnknown 	    = 0;
    private final static int IOObject 	    = 1;
    private final static int IOArray 		= 2;
    private final static int IOInteger 	    = 3;
    private final static int IODecimal 	    = 4;
    private final static int IOBoolean 	    = 5;
    private final static int IOString 	    = 6;

    private static IOJsonMessageConverter INSTANCE = new IOJsonMessageConverter();

    public static IOJsonMessageConverter getInstance() {
        return INSTANCE;
    }


    public ObjectData loadDataFromRequest(HttpServletRequest request){
        Map<String, Data> ioDataMap = new HashMap<String, Data>();
        try {
            if(request.getReader() != null && request.getReader().lines()!=null){
                String requestData = request.getReader().lines().collect(Collectors.joining());
                ObjectMapper mapper = new ObjectMapper();
                JsonNode actualObj = mapper.readTree(requestData);
                Iterator<String> fieldNames = actualObj.fieldNames();
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    JsonNode value = actualObj.get(fieldName);
                    Data data = fromJson(value);
                    if (data != null)
                        ioDataMap.put(fieldName, data);
                }
            }
        } catch (IOException ignored) {

        }

        if(ioDataMap.get("id") == null) {
            RequestPath requestPath = RequestPath.from(request);
            String resourceId = requestPath.getResourceId();
            if (resourceId != null) {
                Data resourceIdData = fromJson(new TextNode(resourceId));
                if (resourceIdData != null)
                    ioDataMap.put("id", resourceIdData);
            }
        }
        return new ObjectData(ioDataMap);
    }

    // to generate response, not just a string
    public String exportDataToResponseBody(ObjectData data){
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        Map<String, Data> ioDataMap = data.getContainer();
        Iterator<String> fieldNames = ioDataMap.keySet().iterator();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (fieldName.equalsIgnoreCase("result")){
                JsonNode node = toJson(ioDataMap.get(fieldName));
                if (node != null)
                    objectNode.set(fieldName, node);
            }
        }
        return objectNode.toString();
    }


    public Data fromJson(JsonNode node){
        Data retVal = null;
        switch (getDataType(node)){
            case IOObject:
                Map<String, Data> fields = new HashMap<String, Data>();
                Iterator<String> fieldNames = node.fieldNames();
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    JsonNode value = node.get(fieldName);
                    Data data = fromJson(value);
                    if (data != null)
                        fields.put(fieldName, data);
                }
                retVal = new ObjectData(fields);
                break;
            case IOArray:
                if (node instanceof ArrayNode){
                    ArrayNode arrayNode = (ArrayNode) node;
                    List<Data> values = new ArrayList<Data>();
                    Iterator<JsonNode> itr = arrayNode.elements();
                    while (itr.hasNext()) {
                        JsonNode next = itr.next();
                        values.add(fromJson(next));
                    }
                    retVal = new ArrayData(values);
                }
                break;
            case IOInteger:
                retVal = new IntegerData(node.asInt());
                break;
            case IODecimal:
                retVal = new DecimalData(node.asDouble());
                break;
            case IOBoolean:
                retVal = new BooleanData(node.asBoolean());
                break;
            case IOString:
                retVal = new StringData(node.asText());
                break;
        }
        return retVal;
    }

    public JsonNode toJson(Data data){
        JsonNode retVal = null;
        ObjectMapper mapper = null;
        switch (getDataType(data)){
            case IOObject:
                mapper = new ObjectMapper();
                ObjectNode objectNode = mapper.createObjectNode();
                Map<String, Data> fields = ((ObjectData)data).getContainer();
                Iterator<String> fieldNames = fields.keySet().iterator();
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    JsonNode node = toJson(fields.get(fieldName));
                    if (node != null)
                        objectNode.set(fieldName, node);
                }
                retVal = objectNode;
                break;
            case IOArray:
                mapper = new ObjectMapper();
                ArrayNode arrayNode = mapper.createArrayNode();
                List<Data> values = ((ArrayData)data).getContainer();
                for (Data value:values) {
                    JsonNode node = toJson(value);
                    arrayNode.add(node);
                }
                retVal = arrayNode;
                break;
            case IOInteger:
                retVal = IntNode.valueOf(((IntegerData)data).getValue());
                break;
            case IODecimal:
                retVal = DoubleNode.valueOf(((DecimalData)data).getValue());
                break;
            case IOBoolean:
                retVal = BooleanNode.valueOf(((BooleanData)data).getValue());
                break;
            case IOString:
                retVal = TextNode.valueOf(((StringData)data).getValue());
                break;
        }
        return retVal;
    }


    private int getDataType(JsonNode node){
        JsonNodeType nodeType = (node != null)?node.getNodeType():JsonNodeType.MISSING;
        if (node.isArray())
            return IOArray;
        else if (node.isObject())
            return IOObject;
        else if (node.isInt())
            return IOInteger;
        else if (node.isDouble())
            return IODecimal;
        else if (node.isBoolean())
            return IOBoolean;
        else if (node.isTextual())
            return IOString;
        else
            return IOUnknown;
    }

    private int getDataType(Data data){
        if (data instanceof ArrayData)
            return IOArray;
        else if (data instanceof ObjectData)
            return IOObject;
        else if (data instanceof IntegerData)
            return IOInteger;
        else if (data instanceof DecimalData)
            return IODecimal;
        else if (data instanceof BooleanData)
            return IOBoolean;
        else if (data instanceof StringData)
            return IOString;
        else
            return IOUnknown;
    }



}