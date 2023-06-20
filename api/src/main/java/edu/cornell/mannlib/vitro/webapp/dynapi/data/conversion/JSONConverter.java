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
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
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
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Procedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonContainerView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.RdfView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.BinaryView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.BooleanView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;

public class JSONConverter {

	private static final String JSON_ROOT = "$";
	private static final String TYPE = "type";
	private static final Log log = LogFactory.getLog(JSONConverter.class.getName());
	private static JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V6);
	private static ObjectMapper mapper = new ObjectMapper();
	private static final Configuration jsonPathConfig = prepareJsonPathConfig();
	//private static final String DEFAULT_OUTPUT_JSON = "{ \"result\" : [ {} ] }";
	private static final String DEFAULT_OUTPUT_JSON = "{}";

	public static void convert(HttpServletRequest request, Procedure procedure, DataStore dataStore)
			throws ConversionException {
		JsonSchema schema = getInputSchema(procedure, dataStore.getResourceId());
		String jsonString = readRequest(request);
		JsonNode jsonRequest = injectResourceId(jsonString, dataStore, procedure);
		Set<ValidationMessage> messages = schema.validate(jsonRequest);
		if (!messages.isEmpty()) {
			validationFailed(jsonRequest, messages);
		}
		Parameters required = procedure.getInputParams();
		ReadContext ctx = JsonPath.using(jsonPathConfig).parse(jsonRequest.toString());
		for (String name : required.getNames()) {
			Parameter param = required.get(name);
			readParam(dataStore, ctx, name, param, procedure);
		}
		Parameters optional = procedure.getOptionalParams();
		for (String name : optional.getNames()) {
            Parameter param = optional.get(name);
            readOptionalParam(dataStore, ctx, name, param, procedure);
        }
	}

	public static void convert(HttpServletResponse response, Procedure procedure,
			DataStore dataStore) throws ConversionException {
		response.setContentType(dataStore.getResponseType().toString());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());

		// TODO: Validate output
		// JsonSchema schema = getOutputSchema(procedure);
		// Set<ValidationMessage> result = schema.validate(jsonResponse);

		try (PrintWriter writer = response.getWriter()) {
			//ObjectData resultData = operationData.getRootData().filter(params.getNames());
			String resultBody = createOutputJson(dataStore, procedure);
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

	private static String createOutputJson(DataStore dataStore, Procedure procedure) throws ConversionException {
		Parameters params = procedure.getOutputParams();
		DocumentContext ctx = getOutputTemplate(procedure);
		for (String name : params.getNames()) {
			final Parameter param = params.get(name);
			String path = getOutputPathPrefix(param, procedure);
			Data data = dataStore.getData(name);
			ctx.put(path, name, convertDataValue(data));
		}
		return ctx.jsonString();
	}
	
    public static JsonNode convertDataValue(Data data) {
        if (RdfView.isRdfNode(data)) {
            return RdfView.getAsJsonNode(data);
        }
        if (data.getParam().isJsonContainer()) {
            return JsonContainerView.asJsonNode(data);
        } 
        if (JsonView.isJsonNode(data.getParam())) {
            return JsonView.getJsonNode(data);
        }
        String serializedValue = data.getSerializedValue();
        if (BooleanView.isBoolean(data)) {
            return mapper.convertValue(Boolean.parseBoolean(serializedValue), BooleanNode.class);
        }
        if (BinaryView.isByteArray(data)) {
            return mapper.convertValue(Boolean.parseBoolean(serializedValue), BinaryNode.class);
        }
        // TODO: implement other types: BigIntegerNode, DecimalNode, DoubleNode,
        // FloatNode, IntNode, LongNode, ShortNode
        return mapper.convertValue(serializedValue, TextNode.class);
    }

	private static DocumentContext getOutputTemplate(Procedure procedure) {
		String template = procedure.getOutputTemplate();
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

	public static void readParam(DataStore dataStore, ReadContext ctx, String name, Parameter param, Procedure procedure) throws ConversionException {
		String paramPath = getReadPath(name, param, procedure);
		JsonNode node = ctx.read(paramPath, JsonNode.class);
		Data data = new Data(param);
		data.setRawString(node.toString());
		data.earlyInitialization();
		dataStore.addData(name, data);
	}
	
    public static void readOptionalParam(DataStore dataStore, ReadContext ctx, String name, Parameter param,
            Procedure procedure) throws ConversionException {
        String paramPath = getReadPath(name, param, procedure);
        try {
            JsonNode node = ctx.read(paramPath, JsonNode.class);
            if (node != null && !node.isMissingNode() && !node.isNull()) {
                Data data = new Data(param);
                data.setRawString(node.asText());
                data.earlyInitialization();
                dataStore.addData(name, data);    
            }
        } catch (Exception e) {
            log.debug(e,e);
        }
    }

	private static JsonNode injectResourceId(String jsonString, DataStore dataStore, Procedure procedure)
			throws ConversionException {
		final String resourceId = dataStore.getResourceId();

		if (StringUtils.isBlank(resourceId)) {
			return readJson(jsonString);
		}

		Parameters params = procedure.getInputParams();
		Parameter param = params.get(RESTEndpoint.RESOURCE_ID);

		if (param == null) {
			return readJson(jsonString);
		}

		String path = getInputPathPrefix(param, procedure);
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
		if (node == null || node.isMissingNode()) {
			String message = "Error reading json:\n" + jsonString;
			throw new ConversionException(message);
		}
		return node;
	}

	private static String getReadPath(String name, Parameter param, Procedure procedure) {
		return getInputPathPrefix(param, procedure) + "." + name;
	}

	private static String getInputPathPrefix(Parameter param, Procedure procedure) {
		String paramPath = param.getInputPath();
		if (StringUtils.isBlank(paramPath)) {
			paramPath = procedure.getInputPath();
			if (StringUtils.isBlank(paramPath)) {
				paramPath = JSON_ROOT;
			}
		}
		return paramPath;
	}

	private static String getOutputPathPrefix(Parameter param, Procedure procedure) {
		String paramPath = param.getOutputPath();
		if (StringUtils.isBlank(paramPath)) {
			paramPath = procedure.getOutputPath();
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
				if (StringUtils.isBlank(jsonString)) {
				    jsonString = "{}";
				}
				jsonRequest = readJson(jsonString);
			}
		} catch (IOException e) {
			log.error(e, e);
		}
		if (jsonRequest == null || jsonRequest.isMissingNode()) {
			String message = "Error reading input json:\n" + jsonString;
			throw new ConversionException(message);
		}
		return jsonString;
	}

	private static JsonSchema getInputSchema(Procedure procedure, String resourceId) throws ConversionException {
		String serializedSchema = procedure.getInputSerializedSchema();
		Parameters params = procedure.getInputParams();
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
