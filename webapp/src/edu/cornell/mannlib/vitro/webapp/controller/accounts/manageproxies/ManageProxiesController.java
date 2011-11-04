/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageProxies;
import edu.cornell.mannlib.vitro.webapp.controller.AbstractPageHandler.Message;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;

/**
 * Parcel out the different actions required of the ManageProxies GUI.
 */
public class ManageProxiesController extends FreemarkerHttpServlet {
	private static final Log log = LogFactory
			.getLog(ManageProxiesController.class);

	private static final String ACTION_CREATE = "/create";
	private static final String ACTION_EDIT = "/edit";

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

		
		if (ACTION_CREATE.equals(action)) {
			return handleCreateRequest(vreq);
		} else if (ACTION_EDIT.equals(action)) {
			return handleEditRequest(vreq);
		} else {
			return handleListRequest(vreq);
		}
	}

	private ResponseValues handleCreateRequest(VitroRequest vreq) {
		ManageProxiesCreatePage page = new ManageProxiesCreatePage(vreq);

		if (page.isValid()) {
			page.createRelationships();
			Message.setMessage(vreq, new SuccessMessage());
		} else {
			Message.setMessage(vreq, new FailureMessage());
		}

		return redirectToList();
	}

	private ResponseValues handleEditRequest(VitroRequest vreq) {
		ManageProxiesEditPage page = new ManageProxiesEditPage(vreq);
		
		if (page.isValid()) {
			page.applyEdits();
			Message.setMessage(vreq, new SuccessMessage());
		} else {
			Message.setMessage(vreq, new FailureMessage());
		}
		
		return redirectToList();
	}
	
	private ResponseValues handleListRequest(VitroRequest vreq) {
		ManageProxiesListPage page = new ManageProxiesListPage(vreq);
		return page.showPage();
	}

	/**
	 * After an successful change, redirect to the list instead of forwarding.
	 * That way, a browser "refresh" won't try to repeat the operation.
	 */
	private ResponseValues redirectToList() {
		return new RedirectResponseValues("/manageProxies/list");
	}

	private static class SuccessMessage extends Message {
		@Override
		public Map<String, Object> getMessageInfoMap() {
			return assembleMap("success", Boolean.TRUE);
		}

	}
	private static class FailureMessage extends Message {
		@Override
		public Map<String, Object> getMessageInfoMap() {
			return assembleMap("failure", Boolean.TRUE);
		}
		
	}
}
