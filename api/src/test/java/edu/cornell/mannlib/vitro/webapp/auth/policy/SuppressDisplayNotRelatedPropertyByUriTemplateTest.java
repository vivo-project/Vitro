package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.DATA_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.FAUX_DATA_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.FAUX_OBJECT_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.OBJECT_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.PUBLISH;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.NamedKeyComponent.NOT_RELATED;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.NamedKeyComponent.SUPPRESSION_BY_URI;
import static edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult.INCONCLUSIVE;
import static edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult.UNAUTHORIZED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.DataPropertyStatementAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.FauxDataPropertyStatementAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.FauxObjectPropertyStatementAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.ObjectPropertyStatementAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.TestAuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.shared.Lock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class SuppressDisplayNotRelatedPropertyByUriTemplateTest extends PolicyTest {

    public static final String POLICY_PATH =
            USER_ACCOUNTS_HOME_FIRSTTIME + "template_suppress_display_not_related_property_by_uri.n3";
    public static final String TEST_DATA = RESOURCES_RULES_PREFIX + "suppress_display_test_data.n3";
    private static final String TEST_ENTITY = "test:alice";
    private static final String OBJECT_ENTITY = "test:orange";
    private static final String TEST_PROPERTY = "test:has";
    private static final String OTHER_PROPERTY = "test:seen";

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
        OntModel dataModel = VitroModelFactory.createOntologyModel();
        try {
            dataModel.enterCriticalSection(Lock.WRITE);
            dataModel.read(TEST_DATA);
        } finally {
            dataModel.leaveCriticalSection();
        }
        EntityPolicyController.grantAccess(TEST_PROPERTY, type, ao, roleUri, NOT_RELATED.toString(),
                SUPPRESSION_BY_URI.toString());

        String dataSetUri = loader.getDataSetUriByKey(SUPPRESSION_BY_URI.toString(), NOT_RELATED.toString(),
                ao.toString(), type.toString(), roleUri);
        DynamicPolicy policy = loader.loadPolicyFromTemplateDataSet(dataSetUri);
        assertTrue(policy != null);
        assertEquals(5000, policy.getPriority());
        countRulesAndAttributes(policy, 1, Collections.singleton(5));
        policyDeniesAccess(policy, dataModel);
        policyNotAffectsOtherTypes(policy, dataModel);
        policyNotAffectsOtherEntities(policy, dataModel);
        policyNotAffectsOtherOperations(policy, dataModel);
        policyNotAffectsOtherRoles(policy, dataModel);
        policyNotAffectsRelatedIndividuals(policy, dataModel);
    }

    private void policyNotAffectsRelatedIndividuals(DynamicPolicy policy, OntModel targetModel) {
        AccessObject object = getAccessObject(targetModel, TEST_PROPERTY);
        TestAuthorizationRequest ar = new TestAuthorizationRequest(object, ao);
        ar.setRoleUris(Arrays.asList(roleUri));
        ar.setEditorUris(new HashSet(Arrays.asList(TEST_ENTITY)));
        assertEquals(INCONCLUSIVE, policy.decide(ar).getDecisionResult());
    }

    private void policyNotAffectsOtherRoles(DynamicPolicy policy, OntModel targetModel) {
        AccessObject object = getAccessObject(targetModel, TEST_PROPERTY);
        TestAuthorizationRequest ar = new TestAuthorizationRequest(object, ao);
        ar.setRoleUris(Arrays.asList(ADMIN));
        assertEquals(INCONCLUSIVE, policy.decide(ar).getDecisionResult());
    }

    private void policyNotAffectsOtherOperations(DynamicPolicy policy, OntModel targetModel) {
        AccessObject object = getAccessObject(targetModel, TEST_PROPERTY);
        TestAuthorizationRequest ar = new TestAuthorizationRequest(object, PUBLISH);
        ar.setRoleUris(Arrays.asList(roleUri));
        assertEquals(INCONCLUSIVE, policy.decide(ar).getDecisionResult());
    }

    private void policyNotAffectsOtherEntities(DynamicPolicy policy, OntModel targetModel) {
        AccessObject object = getAccessObject(targetModel, OTHER_PROPERTY);
        TestAuthorizationRequest ar = new TestAuthorizationRequest(object, ao);
        ar.setRoleUris(Arrays.asList(roleUri));
        assertEquals(INCONCLUSIVE, policy.decide(ar).getDecisionResult());
    }

    private void policyNotAffectsOtherTypes(DynamicPolicy policy, OntModel targetModel) {
        AccessObject object = getWrongAccessObject(targetModel);
        TestAuthorizationRequest ar = new TestAuthorizationRequest(object, ao);
        ar.setRoleUris(Arrays.asList(roleUri));
        assertEquals(INCONCLUSIVE, policy.decide(ar).getDecisionResult());
    }

    private void policyDeniesAccess(DynamicPolicy policy, OntModel targetModel) {
        AccessObject object = getAccessObject(targetModel, TEST_PROPERTY);
        TestAuthorizationRequest ar = new TestAuthorizationRequest(object, ao);
        ar.setRoleUris(Arrays.asList(roleUri));
        assertEquals(UNAUTHORIZED, policy.decide(ar).getDecisionResult());
    }

    private AccessObject getWrongAccessObject(OntModel targetModel) {
        FauxProperty fauxProperty = new FauxProperty(TEST_ENTITY, TEST_PROPERTY, "");
        fauxProperty.setConfigUri(TEST_PROPERTY);
        switch (type) {
            case OBJECT_PROPERTY:
                return new DataPropertyStatementAccessObject(targetModel, TEST_ENTITY, TEST_PROPERTY, "test");
            case DATA_PROPERTY:
                return new ObjectPropertyStatementAccessObject(targetModel, TEST_ENTITY, new Property(TEST_PROPERTY),
                        OBJECT_ENTITY);
            case FAUX_OBJECT_PROPERTY:
                return new FauxDataPropertyStatementAccessObject(targetModel, TEST_ENTITY, fauxProperty, "test");
            case FAUX_DATA_PROPERTY:
                return new FauxObjectPropertyStatementAccessObject(targetModel, TEST_ENTITY, fauxProperty,
                        OBJECT_ENTITY);
            default:
                return null;
        }
    }

    private AccessObject getAccessObject(OntModel targetModel, String property) {
        FauxProperty fauxProperty = new FauxProperty(TEST_ENTITY, property, "");
        fauxProperty.setConfigUri(property);
        switch (type) {
            case DATA_PROPERTY:
                return new DataPropertyStatementAccessObject(targetModel, TEST_ENTITY, property, "test");
            case OBJECT_PROPERTY:
                return new ObjectPropertyStatementAccessObject(targetModel, TEST_ENTITY, new Property(property),
                        OBJECT_ENTITY);
            case FAUX_DATA_PROPERTY:
                return new FauxDataPropertyStatementAccessObject(targetModel, TEST_ENTITY, fauxProperty, "test");
            case FAUX_OBJECT_PROPERTY:
                return new FauxObjectPropertyStatementAccessObject(targetModel, TEST_ENTITY, fauxProperty,
                        OBJECT_ENTITY);
            default:
                return null;
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
            { DISPLAY, DATA_PROPERTY, SELF_EDITOR, 1, num(5) },
            { DISPLAY, OBJECT_PROPERTY, SELF_EDITOR, 1, num(5) },
            { DISPLAY, FAUX_DATA_PROPERTY, SELF_EDITOR, 1, num(5) },
            { DISPLAY, FAUX_OBJECT_PROPERTY, SELF_EDITOR, 1, num(5) }, });
    }

    private static Set<Integer> num(int i) {
        return Collections.singleton(i);
    }
}
