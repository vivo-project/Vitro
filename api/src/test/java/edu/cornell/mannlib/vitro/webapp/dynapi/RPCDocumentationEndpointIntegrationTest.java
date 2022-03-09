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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import edu.cornell.mannlib.vitro.webapp.dynapi.matcher.APIResponseMatcher;

@RunWith(Parameterized.class)
public class RPCDocumentationEndpointIntegrationTest extends ServletContextITest {

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
        rpcEndpoint = new RPCDocumentationEndpoint();

        loadDefaultModel();
        loadTestModel();

        loadModels("n3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-api.n3");

        ActionPool actionPool = ActionPool.getInstance();

        actionPool.init(servletContext);
        actionPool.reload();

        DynamicAPIDocumentation dynamicAPIDocumentation = DynamicAPIDocumentation.getInstance();

        dynamicAPIDocumentation.init(servletContext);

        MockitoAnnotations.openMocks(this);
    }

    @After
    public void afterEach() {

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

        when(request.getServletPath()).thenReturn("/docs/rpc");
        when(request.getPathInfo()).thenReturn(pathInfo);
        when(request.getHeader("Accept")).thenReturn(mimeType);
        when(request.getContentType()).thenReturn(mimeType);
        when(response.getWriter()).thenReturn(responsePrintWriter);

        System.out.println("\n\nRunning Test against: '/docs/rpc" + pathInfo + "'.\n");

        rpcEndpoint.doGet(request, response);

        verify(response, times(1)).setContentType(mimeType);
        verify(responsePrintWriter, times(1)).print(argThat(new APIResponseMatcher(testJson, testExpectedResponse)));
        verify(responsePrintWriter, times(1)).flush();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        final String action = "test_action";
        final String collection = "test_collection";
        final String concept = "test_concept";
        final String document = "test_document";
        final String organization = "test_organization";
        final String person = "test_person";
        final String process = "test_process";
        final String relationship = "test_relationship";

        return Arrays.asList(new Object[][] {
            { null,         false, "rpc/yaml/all.yml",           "All" },
            { action,       false, "rpc/yaml/action.yml",        action },
            { collection,   false, "rpc/yaml/collection.yml",    collection },
            { concept,      false, "rpc/yaml/concept.yml",       concept },
            { document,     false, "rpc/yaml/document.yml",      document },
            { organization, false, "rpc/yaml/organization.yml",  organization },
            { person,       false, "rpc/yaml/person.yml",        person },
            { process,      false, "rpc/yaml/process.yml",       process },
            { relationship, false, "rpc/yaml/relationship.yml",  relationship },
            { null,         true,  "rpc/json/all.json",          "All" },
            { action,       true,  "rpc/json/action.json",       action },
            { collection,   true,  "rpc/json/collection.json",   collection },
            { concept,      true,  "rpc/json/concept.json",      concept },
            { document,     true,  "rpc/json/document.json",     document },
            { organization, true,  "rpc/json/organization.json", organization },
            { person,       true,  "rpc/json/person.json",       person },
            { process,      true,  "rpc/json/process.json",      process },
            { relationship, true,  "rpc/json/relationship.json", relationship },
        });
    }

}
