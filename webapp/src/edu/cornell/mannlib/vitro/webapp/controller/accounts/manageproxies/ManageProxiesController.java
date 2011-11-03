/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageProxies;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;

/**
 * Parcel out the different actions required of the ManageProxies GUI.
 */
public class ManageProxiesController extends FreemarkerHttpServlet {
	private static final Log log = LogFactory
			.getLog(ManageProxiesController.class);

	@Override
	protected Actions requiredActions(VitroRequest vreq) {
		return new Actions(new ManageProxies());
	}

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		if (log.isDebugEnabled()) {
			dumpRequestParameters(vreq);
		}

		String action = vreq.getPathInfo();
		log.debug("action = '" + action + "'");

		return handleListRequest(vreq);
	}

	private ResponseValues handleListRequest(VitroRequest vreq) {
		ManageProxiesListPage page = new ManageProxiesListPage(vreq);
		return page.showPage();
	}

}
