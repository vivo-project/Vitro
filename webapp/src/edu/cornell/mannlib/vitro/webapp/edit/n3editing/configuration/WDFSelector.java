/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public interface WDFSelector {
    public WebappDaoFactory getWdf(HttpServletRequest request, ServletContext context);
}
