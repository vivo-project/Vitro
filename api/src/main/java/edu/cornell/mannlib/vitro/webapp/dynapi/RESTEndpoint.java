package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.RequestPath.REST_BASE_PATH;
import static java.lang.String.format;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Resource;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceKey;
import edu.cornell.mannlib.vitro.webapp.dynapi.request.RequestPath;

@WebServlet(name = "RESTEndpoint", urlPatterns = { REST_BASE_PATH + "/*" })
public class RESTEndpoint extends VitroHttpServlet {

	private static final Log log = LogFactory.getLog(RESTEndpoint.class);

	private ResourcePool resourcePool = ResourcePool.getInstance();
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
			ResourceKey resourceKey = ResourceKey.of(requestPath.getResourceName(), requestPath.getResourceVersion());

			if (log.isDebugEnabled()) {
				resourcePool.printKeys();
			}
			Resource resource = resourcePool.get(resourceKey);
			String method = request.getMethod();

			String actionName = null;

			if (requestPath.isCustomRestAction()) {
				if (method.toUpperCase().equals("POST")) {
					String customRestActionName = requestPath.getActionName();

					try {
						actionName = resource.getCustomRestActionByName(customRestActionName);
					} catch (UnsupportedOperationException e) {
						log.error(format("Custom REST action %s not implemented for resource %s", customRestActionName,
								resource.getKey()), e);
						OperationResult.notImplemented().prepareResponse(response);
						return;
					} finally {
						resource.removeClient();
					}
				} else {
					OperationResult.notImplemented().prepareResponse(response);
					resource.removeClient();
					return;
				}
			} else {
				try {
					actionName = resource.getActionNameByMethod(method);
				} catch (UnsupportedOperationException e) {
					log.error(format("Method %s not implemented for resource %s", method, resource.getKey()), e);
					OperationResult.notImplemented().prepareResponse(response);
					return;
				} finally {
					resource.removeClient();
				}
			}

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
