/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering.filters;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public interface FilterFactory {

    public VitroFilters getFilters(HttpServletRequest request,WebappDaoFactory wdf);
}
