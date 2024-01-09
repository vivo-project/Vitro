package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.INDIVIDUAL;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.NamedKeyComponent.URI_EXCLUSION;
import static edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult.INCONCLUSIVE;
import static edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult.UNAUTHORIZED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.IndividualAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.NamedAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.SimpleAuthorizationRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class DisplayIndividualPageExcludeByUriTemplateTest extends PolicyTest {

    private static final String TEST_ENTITY = "test:entity";

    public static final String POLICY_PATH =
            USER_ACCOUNTS_HOME_FIRSTTIME + "template_exclude_display_individual_page_uri.n3";

    @org.junit.runners.Parameterized.Parameter(0)
    public AccessOperation ao;

    @org.junit.runners.Parameterized.Parameter(1)
    public AccessObjectType type;

    @org.junit.runners.Parameterized.Parameter(2)
    public String roleUri;

    @org.junit.runners.Parameterized.Parameter(3)
    public int rulesCount;

    @org.junit.runners.Parameterized.Parameter(4)
    public Set<Integer> attrCount;

    @Test
    public void testLoadPolicy() {
        load(POLICY_PATH);

        if (roleUri.equals(CUSTOM)) {
            PolicyTemplateController.createRoleDataSets(CUSTOM);
        }
        EntityPolicyController.grantAccess(TEST_ENTITY, type, ao, roleUri, URI_EXCLUSION.toString());

        String dataSetUri =
                loader.getDataSetUriByKey(URI_EXCLUSION.toString(), ao.toString(), type.toString(), roleUri);
        assertFalse(dataSetUri == null);
        DynamicPolicy policy = loader.loadPolicyFromTemplateDataSet(dataSetUri);
        assertTrue(policy != null);
        assertEquals(2000, policy.getPriority());
        countRulesAndAttributes(policy, 1, Collections.singleton(4));
        policyDeniesAccess(policy);

        policyNotAffectsOtherTypes(policy);
        policyNotAffectsOtherEntities(policy);
        policyNotAffectsOtherOperations(policy);
        policyNotAffectsOtherRoles(policy);
    }

    private void policyNotAffectsOtherRoles(DynamicPolicy policy) {
        AccessObject object = new IndividualAccessObject(TEST_ENTITY);
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(object, ao);
        ar.setRoleUris(Arrays.asList(roleUri + "_NOT_EXISTS"));
        assertEquals(INCONCLUSIVE, policy.decide(ar).getDecisionResult());
    }

    private void policyNotAffectsOtherEntities(DynamicPolicy policy) {
        AccessObject object = new IndividualAccessObject("test:anothe_entity");
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(object, ao);
        ar.setRoleUris(Arrays.asList(roleUri));
        assertEquals(INCONCLUSIVE, policy.decide(ar).getDecisionResult());
    }

    private void policyNotAffectsOtherOperations(DynamicPolicy policy) {
        AccessObject object = new IndividualAccessObject(TEST_ENTITY);
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(object, AccessOperation.ADD);
        ar.setRoleUris(Arrays.asList(roleUri));
        assertEquals(INCONCLUSIVE, policy.decide(ar).getDecisionResult());
    }

    private void policyNotAffectsOtherTypes(DynamicPolicy policy) {
        AccessObject object = new NamedAccessObject(TEST_ENTITY);
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(object, ao);
        ar.setRoleUris(Arrays.asList(roleUri));
        assertEquals(INCONCLUSIVE, policy.decide(ar).getDecisionResult());
    }

    private void policyDeniesAccess(DynamicPolicy policy) {
        AccessObject object = new IndividualAccessObject(TEST_ENTITY);
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(object, ao);
        ar.setRoleUris(Arrays.asList(roleUri));
        assertEquals(UNAUTHORIZED, policy.decide(ar).getDecisionResult());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
            { DISPLAY, INDIVIDUAL, ADMIN, 1, num(4) },
            { DISPLAY, INDIVIDUAL, CURATOR, 1, num(4) },
            { DISPLAY, INDIVIDUAL, EDITOR, 1, num(4) },
            { DISPLAY, INDIVIDUAL, SELF_EDITOR, 1, num(4) },
            { DISPLAY, INDIVIDUAL, PUBLIC, 1, num(4) },
            { DISPLAY, INDIVIDUAL, CUSTOM, 1, num(4) },});
    }

    private static Set<Integer> num(int i) {
        return Collections.singleton(i);
    }
}
