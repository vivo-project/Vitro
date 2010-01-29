package edu.cornell.mannlib.vitro.webapp.flags;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;

public class AuthFlag {
    private boolean filterByAuthLevel = true;
    public boolean isFilterByAuthLevel() { return filterByAuthLevel;}
    public void setFilterByAuthLevel(boolean b) { this.filterByAuthLevel = b; }

    private int userSecurityLevel = -1;
    public  void setUserSecurityLevel(int i){ userSecurityLevel = i;}
    public  int getUserSecurityLevel() { return userSecurityLevel; }

}
