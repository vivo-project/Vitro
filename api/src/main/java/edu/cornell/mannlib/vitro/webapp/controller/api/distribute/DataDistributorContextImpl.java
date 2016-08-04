/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.distribute;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.api.distribute.DataDistributorContext;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;

/**
 * Build a DataDistributorContext around the current HTTP request.
 */
public class DataDistributorContextImpl implements DataDistributorContext {
	private final HttpServletRequest req;

	public DataDistributorContextImpl(HttpServletRequest req) {
		this.req = req;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String[]> getRequestParameters() {
		return req.getParameterMap();
	}

	@Override
	public RequestModelAccess getRequestModels() {
		return ModelAccess.on(req);
	}

	@Override
	public boolean isAuthorized(AuthorizationRequest actions) {
		return PolicyHelper.isAuthorizedForActions(req, actions);
	}

}
