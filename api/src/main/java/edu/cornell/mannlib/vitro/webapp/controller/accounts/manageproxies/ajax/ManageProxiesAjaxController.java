/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies.ajax;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.VitroAjaxController;

/**
 * Handle the AJAX functions that are specific to the ManageProxies pages.
 */
@WebServlet(name = "ProxiesAjax", urlPatterns = {"/proxiesAjax/*"} )
public class ManageProxiesAjaxController extends VitroAjaxController {
	private static final Log log = LogFactory
			.getLog(ManageProxiesAjaxController.class);

	private static final String PARAMETER_ACTION = "action";

	@Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		return SimplePermission.MANAGE_OWN_PROXIES.ACTION
				.or(SimplePermission.MANAGE_PROXIES.ACTION);
	}

	@Override
	protected void doRequest(VitroRequest vreq, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			String function = vreq.getParameter(PARAMETER_ACTION);
			if ("getAvailableProxies".equals(function)) {
				new BasicProxiesGetter(this, vreq, resp).processRequest();
			} else if ("moreProxyInfo".equals(function)) {
				new MoreProxyInfo(this, vreq, resp).processRequest();
			} else if ("getAvailableProfiles".equals(function)) {
				new BasicProfilesGetter(this, vreq, resp).processRequest();
			} else if ("moreProfileInfo".equals(function)) {
				new MoreProfileInfo(this, vreq, resp).processRequest();
			} else {
				log.error("Unrecognized function: '" + function + "'");
				resp.getWriter().write("[]");
			}
		} catch (Exception e) {
			log.error(e, e);
			resp.getWriter().write("[]");
		}
	}

}
