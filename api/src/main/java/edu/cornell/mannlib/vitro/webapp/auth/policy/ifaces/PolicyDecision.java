/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces;

/**
 * Object to represent a decision from a policy. The intent is that the message would be presented to users to indicate
 * why they are not authorized for some action.
 */
public interface PolicyDecision {

    DecisionResult getDecisionResult();

    String getStackTrace();

    String getMessage();

    String getDebuggingInfo();

}
