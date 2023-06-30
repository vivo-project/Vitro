package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.RequestPath.RPC_SERVLET_PATH;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.mockito.MockedStatic;

@RunWith(Parameterized.class)
public class RPCEndpointITest extends ServletContextITest {

    private final static String URI_BASE = "http://localhost:8080" + RPC_SERVLET_PATH;

    private RPCEndpoint rpcEndpoint;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private MockedStatic<ActionPool> actionPoolStatic;

    private MockedStatic<QueryExecutionFactory> queryExecutionFactoryStatic;

    private ActionPool actionPool;

    private QueryExecution queryExecution;

    @Parameter(0)
    public String testAction;

    @Parameter(1)
    public String testLimit;

    @Parameter(2)
    public String testEmail;

    @Parameter(3)
    public Integer testStatus;

    @Parameter(4)
    public Method testBefore;

    @Parameter(5)
    public Method testAfter;

    @Parameter(6)
    public String testMessage;

    @Before
    public void beforeEach() throws Exception {
        queryExecution = mock(QueryExecution.class);
        actionPool = new ActionPool();

        actionPoolStatic = mockStatic(ActionPool.class);
        when(ActionPool.getInstance()).thenReturn(actionPool);

        queryExecutionFactoryStatic = mockStatic(QueryExecutionFactory.class);
        when(QueryExecutionFactory.create(any(String.class), any(Model.class))).thenReturn(queryExecution);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        rpcEndpoint = new RPCEndpoint();

        loadDefaultModel();

        when(request.getServletContext()).thenReturn(servletContext);
        when(request.getServletPath()).thenReturn(RPC_SERVLET_PATH);

        actionPool.init(request.getServletContext());
        actionPool.reload();

        if (testAction != null) {
            StringBuffer buffer = new StringBuffer(URI_BASE + "/" + testAction);
            when(request.getRequestURL()).thenReturn(buffer);
            when(request.getPathInfo()).thenReturn("/" + testAction);
        }

        when(request.getParameterMap()).thenReturn(parameterMap);

        mockParameterIntoMap("limit", testLimit);
        mockParameterIntoMap("email", testEmail);
        mockStatus(response);
        runCallback(testBefore);
    }

    @After
    public void afterEach() throws Exception {
        runCallback(testAfter);

        actionPoolStatic.close();
        queryExecutionFactoryStatic.close();
    }

    @Test
    public void doGetTest() {
        rpcEndpoint.doGet(request, response);

        // For all permutations, this should return HTTP 405.
        assertResponseStatus(SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void doPostTest() {
        rpcEndpoint.doPost(request, response);
        assertResponseStatus(testStatus);
    }

    @Test
    public void doDeleteTest() {
        rpcEndpoint.doDelete(request, response);

        // For all permutations, this should return HTTP 405.
        assertResponseStatus(SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void doPutTest() {
        rpcEndpoint.doPut(request, response);

        // For all permutations, this should return HTTP 405.
        assertResponseStatus(SC_METHOD_NOT_ALLOWED);
    }

    private void assertResponseStatus(int status) {
        assertEquals("Invalid Status for test: " + testMessage, status, response.getStatus());
    }

    /**
     * Prevent SPARQL from actually running by returning a mocked response.
     * @throws IOException
     */
    protected void mockSparqlResponseEmptySuccess() throws IOException {
        InputStream stream = readMockFileAsInputStream("sparql/response/json/sparql-empty-success.json");
        when(queryExecution.execSelect()).thenReturn(ResultSetFactory.fromJSON(stream));
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() throws MalformedURLException, NoSuchMethodException, SecurityException {
        int nf = SC_NOT_FOUND;
        int se = SC_INTERNAL_SERVER_ERROR;
        int ok = SC_OK;

        String actionIsEmpty = "";
        String actionIsUnknown = "unknown";
        String actionIsGood = TEST_ACTION_NAME;
        String limitIsEmpty = "";
        String limitIsBad = "-1";
        String limitIsGood = "10";
        String emailIsEmpty = "";
        String emailIsBad = "a";
        String emailIsGood = "example@localhost";

        Method[] before = new Method[] {
            RPCEndpointITest.class.getDeclaredMethod("mockSparqlResponseEmptySuccess")
        };

        return Arrays.asList(new Object[][] {
            // action          limit         email         status before       after   testMessage
            { null,            null,         null,         nf,    null,        null,   "NULL Request" },
            { actionIsEmpty,   null,         null,         nf,    null,        null,   "Empty Action" },
            { actionIsUnknown, null,         null,         se,    null,        null,   "Unknown Action" },
            { actionIsGood,    null,         null,         se,    null,        null,   "NULL Limit" },
            { actionIsGood,    limitIsEmpty, null,         se,    null,        null,   "Empty Limit" },
            { actionIsGood,    limitIsBad,   null,         se,    null,        null,   "Bad Limit" },
            { actionIsGood,    limitIsGood,  null,         se,    null,        null,   "NULL E-mail" },
            { actionIsGood,    limitIsGood,  emailIsEmpty, se,    null,        null,   "Empty E-mail" },
            { actionIsGood,    limitIsGood,  emailIsBad,   se,    null,        null,   "Bad E-mail" },
            { actionIsGood,    limitIsGood,  emailIsGood,  ok,    before[0],   null,   "Valid Request" },
        });
    }
}
