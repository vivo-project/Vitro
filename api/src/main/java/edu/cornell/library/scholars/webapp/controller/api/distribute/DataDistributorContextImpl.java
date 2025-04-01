/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;

/**
 * Build a DataDistributorContext around the current HTTP request.
 */
public class DataDistributorContextImpl implements DataDistributorContext {
    private final RequestModelAccess requestAccess;
    private Map<String, String[]> parameters;
    private UserAccount account;

    public DataDistributorContextImpl(HttpServletRequest req) {
        this.requestAccess = ModelAccess.on(req);
        this.parameters = req.getParameterMap();
        this.account = PolicyHelper.getUserAccount(req);
    }

    public DataDistributorContextImpl(RequestModelAccess requestAccess, Map<String, String[]> parameters,
            UserAccount account) {
        this.requestAccess = requestAccess;
        this.parameters = parameters;
        this.account = account;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String[]> getRequestParameters() {
        return parameters;
    }

    @Override
    public RequestModelAccess getRequestModels() {
        return requestAccess;
    }

    @Override
    public boolean isAuthorized(AuthorizationRequest actions) {
        return PolicyHelper.isAuthorizedForActions(account , actions);
    }

}
