package edu.cornell.mannlib.vitro.webapp.dynapi;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import edu.cornell.mannlib.vitro.webapp.dynapi.matcher.APIResponseMatcher;

@RunWith(Parameterized.class)
public class RPCDocumentationEndpointIntegrationTest extends ServletContextIntegrationTest {

    private RPCDocumentationEndpoint rpcEndpoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private PrintWriter responsePrintWriter;

    @Parameter(0)
    public String testAction;

    @Parameter(1)
    public Boolean testJson;

    @Parameter(2)
    public String testExpectedResponse;

    @Parameter(3)
    public String testMessage;

    @Before
    public void beforeEach() throws IOException {
        LoggingControl.offLogs();
        rpcEndpoint = new RPCDocumentationEndpoint();

        loadDefaultModel();
        loadTestModel();

        loadModels("n3", TEST_PREFIX + "dynamic-api-individuals-api.n3");

        ProcedurePool procedurePool = ProcedurePool.getInstance();

        procedurePool.init(servletContext);
        procedurePool.reload();
        
        RPCPool rpcPool = RPCPool.getInstance();

        rpcPool.init(servletContext);
        rpcPool.reload();

        MockitoAnnotations.openMocks(this);
    }

    @After
    public void after() {
        LoggingControl.restoreLogs();
    }
    
    @Test
    public void doTest() throws IOException {
        String pathInfo = "";
        String mimeType = "application/yaml";

        if (testAction != null) {
            pathInfo += "/" + testAction;
        }

        if (testJson == true) {
            mimeType = "application/json";
        }

        when(request.getServletPath()).thenReturn("/api/docs/rpc");
        when(request.getServerName()).thenReturn("127.0.0.1");
        when(request.getScheme()).thenReturn("http");
        when(request.getServerPort()).thenReturn(8080);
        when(request.getContextPath()).thenReturn("/vitro");
        when(request.getPathInfo()).thenReturn(pathInfo);
        when(request.getHeader("Accept")).thenReturn(mimeType);
        when(request.getContentType()).thenReturn(mimeType);
        when(response.getWriter()).thenReturn(responsePrintWriter);

        System.out.println("Running Test against: '/api/docs/rpc" + pathInfo + "'.");

        rpcEndpoint.doGet(request, response);

        verify(response, times(1)).setContentType(mimeType);
        verify(responsePrintWriter, times(1)).print(argThat(new APIResponseMatcher(testJson, testExpectedResponse)));
        verify(responsePrintWriter, times(1)).flush();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        final String action = "test_action";
        final String actionUri = "https://vivoweb.org/ontology/vitro-dynamic-api/procedure/testProcedure1";
        final String collection = "test_collection";
        final String collectionUri = "https://vivoweb.org/ontology/vitro-dynamic-api/procedure/testCollectionProcedure1";
        final String concept = "test_concept";
        final String conceptUri = "https://vivoweb.org/ontology/vitro-dynamic-api/procedure/testConceptProcedure1";
        final String document = "test_document";
        final String documentUri = "https://vivoweb.org/ontology/vitro-dynamic-api/procedure/testDocumentProcedure1";
        final String organization = "test_organization";
        final String organizationUri = "https://vivoweb.org/ontology/vitro-dynamic-api/procedure/testOrganizationProcedure1";
        final String person = "test_person";
        final String personUri = "https://vivoweb.org/ontology/vitro-dynamic-api/procedure/testPersonProcedure1";
        final String process = "test_process";
        final String processUri = "https://vivoweb.org/ontology/vitro-dynamic-api/procedure/testProcessProcedure1";
        final String relationship = "test_relationship";
        final String relationshipUri = "https://vivoweb.org/ontology/vitro-dynamic-api/procedure/testRelationshipProcedure1";


        return Arrays.asList(new Object[][] {
            // action,      json,  expected response,     message
            { null,         false, "rpc/all",             "All with yaml" },
            { action,       false, "rpc/" + action,       action + " with yaml" },
            { collection,   false, "rpc/" + collection,   collection + " with yaml" },
            { concept,      false, "rpc/" + concept,      concept + " with yaml" },
            { document,     false, "rpc/" + document,     document + " with yaml" },
            { organization, false, "rpc/" + organization, organization + " with yaml" },
            { person,       false, "rpc/" + person,       person + " with yaml" },
            { process,      false, "rpc/" + process,      process + " with yaml" },
            { relationship, false, "rpc/" + relationship, relationship + " with yaml" },

            // action,      json,  expected response,     message
            { null,         true,  "rpc/all",             "All with json" },
            { action,       true,  "rpc/" + action,       action + " with json" },
            { collection,   true,  "rpc/" + collection,   collection + " with json" },
            { concept,      true,  "rpc/" + concept,      concept + " with json" },
            { document,     true,  "rpc/" + document,     document + " with json" },
            { organization, true,  "rpc/" + organization, organization + " with json" },
            { person,       true,  "rpc/" + person,       person + " with json" },
            { process,      true,  "rpc/" + process,      process + " with json" },
            { relationship, true,  "rpc/" + relationship, relationship + " with json" },
        });
    }

}
