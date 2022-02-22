package edu.cornell.mannlib.vitro.webapp.dynapi;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verify;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ActionPool.class)
public class RPCEndpointTest {

	final private static String URI_TEST = "/api/rpc/test";

	private static RPCEndpoint rpcEndpoint;

	@Mock
	private static ActionPool actionPool;

	@Mock
	private static Action action;

	@Mock
	private static OperationResult operationResult;

	@Mock
	private static HttpServletRequest request;

	@Mock
	private static HttpServletResponse response;

	@Mock
	private static Map<String, String[]> params;

	@Mock
	private static ServletContext context;

	@Before
	public void beforeEach() {
		operationResult = createMock(OperationResult.class);
		action = createMock(Action.class);
		actionPool = createMock(ActionPool.class);
		request = createMock(HttpServletRequest.class);
		response = createMock(HttpServletResponse.class);

		// The order of where mockStatic (and possibly all mocks herein) matters and should only be changed cautiously.
		mockStatic(ActionPool.class);
		expect(ActionPool.getInstance()).andReturn(actionPool).anyTimes();
		replay(ActionPool.class);

		rpcEndpoint = new RPCEndpoint();

		actionPool.printNames();
		expectLastCall().anyTimes();
		expect(actionPool.getByName(anyString())).andReturn(action).anyTimes();
		replay(actionPool);

		operationResult.prepareResponse(response);
		expectLastCall().anyTimes();
		replay(operationResult);

		expect(request.getParameterMap()).andReturn(params).anyTimes();
		expect(request.getServletContext()).andReturn(context).anyTimes();
		expect(request.getRequestURI()).andReturn(URI_TEST).anyTimes();
		replay(request);

		action.removeClient();
		expectLastCall().anyTimes();
		expect(action.run(anyObject())).andReturn(operationResult).once();
		replay(action);
	}

	@Test
	public void doGetTest() {
		rpcEndpoint.doGet(request, response);
		verify(request);
		verify(action);
		verify(actionPool);
	}

	@Test
	public void doPostTest() {
		rpcEndpoint.doPost(request, response);
		verify(request);
		verify(action);
		verify(actionPool);
	}

	@Test
	public void doDeleteTest() {
		rpcEndpoint.doDelete(request, response);
		verify(request);
		verify(action);
		verify(actionPool);
	}

	@Test
	public void doPutTest() {
		rpcEndpoint.doPut(request, response);
		verify(request);
		verify(action);
		verify(actionPool);
	}
}
