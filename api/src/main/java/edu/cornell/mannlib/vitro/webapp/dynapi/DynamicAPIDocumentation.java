package edu.cornell.mannlib.vitro.webapp.dynapi;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.APIInformation;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.CustomRESTAction;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Procedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.RPC;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullProcedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullRPC;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullResourceAPI;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPI;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPIKey;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Version;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.ArraySerializationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.JsonContainerSerializationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.PrimitiveSerializationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.SerializationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath;
import edu.cornell.mannlib.vitro.webapp.dynapi.request.DocsRequestPath;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

public class DynamicAPIDocumentation {

    private final static Log log = LogFactory.getLog(DynamicAPIDocumentation.class);

    private final static DynamicAPIDocumentation INSTANCE = new DynamicAPIDocumentation();

    private ConfigurationBeanLoader loader;

    public static DynamicAPIDocumentation getInstance() {
        return INSTANCE;
    }

    public void init(ServletContext ctx) {
        loader = new ConfigurationBeanLoader(DynapiModelProvider.getInstance().getModel(), ctx);
    }

    public OpenAPI generate(DocsRequestPath requestPath) throws ConfigurationBeanLoaderException {
        switch (requestPath.getType()) {
            case REST:
                return generateRestDocs(requestPath);
            case RPC:
                return generateRpcDocs(requestPath);
            default:
                log.error(format("Unknown generate docs request type from path %s", requestPath.getServletPath()));
                return new OpenAPI();
        }
    }

    private OpenAPI generateRestDocs(DocsRequestPath requestPath) throws ConfigurationBeanLoaderException {
        OpenAPI openApi = new OpenAPI();

        APIInformation apiInformation = lookupApiInformation(requestPath);

        if (apiInformation == null) {
            log.error(format("Unable to find API information for version %s", requestPath.getApiVersion()));
            return openApi;
        }

        log.info("Matched api (" + apiInformation.getVersion() + ") is " + apiInformation.getTitle() + ".");

        openApi.setInfo(info(apiInformation));
        
        openApi.setServers(getServers(requestPath));

        Paths paths = new Paths();

        String servletPath = ApiRequestPath.REST_SERVLET_PATH;

        if (requestPath.getResourceName() == null) {
            Version version = Version.of(apiInformation.getVersion());

            Collection<ResourceAPI> resourceAPIs = ResourceAPIPool.getInstance().getComponents(version);

            for (ResourceAPI resourceAPI : resourceAPIs) {
                String resourceName = resourceAPI.getName();

                Tag tag = tag(resourceAPI);

                openApi.addTagsItem(tag);

                // resource collection API
                String resourceCollectionPathKey = format("%s/%s/%s", servletPath, version, resourceName);

                paths.put(resourceCollectionPathKey, collectionPathItem(resourceAPI, tag));

                // resource individual API
                String resourceIndividualPathKey = format("%s/resource:{%s}", resourceCollectionPathKey, RESTEndpoint.RESOURCE_ID);

                paths.put(resourceIndividualPathKey, individualPathItem(resourceAPI, tag));

                for (CustomRESTAction customRestAction : resourceAPI.getCustomRESTActions()) {

                    // resource custom REST action
                    String resourceCustomRESTActionPathKey = format("%s/%s", resourceCollectionPathKey,
                            customRestAction.getName());

                    paths.put(resourceCustomRESTActionPathKey, customRESTActionPathItem(customRestAction, tag));
                }
            }
        } else {
            String resourceName = requestPath.getResourceName();
            String version = apiInformation.getVersion();

            ResourceAPIKey resourceAPIKey = ResourceAPIKey.of(resourceName, version);

            ResourceAPI resourceAPI = ResourceAPIPool.getInstance().get(resourceAPIKey);

            if (!(NullResourceAPI.getInstance().equals(resourceAPI))) {
                Tag tag = tag(resourceAPI);

                openApi.addTagsItem(tag);

                // resource collection API
                String resourceCollectionPathKey = format("%s/%s/%s", servletPath, version, resourceName);

                paths.put(resourceCollectionPathKey, collectionPathItem(resourceAPI, tag));

                // resource individual API
                String resourceIndividualPathKey = format("%s/resource:{%s}", resourceCollectionPathKey, RESTEndpoint.RESOURCE_ID);

                paths.put(resourceIndividualPathKey, individualPathItem(resourceAPI, tag));

                for (CustomRESTAction customRestAction : resourceAPI.getCustomRESTActions()) {

                    // resource custom REST action
                    String resourceCustomRESTActionPathKey = format("%s/%s", resourceCollectionPathKey,
                            customRestAction.getName());

                    paths.put(resourceCustomRESTActionPathKey, customRESTActionPathItem(customRestAction, tag));
                }

                resourceAPI.removeClient();
            } else {
                log.warn(format("Resource %s not found", resourceAPIKey));
            }
        }

        openApi.paths(paths);

        return openApi;
    }

    private List<Server> getServers(DocsRequestPath requestPath) {
        List<Server> servers = new ArrayList<>();
        Server server = new Server();
        server.setUrl(requestPath.getServerUrl());
        servers.add(server);
        return servers;
    }

    private OpenAPI generateRpcDocs(DocsRequestPath requestPath) {
        OpenAPI openApi = new OpenAPI();

        Paths paths = new Paths();

        String servletPath = ApiRequestPath.RPC_SERVLET_PATH;

        // Both "info" and "info.version" are required by OpenAPIv3, even if it is an empty string.
        APIInformation apiInformation = new APIInformation();
        apiInformation.setTitle("RPC API");
        apiInformation.setDescription("An RPC API.");
        apiInformation.setVersion("");

        openApi.setInfo(info(apiInformation));
        
        openApi.setServers(getServers(requestPath));

        if (requestPath.getRPCName() == null) {

            Map<String, RPC> rpcs = RPCPool.getInstance().getComponents();

            Tag tag = tag();
            openApi.addTagsItem(tag);

            for (RPC rpc : rpcs.values()) {

                String actionPathKey = format("%s/%s", servletPath, rpc.getKey());
                
                Procedure procedure = ProcedurePool.getInstance().get(rpc.getProcedureUri());
                
                if (!(NullProcedure.getInstance().equals(procedure))) {
                    paths.put(actionPathKey, actionPathItem(procedure, tag));
                }
                
                procedure.removeClient();
            }

        } else {

            String rpcName = requestPath.getRPCName();

            RPCPool rpcPool = RPCPool.getInstance();
            RPC rpc = rpcPool.get(rpcName);
            if (NullRPC.getInstance().equals(rpc)) {
                log.warn(format("RPC %s not found", rpcName));
                return openApi;
            }
            Procedure procedure = ProcedurePool.getInstance().get(rpc.getProcedureUri());

            if (!(NullProcedure.getInstance().equals(procedure))) {
                Tag tag = tag(rpc);
                openApi.addTagsItem(tag);

                String actionPathKey = format("%s/%s", servletPath, rpcName);

                paths.put(actionPathKey, actionPathItem(procedure, tag));

                procedure.removeClient();

            } else {
                log.warn(format("Action %s not found", rpcName));
            }
            rpc.removeClient();

        }

        openApi.paths(paths);

        return openApi;
    }

    private APIInformation lookupApiInformation(DocsRequestPath requestPath) throws ConfigurationBeanLoaderException {
        Set<APIInformation> apis = loader.loadAll(APIInformation.class);

        APIInformation apiInformation = null;

        for (APIInformation api : apis) {
            if (api.getVersion().equals(requestPath.getApiVersion())) {
                apiInformation = api;
                break;
            }
        }

        return apiInformation;
    }

    private Info info(APIInformation apiInformation) {
        Info info = new Info();
        info.setTitle(apiInformation.getTitle());
        info.setDescription(apiInformation.getDescription());
        info.setVersion(apiInformation.getVersion());

        return info;
    }

    private Tag tag(ResourceAPI resourceAPI) {
        Tag tag = new Tag();

        tag.setName(resourceAPI.getName());

        // No description available per resource API
        tag.setDescription(format("REST %s", resourceAPI.getKey()));

        return tag;
    }

    private Tag tag() {
        Tag tag = new Tag();

        tag.setName("All RPC");
        tag.setDescription("All available custom actions");

        return tag;
    }

    private Tag tag(RPC procedure) {
        Tag tag = new Tag();

        try {
            tag.setName(procedure.getKey());

            // No description available per action
            tag.setDescription(format("RPC %s", procedure.getKey()));
        } catch (NullPointerException e) {
            log.error("RPC not defined for action");
        }

        return tag;
    }

    private PathItem collectionPathItem(ResourceAPI resourceAPI, Tag tag) {
        ProcedurePool procedurePool = ProcedurePool.getInstance();
        PathItem pathItem = new PathItem();

        String collectionGetProcedure = resourceAPI.getProcedureUriOnGetAll();
        if (collectionGetProcedure != null) {
            Procedure procedure = procedurePool.get(collectionGetProcedure);
            pathItem.setGet(collectionGetOperation(procedure, tag));
        }

        String collectionPostProcedure = resourceAPI.getProcedureUriOnPost();
        if (collectionPostProcedure != null) {
            Procedure procedure = procedurePool.get(collectionPostProcedure);
            pathItem.setPost(collectionPostOperation(procedure, tag));
        }

        return pathItem;
    }

    private PathItem individualPathItem(ResourceAPI resourceAPI, Tag tag) {
        ProcedurePool procedurePool = ProcedurePool.getInstance();

        PathItem pathItem = new PathItem();

        pathItem.addParametersItem(individualPathParameter());

        String individualGetProcedure = resourceAPI.getProcedureUriOnGet();
        if (individualGetProcedure != null) {
            Procedure procedure = procedurePool.get(individualGetProcedure);
            pathItem.setGet(individualGetOperation(procedure, tag));
        }

        String individualPutProcedure = resourceAPI.getProcedureUriOnPut();
        if (individualPutProcedure != null) {
            Procedure procedure = procedurePool.get(individualPutProcedure);
            pathItem.setPut(individualPutOperation(procedure, tag));
        }

        String individualPatchProcedure = resourceAPI.getProcedureUriOnPatch();
        if (individualPatchProcedure != null) {
            Procedure procedure = procedurePool.get(individualPatchProcedure);
            pathItem.setPatch(individualPatchOperation(procedure, tag));
        }

        String individualDeleteProcedure = resourceAPI.getProcedureUriOnDelete();
        if (individualDeleteProcedure != null) {
            Procedure procedure = procedurePool.get(individualDeleteProcedure);
            pathItem.setDelete(individualDeleteOperation(procedure, tag));
        }

        return pathItem;
    }

    private PathItem customRESTActionPathItem(CustomRESTAction customRESTAction, Tag tag) {
        ProcedurePool actionPool = ProcedurePool.getInstance();

        PathItem pathItem = new PathItem();

        String targetRPC = customRESTAction.getTargetProcedureUri();
       
        if (targetRPC != null) {
            try (Procedure action = actionPool.get(targetRPC)) {
                pathItem.setPut(customRESTActionPutOperation(action, tag));
            }
        }

        return pathItem;
    }

    private PathItem actionPathItem(Procedure procedure, Tag tag) {
        PathItem pathItem = new PathItem();

        pathItem.setPost(actionPostOperation(procedure, tag));

        return pathItem;
    }

    private Operation collectionGetOperation(Procedure action, Tag tag) {
        Operation operation = new Operation();
        operation.addTagsItem(tag.getName());

        // No description available per action RPC
        // operation.setDescription(format("RPC %s", action.getKey()));

        ApiResponses apiResponses = new ApiResponses();

        addOkApiResponse(apiResponses, action);
        addUnauthorizedApiResponse(apiResponses);
        addForbiddenApiResponse(apiResponses);
        addNotFoundApiResponse(apiResponses);

        operation.setResponses(apiResponses);

        return operation;
    }

    private Operation collectionPostOperation(Procedure action, Tag tag) {
        Operation operation = new Operation();
        operation.addTagsItem(tag.getName());

        // No description available per action RPC
        // operation.setDescription(format("RPC %s", action.getKey()));

        RequestBody requestBody = new RequestBody();
        Content content = new Content();
        MediaType mediaType = new MediaType();
        ObjectSchema schema = new ObjectSchema();

        buildObjectSchema(schema, action.getInputParams());

        mediaType.schema(schema);
        content.addMediaType("application/json", mediaType);
        requestBody.setContent(content);

        operation.setRequestBody(requestBody);

        ApiResponses apiResponses = new ApiResponses();

        addCreatedApiResponse(apiResponses);
        addUnauthorizedApiResponse(apiResponses);
        addForbiddenApiResponse(apiResponses);
        addNotFoundApiResponse(apiResponses);

        operation.setResponses(apiResponses);

        return operation;
    }

    private Operation individualGetOperation(Procedure action, Tag tag) {
        Operation operation = new Operation();
        operation.addTagsItem(tag.getName());

        // No description available per action RPC
        // operation.setDescription(format("RPC %s", action.getKey()));

        ApiResponses apiResponses = new ApiResponses();

        addOkApiResponse(apiResponses, action);
        addUnauthorizedApiResponse(apiResponses);
        addForbiddenApiResponse(apiResponses);
        addNotFoundApiResponse(apiResponses);

        operation.setResponses(apiResponses);

        return operation;
    }

    private Operation individualPutOperation(Procedure action, Tag tag) {
        Operation operation = new Operation();
        operation.addTagsItem(tag.getName());

        // No description available per action RPC
        // operation.setDescription(format("RPC %s", action.getKey()));

        RequestBody requestBody = new RequestBody();
        Content content = new Content();
        MediaType mediaType = new MediaType();
        ObjectSchema schema = new ObjectSchema();

        buildObjectSchema(schema, action.getInputParams());

        mediaType.schema(schema);
        content.addMediaType("application/json", mediaType);
        requestBody.setContent(content);

        operation.setRequestBody(requestBody);

        ApiResponses apiResponses = new ApiResponses();

        addOkApiResponse(apiResponses, action);
        addUnauthorizedApiResponse(apiResponses);
        addForbiddenApiResponse(apiResponses);
        addNotFoundApiResponse(apiResponses);

        operation.setResponses(apiResponses);

        return operation;
    }

    private Operation individualPatchOperation(Procedure action, Tag tag) {
        Operation operation = new Operation();
        operation.addTagsItem(tag.getName());

        // No description available per action RPC
        // operation.setDescription(format("RPC %s", action.getKey()));

        RequestBody requestBody = new RequestBody();
        Content content = new Content();
        MediaType mediaType = new MediaType();
        ObjectSchema schema = new ObjectSchema();

        buildObjectSchema(schema, action.getInputParams());

        mediaType.schema(schema);
        content.addMediaType("application/json", mediaType);
        requestBody.setContent(content);

        operation.setRequestBody(requestBody);

        ApiResponses apiResponses = new ApiResponses();

        addOkApiResponse(apiResponses, action);
        addUnauthorizedApiResponse(apiResponses);
        addForbiddenApiResponse(apiResponses);
        addNotFoundApiResponse(apiResponses);

        operation.setResponses(apiResponses);

        return operation;
    }

    private Operation individualDeleteOperation(Procedure action, Tag tag) {
        Operation operation = new Operation();
        operation.addTagsItem(tag.getName());

        // No description available per action RPC
        // operation.setDescription(format("RPC %s", action.getKey()));

        ApiResponses apiResponses = new ApiResponses();

        addOkApiResponse(apiResponses, action);
        addUnauthorizedApiResponse(apiResponses);
        addForbiddenApiResponse(apiResponses);
        addNotFoundApiResponse(apiResponses);

        operation.setResponses(apiResponses);

        return operation;
    }

    private Operation customRESTActionPutOperation(Procedure action, Tag tag) {
        return individualPutOperation(action, tag);
    }
    
    private Operation customRESTActionPostOperation(Procedure action, Tag tag) {
        return collectionPostOperation(action, tag);
    }

    private Operation customRESTActionGetOperation(Procedure action, Tag tag) {
        return collectionGetOperation(action, tag);
    }

    private Operation customRESTActionPatchOperation(Procedure action, Tag tag) {
        return individualPatchOperation(action, tag);
    }

    private Operation customRESTActionDeleteOperation(Procedure action, Tag tag) {
        return individualDeleteOperation(action, tag);
    }
     

    private Operation actionPostOperation(Procedure action, Tag tag) {
        Operation operation = new Operation();
        operation.addTagsItem(tag.getName());

        // No description available per action RPC
        // operation.setDescription(format("RPC %s", action.getKey()));

        RequestBody requestBody = new RequestBody();
        Content content = new Content();
        MediaType mediaType = new MediaType();
        ObjectSchema schema = new ObjectSchema();

        buildObjectSchema(schema, action.getInputParams());

        mediaType.schema(schema);
        content.addMediaType("application/json", mediaType);
        requestBody.setContent(content);

        operation.setRequestBody(requestBody);

        ApiResponses apiResponses = new ApiResponses();

        addOkApiResponse(apiResponses, action);
        addUnauthorizedApiResponse(apiResponses);
        addForbiddenApiResponse(apiResponses);
        addNotFoundApiResponse(apiResponses);

        operation.setResponses(apiResponses);

        return operation;
    }

    private PathParameter individualPathParameter() {
        PathParameter pathParameter = new PathParameter();

        pathParameter.setName(RESTEndpoint.RESOURCE_ID);
        pathParameter.description("Base64 encoded URI of the resource");
        StringSchema schema = new StringSchema();
        pathParameter.schema(schema);

        return pathParameter;
    }

    private void addOkApiResponse(ApiResponses apiResponses, Procedure action) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("OK");

        Content content = new Content();
        MediaType mediaType = new MediaType();
        ObjectSchema schema = new ObjectSchema();

        buildObjectSchema(schema, action.getOutputParams());

        mediaType.schema(schema);

        content.addMediaType("application/json", mediaType);

        apiResponse.setContent(content);

        apiResponses.put("200", apiResponse);
    }

    private void buildObjectSchema(ObjectSchema objectSchema, Parameters parameters) {
        if (parameters == null) {
            return;
        }
        for (String parameterName : parameters.getNames()) {
            Parameter parameter = parameters.get(parameterName);
            SerializationType serializationType = parameter.getType().getSerializationType();

            if (serializationType instanceof PrimitiveSerializationType) {

                Schema<?> primitiveParameter = toPrimativeSchema(serializationType);

                primitiveParameter.setDescription(parameter.getDescription());

                objectSchema.addProperties(parameterName, primitiveParameter);

            } else if (serializationType instanceof JsonContainerSerializationType) {

                ObjectSchema objectParameter = new ObjectSchema();

                objectParameter.setDescription(parameter.getDescription());

                objectSchema.addProperties(parameterName, objectParameter);

                buildObjectSchema(objectParameter, ((JsonContainerSerializationType) serializationType).getInternalElements());

            } else if (serializationType instanceof ArraySerializationType) {

                ArraySchema arraySchema = new ArraySchema();

                arraySchema.setDescription(parameter.getDescription());

                objectSchema.addProperties(parameterName, arraySchema);

                buildArraySchema(arraySchema, parameter);
            }
        }
    }

    private void buildArraySchema(ArraySchema arraySchema, Parameter parameter) {
        SerializationType parameterType = parameter.getType().getSerializationType();

        SerializationType arrayParameterType = ((ArraySerializationType) parameterType).getElementsType();

        Schema<?> primitiveParameter = null;

        if (arrayParameterType instanceof PrimitiveSerializationType) {
            primitiveParameter = toPrimativeSchema(arrayParameterType);
        } else if (arrayParameterType instanceof JsonContainerSerializationType) {

            primitiveParameter = new ObjectSchema();

            primitiveParameter.setDescription(parameter.getDescription());

            buildObjectSchema((ObjectSchema) primitiveParameter,
                    ((JsonContainerSerializationType) arrayParameterType).getInternalElements());

        } else if (parameterType instanceof ArraySerializationType) {

            primitiveParameter = new ArraySchema();

            primitiveParameter.setDescription(parameter.getDescription());

            buildArraySchema((ArraySchema) primitiveParameter, parameter);
        }

        arraySchema.setItems(primitiveParameter);
    }

    private Schema<?> toPrimativeSchema(SerializationType parameterType) {
        Schema<?> propertySchema;

        if (parameterType.getName().equals("boolean")) {
            propertySchema = new BooleanSchema();
        } else if (parameterType.getName().equals("integer")) {
            propertySchema = new IntegerSchema();
        } else if (parameterType.getName().equals("decimal")) {
            propertySchema = new NumberSchema();
        } else {
            propertySchema = new StringSchema();
        }

        return propertySchema;
    }

    private void addCreatedApiResponse(ApiResponses apiResponses) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("Created");

        apiResponses.put("201", apiResponse);
    }

    private void addUnauthorizedApiResponse(ApiResponses apiResponses) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("Unauthorized");

        Content content = new Content();
        MediaType mediaType = new MediaType();
        StringSchema schema = new StringSchema();

        mediaType.schema(schema);
        content.addMediaType("text/plain", mediaType);
        apiResponse.setContent(content);

        apiResponses.put("401", apiResponse);
    }

    private void addForbiddenApiResponse(ApiResponses apiResponses) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("Forbidden");

        Content content = new Content();
        MediaType mediaType = new MediaType();
        StringSchema schema = new StringSchema();

        mediaType.schema(schema);
        content.addMediaType("text/plain", mediaType);
        apiResponse.setContent(content);

        apiResponses.put("403", apiResponse);
    }

    private void addNotFoundApiResponse(ApiResponses apiResponses) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("Resource not found");

        Content content = new Content();
        MediaType mediaType = new MediaType();
        StringSchema schema = new StringSchema();

        mediaType.schema(schema);
        content.addMediaType("text/plain", mediaType);
        apiResponse.setContent(content);

        apiResponses.put("404", apiResponse);
    }

}
