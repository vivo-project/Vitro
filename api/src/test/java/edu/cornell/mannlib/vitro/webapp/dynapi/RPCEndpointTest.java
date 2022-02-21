package edu.cornell.mannlib.vitro.webapp.dynapi;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verify;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.BeforeClass;
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

	final static String URI_TEST = "/api/rpc/test";

	private static RESTEndpoint restEndpoint;

	@Mock
	private static ActionPool actionPool;

	@Mock
	private Action action;

	@Mock
	private OperationResult operationResult;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@BeforeClass
	public static void beforeAll()  throws IOException, ServletException {
		mockStatic(ActionPool.class);
		expect(ActionPool.getInstance()).andReturn(actionPool).anyTimes();
		replay(ActionPool.class);

		restEndpoint = new RESTEndpoint();
	}

	@Before
	public void beforeEach() {
		actionPool = createMock(ActionPool.class);
		action = createMock(Action.class);

		operationResult = createMock(OperationResult.class);

		request = createMock(HttpServletRequest.class);
		response = createMock(HttpServletResponse.class);

		expect(action.run(anyObject())).andReturn(operationResult);
		replay(action);

		expect(actionPool.getByName(anyString())).andReturn(action);
		replay(actionPool);

		expect(request.getRequestURI()).andReturn(URI_TEST);
	}

	@Test
	public void doGetTest() {
		expect(request.getMethod()).andReturn("GET").atLeastOnce();
		replay(request);

		restEndpoint.doGet(request, response);
		verify(request);
	}

	@Test
	public void doPostTest() {
		expect(request.getMethod()).andReturn("POST").atLeastOnce();
		replay(request);

		restEndpoint.doPost(request, response);
		verify(request);
	}

	@Test
	public void doDeleteTest() {
		expect(request.getMethod()).andReturn("DELETE").atLeastOnce();
		replay(request);

		restEndpoint.doDelete(request, response);
		verify(request);
	}

	@Test
	public void doPutTest() {
		expect(request.getMethod()).andReturn("PUT").atLeastOnce();
		replay(request);

		restEndpoint.doPut(request, response);
		verify(request);
	}
}
