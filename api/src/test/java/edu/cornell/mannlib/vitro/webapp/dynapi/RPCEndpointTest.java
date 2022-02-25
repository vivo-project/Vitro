package edu.cornell.mannlib.vitro.webapp.dynapi;

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

@RunWith(MockitoJUnitRunner.class)
public class RPCEndpointTest {

	private final static String URI_TEST = "/api/rpc/test";

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
		when(actionPool.getByName(any())).thenReturn(action);

		when(request.getParameterMap()).thenReturn(params);
		when(request.getServletContext()).thenReturn(context);
		when(request.getRequestURI()).thenReturn(URI_TEST);

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
	}

	@Test
	public void doPostTest() {
		rpcEndpoint.doPost(request, response);
		verify(action, times(1)).run(any());
	}

	@Test
	public void doDeleteTest() {
		rpcEndpoint.doDelete(request, response);
		verify(action, times(0)).run(any());
	}

	@Test
	public void doPutTest() {
		rpcEndpoint.doPut(request, response);
		verify(action, times(0)).run(any());
	}
}
