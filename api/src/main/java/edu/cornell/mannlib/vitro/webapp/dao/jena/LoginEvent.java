/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

public class LoginEvent extends LoginLogoutEvent {

    public LoginEvent( String loginUri ){
        this.loginUri = loginUri;
    }

}
