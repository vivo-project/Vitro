/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.distribute;

import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;

/**
 * Make this information available to each DataDistributor.
 */
public interface DataDistributorContext {
	Map<String, String[]> getRequestParameters();

	RequestModelAccess getRequestModels();

	boolean isAuthorized(AuthorizationRequest ar);
}
