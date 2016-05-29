/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;

/**
 * Policy decision that is made from some analysis of a set of decisions. 
 * @author bdc34
 *
 */
public class CompositPolicyDecision extends BasicPolicyDecision implements PolicyDecision {
    List<PolicyDecision> subDecisions;
    
    public CompositPolicyDecision(Authorization auth, String message, List<PolicyDecision> subDecisions){
        super( auth, message);
        this.subDecisions = subDecisions;    
    }

}
