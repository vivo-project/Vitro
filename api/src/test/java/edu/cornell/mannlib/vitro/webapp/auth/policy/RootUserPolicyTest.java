package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

public class RootUserPolicyTest extends PolicyTest {

    public static final String ROOT_POLICY_PATH = PolicyTest.USER_ACCOUNTS_HOME_FIRSTTIME + "policy_root_user.n3";

    @Test
    public void testLoadRootUserPolicy() {
        load(ROOT_POLICY_PATH);
        String policyUri = "https://vivoweb.org/ontology/vitro-application/auth/individual/root-user/Policy";
        Set<DynamicPolicy> policies = loader.loadPolicies(policyUri);
        assertEquals(1, policies.size());
        DynamicPolicy policy = policies.iterator().next();
        assertTrue(policy != null);
        assertEquals(10000, policy.getPriority());
        countRulesAndAttributes(policy, 1, Collections.singleton(1));
    }
}
