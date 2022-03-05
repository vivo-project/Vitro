package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.RequestPath.REST_SERVLET_PATH;
import static java.lang.String.format;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPI;
import edu.cornell.mannlib.vitro.webapp.dynapi.request.RequestPath;

@WebServlet(name = "RESTEndpoint", urlPatterns = { REST_SERVLET_PATH + "/*" })
public class RESTEndpoint extends VitroHttpServlet {

	private static final Log log = LogFactory.getLog(RESTEndpoint.class);

	private ResourceAPIPool resourceAPIPool = ResourceAPIPool.getInstance();
	private ActionPool actionPool = ActionPool.getInstance();

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getMethod().equalsIgnoreCase("PATCH")) {
			doPatch(request, response);
		} else {
			super.service(request, response);
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		process(request, response);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		process(request, response);
	}

	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) {
		process(request, response);
	}

	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) {
		process(request, response);
	}

	public void doPatch(HttpServletRequest request, HttpServletResponse response) {
		process(request, response);
	}

	private void process(HttpServletRequest request, HttpServletResponse response) {
		RequestPath requestPath = RequestPath.from(request);

		if (requestPath.isValid()) {
			ResourceAPIKey resourceAPIKey = ResourceAPIKey.of(requestPath.getResourceName(), requestPath.getResourceVersion());

			if (log.isDebugEnabled()) {
				resourceAPIPool.printKeys();
			}
			ResourceAPI resourceAPI = resourceAPIPool.get(resourceAPIKey);
			ResourceAPIKey key = resourceAPI.getKey();
			String method = request.getMethod();

			RPC rpc = null;

			if (requestPath.isCustomRestAction()) {
				String customRestActionName = requestPath.getActionName();
				try {
					rpc = resourceAPI.getCustomRestActionRPC(customRestActionName);
				} catch (UnsupportedOperationException e) {
					log.error(format("Custom REST action %s not implemented for resourceAPI %s", customRestActionName, key), e);
					OperationResult.notImplemented().prepareResponse(response);
					return;
				} finally {
					resourceAPI.removeClient();
				}
			} else {
				try {
					rpc = resourceAPI.getRestRPC(method);
				} catch (UnsupportedOperationException e) {
					log.error(format("Method %s not implemented for resourceAPI %s", method, key), e);
					OperationResult.notImplemented().prepareResponse(response);
					return;
				} finally {
					resourceAPI.removeClient();
				}
			}

			HTTPMethod rpcMethod = rpc.getHttpMethod();

			if (rpcMethod == null || !rpcMethod.getName().toUpperCase().equals(method)) {
				log.error(format("Remote Procedure Call not implemented for resourceAPI %s with method %s", key, method));
				OperationResult.notImplemented().prepareResponse(response);
				return;
			}

			String actionName = rpc.getName();

			if (log.isDebugEnabled()) {
				actionPool.printKeys();
			}
			Action action = actionPool.get(actionName);
			OperationData input = new OperationData(request);
			try {
				OperationResult result = action.run(input);
				result.prepareResponse(response);
			} finally {
				action.removeClient();
			}
		} else {
			OperationResult.notFound().prepareResponse(response);
		}
	}

}
