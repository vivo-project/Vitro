package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

public class RootUserPolicyTest extends PolicyTest {

    public static final String ROOT_POLICY_PATH = PolicyTest.USER_ACCOUNTS_HOME_FIRSTTIME + "policy_root_user.n3";

    @Test
    public void testLoadRootUserPolicy() {
        load(ROOT_POLICY_PATH);
        String policyUri = "https://vivoweb.org/ontology/vitro-application/auth/individual/policy/root-user/Policy";
        DynamicPolicy policy = loader.loadPolicy(policyUri);
        assertTrue(policy != null);
        assertEquals(10000, policy.getPriority());
        countRulesAndAttributes(policy, 1, Collections.singleton(1));
    }
}
