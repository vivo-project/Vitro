/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

import javax.servlet.http.HttpServletRequest;

/**
 * Creates an IdentifierBundle for a ServletRequest/HttpSession. Useful for
 * getting the identifiers that should be associated with a request.
 */
public interface IdentifierBundleFactory {
	/**
	 * Return the IdentifierBundle from this factory. May return an empty
	 * bundle, but never returns null.
	 */
	public IdentifierBundle getIdentifierBundle(HttpServletRequest request);
}
