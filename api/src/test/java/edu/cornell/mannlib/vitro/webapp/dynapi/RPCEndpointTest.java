package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath.RPC_SERVLET_PATH;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;

@RunWith(MockitoJUnitRunner.class)
public class RPCEndpointTest {

    private final static String PATH_INFO = "/test";

    private Map<String, String[]> params;

    private ServletContext context;

    private MockedStatic<ActionPool> actionPoolStatic;

    private RPCEndpoint rpcEndpoint;

    @Mock
    private ActionPool actionPool;

    @Spy
    private Action action;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Before
    public void beforeEach() {
        actionPoolStatic = mockStatic(ActionPool.class);
        when(ActionPool.getInstance()).thenReturn(actionPool);
        when(actionPool.get(any(String.class))).thenReturn(action);

        when(request.getParameterMap()).thenReturn(params);
        when(request.getServletContext()).thenReturn(context);

        rpcEndpoint = new RPCEndpoint();
    }

    @After
    public void afterEach() {
        actionPoolStatic.close();
    }

    @Test
    public void doGetTest() {
        rpcEndpoint.doGet(request, response);
        verify(action, times(0)).run(any());
        verify(response, times(1)).setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void doPostTest() {
        OperationResult result = new OperationResult(HttpServletResponse.SC_OK);

        when(request.getServletPath()).thenReturn(RPC_SERVLET_PATH);
        when(request.getPathInfo()).thenReturn(PATH_INFO);
        when(action.run(any(OperationData.class))).thenReturn(result);

        rpcEndpoint.doPost(request, response);
        verify(action, times(1)).run(any());
        verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void doPostTestOnMissing() {
        when(request.getPathInfo()).thenReturn(EMPTY);

        rpcEndpoint.doPost(request, response);
        verify(action, times(0)).run(any());
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void doDeleteTest() {
        rpcEndpoint.doDelete(request, response);
        verify(action, times(0)).run(any());
        verify(response, times(1)).setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void doPutTest() {
        rpcEndpoint.doPut(request, response);
        verify(action, times(0)).run(any());
        verify(response, times(1)).setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

}
