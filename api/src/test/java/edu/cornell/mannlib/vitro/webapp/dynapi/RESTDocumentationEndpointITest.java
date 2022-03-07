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
public class RESTDocumentationEndpointITest extends ServletContextITest {

    private RESTDocumentationEndpoint restEndpoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Parameter(0)
    public String test;

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
    public void doTest() {

        when(request.getServletPath()).thenReturn("/docs/rest");
        when(request.getPathInfo()).thenReturn("/1");

        restEndpoint.doGet(request, response);
    }

    @Parameterized.Parameters
    public static Collection<String> requests() {
        return new ArrayList<>(Arrays.asList(new String[] {
            "test"
        }));
    }

}
