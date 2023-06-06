package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Policy;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.rules.AccessRule;

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
        //IdentifierBundle ac_subject = ar.getIds();
        AccessObject whatToAuth = ar.getAccessObject();
        //if (ac_subject == null) {
        //    return defaultDecision("whomToAuth was null");
        //}
        if (whatToAuth == null) {
            return defaultDecision("whatToAuth was null");
        }
        for (AccessRule rule : getFilteredRules(ar)) {
            if (rule.match(ar)) {
                if(rule.isAllowMatched()) {
                    log.debug("Access rule " + rule + " approves request " + whatToAuth);
                    return new BasicPolicyDecision(DecisionResult.AUTHORIZED, "Dynamic policy '" + uri + "' rule '" + rule + "' approved " + ar);
                } else {
                    log.debug("Access rule " + rule + " rejects request " + whatToAuth);
                    return new BasicPolicyDecision(DecisionResult.UNAUTHORIZED, "Dynamic policy '" + uri + "' rule '" + rule + "' rejected " + ar);
                }
            } else {
                log.trace("Dynamic policy '" + uri + "' rule '" + rule + "' doesn't match the request " + ar);
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
