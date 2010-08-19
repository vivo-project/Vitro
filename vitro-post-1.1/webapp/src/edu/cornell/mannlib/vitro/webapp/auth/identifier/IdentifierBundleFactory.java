/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Creates an IdentifierBundle for a ServletRequest/HttpSession.  Useful
 * for getting the identifiers that should be associated with a request to
 * a servlet or a JSP.
 *
 * We have this method signature because these are the object that are accessible
 * from JSP TagSupport.pageContext.
 *
 * @author bdc34
 *
 */
public interface IdentifierBundleFactory {
    public IdentifierBundle getIdentifierBundle(ServletRequest request, HttpSession session, ServletContext context);

}
