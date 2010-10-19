/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.flags;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;

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
        authFlag.setUserSecurityLevel(LoginStatusBean.getBean(request).getSecurityLevel());
        return authFlag;
    }
}
