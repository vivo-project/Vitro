/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.rules;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.CheckFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.QuerySolution;

public class AccessRuleFactory {

    private static final Log log = LogFactory.getLog(AccessRuleFactory.class);

    public static AccessRule createRule(QuerySolution qs) {
        AccessRule ar = new AccessRuleImpl();
        String ruleUri = qs.getResource(PolicyLoader.RULE).getURI();
        ar.setRuleUri(ruleUri);
        ar.addCheck(CheckFactory.createCheck(qs));
        if (qs.contains("decision_id") && qs.get("decision_id").isLiteral()) {
            String decisionId = qs.getLiteral("decision_id").getString();
            if (RuleDecision.DENY.toString().equals(decisionId)) {
                ar.setAllowMatched(false);
            }
        }
        return ar;
    }
}
