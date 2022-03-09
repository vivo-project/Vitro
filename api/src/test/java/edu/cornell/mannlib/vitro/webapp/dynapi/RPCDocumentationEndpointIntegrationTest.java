package edu.cornell.mannlib.vitro.webapp.dynapi;

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
    public Boolean testJsonMimeType;

    @Parameter(2)
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

        if (testJsonMimeType == true) {
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

        // Needs to compare against a pre-built expected response body.
        // verify(responsePrintWriter, times(1)).print(expectedReponseBody);
        verify(responsePrintWriter, times(1)).flush();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        final String action = "test_collection";

        return Arrays.asList(new Object[][] {
            // action, json,  message
            { null,    false, "All, Version 1" },
            { null,    false, "All, Version 2.1.0" },
            { action,  false, action + ", Version 1" },
            { action,  false, action + ", Version 2.1.0" },
            { null,    true,  "All, Version 1" },
            { null,    true,  "All, Version 2.1.0" },
            { action,  true,  action + ", Version 1" },
            { action,  true,  action + ", Version 2.1.0" },
        });
    }

}
