package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath.REST_SERVLET_PATH;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_UNION;
import static java.lang.String.format;

import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.APIInformation;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
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

    private ServletContext ctx;
    private ConfigurationBeanLoader loader;
    private ContextModelAccess modelAccess;
    private OntModel dynamicAPIModel;

    public static DynamicAPIDocumentation getInstance() {
        return INSTANCE;
    }

    public void init(ServletContext ctx) {
        this.ctx = ctx;
        modelAccess = ModelAccess.on(ctx);
        dynamicAPIModel = modelAccess.getOntModel(FULL_UNION);
        loader = new ConfigurationBeanLoader(dynamicAPIModel, ctx);
    }

    public OpenAPI generate(DocsRequestPath requestPath) throws ConfigurationBeanLoaderException {

        Set<APIInformation> apis = loader.loadAll(APIInformation.class);

        APIInformation apiInformation = null;

        for (APIInformation api : apis) {
            if (api.getVersion().equals(requestPath.getApiVersion())) {
                apiInformation = api;
                break;
            }
        }

        OpenAPI openApi = new OpenAPI();

        if (apiInformation == null) {
            return openApi;
        }

        openApi.setInfo(info(apiInformation));

        Paths paths = new Paths();

        String restPathKey = REST_SERVLET_PATH;

        if (requestPath.getResourceName() == null) {

            Version version = Version.of(apiInformation.getVersion());

            List<ResourceAPI> resourceAPIs = ResourceAPIPool.getInstance().getComponents(version);

            for (ResourceAPI resourceAPI : resourceAPIs) {
                String resourceName = resourceAPI.getName();

                Tag tag = tag(resourceAPI);

                openApi.addTagsItem(tag);

                // resource collection API
                String resourceCollectionPathKey = format("%s/%s/%s", restPathKey, version, resourceName);

                paths.put(resourceCollectionPathKey, collectionPathItem(resourceAPI, tag));

                // resource individual API
                String resourceIndividualPathKey = format("%s/resource:{resourceId}", resourceCollectionPathKey);

                paths.put(resourceIndividualPathKey, individualPathItem(resourceAPI, tag));
            }

        } else {

            String resourceName = requestPath.getResourceName();
            String version = apiInformation.getVersion();

            ResourceAPIKey resourceAPIKey = ResourceAPIKey.of(resourceName, version);

            ResourceAPI resourceAPI = ResourceAPIPool.getInstance().get(resourceAPIKey);

            Tag tag = tag(resourceAPI);

            openApi.addTagsItem(tag);

            // resource collection API
            String resourceCollectionPathKey = format("%s/%s/%s", restPathKey, version, resourceName);

            paths.put(resourceCollectionPathKey, collectionPathItem(resourceAPI, tag));

            // resource individual API
            String resourceIndividualPathKey = format("%s/resource:{resourceId}", resourceCollectionPathKey);

            paths.put(resourceIndividualPathKey, individualPathItem(resourceAPI, tag));

        }

        openApi.paths(paths);

        return openApi;
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
        tag.setDescription(format("REST for %s", resourceAPI.getName()));

        return tag;
    }

    private PathItem collectionPathItem(ResourceAPI resourceAPI, Tag tag) {
        ActionPool actionPool = ActionPool.getInstance();

        PathItem pathItem = new PathItem();

        // No way to distinguish an RCP get for collection or individual
        RPC collectionGetRPC = resourceAPI.getRpcOnGet();
        Action collectionGetAction = actionPool.get(collectionGetRPC.getName());

        pathItem.setGet(collectionGetOperation(collectionGetAction, tag));

        RPC collectionPostRPC = resourceAPI.getRpcOnPost();
        Action collectionPostAction = actionPool.get(collectionPostRPC.getName());

        pathItem.setPost(collectionPostOperation(collectionPostAction, tag));

        return pathItem;
    }

    private PathItem individualPathItem(ResourceAPI resourceAPI, Tag tag) {
        ActionPool actionPool = ActionPool.getInstance();

        PathItem pathItem = new PathItem();

        pathItem.addParametersItem(individualPathParameter());

        // No way to distinguish an RCP get for collection or individual
        RPC individualGetRPC = resourceAPI.getRpcOnGet();
        Action individualGetAction = actionPool.get(individualGetRPC.getName());

        pathItem.setGet(individualGetOperation(individualGetAction, tag));

        RPC individualPutRPC = resourceAPI.getRpcOnGet();
        Action individualPutAction = actionPool.get(individualPutRPC.getName());

        pathItem.setPut(individualPutOperation(individualPutAction, tag));

        RPC individualPatchRPC = resourceAPI.getRpcOnGet();
        Action individualPatchAction = actionPool.get(individualPatchRPC.getName());

        pathItem.setPatch(individualPatchOperation(individualPatchAction, tag));

        RPC individualDeleteRPC = resourceAPI.getRpcOnGet();
        Action individualDeleteAction = actionPool.get(individualDeleteRPC.getName());

        pathItem.setDelete(individualDeleteOperation(individualDeleteAction, tag));

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

    private PathParameter individualPathParameter() {
        PathParameter pathParameter = new PathParameter();

        pathParameter.setName("resourceId");
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

            buildObjectSchema((ObjectSchema) primitiveParameter, ((ObjectParameterType) arrayParameterType).getInternalElements());

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
