/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath.RPC_SERVLET_PATH;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionMethod;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.FormDataConverter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.DynapiModelFactory;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DefaultFormat;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

@RunWith(Parameterized.class)
public class RPCEndpointIntegrationTest extends AbstractIntegrationTest {

    private final static String URI_BASE = "http://localhost:8080" + RPC_SERVLET_PATH;

    private static MockedStatic<DynapiModelFactory> dynapiModelFactory;

    private ByteArrayOutputStream baos;

    private RPCEndpoint rpcEndpoint;

    private ProcedurePool procedurePool;
    private RPCPool rpcPool;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private UserAccount user;

    @Parameter(0)
    public String testProcedureName;

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

    @BeforeClass
    public static void setupStaticObjects() {
        dynapiModelFactory = mockStatic(DynapiModelFactory.class);
    }

    @AfterClass
    public static void after() {
        dynapiModelFactory.close();
    }

    @Before
    public void beforeEach() throws Exception {
        offLogs();
        procedurePool = ProcedurePool.getInstance();
        rpcPool = RPCPool.getInstance();

        rpcEndpoint = new RPCEndpoint();

        loadDefaultModel();

        procedurePool.init();
        procedurePool.reload();

        rpcPool.init();
        rpcPool.reload();

        MockitoAnnotations.openMocks(this);

        baos = new ByteArrayOutputStream();
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ContentType.APPLICATION_JSON.toString());
        PrintWriter writer = new PrintWriter(baos, true);
        when(response.getWriter()).thenReturn(writer);

        when(request.getServletPath()).thenReturn(RPC_SERVLET_PATH);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(user.isRootUser()).thenReturn(true);
        dynapiModelFactory.when(() -> DynapiModelFactory.getModel(any(String.class))).thenReturn(ontModel);

        if (testProcedureName != null) {
            StringBuffer buffer = new StringBuffer(URI_BASE + "/" + testProcedureName);
            when(request.getRequestURL()).thenReturn(buffer);
            when(request.getPathInfo()).thenReturn("/" + testProcedureName);
        }

        when(request.getParameterMap()).thenReturn(parameterMap);
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ContentType.APPLICATION_JSON.toString());
        final StringWriter out = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(out));
        mockParameterIntoMap("limit", testLimit);
        mockParameterIntoMap("email", testEmail);
        mockStatus(response);
        runCallback(testBefore);
    }

    @After
    public void afterEach() throws Exception {
        runCallback(testAfter);
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        restoreLogs();
    }

    public static void offLogs() {
        LoggingControl.offLogs();
        LoggingControl.offLog(RPCEndpoint.class);
        LoggingControl.offLog(Endpoint.class);
        LoggingControl.offLog(FormDataConverter.class);
        LoggingControl.offLog(ConversionMethod.class);
        LoggingControl.offLog(DefaultFormat.class);
    }

    public static void restoreLogs() {
        LoggingControl.restoreLogs();
        LoggingControl.restoreLog(RPCEndpoint.class);
        LoggingControl.restoreLog(Endpoint.class);
        LoggingControl.restoreLog(FormDataConverter.class);
        LoggingControl.restoreLog(ConversionMethod.class);
        LoggingControl.restoreLog(DefaultFormat.class);
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
    public static Collection<Object[]> requests()
            throws MalformedURLException, NoSuchMethodException, SecurityException {
        int nf = SC_NOT_FOUND;
        int se = SC_INTERNAL_SERVER_ERROR;
        int br = SC_BAD_REQUEST;
        int ok = SC_OK;

        String actionIsEmpty = "";
        String actionIsUnknown = "unknown";
        String actionIsGood = "test_action";
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
            { actionIsUnknown, null,         null,         br,    null,  null,   "Unknown Action" },
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
