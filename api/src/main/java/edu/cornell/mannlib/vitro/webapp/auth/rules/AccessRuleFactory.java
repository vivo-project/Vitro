package edu.cornell.mannlib.vitro.webapp.auth.rules;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.QuerySolution;

public class AccessRuleFactory {

    private static final Log log = LogFactory.getLog(AccessRuleFactory.class);

    public static AccessRule createRule(QuerySolution qs) {
        AccessRule ar = new AccessRuleImpl();
        String ruleUri = qs.getResource(PolicyLoader.RULE).getURI();
        if (qs.contains("dataSetUri")) {
            ruleUri += "." + qs.getResource("dataSetUri").getURI();
        }

        ar.setRuleUri(ruleUri);
        ar.addAttribute(AttributeFactory.createAttribute(qs));
        if (qs.contains("decision_id") && qs.get("decision_id").isLiteral()) {
            String decisionId = qs.getLiteral("decision_id").getString();
            if (decisionId.equals("DENY")) {
                ar.setAllowMatched(false);
            }
        }
        return ar;
    }
}
