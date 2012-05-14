/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.baseservices.httpservice;

import java.io.IOException;
import java.util.Dictionary;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;

/**
 * This is how a registered servlet will process an HTTP request.
 */
public class ServletServicer extends Servicer {
	private static final Log log = LogFactory.getLog(ServletServicer.class);

	private final Servlet servlet;
	private final BasicServletConfig servletConfig;
	private final ServletContextWrapper servletContext;

	public ServletServicer(String alias, Bundle bundle,
			HttpContext httpContext, Servlet servlet,
			Dictionary<String, String> initparams,
			ServletContextWrapper servletContext) {
		super(alias, bundle, httpContext);

		this.servlet = servlet;
		this.servletConfig = new BasicServletConfig(servletContext, alias,
				initparams);
		this.servletContext = servletContext;
	}

	public Servlet getServlet() {
		return servlet;
	}

	public BasicServletConfig getServletConfig() {
		return servletConfig;
	}

	public ServletContextWrapper getServletContext() {
		return servletContext;
	}

	@Override
	public void init() throws ServletException {
		log.debug("initializing servlet at '" + getAlias() + "'");
		servlet.init(servletConfig);
	}

	/**
	 * When a servlet is unregistered, call its lifecyle method.
	 */
	@Override
	public void dispose() {
		log.debug("destroying servlet at '" + getAlias() + "'");
		getServlet().destroy();
	}

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		log.debug("servicing servlet request for '" + figureRequestUri(req)
				+ "'");
		AdjustedRequestWrapper wrappedReq = new AdjustedRequestWrapper(req,
				getAlias());
		delegateToRegisteredServlet(wrappedReq, resp);
	}

	/**
	 * Ask the HttpContext whether this request is authorized. If it is not, the
	 * HttpContext will set the appropriate error code and message.
	 * 
	 * If the request is authorized, ask the servlet to process it.
	 */
	private void delegateToRegisteredServlet(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		boolean authorized = getHttpContext().handleSecurity(req, resp);
		if (authorized) {
			getServlet().service(req, resp);
		}
	}

	@Override
	public String toString() {
		return "ServletServicer[alias=" + getAlias() + ", servlet=" + servlet
				+ ", initparams=" + servletConfig + ", context="
				+ getHttpContext() + "]";
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	/**
	 * If the registered alias for this servlet is shorter than the request URI,
	 * then the servlet must see the request differently.
	 * 
	 * The servlet path must always match the alias, and the remainder of the
	 * URI must be available as the path info.
	 */
	private class AdjustedRequestWrapper extends HttpServletRequestWrapper {
		private final String servletPath;
		private final String pathInfo;

		public AdjustedRequestWrapper(HttpServletRequest req, String alias) {
			super(req);
			String originalServletPath = req.getServletPath();
			String originalPathInfo = req.getPathInfo();

			this.servletPath = alias;

			String newPathInfo = figureRequestUri(req)
					.substring(alias.length());
			this.pathInfo = (newPathInfo.isEmpty()) ? null : newPathInfo;

			log.debug("servletPath '" + originalServletPath + "' becomes '"
					+ this.servletPath + "'");
			log.debug("pathInfo '" + originalPathInfo + "' becomes '"
					+ this.pathInfo + "'");
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

}
