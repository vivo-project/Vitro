/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A simple filter to log the HTTP requests received by Tomcat
 * @author bjl23
 *
 */
public class RequestLoggerFilter implements Filter {

	private static final Log log = LogFactory.getLog(RequestLoggerFilter.class);
	
	public void destroy() {
		// TODO Auto-generated method stub

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		try {
			if (request instanceof HttpServletRequest) {
				HttpServletRequest theRequest = (HttpServletRequest) request;
				StringBuffer requestedLocation = new StringBuffer();
				requestedLocation.append(theRequest.getLocalName()).append(":");
				requestedLocation.append(theRequest.getLocalPort());
				requestedLocation.append(theRequest.getRequestURI());
				if (theRequest.getQueryString() != null) {
					requestedLocation.append("?").append(theRequest.getQueryString());
				}
				log.debug("Incoming request: "+requestedLocation.toString());
			}
		} catch (Exception e) {
			// This shouldn't really happen, but if it does, we'll be ready for it.
		} finally {		
			filterChain.doFilter(request, response);
		}
	}

	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

}
