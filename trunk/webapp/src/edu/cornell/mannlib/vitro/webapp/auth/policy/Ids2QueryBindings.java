/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.List;

import com.hp.hpl.jena.query.QuerySolutionMap;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

public interface Ids2QueryBindings {    
    /**
     * Returns null if no binding can be made.  In some implementations this 
     * might be different than an empty QuerySolutionMap.  Must be thread safe.
     */
    public List<QuerySolutionMap> makeScopeBinding(IdentifierBundle ids, RequestedAction action );
}
