package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.RawData;

public class JSONConverter {

	private static final String TYPE = "type";
	private static final Log log = LogFactory.getLog(JSONConverter.class.getName());
	private static JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V6);
	private static ObjectMapper mapper = new ObjectMapper();
	
	public static void convert(HttpServletRequest request, Action action, DataStore dataStore) throws ConversionException {
		JsonSchema schema = getInputSchema(action);
		JsonNode jsonRequest = readRequest(request);
		Set<ValidationMessage> messages = schema.validate(jsonRequest);
		if (!messages.isEmpty()) {
			//validationFailed(jsonRequest, messages);
		}
		Parameters required = action.getRequiredParams();
		ReadContext ctx = JsonPath.parse(jsonRequest.toString());
		for (String name : required.getNames()) {
			Parameter param = required.get(name);
			String paramPath = param.getInputPath();
			if (StringUtils.isBlank(paramPath)) {
				paramPath = createPath(name);
			}
			String value = ctx.read(paramPath,String.class);
			RawData data = new RawData(param);
			data.setRawString(value);
			dataStore.addData(name, data);
		}
		//TODO: get jsonPath to node from schema
	}
	
	public static void convert(HttpServletResponse response, Action action, DataStore dataStore){
		JsonSchema schema = getOutputSchema(action);
		Parameters params = action.getProvidedParams();
		//Set<ValidationMessage> result = schema.validate(jsonResponse);

		
		//TODO: Validate output
	}
	
	private static String createPath(String name) {
		return "$." + name ;
	}

	private static JsonNode readRequest(HttpServletRequest request) throws ConversionException {
		JsonNode jsonRequest = null;
		String jsonString = null;
		try {
			if (request.getReader() != null && request.getReader().lines() != null) {
			    jsonString = request.getReader().lines().collect(Collectors.joining());
			    jsonRequest = mapper.readTree(jsonString);
			}
		} catch (IOException e) {
			log.error(e,e);
			String message = "Error reading input json:\n" + jsonString;
			throw new ConversionException(message);
		}
		return jsonRequest;
	}

	private static JsonSchema getInputSchema(Action action) {
		String serializedSchema = action.getInputSerializedSchema();
		Parameters params = action.getRequiredParams();
		return getSchema(serializedSchema, params);
	}

	private static JsonSchema getOutputSchema(Action action) {
		String serializedSchema = action.getOutputSerializedSchema();
		Parameters params = action.getProvidedParams();

		return getSchema(serializedSchema, params);
	}
	
	private static JsonSchema getSchema(String serializedSchema, Parameters params) {
		JsonNode nativeSchema = deserializeSchema(serializedSchema);
		if (nativeSchema != null) {
			JsonSchema jsonSchema = factory.getSchema(nativeSchema);
			return jsonSchema;
		}
	    ObjectNode schema = createSchema(params);
	    JsonSchema jsonSchema = factory.getSchema(schema);
		return jsonSchema;
	}

	private static JsonNode deserializeSchema(String serializedSchema) {
		JsonNode nativeSchema = null;
		if (!StringUtils.isBlank(serializedSchema)) {
			 try {
				nativeSchema = mapper.readTree(serializedSchema);
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return nativeSchema;
	}

	private static ObjectNode createSchema(Parameters params) {
	    ObjectNode schema = mapper.createObjectNode();
	    schema.put("$schema", "http://json-schema.org/draft-06/schema#");
	    schema.put(TYPE, "object");
	    ObjectNode properties = mapper.createObjectNode();
	    schema.set("properties", properties);
	    for (String name : params.getNames()) {
	    	Parameter parameter = params.get(name);
	    	String type = parameter.getSerializedType();
		    ObjectNode paramNode = mapper.createObjectNode();
		    paramNode.put(TYPE, type);
		    properties.set(name, paramNode);
	    }
		return schema;
	}

	private static void validationFailed(JsonNode jsonRequest, Set<ValidationMessage> messages)
			throws ConversionException {
		StringBuilder sb = new StringBuilder();
		sb.append("Json validation failed:\n");
		for (ValidationMessage vm : messages) {
			sb.append(vm.toString() + "\n");
		}
		sb.append("input json:\n" + jsonRequest);
		throw new ConversionException(sb.toString());
	}
	
	
}
