/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Policy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.rules.AccessRule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DynamicPolicy implements Policy {
    private static final Log log = LogFactory.getLog(DynamicPolicy.class);
    private String uri;

    @Override
    public String getUri() {
        return uri;
    }

    private long priority;

    public long getPriority() {
        return priority;
    }

    private Set<AccessRule> rules = Collections.synchronizedSet(new HashSet<>());

    public Set<AccessRule> getRules() {
        return rules;
    }

    public void addRules(Set<AccessRule> addition) {
        rules.addAll(addition);
    }

    public DynamicPolicy(String uri, long priority) {
        this.uri = uri;
        this.priority = priority;
    }

    @Override
    public PolicyDecision decide(AuthorizationRequest ar) {
        AccessObject whatToAuth = ar.getAccessObject();
        if (whatToAuth == null) {
            return defaultDecision("whatToAuth was null");
        }
        for (AccessRule rule : getFilteredRules(ar)) {
            if (rule.match(ar)) {
                if (rule.isAllowMatched()) {
                    log.debug("Access rule " + rule.getRuleUri() + " approves request " + whatToAuth);
                    String message = "Dynamic policy '" + uri + "' rule '" + rule.getRuleUri() + "' approved " + ar;
                    return new BasicPolicyDecision(DecisionResult.AUTHORIZED, message);
                } else {
                    log.debug("Access rule " + rule.getRuleUri() + " rejects request " + whatToAuth);
                    String message = "Dynamic policy '" + uri + "' rule '" + rule.getRuleUri() + "' rejected " + ar;
                    return new BasicPolicyDecision(DecisionResult.UNAUTHORIZED, message);
                }
            } else {
                log.trace("Dynamic policy '" + uri + "' rule '" + rule.getRuleUri() + "' doesn't match the request "
                        + ar);
            }
        }

        return defaultDecision("no permission will approve " + whatToAuth);
    }

    public Set<AccessRule> getFilteredRules(AuthorizationRequest ar) {
        return rules;
    }

    private PolicyDecision defaultDecision(String message) {
        return new BasicPolicyDecision(DecisionResult.INCONCLUSIVE, message);
    }
}
