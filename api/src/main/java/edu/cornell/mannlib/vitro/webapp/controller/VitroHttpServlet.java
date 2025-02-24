/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.DisplayMessage;
import edu.cornell.mannlib.vitro.webapp.beans.ResourceBean;
import edu.cornell.mannlib.vitro.webapp.config.ContextPath;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.LogoutRedirector;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;

public class VitroHttpServlet extends HttpServlet implements MultipartRequestWrapper.ParsingStrategy {
	private static final long serialVersionUID = 1L;

	protected static DateFormat publicDateFormat = new SimpleDateFormat(
			"M/dd/yyyy");

	private static final Log log = LogFactory.getLog(VitroHttpServlet.class
			.getName());

	public final static String XHTML_MIMETYPE = "application/xhtml+xml";
	public final static String HTML_MIMETYPE = "text/html";

	public final static String RDFXML_MIMETYPE = "application/rdf+xml";
    public final static String JSON_MIMETYPE = "application/json";
    public final static String JSON_LD_MIMETYPE = "application/ld+json";
	public final static String N3_MIMETYPE = "text/n3"; // unofficial and unregistered
	public final static String TTL_MIMETYPE = "text/turtle"; // unofficial and unregistered

	@Override
	public final void service(ServletRequest req, ServletResponse resp)
			throws ServletException, IOException {
		if ((req instanceof HttpServletRequest)
				&& (resp instanceof HttpServletResponse)) {
			HttpServletRequest hreq = (HttpServletRequest) req;

			hreq = MultipartRequestWrapper.parse(hreq, this);

			if (log.isTraceEnabled()) {
				dumpRequestHeaders(hreq);
			}

			super.service(hreq, resp);
		} else {
			super.service(req, resp);
		}
	}

	/**
	 * Override this to change the maximum size of uploaded files in multipart
	 * requests.
	 */
	@Override
	public long maximumMultipartFileSize() {
		return 50 * 1024 * 1024; // default is 50 megabytes
	}

	/**
	 * Override this to change the way that exceptions are handled when parsing
	 * a multipart request. Be aware that multipart parameters have been lost,
	 * and that may include form fields.
	 */
	@Override
	public boolean stashFileSizeException() {
		return false;
	}

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
	 *            the combination of RequestedActions that must be authorized.
	 */
	protected boolean isAuthorizedToDisplayPage(HttpServletRequest request,
			HttpServletResponse response, AuthorizationRequest actions) {
		// Record restricted pages so we won't return to them on logout
		if (actions != AuthorizationRequest.AUTHORIZED) {
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
					I18n.bundle(request).text("insufficient_authorization"));
			response.sendRedirect(ContextPath.getPath(request));
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
		return ContextPath.getPath(request) + Controllers.AUTHENTICATE
				+ "?afterLogin=" + encodedAfterLoginUrl;
	}

	protected void sortForPickList(List<? extends ResourceBean> beans,
	        VitroRequest vreq) {
	    beans.sort(new PickListSorter(vreq));
	}

	protected class PickListSorter implements Comparator<ResourceBean> {

	    Collator collator;

	    public PickListSorter(VitroRequest vreq) {
	        this.collator = vreq.getCollator();
	    }

	    public int compare(ResourceBean b1, ResourceBean b2) {
	        return collator.compare(b1.getPickListName(), b2.getPickListName());
	    }

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
