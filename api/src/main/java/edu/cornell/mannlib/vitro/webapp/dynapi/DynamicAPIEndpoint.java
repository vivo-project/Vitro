package edu.cornell.mannlib.vitro.webapp.dynapi;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ProcessResult;

@WebServlet(name = "DynamicAPIEndpoint", urlPatterns = { "/dynapi/*" })
public class DynamicAPIEndpoint extends VitroHttpServlet  {

	private static final long serialVersionUID = 1L;
 	private static final Log log = LogFactory.getLog(DynamicAPIEndpoint.class);
	private ActionPool actionPool;
 	
	
	@Override
	public void init(ServletConfig sc) {
		actionPool = ActionPool.getInstance();
		ServletContext ctx = sc.getServletContext();
		actionPool.init(ctx);
	}
	
	@Override
	public void doPost( HttpServletRequest request, HttpServletResponse response ) {
		String requestURL = request.getRequestURI();
		String actionName = requestURL.substring(requestURL.lastIndexOf("/") + 1 );
		log.debug(actionName);
		actionPool.printActionNames();
		Action action = actionPool.getByName(actionName);
		ProcessInput input = new ProcessInput(request);
		ProcessResult result = action.run(input);
		result.prepareResponse(response);
	}
}
