package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath.RPC_SERVLET_PATH;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.DynapiModelFactory;

@RunWith(Parameterized.class)
public class RPCEndpointIntegrationTest extends ServletContextIntegrationTest {

    private final static String URI_BASE = "http://localhost:8080" + RPC_SERVLET_PATH;

    private static MockedStatic<DynapiModelFactory> dynapiModelFactory;

    private ByteArrayOutputStream baos;
    
    private RPCEndpoint rpcEndpoint;

    private ActionPool actionPool;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private UserAccount user;

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
        actionPool = ActionPool.getInstance();

        rpcEndpoint = new RPCEndpoint();

        loadDefaultModel();

        actionPool.init(servletContext);
        actionPool.reload();

        MockitoAnnotations.openMocks(this);
        
        baos = new ByteArrayOutputStream();
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ContentType.APPLICATION_JSON.toString());
        PrintWriter writer = new PrintWriter(baos, true);
        when(response.getWriter()).thenReturn(writer);

        when(request.getServletContext()).thenReturn(servletContext);
        when(request.getServletPath()).thenReturn(RPC_SERVLET_PATH);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(user.isRootUser()).thenReturn(true);
        dynapiModelFactory.when(() -> DynapiModelFactory.getModel(any(String.class))).thenReturn(ontModel);
        
        if (testAction != null) {
            StringBuffer buffer = new StringBuffer(URI_BASE + "/" + testAction);
            when(request.getRequestURL()).thenReturn(buffer);
            when(request.getPathInfo()).thenReturn("/" + testAction);
        }

        when(request.getParameterMap()).thenReturn(parameterMap);
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ContentType.APPLICATION_JSON.toString());
        when(response.getWriter()).thenReturn(new PrintWriter(System.out));
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
        int br = SC_BAD_REQUEST;
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
