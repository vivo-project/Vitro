/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.ajax;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * A base class for servlets that handle AJAX requests.
 */
public abstract class VitroAjaxController extends HttpServlet {
	/**
	 * Sub-classes must implement this method to verify that the user is
	 * authorized to execute this request.
	 */
	protected abstract boolean testIsAuthorized(HttpServletRequest request);

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
		if (testIsAuthorized(vreq)) {
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

}
