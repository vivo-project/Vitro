/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

public class LogoutEvent extends LoginLogoutEvent {

    public LogoutEvent( String loginUri ){
        this.loginUri = loginUri;
    }

}
