/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;

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
	 * If not logged in, send them to the login page.
	 */
	public static boolean checkLoginStatus(HttpServletRequest request,
			HttpServletResponse response) {
		if (LoginStatusBean.getBean(request).isLoggedIn()) {
			return true;
		} else {
			try {
				redirectToLoginPage(request, response);
			} catch (IOException ioe) {
				log.error("checkLoginStatus() could not redirect to login page");
			}
			return false;
		}
	}

	/**
	 * If not logged in at the minimum level or higher, send them to the login
	 * page.
	 */
	public static boolean checkLoginStatus(HttpServletRequest request,
			HttpServletResponse response, int minimumLevel) {
		if (LoginStatusBean.getBean(request).isLoggedInAtLeast(minimumLevel)) {
			return true;
		} else {
			try {
				redirectToLoginPage(request, response);
			} catch (IOException ioe) {
				log.error("checkLoginStatus() could not redirect to login page");
			}
			return false;
		}
	}

	/**
	 * Not adequately logged in. Send them to the login page, and then back to
	 * the page that invoked this.
	 */
	public static void redirectToLoginPage(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String postLoginRequest;

		String queryString = request.getQueryString();
		if ((queryString == null) || queryString.isEmpty()) {
			postLoginRequest = request.getRequestURI();
		} else {
			postLoginRequest = request.getRequestURI() + "?" + queryString;
		}

		request.getSession().setAttribute("postLoginRequest", postLoginRequest);
		String loginPage = request.getContextPath() + Controllers.LOGIN;
		response.sendRedirect(loginPage);
	}

}
