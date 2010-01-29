package edu.cornell.mannlib.vitro.webapp.dao.jena;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

public class LogoutEvent extends LoginLogoutEvent {
	     
    public LogoutEvent( String loginUri ){        
        this.loginUri = loginUri;       
    }
    
}
