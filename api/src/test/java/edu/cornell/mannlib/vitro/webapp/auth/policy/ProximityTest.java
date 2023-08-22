package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.ObjectPropertyStatementAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.SimpleAuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.rules.AccessRule;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.Lock;
import org.junit.Test;

public class ProximityTest extends PolicyTest {

    private static final String PROXIMITY_POLICY_PATH = RESOURCES_PREFIX + "proximity_test_policy.n3";
    private static final String PROXIMITY_DATA_PATH = RESOURCES_PREFIX + "proximity_test_data.n3";

    @Test
    public void testProximityPolicy() {
        load(PROXIMITY_POLICY_PATH);
        String policyUri = "https://vivoweb.org/ontology/vitro-application/auth/individual/ProximityTestPolicy";
        DynamicPolicy policy = loader.loadPolicy(policyUri);
        assertTrue(policy != null);
        assertTrue(policy.getRules().size() == 1);
        AccessRule rule = policy.getRules().iterator().next();
        assertEquals(true, rule.isAllowMatched());
        assertEquals(1, rule.getAttributesCount());

        Model targetModel = ModelFactory.createDefaultModel();
        try {
            targetModel.enterCriticalSection(Lock.WRITE);
            targetModel.read(PROXIMITY_DATA_PATH);
        } finally {
            targetModel.leaveCriticalSection();
        }
        AccessObject ao = new ObjectPropertyStatementAccessObject(targetModel, "test:publication", null, null);
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(ao, AccessOperation.EDIT);
        ar.setEditorUris(Arrays.asList("test:bob"));
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        ar = new SimpleAuthorizationRequest(ao, AccessOperation.EDIT);
        ar.setEditorUris(Arrays.asList("test:alice"));
        assertEquals(DecisionResult.AUTHORIZED, policy.decide(ar).getDecisionResult());
        ar = new SimpleAuthorizationRequest(ao, AccessOperation.EDIT);
    }
}
