package edu.cornell.mannlib.vitro.webapp.dynapi;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;

@WebServlet(name = "RPCEndpoint", urlPatterns = { "/api/rpc/*" })
public class RPCEndpoint extends VitroHttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(RPCEndpoint.class);

	private ActionPool actionPool = ActionPool.getInstance();

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		OperationResult.notImplemented().prepareResponse(response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		if (request.getPathInfo() == null) {
			OperationResult.notFound().prepareResponse(response);
			return;
		}

		String[] paths = request.getPathInfo().split("/");

		if (paths.length < 2) {
			OperationResult.notFound().prepareResponse(response);
			return;
		}

		String actionName = paths[1];

		actionPool.printKeys();
		Action action = actionPool.get(actionName);
		OperationData input = new OperationData(request);

		try {
			OperationResult result = action.run(input);
			result.prepareResponse(response);
		} finally {
			action.removeClient();
		}
	}

	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) {
		OperationResult.notImplemented().prepareResponse(response);
	}

	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) {
		OperationResult.notImplemented().prepareResponse(response);
	}
}
