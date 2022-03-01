package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.RequestPath.RPC_BASE_PATH;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.request.RequestPath;

@WebServlet(name = "RPCEndpoint", urlPatterns = { RPC_BASE_PATH + "/*" })
public class RPCEndpoint extends VitroHttpServlet {

	private static final Log log = LogFactory.getLog(RESTEndpoint.class);

	private ActionPool actionPool = ActionPool.getInstance();

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		OperationResult.notImplemented().prepareResponse(response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		RequestPath requestPath = RequestPath.from(request);
		if (requestPath.isValid()) {
			if (log.isDebugEnabled()) {
				actionPool.printKeys(); 
			}
			Action action = actionPool.get(requestPath.getActionName());
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

	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) {
		OperationResult.notImplemented().prepareResponse(response);
	}

	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) {
		OperationResult.notImplemented().prepareResponse(response);
	}

}
