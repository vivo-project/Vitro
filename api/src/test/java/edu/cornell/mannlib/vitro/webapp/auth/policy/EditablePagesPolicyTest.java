package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import org.junit.Test;

public class EditablePagesPolicyTest extends PolicyTest {

    public static final String EDITABLE_PAGES_POLICY_PATH =
            PolicyTest.USER_ACCOUNTS_HOME_FIRSTTIME + "policy_editable_pages.n3";

    @Test
    public void testLoadEditablePagesPolicy() {
        load(EDITABLE_PAGES_POLICY_PATH);
        String policyUri = VitroVocabulary.AUTH_INDIVIDUAL_PREFIX + "policy/edit-individual-pages/Policy";
        Set<DynamicPolicy> policies = loader.loadPolicies(policyUri);
        assertEquals(1, policies.size());
        DynamicPolicy policy = policies.iterator().next();
        assertTrue(policy != null);
        countRulesAndAttributes(policy, 2, new HashSet<>(Arrays.asList(5, 6)));
    }
}
