/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
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

		setCharacterEncodingOnRequest(req);
		setContentTypeOnResponse(req, servletResponse);

		filterChain.doFilter(servletRequest, servletResponse);
	}

	private void setCharacterEncodingOnRequest(HttpServletRequest req)
			throws UnsupportedEncodingException {
		if (!ServletFileUpload.isMultipartContent(req)) {
			req.setCharacterEncoding("UTF-8");
		}
	}

	private void setContentTypeOnResponse(HttpServletRequest req,
			ServletResponse servletResponse) {
		if (servletResponse.getContentType() != null) {
			return;
		}
		
		String uri = req.getRequestURI();
		if (!hasExtension(uri)) {
			servletResponse.setContentType("text/html;charset=UTF-8");
			return;
		}

		String typeFromContext = req.getSession().getServletContext()
				.getMimeType(uri);
		if (typeFromContext == null) {
			servletResponse.setContentType("text/html;charset=UTF-8");
			return;
		}

		servletResponse.setContentType(typeFromContext);
		servletResponse.setCharacterEncoding("UTF-8");
	}

	private boolean hasExtension(String uri) {
		return uri.matches(".+" // some stuff
				+ "\\." // a literal period
				+ "[^/]+" // some stuff that's not a slash
				+ "$" // the end of the URI
		);
	}

	@Override
	public void destroy() {
		// Nothing to tear down
	}
}
