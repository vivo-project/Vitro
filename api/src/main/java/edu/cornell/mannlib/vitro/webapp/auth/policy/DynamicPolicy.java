/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Policy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.rules.AccessRule;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DynamicPolicy implements Policy {
    private static final Log log = LogFactory.getLog(DynamicPolicy.class);
    private String uri;

    @Override
    public String getUri() {
        return uri;
    }
    
    @Override
    public String getShortUri() {
        return shortenUri(uri);
    }

    private long priority;

    public long getPriority() {
        return priority;
    }

    private Set<AccessRule> rules = Collections.synchronizedSet(new HashSet<>());

    public Set<AccessRule> getRules() {
        return rules;
    }

    public void addRules(Collection<AccessRule> collection) {
        rules.addAll(collection);
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
            String policyUri = shortenUri(uri);
            String ruleUri = shortenUri(rule.getRuleUri());
            if (rule.match(ar)) {
                if (rule.isAllowMatched()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Policy '" + policyUri + "' rule '" + ruleUri + "' approved request " + whatToAuth);
                    }
                    String message = "Policy '" + policyUri + "' rule '" + ruleUri + "' approved " + ar;
                    return new BasicPolicyDecision(DecisionResult.AUTHORIZED, message);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Policy '" + policyUri + "' rule " + ruleUri + " rejected request " + whatToAuth);
                    }
                    String message = "Policy '" + policyUri + "' rule '" + ruleUri + "' rejected request" + ar;
                    return new BasicPolicyDecision(DecisionResult.UNAUTHORIZED, message);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Policy '" + policyUri + "' rule '" + ruleUri + "' didn't match request " + ar);
                }
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

    private static String shortenUri(String uri) {
        if (uri.startsWith(VitroVocabulary.AUTH_INDIVIDUAL_PREFIX)) {
            return "ai:" + uri.substring(VitroVocabulary.AUTH_INDIVIDUAL_PREFIX.length());
        }
        return uri;
    }
}
