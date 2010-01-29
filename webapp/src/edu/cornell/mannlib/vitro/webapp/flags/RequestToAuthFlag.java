package edu.cornell.mannlib.vitro.webapp.flags;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.flags.AuthFlag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by IntelliJ IDEA.
 * User: bdc34
 * Date: Apr 5, 2007
 * Time: 11:12:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class RequestToAuthFlag {
    public static AuthFlag makeAuthFlag(HttpServletRequest request){
        AuthFlag authFlag = new AuthFlag();
        authFlag.setUserSecurityLevel(0);

        HttpSession currentSession = request.getSession();
        if( currentSession == null )
            return authFlag;

        LoginFormBean f = (LoginFormBean) currentSession.getAttribute( "loginHandler" );
        if (f!=null) {
            if (f.getLoginStatus().equals("authenticated")) { // test if session is still valid
                if (currentSession.getId().equals(f.getSessionId())) {
                    if (request.getRemoteAddr().equals(f.getLoginRemoteAddr())) {
                        authFlag.setUserSecurityLevel(Integer.parseInt(f.getLoginRole()));
                    }
                }
            }
        }

        return authFlag;
    }
}
