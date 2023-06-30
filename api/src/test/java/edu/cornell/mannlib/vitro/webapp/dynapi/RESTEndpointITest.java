package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.RequestPath.REST_SERVLET_PATH;
import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

@RunWith(Parameterized.class)
public class RESTEndpointITest extends ServletContextITest {

    private final static String BASE_URL = "http://localhost:8080";

    private final static String MOCK_BASE_PATH = "src/test/resources/dynapi/mock";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RESTEndpoint restEndpoint;

    private ActionPool actionPool;

    private ResourcePool resourcePool;

    @Mock
    private QueryExecution queryExecution;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServletInputStream requestInputStream;

    @Mock
    private HttpServletResponse response;

    @Spy
    private ServletOutputStream responseOutputStream;

    private MockedStatic<QueryExecutionFactory> queryExecutionFactoryStatic;

    @Parameter(0)
    public String testRequestMethod;

    @Parameter(1)
    public String testRequestPath;

    @Parameter(2)
    public String testRequestParamsFile;

    @Parameter(3)
    public String testRequestBodyFile;

    @Parameter(4)
    public String testSparqlResponseFile;

    @Parameter(5)
    public Integer testResponseStatus;

    @Parameter(6)
    public String testResponseBodyFile;

    @Parameter(7)
    public String testMessage;

    @Before
    public void beforeEach() throws IOException {
        actionPool = ActionPool.getInstance();
        resourcePool = ResourcePool.getInstance();

        restEndpoint = new RESTEndpoint();

        loadDefaultModel();
        loadTestModel();

        actionPool.init(servletContext);
        actionPool.reload();

        resourcePool.init(servletContext);
        resourcePool.reload();

        MockitoAnnotations.openMocks(this);

        when(request.getServletContext()).thenReturn(servletContext);

        queryExecutionFactoryStatic = mockStatic(QueryExecutionFactory.class);
        when(QueryExecutionFactory.create(any(String.class), any(Model.class))).thenReturn(queryExecution);

        mockStatus(response);
    }

    @After
    public void afterEach() {
        queryExecutionFactoryStatic.close();
    }

    @Test
    public void doTest() throws IOException {
        when(request.getMethod()).thenReturn(testRequestMethod);
        when(request.getRequestURL()).thenReturn(new StringBuffer(BASE_URL + REST_SERVLET_PATH + testRequestPath));
        when(request.getRequestURI()).thenReturn(BASE_URL + REST_SERVLET_PATH + testRequestPath);
        when(request.getServletPath()).thenReturn(REST_SERVLET_PATH);
        when(request.getPathInfo()).thenReturn(testRequestPath);

        if (testRequestParamsFile != null) {
            String filePath = format("%s/rest/request/params/%s/%s", MOCK_BASE_PATH, testRequestMethod.toLowerCase(), testRequestParamsFile);
            Map<String, String[]> params = objectMapper.readValue(new File(filePath), new TypeReference<HashMap<String, String[]>>() {});
            when(request.getParameterMap()).thenReturn(params);
        }

        if (testRequestBodyFile != null) {
            when(request.getInputStream()).thenReturn(requestInputStream);
            // mock requestInputStream read with input stream from file
            // String filePath = format("%s/rest/request/body/%s/%s", MOCK_BASE_PATH, testRequestMethod.toLowerCase(), testRequestParamsFile);
        }

        if (testSparqlResponseFile != null) {
            InputStream stream = readMockFileAsInputStream(testSparqlResponseFile);
            when(queryExecution.execSelect()).thenReturn(ResultSetFactory.fromJSON(stream));
        }

        if (testResponseBodyFile != null) {
            when(response.getOutputStream()).thenReturn(responseOutputStream);
            // mock responseOutputStream to a string to compare with file
            // String filePath = format("%s/rest/response/body/%s/%s", MOCK_BASE_PATH, testRequestMethod.toLowerCase(), testRequestParamsFile);
        }

        run(testRequestMethod);

        if (testResponseStatus != null) {
            verify(response, times(1)).setStatus(testResponseStatus);
        } else {
            verify(response, times(0)).setStatus(anyInt());
        }

        assertEquals("Invalid Status for test: " + testMessage, status, response.getStatus());
    }

    private void run(String method) {
        switch (method) {
            case "POST":
                restEndpoint.doPost(request, response);
                break;
            case "GET":
                restEndpoint.doGet(request, response);
                break;
            case "PUT":
                restEndpoint.doPut(request, response);
                break;
            case "PATCH":
                restEndpoint.doPatch(request, response);
                break;
            case "DELETE":
                restEndpoint.doDelete(request, response);
                break;
            default:
                break;
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        List<Object[]> requests = new ArrayList<>(Arrays.asList(new Object[][] {
            { "POST",   "/1/test_not_found", null, null, null, SC_NOT_FOUND,          null, "Resource not found" },
            { "GET",    "/1/test_not_found", null, null, null, SC_NOT_FOUND,          null, "Resource not found" },
            { "PUT",    "/1/test_not_found", null, null, null, SC_METHOD_NOT_ALLOWED, null, "Method not allowed before looking for resource" },
            { "PATCH",  "/1/test_not_found", null, null, null, SC_METHOD_NOT_ALLOWED, null, "Method not allowed before looking for resource" },
            { "DELETE", "/1/test_not_found", null, null, null, SC_METHOD_NOT_ALLOWED, null, "Method not allowed before looking for resource" }
        }));

        // update with applicable individual ids, ideally they would exist in model for integration testing
        requests.addAll(requests("test_collection_resource",   "https://scholars.institution.edu/individual/n1f9d4ddc", "test_collection_custom_action_name"));
        requests.addAll(requests("test_concept_resource",      "https://scholars.institution.edu/individual/n1f9d4ddc", "test_concept_custom_action_name"));
        requests.addAll(requests("test_document_resource",     "https://scholars.institution.edu/individual/n1f9d4ddc", "test_document_custom_action_name"));
        requests.addAll(requests("test_organization_resource", "https://scholars.institution.edu/individual/n1f9d4ddc", "test_organization_custom_action_name"));
        requests.addAll(requests("test_person_resource",       "https://scholars.institution.edu/individual/n1f9d4ddc", "test_person_custom_action_name"));
        requests.addAll(requests("test_process_resource",      "https://scholars.institution.edu/individual/n1f9d4ddc", "test_process_custom_action_name"));
        requests.addAll(requests("test_relationship_resource", "https://scholars.institution.edu/individual/n1f9d4ddc", "test_relationship_custom_action_name"));

        return requests;
    }

    public static List<Object[]> requests(String resourceName, String individualUri, String customRestActionName) {
        // would be ideal if sparql made actual call during integration tests
        String sparqlResFile = "sparql/response/json/sparql-empty-success.json";

        // TODO: create appropriate request body, request params, and response body files when applicable
        // String restReqParamsFile = format("%s.json", resourceName);
        // String restReqBodyFile = format("%s.json", resourceName);
        // String restResBodyFile = format("%s.json", resourceName);
        String restReqParamsFile = "test_collection_resource.json";
        String restReqBodyFile = "test_collection_resource.json";
        String restResBodyFile = "test_collection_resource.json";

        String resourcePath = format("/1/%s", resourceName);

        String individualResourcePath = format("%s/resource:%s", resourcePath, new String(Base64.getEncoder().encode(individualUri.getBytes())));

        String customRestActionPath = format("%s/%s", individualResourcePath, customRestActionName);

        return new ArrayList<>(Arrays.asList(new Object[][] {
            { "POST",   resourcePath,           restReqParamsFile, restReqBodyFile, sparqlResFile, SC_OK,                 restResBodyFile, "Create collection resource" },
            { "GET",    resourcePath,           restReqParamsFile, null,            sparqlResFile, SC_OK,                 restResBodyFile, "Get collection resources" },
            { "PUT",    resourcePath,           restReqParamsFile, null,            null,          SC_METHOD_NOT_ALLOWED, null,            "Update not allowed on collection" },
            { "PATCH",  resourcePath,           restReqParamsFile, null,            null,          SC_METHOD_NOT_ALLOWED, null,            "Patch not allowed on collection" },
            { "DELETE", resourcePath,           restReqParamsFile, null,            null,          SC_METHOD_NOT_ALLOWED, null,            "Delete not allowed on collection" },

            { "POST",   individualResourcePath, restReqParamsFile, null,            null,          SC_METHOD_NOT_ALLOWED, null,            "Create not allowed on individualt resource" },
            { "GET",    individualResourcePath, restReqParamsFile, null,            sparqlResFile, SC_OK,                 restResBodyFile, "Get individual resource" },
            { "PUT",    individualResourcePath, restReqParamsFile, restReqBodyFile, sparqlResFile, SC_OK,                 restResBodyFile, "Update individual resource" },
            { "PATCH",  individualResourcePath, restReqParamsFile, restReqBodyFile, sparqlResFile, SC_OK,                 restResBodyFile, "Patch individual resource" },
            { "DELETE", individualResourcePath, restReqParamsFile, null,            sparqlResFile, SC_OK,                 restResBodyFile, "Delete individual resource" },

            { "POST",   customRestActionPath,   restReqParamsFile, null,            null,          SC_METHOD_NOT_ALLOWED, null,            "Method unsupported by Custom REST action" },
            { "GET",    customRestActionPath,   restReqParamsFile, null,            null,          SC_METHOD_NOT_ALLOWED, null,            "Method unsupported by Custom REST action" },
            { "PUT",    customRestActionPath,   restReqParamsFile, restReqBodyFile, sparqlResFile, SC_OK,                 restResBodyFile, "Run Custom REST action" },
            { "PATCH",  customRestActionPath,   restReqParamsFile, null,            null,          SC_METHOD_NOT_ALLOWED, null,            "Method unsupported by Custom REST action" },
            { "DELETE", customRestActionPath,   restReqParamsFile, null,            null,          SC_METHOD_NOT_ALLOWED, null,            "Method unsupported by Custom REST action" }
        }));
    }

}
