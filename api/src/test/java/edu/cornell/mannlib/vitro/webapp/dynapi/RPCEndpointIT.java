package edu.cornell.mannlib.vitro.webapp.dynapi;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_NOT_IMPLEMENTED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.MockedStatic;

import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpServletResponseStub;

@RunWith(Parameterized.class)
public class RPCEndpointIT extends ServletContextTest {

    private final static String URI_BASE = "http://localhost/api/rpc/";

    private RPCEndpoint rpcEndpoint;

    private HttpServletRequestStub request;

    private HttpServletResponseStub response;

    private MockedStatic<ActionPool> actionPoolStatic;

    private MockedStatic<QueryExecutionFactory> queryExecutionFactoryStatic;

    private ActionPool actionPool;

    private QueryExecution queryExecution;

    private String testAction;
    private String testLimit;
    private String testEmail;
    private Integer testStatus;
    private String testMessage;

    public RPCEndpointIT(String testAction, String testLimit, String testEmail, Integer testStatus, String testMessage) {
        this.testAction = testAction;
        this.testLimit = testLimit;
        this.testEmail = testEmail;
        this.testStatus = testStatus;
        this.testMessage = testMessage;
    }

    @Before
    public void beforeEach() throws IOException {
        queryExecution = mock(QueryExecution.class);
        actionPool = spy(new ActionPool());

        actionPoolStatic = mockStatic(ActionPool.class);
        when(ActionPool.getInstance()).thenReturn(actionPool);

        queryExecutionFactoryStatic = mockStatic(QueryExecutionFactory.class);
        when(QueryExecutionFactory.create(any(String.class), any(Model.class))).thenReturn(queryExecution);

        request = new HttpServletRequestStub();
        response = new HttpServletResponseStub();
        rpcEndpoint = new RPCEndpoint();

        loadDefaultModel();

        request.setServletContext(servletContext);

        actionPool.init(request.getServletContext());
        actionPool.reload();

        if (testAction != null) {
            request.setRequestUrl(new URL(URI_BASE + testAction));
            request.setPathInfo("/" + testAction);
        }

        if (testLimit != null) {
            request.addParameter("limit", testLimit.toString());
        }

        if (testEmail != null) {
            request.addParameter("email", testEmail.toString());
        }
    }

    @After
    public void afterEach() {
        actionPoolStatic.close();
        queryExecutionFactoryStatic.close();
    }

    @Test
    public void doGetTest() throws MalformedURLException {
        rpcEndpoint.doGet(request, response);

        // For all permutations, this should return HTTP 501.
        assertResponseStatus(SC_NOT_IMPLEMENTED);
    }

    @Test
    public void doPostTest() throws MalformedURLException {

        // Prevent SPARQL from actually running by returning a mocked response.
        String json = "{ \"head\": { \"vars\": [ \"title\" ] }, \"results\": { \"bindings\": [ ] } }";
        InputStream stream = new ByteArrayInputStream(json.getBytes());
        when(queryExecution.execSelect()).thenReturn(ResultSetFactory.fromJSON(stream));

        rpcEndpoint.doPost(request, response);
        assertResponseStatus(testStatus);
    }

    @Test
    public void doDeleteTest() throws MalformedURLException {
        rpcEndpoint.doDelete(request, response);

        // For all permutations, this should return HTTP 501.
        assertResponseStatus(SC_NOT_IMPLEMENTED);
    }

    @Test
    public void doPutTest() throws MalformedURLException {
        rpcEndpoint.doPut(request, response);

        // For all permutations, this should return HTTP 501.
        assertResponseStatus(SC_NOT_IMPLEMENTED);
    }

    private void assertResponseStatus(int status) {
        assertEquals("Invalid Status for test: " + testMessage, status, response.getStatus());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() throws MalformedURLException {
        int nf = SC_NOT_FOUND;
        int se = SC_INTERNAL_SERVER_ERROR;
        int ni = SC_NOT_IMPLEMENTED;
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

        return Arrays.asList(new Object[][] {
            { null,            null,         null,         nf, "NULL Request" },
            { actionIsEmpty,   null,         null,         nf, "Empty Action" },
            { actionIsUnknown, null,         null,         ni, "Unknown Action" },
            { actionIsGood,    null,         null,         se, "NULL Limit" },
            { actionIsGood,    limitIsEmpty, null,         se, "Empty Limit" },
            { actionIsGood,    limitIsBad,   null,         se, "Bad Limit" },
            { actionIsGood,    limitIsGood,  null,         se, "NULL E-mail" },
            { actionIsGood,    limitIsGood,  emailIsEmpty, se, "Empty E-mail" },
            { actionIsGood,    limitIsGood,  emailIsBad,   se, "Bad E-mail" },
            { actionIsGood,    limitIsGood,  emailIsGood,  ok, "Valid Request" },
        });
    }
}
