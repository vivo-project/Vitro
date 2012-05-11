/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.httpservice;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Put this filter in line to catch all requests. It will ask the
 * HttpRequestHandler whether it wants to service the request. If not, it passes
 * the request on to the normal Tomcat mechanism.
 * 
 * This probably should be near the end of the filter chain.
 */
public class HttpServiceFilter implements Filter {
	private static final Log log = LogFactory.getLog(HttpServiceFilter.class);

	/**
	 * This static field provides a klunky way to link these objects.
	 */
	private static HttpRequestHandler httpRequestHandler;

	public static void setHttpRequestHandler(
			HttpRequestHandler httpRequestHandler) {
		HttpServiceFilter.httpRequestHandler = httpRequestHandler;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		if (httpRequestHandler == null) {
			log.debug("no httpRequestHandler");
			chain.doFilter(req, resp);
			return;
		}

		if (!(req instanceof HttpServletRequest)) {
			log.debug("req is not an HttpServletRequest: " + req);
			chain.doFilter(req, resp);
			return;
		}

		if (!(resp instanceof HttpServletResponse)) {
			log.debug("req is not an HttpServletResponse: " + resp);
			chain.doFilter(req, resp);
			return;
		}

		HttpServletRequest hreq = (HttpServletRequest) req;
		HttpServletResponse hresp = (HttpServletResponse) resp;
		String requestUrl = hreq.getRequestURL().toString();
		boolean serviced = httpRequestHandler.serviceRequest(hreq, hresp);
		if (serviced) {
			log.debug("httpService serviced the request: " + requestUrl);
			return;
		}

		log.debug("httpService did not service the request: " + requestUrl);
		chain.doFilter(req, resp);
	}

	@Override
	public void init(FilterConfig fc) throws ServletException {
		// nothing to initialize
	}

	@Override
	public void destroy() {
		// nothing to destroy.
	}

}
