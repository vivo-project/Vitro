package edu.cornell.mannlib.vitro.webapp.dao.jena;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

public class LoginEvent extends LoginLogoutEvent {

    public LoginEvent( String loginUri ){        
        this.loginUri = loginUri;       
    }
	
}
