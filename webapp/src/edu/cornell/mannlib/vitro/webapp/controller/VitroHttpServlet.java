/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.DisplayMessage;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.LogoutRedirector;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;

public class VitroHttpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected static DateFormat publicDateFormat = new SimpleDateFormat(
			"M/dd/yyyy");

	private static final Log log = LogFactory.getLog(VitroHttpServlet.class
			.getName());

	public final static String XHTML_MIMETYPE = "application/xhtml+xml";
	public final static String HTML_MIMETYPE = "text/html";

	public final static String RDFXML_MIMETYPE = "application/rdf+xml";
	public final static String N3_MIMETYPE = "text/n3"; // unofficial and unregistered
	public final static String TTL_MIMETYPE = "text/turtle"; // unofficial and unregistered

	@Override
	public final void service(ServletRequest req, ServletResponse resp)
			throws ServletException, IOException {
		if ((req instanceof HttpServletRequest)
				&& (resp instanceof HttpServletResponse)) {
			HttpServletRequest hreq = (HttpServletRequest) req;
			HttpServletResponse hresp = (HttpServletResponse) resp;

			if (log.isTraceEnabled()) {
				dumpRequestHeaders(hreq);
			}
		}

		super.service(req, resp);
	}

	/**
	 * Show this to the user if they are logged in, but still not authorized to
	 * view the page.
	 */
	private static final String INSUFFICIENT_AUTHORIZATION_MESSAGE = "We're sorry, "
			+ "but you are not authorized to view the page you requested. "
			+ "If you think this is an error, "
			+ "please contact us and we'll be happy to help.";

	/**
	 * doGet does nothing.
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// nothing to do
	}

	/**
	 * doPost does the same thing as the doGet method
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * Don't display a page that the user isn't authorized to see.
	 * 
	 * @param actions
	 *            the RequestedActions that must be authorized.
	 */
	protected boolean isAuthorizedToDisplayPage(HttpServletRequest request,
			HttpServletResponse response, RequestedAction... actions) {
		return isAuthorizedToDisplayPage(request, response,
				new Actions(Arrays.asList(actions)));
	}

	/**
	 * Don't display a page that the user isn't authorized to see.
	 * 
	 * @param actions
	 *            the combination of RequestedActions that must be authorized.
	 */
	protected boolean isAuthorizedToDisplayPage(HttpServletRequest request,
			HttpServletResponse response, Actions actions) {
		// Record restricted pages so we won't return to them on logout
		if (!actions.isEmpty()) {
			LogoutRedirector.recordRestrictedPageUri(request);
		}

		if (PolicyHelper.isAuthorizedForActions(request, actions)) {
			log.debug("Servlet '" + this.getClass().getSimpleName()
					+ "' is authorized for actions: " + actions);
			return true;
		}

		log.debug("Servlet '" + this.getClass().getSimpleName()
				+ "' is not authorized for actions: " + actions);
		redirectUnauthorizedRequest(request, response);
		return false;
	}

	// ----------------------------------------------------------------------
	// static utility methods for all Vitro servlets
	// ----------------------------------------------------------------------

	public static void redirectUnauthorizedRequest(HttpServletRequest request,
			HttpServletResponse response) {
		if (LoginStatusBean.getBean(request).isLoggedIn()) {
			redirectToInsufficientAuthorizationPage(request, response);
		} else {
			redirectToLoginPage(request, response);
		}
	}
	
	/**
	 * Logged in, but with insufficient authorization. Send them to the home page
	 * with a message. They won't be coming back.
	 */
	public static void redirectToInsufficientAuthorizationPage(
			HttpServletRequest request, HttpServletResponse response) {
		try {
			DisplayMessage.setMessage(request,
					INSUFFICIENT_AUTHORIZATION_MESSAGE);
			response.sendRedirect(request.getContextPath());
		} catch (IOException e) {
			log.error("Could not redirect to show insufficient authorization.");
		}
	}

	/**
	 * Not logged in. Send them to the login page, and then back to the page
	 * that invoked this.
	 */
	public static void redirectToLoginPage(HttpServletRequest request,
			HttpServletResponse response) {
		String returnUrl = assembleUrlToReturnHere(request);
		String loginUrlWithReturn = assembleLoginUrlWithReturn(request,
				returnUrl);

		try {
			response.sendRedirect(loginUrlWithReturn);
		} catch (IOException ioe) {
			log.error("Could not redirect to login page");
		}
	}

	private static String assembleUrlToReturnHere(HttpServletRequest request) {
		String queryString = request.getQueryString();
		if ((queryString == null) || queryString.isEmpty()) {
			return request.getRequestURI();
		} else {
			return request.getRequestURI() + "?"
					+ UrlBuilder.urlEncode(queryString);
		}
	}

	private static String assembleLoginUrlWithReturn(
			HttpServletRequest request, String afterLoginUrl) {
		String encodedAfterLoginUrl = afterLoginUrl;
		try {
			encodedAfterLoginUrl = URLEncoder.encode(afterLoginUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("Really? No UTF-8 encoding?", e);
		}
		return request.getContextPath() + Controllers.AUTHENTICATE
				+ "?afterLogin=" + encodedAfterLoginUrl;
	}

	/**
	 * If logging on the subclass is set to the TRACE level, dump the HTTP
	 * headers on the request.
	 */
	private void dumpRequestHeaders(HttpServletRequest req) {
		@SuppressWarnings("unchecked")
		Enumeration<String> names = req.getHeaderNames();

		Log subclassLog = LogFactory.getLog(this.getClass());
		subclassLog.trace("----------------------request:"
				+ req.getRequestURL());
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			if (!BORING_HEADERS.contains(name)) {
				subclassLog.trace(name + "=" + req.getHeader(name));
			}
		}
	}

	/** Don't dump the contents of these headers, even if log.trace is enabled. */
	private static final List<String> BORING_HEADERS = new ArrayList<String>(
			Arrays.asList(new String[] { "host", "user-agent", "accept",
					"accept-language", "accept-encoding", "accept-charset",
					"keep-alive", "connection" }));

	/**
	 * A child class may call this if logging is set to debug level.
	 */
	protected void dumpRequestParameters(HttpServletRequest req) {
		Log subclassLog = LogFactory.getLog(this.getClass());
		
		@SuppressWarnings("unchecked")
		Map<String, String[]> map = req.getParameterMap();
		
		for (String key : map.keySet()) {
			String[] values = map.get(key);
			subclassLog.debug("Parameter '" + key + "' = "
					+ Arrays.deepToString(values));
		}
	}
}
