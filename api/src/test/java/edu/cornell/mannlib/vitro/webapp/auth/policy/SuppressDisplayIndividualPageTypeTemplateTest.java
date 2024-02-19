package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.INDIVIDUAL;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.NamedKeyComponent.SUPPRESSION_BY_TYPE;
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
import edu.cornell.mannlib.vitro.webapp.auth.attributes.NamedKeyComponent;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.IndividualAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.NamedAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.SimpleAuthorizationRequest;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.Lock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class SuppressDisplayIndividualPageTypeTemplateTest extends PolicyTest {

    private static final NamedKeyComponent NAMED_KEY = SUPPRESSION_BY_TYPE;

    private static final String TEST_ENTITY = "test:alice";
    private static final String TEST_TYPE = "test:person";


    public static final String POLICY_PATH =
            USER_ACCOUNTS_HOME_FIRSTTIME + "template_suppress_display_individual_page_by_type.n3";
    public static final String TEST_DATA = RESOURCES_RULES_PREFIX + "suppress_display_test_data.n3";

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
        Model dataModel = ModelFactory.createDefaultModel();
        try {
            dataModel.enterCriticalSection(Lock.WRITE);
            dataModel.read(TEST_DATA);
        } finally {
            dataModel.leaveCriticalSection();
        }
        if (roleUri.equals(CUSTOM)) {
            PolicyTemplateController.createRoleDataSets(CUSTOM);
        }
        EntityPolicyController.grantAccess(TEST_TYPE, type, ao, roleUri, NAMED_KEY.toString());

        String dataSetUri =
                loader.getDataSetUriByKey(NAMED_KEY.toString(), ao.toString(), type.toString(), roleUri);
        assertFalse(dataSetUri == null);
        DynamicPolicy policy = loader.loadPolicyFromTemplateDataSet(dataSetUri);
        assertTrue(policy != null);
        assertEquals(1500, policy.getPriority());
        countRulesAndAttributes(policy, 1, Collections.singleton(4));
        policyDeniesAccess(policy, dataModel);

        policyNotAffectsOtherTypes(policy, dataModel);
        policyNotAffectsOtherEntities(policy, dataModel);
        policyNotAffectsOtherOperations(policy, dataModel);
        policyNotAffectsOtherRoles(policy, dataModel);
    }

    private void policyNotAffectsOtherRoles(DynamicPolicy policy, Model targetModel) {
        AccessObject object = new IndividualAccessObject(TEST_ENTITY);
        object.setModel(targetModel);
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(object, AccessOperation.DISPLAY);
        ar.setRoleUris(Arrays.asList(roleUri + "_NOT_EXISTS"));
        assertEquals(INCONCLUSIVE, policy.decide(ar).getDecisionResult());
    }

    private void policyNotAffectsOtherEntities(DynamicPolicy policy, Model targetModel) {
        AccessObject object = new IndividualAccessObject("test:another_entity");
        object.setModel(targetModel);
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(object, ao);
        ar.setRoleUris(Arrays.asList(roleUri));
        assertEquals(INCONCLUSIVE, policy.decide(ar).getDecisionResult());
    }

    private void policyNotAffectsOtherOperations(DynamicPolicy policy, Model targetModel) {
        AccessObject object = new IndividualAccessObject(TEST_ENTITY);
        object.setModel(targetModel);
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(object, AccessOperation.ADD);
        ar.setRoleUris(Arrays.asList(roleUri));
        assertEquals(INCONCLUSIVE, policy.decide(ar).getDecisionResult());
    }

    private void policyNotAffectsOtherTypes(DynamicPolicy policy, Model targetModel) {
        AccessObject object = new NamedAccessObject(TEST_ENTITY);
        object.setModel(targetModel);
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(object, ao);
        ar.setRoleUris(Arrays.asList(roleUri));
        assertEquals(INCONCLUSIVE, policy.decide(ar).getDecisionResult());
    }

    private void policyDeniesAccess(DynamicPolicy policy, Model targetModel) {
        AccessObject object = new IndividualAccessObject(TEST_ENTITY);
        object.setModel(targetModel);
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
