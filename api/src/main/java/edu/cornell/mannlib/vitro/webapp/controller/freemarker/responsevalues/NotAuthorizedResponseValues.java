/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;

/**
 * This allows processRequest() in sub-classes of FreemarkerHttpServlet to
 * decide that the request is not authorized, and properly handle the
 * redirection.
 */
public class NotAuthorizedResponseValues extends BaseResponseValues {
	/**
	 * If logging is turned on, this will be written to the log as a reason for
	 * rejecting the servlet.
	 */
	private final String logMessage;

	public NotAuthorizedResponseValues(String logMessage) {
		this.logMessage = logMessage;
	}

	public RequestedAction getUnauthorizedAction() {
		return new RequestedAction() {
			@Override
			public String toString() {
				return "Servlet not authorized: " + logMessage;
			}
		};
	}
}
