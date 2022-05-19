package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath.REST_SERVLET_PATH;
import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.HTTPMethod;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.RPC;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPI;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPIKey;

@RunWith(Parameterized.class)
public class RESTEndpointTest {

    private final static String PATH_INFO = "/1/test";

    private Map<String, String[]> params = new HashMap<>();

    private MockedStatic<ResourceAPIPool> resourceAPIPoolStatic;

    private MockedStatic<ActionPool> actionPoolStatic;

    @Mock
    private ServletContext context;

    @Mock
    private ResourceAPIPool resourceAPIPool;

    @Mock
    private ActionPool actionPool;

    @Mock
    private ResourceAPI resourceAPI;

    @Mock
    private RPC rpc;

    @Mock
    private HTTPMethod httpMethod;

    @Spy
    private Action action;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private RESTEndpoint restEndpoint;

    @Parameter(0)
    public String testMethod;

    @Parameter(1)
    public String testPathInfo;

    @Parameter(2)
    public String testActionName;

    @Parameter(3)
    public int[] testExpectedCounts;

    @Parameter(4)
    public int testExpectedStatus;

    @Parameter(5)
    public String testMessage;

    @Before
    public void beforeEach() {
        MockitoAnnotations.openMocks(this);
        resourceAPIPoolStatic = mockStatic(ResourceAPIPool.class);
        actionPoolStatic = mockStatic(ActionPool.class);

        when(ResourceAPIPool.getInstance()).thenReturn(resourceAPIPool);
        when(resourceAPIPool.get(any(ResourceAPIKey.class))).thenReturn(resourceAPI);

        when(ActionPool.getInstance()).thenReturn(actionPool);
        when(actionPool.get(any(String.class))).thenReturn(action);

        when(request.getParameterMap()).thenReturn(params);
        when(request.getServletContext()).thenReturn(context);

        restEndpoint = new RESTEndpoint();
    }

    @After
    public void afterEach() {
        resourceAPIPoolStatic.close();
        actionPoolStatic.close();
    }

    @Test
    public void doTest() {
        when(request.getServletPath()).thenReturn(REST_SERVLET_PATH);
        when(request.getMethod()).thenReturn(testMethod);
        when(request.getPathInfo()).thenReturn(testPathInfo);

        when(action.run(any(OperationData.class)))
            .thenReturn(new OperationResult(testExpectedStatus));

        when(httpMethod.getName()).thenReturn(testMethod);

        when(rpc.getName()).thenReturn(testActionName);
        when(rpc.getHttpMethod()).thenReturn(httpMethod);

        when(resourceAPI.getRestRPC(testMethod)).thenReturn(rpc);
        when(resourceAPI.getCustomRestActionRPC(testActionName)).thenReturn(rpc);
        doNothing().when(resourceAPI).removeClient();

        run(testMethod);

        verify(resourceAPI, times(testExpectedCounts[0])).getRestRPC(any());
        verify(resourceAPI, times(testExpectedCounts[1])).getCustomRestActionRPC(any());
        verify(resourceAPI, times(testExpectedCounts[2])).removeClient();
        verify(action, times(testExpectedCounts[3])).run(any());
        verify(action, times(testExpectedCounts[4])).removeClient();
        verify(response, times(testExpectedCounts[5])).setStatus(testExpectedStatus);
    }

    private void run(String method) {
        switch (method) {
            case "POST":
                restEndpoint.doPost(request, response);
                break;
            case "GET":
                restEndpoint.doGet(request, response);
                break;
            case "PUT":
                restEndpoint.doPut(request, response);
                break;
            case "PATCH":
                restEndpoint.doPatch(request, response);
                break;
            case "DELETE":
                restEndpoint.doDelete(request, response);
                break;
            default:
                break;
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        String actionName = "test";
        String customRestActionPathInfo = format("%s/%s", PATH_INFO, actionName);

        return Arrays.asList(new Object[][] {
            // expected counts key
            // resource.getRestRPC, resource.getCustomRestActionRPC, resource.removeClient, action.run, action.removeClient, response.setStatus

            // method   path info                 action      expected counts                 expected status        message
            { "POST",   PATH_INFO,                actionName, new int[] { 1, 0, 1, 1, 1, 1 }, SC_OK,                 "Create collection resource" },
            { "GET",    PATH_INFO,                actionName, new int[] { 1, 0, 1, 1, 1, 1 }, SC_OK,                 "Get collection resources" },
            { "PUT",    PATH_INFO,                actionName, new int[] { 0, 0, 0, 0, 0, 1 }, SC_METHOD_NOT_ALLOWED, "Cannot put on resource collecion" },
            { "PATCH",  PATH_INFO,                actionName, new int[] { 0, 0, 0, 0, 0, 1 }, SC_METHOD_NOT_ALLOWED, "Cannot patch on resource collection" },
            { "DELETE", PATH_INFO,                actionName, new int[] { 0, 0, 0, 0, 0, 1 }, SC_METHOD_NOT_ALLOWED, "Cannot delete on resource collection" },

            { "POST",   customRestActionPathInfo, actionName, new int[] { 0, 1, 1, 1, 1, 1 }, SC_OK,                 "Resource found with supported method" },
            { "GET",    customRestActionPathInfo, actionName, new int[] { 0, 1, 1, 1, 1, 1 }, SC_METHOD_NOT_ALLOWED, "Resource found with unsupported method" },
            { "PUT",    customRestActionPathInfo, actionName, new int[] { 0, 0, 0, 0, 0, 1 }, SC_METHOD_NOT_ALLOWED, "Method unsupported by custom REST action" },
            { "PATCH",  customRestActionPathInfo, actionName, new int[] { 0, 0, 0, 0, 0, 1 }, SC_METHOD_NOT_ALLOWED, "Method unsupported by custom REST action" },
            { "DELETE", customRestActionPathInfo, actionName, new int[] { 0, 0, 0, 0, 0, 1 }, SC_METHOD_NOT_ALLOWED, "Method unsupported by custom REST action" }
            
        });
    }

}
