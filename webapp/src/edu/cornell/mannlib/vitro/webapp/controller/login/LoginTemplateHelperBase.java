/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.login;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;

/**
 * A temporary means of displaying the Login templates within the SiteAdmin
 * form.
 * 
 * The constructor insures that the ServletContext is set.
 */
public class LoginTemplateHelperBase extends FreemarkerHttpServlet {
	private final ServletContext servletContext;

	LoginTemplateHelperBase(HttpServletRequest req) {
		this.servletContext = req.getSession().getServletContext();
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

}
