/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * When a user logs out, where will they go?
 */
public class LogoutRedirector {
	private static final Log log = LogFactory.getLog(LogoutRedirector.class);
	private static final String ATTRIBUTE_RESTRICTED_PAGE_URIS = "uris_of_restricted_pages";

	/**
	 * If the page they were on was not restricted, send them back to it.
	 * Otherwise, send them to the home page.
	 */
	public static String getRedirectUrl(HttpServletRequest request,
			HttpServletResponse response, String referrer) throws IOException {
		String referringUri = figureUriFromUrl(request, referrer);

		log.debug("referringUri: '" + referringUri + "', restrictedUris="
				+ getRestrictedPageUris(request));

		if ((referringUri == null)
				|| (getRestrictedPageUris(request).contains(referringUri))) {
			log.debug("Sending to home page.");
			return request.getContextPath();
		} else {
			log.debug("Sending back to referring page.");
			return referrer;
		}
	}

	private static String figureUriFromUrl(HttpServletRequest request,
			String referrer) {
		String postContext = breakBeforeContextPath(request.getContextPath(),
				referrer);
		String uri = removeQueryString(postContext);
		log.debug("referrer='" + referrer + "', uri='" + uri + "'");
		return uri;
	}

	private static String breakBeforeContextPath(String contextPath, String url) {
		if (url == null) {
			return null;
		}

		int contextHere = url.indexOf(contextPath);
		if (contextHere == -1) {
			return null;
		} else {
			return url.substring(contextHere);
		}
	}

	private static String removeQueryString(String fragment) {
		if (fragment == null) {
			return null;
		}

		int questionHere = fragment.indexOf('?');
		if (questionHere == -1) {
			return fragment;
		} else {
			return fragment.substring(0, questionHere);
		}
	}

	/**
	 * This must be called each time VitroHttpRequest checks to see whether a
	 * page's restrictions are met, so we know which pages are restricted.
	 * 
	 * We might be content to just know the last restricted page, but that could
	 * lead to problems if two pages are nested.
	 */
	public static void recordRestrictedPageUri(HttpServletRequest request) {
		String uri = request.getRequestURI();
		log.debug("Recording restricted URI: '" + uri + "'");
		getRestrictedPageUris(request).add(uri);
	}

	private static Set<String> getRestrictedPageUris(HttpServletRequest request) {
		HttpSession session = request.getSession();

		@SuppressWarnings("unchecked")
		Set<String> restrictedPageUris = (Set<String>) session
				.getAttribute(ATTRIBUTE_RESTRICTED_PAGE_URIS);

		if (restrictedPageUris == null) {
			restrictedPageUris = new HashSet<String>();
			session.setAttribute(ATTRIBUTE_RESTRICTED_PAGE_URIS,
					restrictedPageUris);
		}

		return restrictedPageUris;
	}
}
