package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject.SOME_PREDICATE;
import static edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject.SOME_URI;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.AUTH_INDIVIDUAL_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.objects.ObjectPropertyStatementAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.SimpleAuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.rules.AccessRule;
import org.junit.Test;

public class EditablePagesPolicyTest extends PolicyTest {

    public static final String RELATED_EDITABLE_PAGES_POLICY_PATH =
            PolicyTest.USER_ACCOUNTS_HOME_FIRSTTIME + "policy_related_editable_pages.n3";
    public static final String EDITABLE_PAGES_TEMPLATE_PATH =
            PolicyTest.USER_ACCOUNTS_HOME_FIRSTTIME + "template_editable_pages.n3";

    @Test
    public void testLoadRelatedEditablePagesPolicy() {
        load(RELATED_EDITABLE_PAGES_POLICY_PATH);
        String policyUri = AUTH_INDIVIDUAL_PREFIX + "edit-related-individual-pages/Policy";
        Set<DynamicPolicy> policies = loader.loadPolicies(policyUri);
        assertEquals(1, policies.size());
        DynamicPolicy policy = policies.iterator().next();
        assertTrue(policy != null);
        countRulesAndAttributes(policy, 1, new HashSet<>(Arrays.asList(6)));
    }

    @Test
    public void testAdminEditablePagesPolicy() {
        load(EDITABLE_PAGES_TEMPLATE_PATH);
        String dataSetUri = AUTH_INDIVIDUAL_PREFIX + "edit-individual-pages/AdminRoleDataSet";
        DynamicPolicy policy = loader.loadPolicyFromTemplateDataSet(dataSetUri);
        assertTrue(policy != null);
        assertEquals(1, policy.getRules().size());
        final AccessRule rule = policy.getRules().iterator().next();
        assertEquals(true, rule.isAllowMatched());
        assertEquals(5, rule.getChecksCount());

        AccessOperation operation = AccessOperation.EDIT;
        ObjectPropertyStatementAccessObject object =
                new ObjectPropertyStatementAccessObject(null, "test://uri", SOME_PREDICATE, SOME_URI);
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(object, operation);
        ar.setRoleUris(Arrays.asList(CURATOR));
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        ar.setRoleUris(Arrays.asList(ADMIN));
        assertEquals(DecisionResult.AUTHORIZED, policy.decide(ar).getDecisionResult());
    }

    @Test
    public void testCuratorEditablePagesPolicy() {
        load(EDITABLE_PAGES_TEMPLATE_PATH);
        String dataSetUri = AUTH_INDIVIDUAL_PREFIX + "edit-individual-pages/CuratorRoleDataSet";
        DynamicPolicy policy = loader.loadPolicyFromTemplateDataSet(dataSetUri);
        assertTrue(policy != null);
        assertEquals(1, policy.getRules().size());
        final AccessRule rule = policy.getRules().iterator().next();
        assertEquals(true, rule.isAllowMatched());
        assertEquals(5, rule.getChecksCount());

        AccessOperation operation = AccessOperation.EDIT;
        ObjectPropertyStatementAccessObject object =
                new ObjectPropertyStatementAccessObject(null, "test://uri", SOME_PREDICATE, SOME_URI);
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(object, operation);
        ar.setRoleUris(Arrays.asList(ADMIN));
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        ar.setRoleUris(Arrays.asList(CURATOR));
        assertEquals(DecisionResult.AUTHORIZED, policy.decide(ar).getDecisionResult());
    }

    @Test
    public void testEditorEditablePagesPolicy() {
        load(EDITABLE_PAGES_TEMPLATE_PATH);
        String dataSetUri = AUTH_INDIVIDUAL_PREFIX + "edit-individual-pages/EditorRoleDataSet";
        DynamicPolicy policy = loader.loadPolicyFromTemplateDataSet(dataSetUri);
        assertTrue(policy != null);
        assertEquals(1, policy.getRules().size());
        final AccessRule rule = policy.getRules().iterator().next();
        assertEquals(true, rule.isAllowMatched());
        assertEquals(5, rule.getChecksCount());

        AccessOperation operation = AccessOperation.EDIT;
        ObjectPropertyStatementAccessObject object =
                new ObjectPropertyStatementAccessObject(null, "test://uri", SOME_PREDICATE, SOME_URI);
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(object, operation);
        ar.setRoleUris(Arrays.asList(ADMIN));
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        ar.setRoleUris(Arrays.asList(EDITOR));
        assertEquals(DecisionResult.AUTHORIZED, policy.decide(ar).getDecisionResult());
    }

    @Test
    public void testCustomRole() {
        load(EDITABLE_PAGES_TEMPLATE_PATH);

        // Create custom data set
        PolicyTemplateController.createRoleDataSets(CUSTOM);
        // Get data set uri by key: role uri and named object
        String dataSetUri = AUTH_INDIVIDUAL_PREFIX + "edit-individual-pages/CustomRoleDataSet";

        assertTrue(dataSetUri != null);
        DynamicPolicy policy = loader.loadPolicyFromTemplateDataSet(dataSetUri);
        assertTrue(policy != null);
        assertEquals(1, policy.getRules().size());
        final AccessRule rule = policy.getRules().iterator().next();
        assertEquals(true, rule.isAllowMatched());
        assertEquals(5, rule.getChecksCount());

        AccessOperation operation = AccessOperation.EDIT;
        ObjectPropertyStatementAccessObject object =
                new ObjectPropertyStatementAccessObject(null, "test://uri", SOME_PREDICATE, SOME_URI);
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(object, operation);
        ar.setRoleUris(Arrays.asList(PUBLIC));
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        ar.setRoleUris(Arrays.asList(CUSTOM));
        assertEquals(DecisionResult.AUTHORIZED, policy.decide(ar).getDecisionResult());
    }

}
