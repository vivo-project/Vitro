/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath.RPC_SERVLET_PATH;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Procedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.RPC;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RPCEndpointTest {

    private final static String PATH_INFO = "/test";

    private Map<String, String[]> params;

    private ByteArrayOutputStream baos;

    private MockedStatic<ProcedurePool> actionPoolStatic;

    private MockedStatic<RPCPool> rpcPoolStatic;

    private RPCEndpoint rpcEndpoint;

    @Mock
    private HttpSession session;

    @Mock
    private UserAccount user;

    @Mock
    private ProcedurePool procedurePool;

    @Mock
    private RPCPool rpcApiPool;

    @Mock
    private RPC rpc;

    @Spy
    private Procedure procedure;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Before
    public void beforeEach() {
        baos = new ByteArrayOutputStream();
        actionPoolStatic = mockStatic(ProcedurePool.class);
        rpcPoolStatic = mockStatic(RPCPool.class);

        when(ProcedurePool.getInstance()).thenReturn(procedurePool);
        when(RPCPool.getInstance()).thenReturn(rpcApiPool);

        when(procedurePool.get(any(String.class))).thenReturn(procedure);

        when(rpcApiPool.get(any(String.class))).thenReturn(rpc);
        when(rpc.getProcedureUri()).thenReturn("procedure_uri");

        when(request.getParameterMap()).thenReturn(params);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(user.isRootUser()).thenReturn(true);

        rpcEndpoint = new RPCEndpoint();

    }

    @After
    public void afterEach() {
        actionPoolStatic.close();
        rpcPoolStatic.close();

        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void doGetTest() {
        rpcEndpoint.doGet(request, response);
        verify(procedure, times(0)).run(any());
        verify(response, times(1)).setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void doPostTest() throws IOException {
        OperationResult result = OperationResult.ok();

        when(request.getServletPath()).thenReturn(RPC_SERVLET_PATH);
        when(request.getPathInfo()).thenReturn(PATH_INFO);
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ContentType.APPLICATION_JSON.toString());
        PrintWriter writer = new PrintWriter(baos, true);
        when(response.getWriter()).thenReturn(writer);
        when(procedure.run(any(DataStore.class))).thenReturn(result);
        rpcEndpoint.doPost(request, response);
        verify(procedure, times(1)).run(any());
        verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void doPostTestOnMissing() {
        when(request.getPathInfo()).thenReturn(EMPTY);

        rpcEndpoint.doPost(request, response);
        verify(procedure, times(0)).run(any());
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void doDeleteTest() {
        rpcEndpoint.doDelete(request, response);
        verify(procedure, times(0)).run(any());
        verify(response, times(1)).setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void doPutTest() {
        rpcEndpoint.doPut(request, response);
        verify(procedure, times(0)).run(any());
        verify(response, times(1)).setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

}
