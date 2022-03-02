package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.RequestPath.REST_BASE_PATH;
import static org.apache.commons.lang3.StringUtils.EMPTY;
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
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Resource;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceKey;

@RunWith(Parameterized.class)
public class RESTEndpointTest {

	private final static String PATH_INFO = "/1/test";
	private final static String CONTEXT_PATH = REST_BASE_PATH + PATH_INFO;

	private Map<String, String[]> params = new HashMap<>();

	private MockedStatic<ResourcePool> resourcePoolStatic;

	private MockedStatic<ActionPool> actionPoolStatic;

	@Mock
	private ServletContext context;

	@Mock
	private ResourcePool resourcePool;

	@Mock
	private ActionPool actionPool;

	@Mock
	private Resource resource;

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
	public String testAction;

	@Before
	public void beforeEach() {
		MockitoAnnotations.openMocks(this);
		resourcePoolStatic = mockStatic(ResourcePool.class);
		actionPoolStatic = mockStatic(ActionPool.class);

		when(ResourcePool.getInstance()).thenReturn(resourcePool);
		when(resourcePool.get(any(ResourceKey.class))).thenReturn(resource);

		when(ActionPool.getInstance()).thenReturn(actionPool);
		when(actionPool.get(any(String.class))).thenReturn(action);

		when(request.getParameterMap()).thenReturn(params);
		when(request.getServletContext()).thenReturn(context);

		restEndpoint = new RESTEndpoint();
	}

	@After
	public void afterEach() {
		resourcePoolStatic.close();
		actionPoolStatic.close();
	}

	@Test
	public void doTest() {
		prepareMocks(testMethod, testAction);
		run(testMethod);
		verifyMocksOk();
	}

	@Test
	public void doTestNotFound() {
		prepareMocksNotFound(testMethod, testAction);
		run(testMethod);
		verifyMocksNotFound();
	}

	@Test
	public void doTestUnsupportedMethod() {
		prepareMocksUnsupportedMethod(testMethod, testAction);
		run(testMethod);
		verifyMocksUnsupportedMethod();
	}

	@Test
	public void doTestRPCNotImplemented() {
		prepareMocksRPCNotImplemented(testMethod, testAction);
		run(testMethod);
		verifyMocksRPCNotImplemented();
	}

	@Test
	public void doTestCustomAction() {
		prepareMocksCustomAction(testMethod, testAction);
		run(testMethod);
		verifyMocksCustomActionOk();
	}

	@Test
	public void doTestCustomActionUnsupported() {
		prepareMocksUnsupportedCustomAction(testMethod, testAction);
		run(testMethod);
		verifyMocksCustomActionUnsupported();
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

	private void prepareMocks(String method, String actionName) {
		when(request.getMethod()).thenReturn(method);
		when(request.getContextPath()).thenReturn(CONTEXT_PATH);
		when(request.getPathInfo()).thenReturn(PATH_INFO);

		when(action.run(any(OperationData.class)))
			.thenReturn(new OperationResult(HttpServletResponse.SC_OK));

		when(httpMethod.getName()).thenReturn(method);

		when(rpc.getName()).thenReturn(actionName);
		when(rpc.getHttpMethod()).thenReturn(httpMethod);

		when(resource.getRestRPC(method)).thenReturn(rpc);
		doNothing().when(resource).removeClient();
	}

	private void prepareMocksNotFound(String method, String actionName) {
		prepareMocks(method, actionName);
		when(request.getPathInfo()).thenReturn(EMPTY);
	}

	private void prepareMocksUnsupportedMethod(String method, String actionName) {
		prepareMocks(method, actionName);
		when(resource.getRestRPC(method))
				.thenThrow(new UnsupportedOperationException("Unsupported method"));
	}

	private void prepareMocksRPCNotImplemented(String method, String actionName) {
		prepareMocks(method, actionName);
		when(httpMethod.getName()).thenReturn("FUBAR");
	}

	private void prepareMocksCustomAction(String method, String actionName) {
		when(request.getMethod()).thenReturn(method);
		when(request.getContextPath()).thenReturn(CONTEXT_PATH + "/" + actionName);
		when(request.getPathInfo()).thenReturn(PATH_INFO + "/" + actionName);

		when(action.run(any(OperationData.class)))
			.thenReturn(new OperationResult(HttpServletResponse.SC_OK));

		when(httpMethod.getName()).thenReturn(method);

		when(rpc.getName()).thenReturn(actionName);
		when(rpc.getHttpMethod()).thenReturn(httpMethod);

		when(resource.getCustomRestActionRPC(actionName)).thenReturn(rpc);
		doNothing().when(resource).removeClient();
	}

	private void prepareMocksUnsupportedCustomAction(String method, String actionName) {
		prepareMocksCustomAction(method, actionName);
		when(resource.getCustomRestActionRPC(actionName))
			.thenThrow(new UnsupportedOperationException("Unsupported custom action"));
	}

	private void verifyMocksOk() {
		verifyMocks(new int[] { 1, 0, 1, 1, 1, 1 }, HttpServletResponse.SC_OK);
	}

	private void verifyMocksNotFound() {
		verifyMocks(new int[] { 0, 0, 0, 0, 0, 1 }, HttpServletResponse.SC_NOT_FOUND);
	}

	private void verifyMocksUnsupportedMethod() {
		verifyMocks(new int[] { 1, 0, 1, 0, 0, 1 }, HttpServletResponse.SC_NOT_IMPLEMENTED);
	}

	private void verifyMocksRPCNotImplemented() {
		verifyMocks(new int[] { 1, 0, 1, 0, 0, 1 }, HttpServletResponse.SC_NOT_IMPLEMENTED);
	}

	private void verifyMocksCustomActionOk() {
		verifyMocks(new int[] { 0, 1, 1, 1, 1, 1 }, HttpServletResponse.SC_OK);
	}

	private void verifyMocksCustomActionUnsupported() {
		verifyMocks(new int[] { 0, 1, 1, 0, 0, 1 }, HttpServletResponse.SC_NOT_IMPLEMENTED);
	}

	private void verifyMocks(int[] times, int status) {
		verify(resource, times(times[0])).getRestRPC(any());
		verify(resource, times(times[1])).getCustomRestActionRPC(any());
		verify(resource, times(times[2])).removeClient();
		verify(action, times(times[3])).run(any());
		verify(action, times(times[4])).removeClient();
		verify(response, times(times[5])).setStatus(status);
	}

	@Parameterized.Parameters
	public static Collection<String[]> requests() {
		return Arrays.asList(new String[][] {
			// method action
			{ "POST", "test" },
			{ "GET", "test" },
			{ "PUT", "test" },
			{ "PATCH", "test" },
			{ "DELETE", "test" }
		});
	}

}
