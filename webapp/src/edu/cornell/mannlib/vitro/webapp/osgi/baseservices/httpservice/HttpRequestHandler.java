/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.httpservice;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An object that can handle HTTP requests for the HttpServiceFilter.
 */
public interface HttpRequestHandler {

	/**
	 * If we have a registered servlet or a registered resource that can satisfy
	 * this request, do so.
	 * 
	 * @return true if the request was satisfied. false otherwise.
	 */
	boolean serviceRequest(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException;

}
