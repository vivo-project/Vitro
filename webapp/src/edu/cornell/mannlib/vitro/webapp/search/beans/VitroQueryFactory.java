package edu.cornell.mannlib.vitro.webapp.search.beans;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.flags.PortalFlag;
import edu.cornell.mannlib.vitro.webapp.search.SearchException;

public interface VitroQueryFactory {
    public VitroQuery getQuery(VitroRequest req, PortalFlag portalState)throws SearchException;
}
