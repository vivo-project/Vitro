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
public class RESTDocumentationEndpointIntegrationTest extends ServletContextITest {

    private RESTDocumentationEndpoint restEndpoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private PrintWriter responsePrintWriter;

    @Parameter(0)
    public String testVersion;

    @Parameter(1)
    public String testResource;

    @Parameter(2)
    public Boolean testJson;

    @Parameter(3)
    public String testExpectedResponse;

    @Parameter(4)
    public String testMessage;

    @Before
    public void beforeEach() throws IOException {
        restEndpoint = new RESTDocumentationEndpoint();

        loadDefaultModel();
        loadTestModel();

        loadModels("n3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-api.n3");

        ActionPool actionPool = ActionPool.getInstance();
        ResourceAPIPool resourceAPIPool = ResourceAPIPool.getInstance();

        actionPool.init(servletContext);
        actionPool.reload();

        resourceAPIPool.init(servletContext);
        resourceAPIPool.reload();

        DynamicAPIDocumentation dynamicAPIDocumentation = DynamicAPIDocumentation.getInstance();

        dynamicAPIDocumentation.init(servletContext);

        MockitoAnnotations.openMocks(this);
    }

    @After
    public void afterEach() {

    }

    @Test
    public void doTest() throws IOException {
        String pathInfo = "/" + testVersion;
        String mimeType = "application/yaml";

        if (testResource != null) {
            pathInfo += "/" + testResource;
        }

        if (testJson == true) {
            mimeType = "application/json";
        }

        when(request.getServletPath()).thenReturn("/docs/rest");
        when(request.getPathInfo()).thenReturn(pathInfo);
        when(request.getHeader("Accept")).thenReturn(mimeType);
        when(request.getContentType()).thenReturn(mimeType);
        when(response.getWriter()).thenReturn(responsePrintWriter);

        System.out.println("\n\nRunning Test against: '/docs/rest" + pathInfo + "'.\n");

        restEndpoint.doGet(request, response);

        verify(response, times(1)).setContentType(mimeType);
        //verify(responsePrintWriter, times(1)).print(argThat(new APIResponseMatcher(testJson, testExpectedResponse)));
        verify(responsePrintWriter, times(1)).flush();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        final String collection = "test_collection_resource";

        return Arrays.asList(new Object[][] {
            // version resource,   json,  expectedResponse, message
            { "1",     null,       false, null,             "All, Version 1" },
            { "2.1.0", null,       false, null,             "All, Version 2.1.0" },
            { "1",     collection, false, null,             collection + ", Version 1" },
            { "2.1.0", collection, false, null,             collection + ", Version 2.1.0" },
            { "1",     null,       true,  null,             "All, Version 1" },
            { "2.1.0", null,       true,  null,             "All, Version 2.1.0" },
            { "1",     collection, true,  null,             collection + ", Version 1" },
            { "2.1.0", collection, true,  null,             collection + ", Version 2.1.0" }
        });
    }

}
