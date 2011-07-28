/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.beans;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.search.SearchException;

public interface VitroQueryFactory {
    public VitroQuery getQuery(VitroRequest req) throws SearchException;
}
