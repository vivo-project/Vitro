/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.flags;


public class AuthFlag {
    private boolean filterByAuthLevel = true;
    public boolean isFilterByAuthLevel() { return filterByAuthLevel;}
    public void setFilterByAuthLevel(boolean b) { this.filterByAuthLevel = b; }

    private int userSecurityLevel = -1;
    public  void setUserSecurityLevel(int i){ userSecurityLevel = i;}
    public  int getUserSecurityLevel() { return userSecurityLevel; }

}
