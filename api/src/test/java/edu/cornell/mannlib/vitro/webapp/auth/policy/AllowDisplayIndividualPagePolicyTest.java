package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.ADD;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult.AUTHORIZED;
import static edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult.INCONCLUSIVE;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.AUTH_INDIVIDUAL_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.IndividualAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.NamedAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.SimpleAuthorizationRequest;
import org.junit.Test;

public class AllowDisplayIndividualPagePolicyTest extends PolicyTest {

    public static final String POLICY_PATH = USER_ACCOUNTS_HOME_FIRSTTIME + "policy_allow_display_individual_page.n3";

    @Test
    public void testLoadPolicy() {
        load(POLICY_PATH);
        String policyUri = AUTH_INDIVIDUAL_PREFIX + "allow-display-individual-page/Policy";
        Set<DynamicPolicy> policies = loader.loadPolicies(policyUri);
        assertEquals(1, policies.size());
        DynamicPolicy policy = policies.iterator().next();
        assertTrue(policy != null);
        assertEquals(1000, policy.getPriority());
        countRulesAndAttributes(policy, 1, Collections.singleton(2));
        AccessObject ao = new IndividualAccessObject("https://test-individual");
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(ao, DISPLAY);
        ar.setRoleUris(Arrays.asList(PUBLIC));
        assertEquals(AUTHORIZED, policy.decide(ar).getDecisionResult());
        ar = new SimpleAuthorizationRequest(ao, ADD);
        assertEquals(INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        ao = new NamedAccessObject("https://test-individual");
        ar = new SimpleAuthorizationRequest(ao, DISPLAY);
        assertEquals(INCONCLUSIVE, policy.decide(ar).getDecisionResult());
    }
}
