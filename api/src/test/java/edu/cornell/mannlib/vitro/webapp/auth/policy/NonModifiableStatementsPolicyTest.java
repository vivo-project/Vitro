package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.DataPropertyStatementAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.ObjectPropertyStatementAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.SimpleAuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.rules.AccessRule;
import edu.cornell.mannlib.vitro.webapp.beans.Property;

public class NonModifiableStatementsPolicyTest extends PolicyTest{

    public static final String NOT_MODIFIABLE_STATEMENTS_POLICY_PATH = "policy_not_modifiable_statements";
    
    @Test
    public void testNonModifiableStatementsPolicy() {        
        load(USER_ACCOUNTS_HOME_EVERYTIME + NOT_MODIFIABLE_STATEMENTS_POLICY_PATH + EXT);
        load(USER_ACCOUNTS_HOME_FIRSTTIME + NOT_MODIFIABLE_STATEMENTS_POLICY_PATH + DATASET + EXT);

        String policyUri = "https://vivoweb.org/ontology/vitro-application/auth/individual/NotModifiableStatementsPolicy";
        DynamicPolicy policy = loader.loadPolicy(policyUri);
        assertTrue(policy != null);
        assertEquals(8000, policy.getPriority());
        assertEquals(5, policy.getRules().size());
        final AccessRule rule = policy.getRules().iterator().next();
        assertEquals(false, rule.isAllowMatched());
        for (AccessRule irule : policy.getRules()) {
            assertEquals(3, irule.getAttributesCount());
        }
        
        AccessObject ao = new ObjectPropertyStatementAccessObject(null, "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Valid", null, null);
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(ao, AccessOperation.DROP);
        assertEquals(DecisionResult.UNAUTHORIZED, policy.decide(ar).getDecisionResult());
        ao = new ObjectPropertyStatementAccessObject(null, "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#modTime", null, null);
        ar = new SimpleAuthorizationRequest(ao, AccessOperation.DROP);
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        
        ao = new ObjectPropertyStatementAccessObject(null, null, new Property("http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Valid"), null);
        ar = new SimpleAuthorizationRequest(ao, AccessOperation.DROP);
        assertEquals(DecisionResult.UNAUTHORIZED, policy.decide(ar).getDecisionResult());
        ao = new ObjectPropertyStatementAccessObject(null, null, new Property("http://vitro.mannlib.cornell.edu/ns/vitro/0.7#modTime"), null);
        ar = new SimpleAuthorizationRequest(ao, AccessOperation.DROP);
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        
        ao = new ObjectPropertyStatementAccessObject(null, null, null, "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Valid");
        ar = new SimpleAuthorizationRequest(ao, AccessOperation.DROP);
        assertEquals(DecisionResult.UNAUTHORIZED, policy.decide(ar).getDecisionResult());
        ao = new ObjectPropertyStatementAccessObject(null, null, null, "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#modTime");
        ar = new SimpleAuthorizationRequest(ao, AccessOperation.DROP);
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        
        //Data property statement
        ao = new DataPropertyStatementAccessObject(null, "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Valid", null, null);
        ar = new SimpleAuthorizationRequest(ao, AccessOperation.DROP);
        assertEquals(DecisionResult.UNAUTHORIZED, policy.decide(ar).getDecisionResult());
        ao = new DataPropertyStatementAccessObject(null, "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#modTime", null, null);
        ar = new SimpleAuthorizationRequest(ao, AccessOperation.DROP);
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        
        ao = new DataPropertyStatementAccessObject(null, null, "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Valid", null);
        ar = new SimpleAuthorizationRequest(ao, AccessOperation.DROP);
        assertEquals(DecisionResult.UNAUTHORIZED, policy.decide(ar).getDecisionResult());
        ao = new DataPropertyStatementAccessObject(null, null, "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#modTime", null);
        ar = new SimpleAuthorizationRequest(ao, AccessOperation.DROP);
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
    }
}
