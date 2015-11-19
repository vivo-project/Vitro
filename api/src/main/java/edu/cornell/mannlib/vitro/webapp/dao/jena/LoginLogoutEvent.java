/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

public abstract class LoginLogoutEvent {

	protected String loginUri = null;
    
    public String getLoginUri() {
        return loginUri;
    }
}
