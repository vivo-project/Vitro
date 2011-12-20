/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerConfigurationLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * A base class for servlets that handle AJAX requests.
 */
public abstract class VitroAjaxController extends HttpServlet {
    
    private static final Log log = LogFactory.getLog(VitroAjaxController.class);

	/**
	 * Sub-classes must implement this method to handle both GET and POST
	 * requests.
	 */
	protected abstract void doRequest(VitroRequest vreq,
			HttpServletResponse resp) throws ServletException, IOException;

	/**
	 * Sub-classes should not override this. Instead, implement doRequest().
	 */
	@Override
	protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		VitroRequest vreq = new VitroRequest(req);
		if (PolicyHelper.isAuthorizedForActions(vreq, requiredActions(vreq))) {
			doRequest(vreq, resp);
		} else {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Not authorized");
		}
	}

	/**
	 * Sub-classes should not override this. Instead, implement doRequest().
	 */
	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
	
    /**
     * By default, a controller requires authorization for no actions.
     * Subclasses that require authorization to process their page will override 
	 *    to return the actions that require authorization.
	 * In some cases, the choice of actions will depend on the contents of the request.
     */
    @SuppressWarnings("unused")
	protected Actions requiredActions(VitroRequest vreq) {
        return Actions.AUTHORIZED;
    }
    
	/** 
	 * Returns the current Freemarker Configuration so the controller can process
	 * its data through a template.
	 */
	protected final Configuration getFreemarkerConfiguration(VitroRequest vreq) {
		return FreemarkerConfigurationLoader.getConfig(vreq, getServletContext());
	}
	
	/**
	 * Process data through a Freemarker template and output the result.
	 */
	protected void writeTemplate(String templateName, Map<String, Object> map, 
	        Configuration config, HttpServletRequest request, HttpServletResponse response) {
        Template template = null;
        try {
            template = config.getTemplate(templateName);
            PrintWriter out = response.getWriter();
            template.process(map, out);
        } catch (Exception e) {
            log.error(e, e);
        } 
	}
    
    protected void doError(HttpServletResponse response, String errorMsg, int httpstatus){
        response.setStatus(httpstatus);
        try {
            response.getWriter().write(errorMsg);
        } catch (IOException e) {
            log.debug("IO exception during output",e );
        }
    }
}
