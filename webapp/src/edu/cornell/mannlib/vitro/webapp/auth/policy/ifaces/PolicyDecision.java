package edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

public interface PolicyDecision {
    public Authorization getAuthorized();

    public String getStackTrace();
    public String getMessage();
    public String getDebuggingInfo();
}
