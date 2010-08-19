/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This filter overrides the HTTPServletResponse's encodeURL method
 * to rewrite requests for resources so that lengthy URI parameters
 * are rewritten to use faux directories representing namespaces.
 * It will also remove home parameters and rewrite them as URL
 * prefixes
 * @author bjl23
 *
 */
public class URLRewriterFilter implements Filter {

	private ServletContext _context;
	
	public void destroy() {
		// Nothing to do here
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		if (response instanceof HttpServletResponse) {
			chain.doFilter(request, new URLRewritingHttpServletResponse((HttpServletResponse)response,(HttpServletRequest)request,_context));
		} else {
			chain.doFilter(request,response);
		}

	}

	public void init(FilterConfig config) throws ServletException {
		_context = config.getServletContext();
	}

}
