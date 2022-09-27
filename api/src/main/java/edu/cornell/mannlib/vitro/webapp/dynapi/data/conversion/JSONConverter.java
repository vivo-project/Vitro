package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
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
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import edu.cornell.mannlib.vitro.webapp.dynapi.RESTEndpoint;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonContainerView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;

public class JSONConverter {

	private static final String JSON_ROOT = "$";
	private static final String TYPE = "type";
	private static final Log log = LogFactory.getLog(JSONConverter.class.getName());
	private static JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V6);
	private static ObjectMapper mapper = new ObjectMapper();
	private static final Configuration jsonPathConfig = prepareJsonPathConfig();
	private static final String DEFAULT_OUTPUT_JSON = "{ \"result\" : [ {} ] }";

	public static void convert(HttpServletRequest request, Action action, DataStore dataStore)
			throws ConversionException {
		JsonSchema schema = getInputSchema(action, dataStore.getResourceId());
		String jsonString = readRequest(request);
		JsonNode jsonRequest = injectResourceId(jsonString, dataStore, action);
		Set<ValidationMessage> messages = schema.validate(jsonRequest);
		if (!messages.isEmpty()) {
			validationFailed(jsonRequest, messages);
		}
		Parameters required = action.getInputParams();
		ReadContext ctx = JsonPath.using(jsonPathConfig).parse(jsonRequest.toString());
		for (String name : required.getNames()) {
			Parameter param = required.get(name);
			readParam(dataStore, ctx, name, param, action);
		}
	}

	public static void convert(HttpServletResponse response, Action action,
			DataStore dataStore) throws ConversionException {
		response.setContentType(dataStore.getResponseType().toString());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());

		Parameters params = action.getOutputParams();
		// TODO: Validate output
		// JsonSchema schema = getOutputSchema(action);
		// Set<ValidationMessage> result = schema.validate(jsonResponse);

		try (PrintWriter writer = response.getWriter()) {
			//ObjectData resultData = operationData.getRootData().filter(params.getNames());
			String resultBody = createOutputJson(dataStore, action);
			//resultBody = IOJsonMessageConverter.getInstance().exportDataToResponseBody(resultData);
			writer.print(resultBody);
			writer.flush();
		} catch (IOException e) {
			log.error(e,e);
			throw new ConversionException("IO exception while preparing response");
		} catch (NullPointerException e) {
			log.error(e,e);
			throw new ConversionException("NPE while preparing response");
		} catch (Exception e) {
			log.error(e,e);
			throw new ConversionException(e.getLocalizedMessage());
		}
	}

	private static String createOutputJson(DataStore dataStore, Action action) throws ConversionException {
		Parameters params = action.getOutputParams();
		DocumentContext ctx = getOutputTemplate(action);
		for (String name : params.getNames()) {
			final Parameter param = params.get(name);
			String path = getOutputPathPrefix(param, action);
			Data data = dataStore.getData(name);
			//TODO: General schema for objects, arrays and simple values is to get 
			//the serialised by RawData and the put to the context here. 
			if (data.getParam().isJsonContainer()) {
				ctx.put(path, name, JsonContainerView.asJsonNode(data));
			} else {
				ctx.put(path, name, data.getSerializedValue());		
			}
		}
		return ctx.jsonString();
	}

	private static DocumentContext getOutputTemplate(Action action) {
		String template = action.getOutputTemplate();
		DocumentContext ctx = null;
		if (StringUtils.isBlank(template)) {
			template = DEFAULT_OUTPUT_JSON;
		}
		try {
			ctx = JsonPath.using(jsonPathConfig).parse(DEFAULT_OUTPUT_JSON);
		} catch(Exception e) {
			log.error(e,e);
		}
		return ctx;
	}

	public static void readParam(DataStore dataStore, ReadContext ctx, String name, Parameter param, Action action) throws ConversionException {
		String paramPath = getReadPath(name, param, action);
		JsonNode node = ctx.read(paramPath, JsonNode.class);
		Data data = new Data(param);
		data.setRawString(node.toString());
		data.earlyInitialization();
		dataStore.addData(name, data);
	}

	private static JsonNode injectResourceId(String jsonString, DataStore dataStore, Action action)
			throws ConversionException {
		final String resourceId = dataStore.getResourceId();

		if (StringUtils.isBlank(resourceId)) {
			return readJson(jsonString);
		}

		Parameters params = action.getInputParams();
		Parameter param = params.get(RESTEndpoint.RESOURCE_ID);

		if (param == null) {
			return readJson(jsonString);
		}

		String path = getInputPathPrefix(param, action);
		DocumentContext ctx = JsonPath.using(jsonPathConfig).parse(jsonString).put(path, RESTEndpoint.RESOURCE_ID,
				resourceId);
		return readJson(ctx.jsonString());
	}

	public static JsonNode readJson(String jsonString) throws ConversionException {
		JsonNode node = null;
		try {
			node = mapper.readTree(jsonString);
		} catch (JsonProcessingException e) {
			log.error(e, e);
		}
		if (node == null) {
			String message = "Error reading json:\n" + jsonString;
			throw new ConversionException(message);
		}
		return node;
	}

	private static String getReadPath(String name, Parameter param, Action action) {
		return getInputPathPrefix(param, action) + "." + name;
	}

	private static String getInputPathPrefix(Parameter param, Action action) {
		String paramPath = param.getInputPath();
		if (StringUtils.isBlank(paramPath)) {
			paramPath = action.getInputPath();
			if (StringUtils.isBlank(paramPath)) {
				paramPath = JSON_ROOT;
			}
		}
		return paramPath;
	}

	private static String getOutputPathPrefix(Parameter param, Action action) {
		String paramPath = param.getOutputPath();
		if (StringUtils.isBlank(paramPath)) {
			paramPath = action.getOutputPath();
			if (StringUtils.isBlank(paramPath)) {
				paramPath = JSON_ROOT ; //+ ".result[0]";
			}
		}
		return paramPath;
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
			log.error(e, e);
		}
		if (jsonRequest == null) {
			String message = "Error reading input json:\n" + jsonString;
			throw new ConversionException(message);
		}
		return jsonString;
	}

	private static JsonSchema getInputSchema(Action action, String resourceId) throws ConversionException {
		String serializedSchema = action.getInputSerializedSchema();
		Parameters params = action.getInputParams();
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
			String type = parameter.getType().getSerializationType().getName();
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

	private static Configuration prepareJsonPathConfig() {
		return Configuration.builder().jsonProvider(new JacksonJsonProvider(mapper))
				.mappingProvider(new JacksonMappingProvider(mapper))
				.options(Option.DEFAULT_PATH_LEAF_TO_NULL, Option.SUPPRESS_EXCEPTIONS).build();
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
