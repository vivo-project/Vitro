/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Add X-FRAME-OPTIONS response header to tell IE8 (and any other browsers who
 * decide to implement) not to display this content in a frame.
 * 
 * For details, refer to
 * http://blogs.msdn.com/sdl/archive/2009/02/05/clickjacking-defense-in-ie8.aspx,
 * https://www.owasp.org/index.php/ClickjackFilter_for_Java_EE
 */
public class ClickjackFilter implements Filter {
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (response instanceof HttpServletResponse) {
			((HttpServletResponse) response).setHeader("X-FRAME-OPTIONS",
					"SAMEORIGIN");
		}
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig fc) throws ServletException {
		// Nothing to set up.
	}

	@Override
	public void destroy() {
		// Nothing to tear down.
	}

}
