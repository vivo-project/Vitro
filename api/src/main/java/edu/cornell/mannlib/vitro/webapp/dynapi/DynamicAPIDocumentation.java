package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.OperationData.RESOURCE_ID;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_UNION;
import static java.lang.String.format;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.APIInformation;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.CustomRESTAction;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.DefaultAction;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.DefaultResourceAPI;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.HTTPMethod;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.RPC;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPI;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPIKey;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Version;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ArrayParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ObjectParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.PrimitiveParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath;
import edu.cornell.mannlib.vitro.webapp.dynapi.request.DocsRequestPath;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
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
import io.swagger.v3.oas.models.tags.Tag;

public class DynamicAPIDocumentation {

    private final static Log log = LogFactory.getLog(DynamicAPIDocumentation.class);

    private final static DynamicAPIDocumentation INSTANCE = new DynamicAPIDocumentation();

    private ConfigurationBeanLoader loader;
    private ContextModelAccess modelAccess;
    private OntModel dynamicAPIModel;

    public static DynamicAPIDocumentation getInstance() {
        return INSTANCE;
    }

    public void init(ServletContext ctx) {
        modelAccess = ModelAccess.on(ctx);
        dynamicAPIModel = modelAccess.getOntModel(FULL_UNION);
        loader = new ConfigurationBeanLoader(dynamicAPIModel, ctx);
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
                String resourceIndividualPathKey = format("%s/resource:{%s}", resourceCollectionPathKey, RESOURCE_ID);

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

            if (!(resourceAPI instanceof DefaultResourceAPI)) {
                Tag tag = tag(resourceAPI);

                openApi.addTagsItem(tag);

                // resource collection API
                String resourceCollectionPathKey = format("%s/%s/%s", servletPath, version, resourceName);

                paths.put(resourceCollectionPathKey, collectionPathItem(resourceAPI, tag));

                // resource individual API
                String resourceIndividualPathKey = format("%s/resource:{%s}", resourceCollectionPathKey, RESOURCE_ID);

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

        if (requestPath.getActionName() == null) {

            Map<String, Action> actions = ActionPool.getInstance().getComponents();

            Tag tag = tag();
            openApi.addTagsItem(tag);

            for (Action action : actions.values()) {

                String actionName = action.getKey();

                String actionPathKey = format("%s/%s", servletPath, actionName);

                paths.put(actionPathKey, actionPathItem(action, tag));
            }

        } else {

            String actionName = requestPath.getActionName();

            Action action = ActionPool.getInstance().get(actionName);

            if (!(action instanceof DefaultAction)) {
                Tag tag = tag(action);
                openApi.addTagsItem(tag);

                String actionPathKey = format("%s/%s", servletPath, actionName);

                paths.put(actionPathKey, actionPathItem(action, tag));

                action.removeClient();

            } else {
                log.warn(format("Action %s not found", actionName));
            }

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

    private Tag tag(Action action) {
        Tag tag = new Tag();

        try {
            tag.setName(action.getKey());

            // No description available per action
            tag.setDescription(format("RPC %s", action.getKey()));
        } catch (NullPointerException e) {
            log.error("RPC not defined for action");
        }

        return tag;
    }

    private PathItem collectionPathItem(ResourceAPI resourceAPI, Tag tag) {
        ActionPool actionPool = ActionPool.getInstance();
        PathItem pathItem = new PathItem();

        // No way to distinguish an RCP get for collection or individual
        RPC collectionGetRPC = resourceAPI.getRpcOnGet();
        if (collectionGetRPC != null) {
            Action action = actionPool.get(collectionGetRPC.getName());
            pathItem.setGet(collectionGetOperation(action, tag));
        }

        RPC collectionPostRPC = resourceAPI.getRpcOnPost();
        if (collectionPostRPC != null) {
            Action action = actionPool.get(collectionPostRPC.getName());
            pathItem.setPost(collectionPostOperation(action, tag));
        }

        return pathItem;
    }

    private PathItem individualPathItem(ResourceAPI resourceAPI, Tag tag) {
        ActionPool actionPool = ActionPool.getInstance();

        PathItem pathItem = new PathItem();

        pathItem.addParametersItem(individualPathParameter());

        // No way to distinguish an RCP get for collection or individual
        RPC individualGetRPC = resourceAPI.getRpcOnGet();
        if (individualGetRPC != null) {
            Action action = actionPool.get(individualGetRPC.getName());
            pathItem.setGet(individualGetOperation(action, tag));
        }

        RPC individualPutAction = resourceAPI.getRpcOnPut();
        if (individualPutAction != null) {
            Action action = actionPool.get(individualPutAction.getName());
            pathItem.setPut(individualPutOperation(action, tag));
        }

        RPC individualPatchAction = resourceAPI.getRpcOnPatch();
        if (individualPatchAction != null) {
            Action action = actionPool.get(individualPatchAction.getName());
            pathItem.setPatch(individualPatchOperation(action, tag));
        }

        RPC individualDeleteAction = resourceAPI.getRpcOnDelete();
        if (individualDeleteAction != null) {
            Action action = actionPool.get(individualDeleteAction.getName());
            pathItem.setDelete(individualDeleteOperation(action, tag));
        }

        return pathItem;
    }

    private PathItem customRESTActionPathItem(CustomRESTAction customRESTAction, Tag tag) {
        ActionPool actionPool = ActionPool.getInstance();

        PathItem pathItem = new PathItem();

        RPC targetRPC = customRESTAction.getTargetRPC();
        if (targetRPC != null) {
            Action action = actionPool.get(targetRPC.getName());
            HTTPMethod httpMethod = targetRPC.getHttpMethod();

            if (httpMethod != null) {
                switch (httpMethod.getName().toUpperCase()) {
                    case "POST":
                        pathItem.setPost(customRESTActionPostOperation(action, tag));
                        break;
                    case "GET":
                        pathItem.setGet(customRESTActionGetOperation(action, tag));
                        break;
                    case "PUT":
                        pathItem.setPut(customRESTActionPutOperation(action, tag));
                        break;
                    case "PATCH":
                        pathItem.setPatch(customRESTActionPatchOperation(action, tag));
                        break;
                    case "DELETE":
                        pathItem.setDelete(customRESTActionDeleteOperation(action, tag));
                        break;
                    default:
                        break;
                }
            }
        }

        return pathItem;
    }

    private PathItem actionPathItem(Action action, Tag tag) {
        PathItem pathItem = new PathItem();

        pathItem.setPost(actionPostOperation(action, tag));

        return pathItem;
    }

    private Operation collectionGetOperation(Action action, Tag tag) {
        Operation operation = new Operation();
        operation.addTagsItem(tag.getName());

        // No description available per resource API rpc
        // operation.setDescription("");

        ApiResponses apiResponses = new ApiResponses();

        addOkApiResponse(apiResponses, action);
        addUnauthorizedApiResponse(apiResponses);
        addForbiddenApiResponse(apiResponses);
        addNotFoundApiResponse(apiResponses);

        operation.setResponses(apiResponses);

        return operation;
    }

    private Operation collectionPostOperation(Action action, Tag tag) {
        Operation operation = new Operation();
        operation.addTagsItem(tag.getName());

        // No description available per resource API rpc
        // operation.setDescription("");

        RequestBody requestBody = new RequestBody();
        Content content = new Content();
        MediaType mediaType = new MediaType();
        ObjectSchema schema = new ObjectSchema();

        buildObjectSchema(schema, action.getRequiredParams());

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

    private Operation individualGetOperation(Action action, Tag tag) {
        Operation operation = new Operation();
        operation.addTagsItem(tag.getName());

        // No description available per resource API rpc
        // operation.setDescription("");

        ApiResponses apiResponses = new ApiResponses();

        addOkApiResponse(apiResponses, action);
        addUnauthorizedApiResponse(apiResponses);
        addForbiddenApiResponse(apiResponses);
        addNotFoundApiResponse(apiResponses);

        operation.setResponses(apiResponses);

        return operation;
    }

    private Operation individualPutOperation(Action action, Tag tag) {
        Operation operation = new Operation();
        operation.addTagsItem(tag.getName());

        // No description available per resource API rpc
        // operation.setDescription("");

        RequestBody requestBody = new RequestBody();
        Content content = new Content();
        MediaType mediaType = new MediaType();
        ObjectSchema schema = new ObjectSchema();

        buildObjectSchema(schema, action.getRequiredParams());

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

    private Operation individualPatchOperation(Action action, Tag tag) {
        Operation operation = new Operation();
        operation.addTagsItem(tag.getName());

        // No description available per resource API rpc
        // operation.setDescription("");

        RequestBody requestBody = new RequestBody();
        Content content = new Content();
        MediaType mediaType = new MediaType();
        ObjectSchema schema = new ObjectSchema();

        buildObjectSchema(schema, action.getRequiredParams());

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

    private Operation individualDeleteOperation(Action action, Tag tag) {
        Operation operation = new Operation();
        operation.addTagsItem(tag.getName());

        // No description available per resource API rpc
        // operation.setDescription("");

        ApiResponses apiResponses = new ApiResponses();

        addOkApiResponse(apiResponses, action);
        addUnauthorizedApiResponse(apiResponses);
        addForbiddenApiResponse(apiResponses);
        addNotFoundApiResponse(apiResponses);

        operation.setResponses(apiResponses);

        return operation;
    }

    private Operation customRESTActionGetOperation(Action action, Tag tag) {
        return collectionGetOperation(action, tag);
    }

    private Operation customRESTActionPostOperation(Action action, Tag tag) {
        return collectionPostOperation(action, tag);
    }

    private Operation customRESTActionPutOperation(Action action, Tag tag) {
        return individualPutOperation(action, tag);
    }

    private Operation customRESTActionPatchOperation(Action action, Tag tag) {
        return individualPatchOperation(action, tag);
    }

    private Operation customRESTActionDeleteOperation(Action action, Tag tag) {
        return individualDeleteOperation(action, tag);
    }

    private Operation actionPostOperation(Action action, Tag tag) {
        Operation operation = new Operation();
        operation.addTagsItem(tag.getName());

        // No description available per resource API rpc
        // operation.setDescription("");

        RequestBody requestBody = new RequestBody();
        Content content = new Content();
        MediaType mediaType = new MediaType();
        ObjectSchema schema = new ObjectSchema();

        buildObjectSchema(schema, action.getRequiredParams());

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

        pathParameter.setName(RESOURCE_ID);
        pathParameter.description("Base64 encoded URI of the resource");
        StringSchema schema = new StringSchema();
        pathParameter.schema(schema);

        return pathParameter;
    }

    private void addOkApiResponse(ApiResponses apiResponses, Action action) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("OK");

        Content content = new Content();
        MediaType mediaType = new MediaType();
        ObjectSchema schema = new ObjectSchema();

        buildObjectSchema(schema, action.getProvidedParams());

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
            ParameterType parameterType = parameter.getType();

            if (parameterType instanceof PrimitiveParameterType) {

                Schema<?> primitiveParameter = toPrimativeSchema(parameterType);

                primitiveParameter.setDescription(parameter.getDescription());

                objectSchema.addProperties(parameterName, primitiveParameter);

            } else if (parameterType instanceof ObjectParameterType) {

                ObjectSchema objectParameter = new ObjectSchema();

                objectParameter.setDescription(parameter.getDescription());

                objectSchema.addProperties(parameterName, objectParameter);

                buildObjectSchema(objectParameter, ((ObjectParameterType) parameterType).getInternalElements());

            } else if (parameterType instanceof ArrayParameterType) {

                ArraySchema arraySchema = new ArraySchema();

                arraySchema.setDescription(parameter.getDescription());

                objectSchema.addProperties(parameterName, arraySchema);

                buildArraySchema(arraySchema, parameter);
            }
        }
    }

    private void buildArraySchema(ArraySchema arraySchema, Parameter parameter) {
        ParameterType parameterType = parameter.getType();

        ParameterType arrayParameterType = ((ArrayParameterType) parameterType).getElementsType();

        Schema<?> primitiveParameter = null;

        if (arrayParameterType instanceof PrimitiveParameterType) {
            primitiveParameter = toPrimativeSchema(arrayParameterType);
        } else if (arrayParameterType instanceof ObjectParameterType) {

            primitiveParameter = new ObjectSchema();

            primitiveParameter.setDescription(parameter.getDescription());

            buildObjectSchema((ObjectSchema) primitiveParameter,
                    ((ObjectParameterType) arrayParameterType).getInternalElements());

        } else if (parameterType instanceof ArrayParameterType) {

            primitiveParameter = new ArraySchema();

            primitiveParameter.setDescription(parameter.getDescription());

            buildArraySchema((ArraySchema) primitiveParameter, parameter);
        }

        arraySchema.setItems(primitiveParameter);
    }

    private Schema<?> toPrimativeSchema(ParameterType parameterType) {
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
