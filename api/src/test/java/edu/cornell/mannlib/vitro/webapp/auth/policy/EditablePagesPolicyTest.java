package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

public class EditablePagesPolicyTest extends PolicyTest {

    public static final String EDITABLE_PAGES_POLICY_PATH = PolicyTest.USER_ACCOUNTS_HOME_FIRSTTIME + "policy_editable_pages.n3";

    @Test
    public void testLoadEditablePagesPolicy() {
        load(EDITABLE_PAGES_POLICY_PATH);
        String policyUri = "https://vivoweb.org/ontology/vitro-application/auth/individual/policy/edit-individual-pages/Policy";
        DynamicPolicy policy = loader.loadPolicy(policyUri);
        assertTrue(policy != null);
        countRulesAndAttributes(policy, 2, new HashSet<>(Arrays.asList(5, 6)));
    }
}
