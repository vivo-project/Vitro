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

import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class CharsetEncodingFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// Nothing to set up
	}

	@Override
	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) servletRequest;
		ServletContext ctx = req.getSession().getServletContext();
		String url = req.getRequestURL().toString();

		if (!ServletFileUpload.isMultipartContent(req)) {
			servletRequest.setCharacterEncoding("UTF-8");
		}

		if (req.getContentType() == null) {
			String typeFromContext = ctx.getMimeType(url);
			if (typeFromContext == null) {
				servletResponse.setContentType("text/html;charset=UTF-8");
			} else {
				servletResponse.setContentType(typeFromContext);
			}
		}

		filterChain.doFilter(servletRequest, servletResponse);
	}

	@Override
	public void destroy() {
		// Nothing to tear down
	}
}
