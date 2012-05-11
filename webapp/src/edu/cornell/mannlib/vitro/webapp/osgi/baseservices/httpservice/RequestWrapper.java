/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.httpservice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO
 */
public class RequestWrapper extends HttpServletRequestWrapper {
	private static final Log log = LogFactory.getLog(RequestWrapper.class);

	private final String servletPath;
	private final String pathInfo;

	public RequestWrapper(HttpServletRequest req, String matchingAlias) {
		super(req);

		String originalServletPath = req.getServletPath();
		String originalPathInfo = req.getPathInfo();

		log.debug("original requestURI is '" + req.getRequestURI() + "'");
		log.debug("original servletPath is '" + originalServletPath + "'");
		log.debug("original pathInfo is '" + originalPathInfo + "'");

		if (!originalServletPath.startsWith(matchingAlias)) {
			throw new IllegalStateException("alias '" + matchingAlias
					+ "' is not a valid match for servlet path '"
					+ originalServletPath + "'");
		}

		this.servletPath = matchingAlias;
		this.pathInfo = adjustPathInfo(originalServletPath, originalPathInfo,
				matchingAlias);
		log.debug("new servletPath is '" + this.servletPath + "'");
		log.debug("new pathInfo is '" + this.pathInfo + "'");
	}

	/** Path info should never be empty, but it may be null. */
	private String adjustPathInfo(String originalServletPath,
			String originalPathInfo, String matchingAlias) {
		String combinedPath = (originalPathInfo == null) ? originalServletPath
				: originalServletPath + originalPathInfo;
		String newPathInfo = combinedPath.substring(matchingAlias.length());
		return (newPathInfo.isEmpty()) ? null : newPathInfo;
	}

	@Override
	public String getPathInfo() {
		return this.pathInfo;
	}

	@Override
	public String getServletPath() {
		return this.servletPath;
	}

}
