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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import edu.cornell.mannlib.vitro.webapp.dynapi.RESTEndpoint;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.RawData;

public class JSONConverter {

	private static final String JSON_ROOT = "$";
	private static final String TYPE = "type";
	private static final Log log = LogFactory.getLog(JSONConverter.class.getName());
	private static JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V6);
	private static ObjectMapper mapper = new ObjectMapper();
	
	public static void convert(HttpServletRequest request, Action action, DataStore dataStore) throws ConversionException {
		JsonSchema schema = getInputSchema(action, dataStore.getResourceId());
		String jsonString = readRequest(request);
		JsonNode jsonRequest = injectResourceId(jsonString, schema, dataStore, action);
		Set<ValidationMessage> messages = schema.validate(jsonRequest);
		if (!messages.isEmpty()) {
			validationFailed(jsonRequest, messages);
		}
		Parameters required = action.getRequiredParams();
		ReadContext ctx = JsonPath.parse(jsonRequest.toString());
		for (String name : required.getNames()) {
			Parameter param = required.get(name);
			String paramPath = getReadPath(name, param);
			String value = ctx.read(paramPath,String.class);
			RawData data = new RawData(param);
			data.setRawString(value);
			dataStore.addData(name, data);
		}
	}

	private static JsonNode injectResourceId(String jsonString, JsonSchema schema, DataStore dataStore, Action action) throws ConversionException {
		final String resourceId = dataStore.getResourceId();
		
		if (StringUtils.isBlank(resourceId)) {
			return readJson(jsonString);
		}
		
		Parameters params = action.getRequiredParams();
		Parameter param = params.get(RESTEndpoint.RESOURCE_ID);
		
		if (param == null) {
			return readJson(jsonString);
		}
		
		String path = getPathPrefix(param);
		Configuration conf = Configuration.defaultConfiguration()
				.addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL)
				.addOptions(Option.SUPPRESS_EXCEPTIONS);
		DocumentContext ctx = JsonPath
				.using(conf)
				.parse(jsonString)
				.put(path, RESTEndpoint.RESOURCE_ID, resourceId);
		return readJson(ctx.jsonString());

	}

	private static JsonNode readJson(String jsonString) throws ConversionException {
		JsonNode node = null;
		try {
			node = mapper.readTree(jsonString);
		} catch (JsonProcessingException e) {
			log.error(e,e);
		}
		if (node == null) {
			String message = "Error reading json:\n" + jsonString;
			throw new ConversionException(message);
		}
		return node;
	}
	
	private static String getReadPath(String name, Parameter param) {
		return getPathPrefix(param) + "." + name;
	}
	
	private static String getPathPrefix(Parameter param) {
		String paramPath = param.getInputPath();
		if (StringUtils.isBlank(paramPath)) {
			paramPath = JSON_ROOT;
		}
		return paramPath;
	}
	
	public static void convert(HttpServletResponse response, Action action, DataStore dataStore) throws ConversionException{
		JsonSchema schema = getOutputSchema(action);
		Parameters params = action.getProvidedParams();
		//Set<ValidationMessage> result = schema.validate(jsonResponse);

		
		//TODO: Validate output
	}

	private static String readRequest(HttpServletRequest request) throws ConversionException {
		JsonNode jsonRequest = null;
		String jsonString = null;
		try {
			if (request.getReader() != null && request.getReader().lines() != null) {
			    jsonString = request.getReader().lines().collect(Collectors.joining());
			    jsonRequest = readJson(jsonString);
			}
		} catch (IOException e) {
			log.error(e,e);
		}
		if (jsonRequest == null) {
			String message = "Error reading input json:\n" + jsonString;
			throw new ConversionException(message);
		}
		return jsonString;
	}

	private static JsonSchema getInputSchema(Action action, String resourceId) throws ConversionException {
		String serializedSchema = action.getInputSerializedSchema();
		Parameters params = action.getRequiredParams();
		JsonNode nativeSchema = deserializeSchema(serializedSchema);
		if (nativeSchema != null) {
			JsonSchema jsonSchema = factory.getSchema(nativeSchema);
			return jsonSchema;
		}
	    ObjectNode schema = createSchema(params);
	    JsonSchema jsonSchema = factory.getSchema(schema);
		return jsonSchema;
	}	

	private static JsonSchema getOutputSchema(Action action) throws ConversionException {
		String serializedSchema = action.getOutputSerializedSchema();
		Parameters params = action.getProvidedParams();

		JsonNode nativeSchema = deserializeSchema(serializedSchema);
		if (nativeSchema != null) {
			JsonSchema jsonSchema = factory.getSchema(nativeSchema);
			return jsonSchema;
		}
	    ObjectNode schema = createSchema(params);
	    JsonSchema jsonSchema = factory.getSchema(schema);
		return jsonSchema;
	
	}

	private static JsonNode deserializeSchema(String serializedSchema) throws ConversionException {
		JsonNode nativeSchema = null;
		if (!StringUtils.isBlank(serializedSchema)) {
			 nativeSchema = readJson(serializedSchema);
		}
		return nativeSchema;
	}

	private static ObjectNode createSchema(Parameters params) {
	    ObjectNode schema = mapper.createObjectNode();
	    schema.put("$schema", "http://json-schema.org/draft-06/schema#");
	    schema.put(TYPE, "object");
	    ObjectNode properties = mapper.createObjectNode();
	    schema.set("properties", properties);
	    ArrayNode required = mapper.createArrayNode();
	    for (String name : params.getNames()) {
	    	Parameter parameter = params.get(name);
	    	String type = parameter.getSerializedType();
		    ObjectNode paramNode = mapper.createObjectNode();
		    paramNode.put(TYPE, type);
		    properties.set(name, paramNode);
		    if (!parameter.isOptional()) {
		    	required.add(name);
		    }
	    }
	    if (!required.isEmpty()) {
		    schema.set("required", required);
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
