/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces;

public interface PolicyDecision {
    public Authorization getAuthorized();

    public String getStackTrace();
    public String getMessage();
    public String getDebuggingInfo();
}
