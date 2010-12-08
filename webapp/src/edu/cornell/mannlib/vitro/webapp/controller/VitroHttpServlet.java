/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.DisplayMessage;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.LogoutRedirector;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State;

public class VitroHttpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected static DateFormat publicDateFormat = new SimpleDateFormat(
			"M/dd/yyyy");

	private static final Log log = LogFactory.getLog(VitroHttpServlet.class
			.getName());

	public final static String XHTML_MIMETYPE = "application/xhtml+xml";
	public final static String HTML_MIMETYPE = "text/html";

	public final static String RDFXML_MIMETYPE = "application/rdf+xml";
	public final static String N3_MIMETYPE = "text/n3"; // unofficial and
														// unregistered
	public final static String TTL_MIMETYPE = "text/turtle"; // unofficial and
																// unregistered

	/**
	 * Show this to the user if they are logged in, but still not authorized to
	 * view the page.
	 */
	private static final String INSUFFICIENT_AUTHORIZATION_MESSAGE = "We're sorry, "
			+ "but you are not authorized to view the page you requested. "
			+ "If you think this is an error, "
			+ "please contact us and we'll be happy to help.";

	/**
	 * Setup the auth flag, portal flag and portal bean objects. Put them in the
	 * request attributes.
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		setup(request);
	}

	protected final void setup(HttpServletRequest request) {

		// check to see if VitroRequestPrep filter was run
		if (request.getAttribute("appBean") == null
				|| request.getAttribute("webappDaoFactory") == null) {
			log.warn("request scope was not prepared by VitroRequestPrep");
		}
	}

	/**
	 * doPost does the same thing as the doGet method
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	// ----------------------------------------------------------------------
	// static utility methods for all Vitro servlets
	// ----------------------------------------------------------------------

	/**
	 * If not logged in, redirect them to the login page.
	 */
	public static boolean checkLoginStatus(HttpServletRequest request,
			HttpServletResponse response) {
		LogoutRedirector.recordRestrictedPageUri(request);
		if (LoginStatusBean.getBean(request).isLoggedIn()) {
			return true;
		} else {
			redirectToLoginPage(request, response);
			return false;
		}
	}

	/**
	 * If not logged in at the required level, redirect them to the appropriate page.
	 */
	public static boolean checkLoginStatus(HttpServletRequest request,
			HttpServletResponse response, int minimumLevel) {
		LogoutRedirector.recordRestrictedPageUri(request);
		if (LoginStatusBean.getBean(request).isLoggedInAtLeast(minimumLevel)) {
			return true;
		} else if (LoginStatusBean.getBean(request).isLoggedIn()) {
			redirectToInsufficientAuthorizationPage(request, response);
			return false;
		} else {
			redirectToLoginPage(request, response);
			return false;
		}
	}

	/**
	 * Logged in, but with insufficent authorization. Send them to the
	 * home page with a message. They won't be coming back.
	 */
	private static void redirectToInsufficientAuthorizationPage(
			HttpServletRequest request, HttpServletResponse response) {
		try {
			DisplayMessage.setMessage(request, INSUFFICIENT_AUTHORIZATION_MESSAGE);
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
			HttpServletResponse response)  {
		LoginProcessBean bean = new LoginProcessBean();
		bean.setState(State.LOGGING_IN);
		bean.setAfterLoginUrl(assembleAfterLoginUrl(request));
		LoginProcessBean.setBean(request, bean);
		
		String loginPage = request.getContextPath() + Controllers.LOGIN;
		
		try {
			response.sendRedirect(loginPage);
		} catch (IOException ioe) {
			log.error("Could not redirect to login page");
		}
	}

	private static String assembleAfterLoginUrl(HttpServletRequest request) {
		String queryString = request.getQueryString();
		if ((queryString == null) || queryString.isEmpty()) {
			return request.getRequestURI();
		} else {
			return request.getRequestURI() + "?" + queryString;
		}
	}

	/**
	 * If logging is set to the TRACE level, dump the HTTP headers on the request.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void service(ServletRequest req, ServletResponse resp)
			throws ServletException, IOException {
		if (log.isTraceEnabled()) {
			HttpServletRequest request = (HttpServletRequest) req;
			Enumeration<String> names = request.getHeaderNames();
			log.trace("----------------------request:" + request.getRequestURL());
			while (names.hasMoreElements()) {
				String name = names.nextElement();
				if (!BORING_HEADERS.contains(name)) {
					log.trace(name + "=" + request.getHeader(name));
				}
			}
		}

		super.service(req, resp);
	}

	/** Don't dump the contents of these headers, even if log.trace is enabled. */
	private static final List<String> BORING_HEADERS = new ArrayList<String>(
			Arrays.asList(new String[] { "host", "user-agent", "accept",
					"accept-language", "accept-encoding", "accept-charset",
					"keep-alive", "connection" }));
	
}
