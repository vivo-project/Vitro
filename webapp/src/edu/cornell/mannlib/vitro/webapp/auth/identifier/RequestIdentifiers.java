/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Build a list of Identifiers that apply to the current request and cache them
 * in the request.
 */
public class RequestIdentifiers {
	private static final String ATTRIBUTE_ID_BUNDLE = RequestIdentifiers.class
			.getName();

	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

	/**
	 * If the currently applicable Identifiers have been cached in the request,
	 * get them. If not, assemble them from the active factories, and cache them
	 * in the request.
	 * 
	 * This method might return an empty bundle, but it never returns null.
	 */
	public static IdentifierBundle getIdBundleForRequest(ServletRequest request) {
		if (!(request instanceof HttpServletRequest)) {
			return new ArrayIdentifierBundle();
		}
		HttpServletRequest hreq = (HttpServletRequest) request;

		Object obj = hreq.getAttribute(ATTRIBUTE_ID_BUNDLE);
		if (obj == null) {
			obj = ActiveIdentifierBundleFactories.getIdentifierBundle(hreq);
			hreq.setAttribute(ATTRIBUTE_ID_BUNDLE, obj);
		}

		if (!(obj instanceof IdentifierBundle)) {
			throw new IllegalStateException("Expected to find an instance of "
					+ IdentifierBundle.class.getName()
					+ " in the request, but found an instance of "
					+ obj.getClass().getName() + " instead.");
		}

		return (IdentifierBundle) obj;
	}

	/**
	 * The login status has changed, so discard the cached Identifiers.
	 */
	public static void resetIdentifiers(ServletRequest request) {
		request.removeAttribute(ATTRIBUTE_ID_BUNDLE);
	}

}
