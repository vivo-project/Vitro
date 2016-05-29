/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;

/**
 *  Represents the result of querying a Policy for permission to perform
 * a RequestedAction.
 */
public class BasicPolicyDecision implements PolicyDecision{

    String debuggingInfo;
    String message;
    String StackTrace;
    Authorization authorized;



    public BasicPolicyDecision( Authorization authorized, String message) {
        super();
        this.message = message;
        this.authorized = authorized;
    }

    public Authorization getAuthorized() {
        return authorized;
    }
    public BasicPolicyDecision setAuthorized(Authorization auth) {
        this.authorized = auth;
        return this;
    }
    public String getDebuggingInfo() {
        return debuggingInfo;
    }
    public BasicPolicyDecision setDebuggingInfo(String debuggingInfo) {
        this.debuggingInfo = debuggingInfo;
        return this;
    }
    public String getMessage() {
        return message;
    }
    public BasicPolicyDecision setMessage(String message) {
        this.message = message;
        return this;
    }
    public String getStackTrace() {
        return StackTrace;
    }
    public void setStackTrace(String stackTrace) {
        StackTrace = stackTrace;
    }
    
    public String toString(){ 
        return authorized + ": " + message;
    }
}
