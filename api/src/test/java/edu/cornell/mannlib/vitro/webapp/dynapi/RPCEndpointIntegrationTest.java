package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath.RPC_SERVLET_PATH;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
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
public class RPCEndpointIntegrationTest extends ServletContextIntegrationTest {

    private final static String URI_BASE = "http://localhost:8080" + RPC_SERVLET_PATH;

    private RPCEndpoint rpcEndpoint;

    private ActionPool actionPool;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

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
        actionPool = ActionPool.getInstance();

        rpcEndpoint = new RPCEndpoint();

        loadDefaultModel();

        actionPool.init(servletContext);
        actionPool.reload();

        MockitoAnnotations.openMocks(this);

        when(request.getServletContext()).thenReturn(servletContext);
        when(request.getServletPath()).thenReturn(RPC_SERVLET_PATH);

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

        return Arrays.asList(new Object[][] {
            // action          limit         email         status before after   testMessage
            { null,            null,         null,         nf,    null,  null,   "NULL Request" },
            { actionIsEmpty,   null,         null,         nf,    null,  null,   "Empty Action" },
            { actionIsUnknown, null,         null,         se,    null,  null,   "Unknown Action" },
            { actionIsGood,    null,         null,         se,    null,  null,   "NULL Limit" },
            { actionIsGood,    limitIsEmpty, null,         se,    null,  null,   "Empty Limit" },
            { actionIsGood,    limitIsBad,   null,         se,    null,  null,   "Bad Limit" },
            { actionIsGood,    limitIsGood,  null,         se,    null,  null,   "NULL E-mail" },
            { actionIsGood,    limitIsGood,  emailIsEmpty, se,    null,  null,   "Empty E-mail" },
            { actionIsGood,    limitIsGood,  emailIsBad,   se,    null,  null,   "Bad E-mail" },
            { actionIsGood,    limitIsGood,  emailIsGood,  ok,    null,  null,   "Valid Request" },
        });
    }
}
