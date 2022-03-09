package edu.cornell.mannlib.vitro.webapp.dynapi;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
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
public class RPCDocumentationEndpointITest extends ServletContextITest {

    private RPCDocumentationEndpoint rpcEndpoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Parameter(0)
    public String testVersion;

    @Parameter(1)
    public String testResource;

    @Parameter(2)
    public String testMessage;

    @Before
    public void beforeEach() throws IOException {
        rpcEndpoint = new RPCDocumentationEndpoint();

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
    public void doTest() {
        String pathInfo = "/" + testVersion;

        if (testResource != null) {
            pathInfo += "/" + testResource;
        }

        when(request.getServletPath()).thenReturn("/docs/rpc");
        when(request.getPathInfo()).thenReturn(pathInfo);

        System.out.println("\n\nRunning Test against: '/docs/rpc" + pathInfo + "'.\n");

        rpcEndpoint.doGet(request, response);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        final String collection = "test_collection_resource";

        return Arrays.asList(new Object[][] {
            // version resource,   message
            { "1",     null,       "All, Version 1" },
            { "2.1.0", null,       "All, Version 2.1.0" },
            { "1",     collection, collection + ", Version 1" },
            { "2.1.0", collection, collection + ", Version 2.1.0" },
        });
    }

}
