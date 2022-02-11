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
public class RPCEndpoint extends VitroHttpServlet  {

	private static final long serialVersionUID = 1L;
 	private static final Log log = LogFactory.getLog(RPCEndpoint.class);
	private ActionPool actionPool = ActionPool.getInstance();
	
	@Override
	public void doGet( HttpServletRequest request, HttpServletResponse response ) {
		process(request, response);
	}
	
	@Override
	public void doPost( HttpServletRequest request, HttpServletResponse response ) {
		process(request, response);
	}
	
	@Override
	public void doDelete( HttpServletRequest request, HttpServletResponse response ) {
		process(request, response);
	}
	
	@Override
	public void doPut( HttpServletRequest request, HttpServletResponse response ) {
		process(request, response);
	}
	
	private void process(HttpServletRequest request, HttpServletResponse response) {
		String requestURL = request.getRequestURI();
		String actionName = requestURL.substring(requestURL.lastIndexOf("/") + 1 );
		log.debug(actionName);
		actionPool.printActionNames();
		Action action = actionPool.getByName(actionName);
		OperationData input = new OperationData(request);
		OperationResult result = action.run(input);
		result.prepareResponse(response);
	}
}
