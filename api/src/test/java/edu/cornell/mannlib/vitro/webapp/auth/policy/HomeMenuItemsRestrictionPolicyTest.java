package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.ObjectPropertyStatementAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.SimpleAuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.rules.AccessRule;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import org.junit.Test;

public class HomeMenuItemsRestrictionPolicyTest extends PolicyTest {

    public static final String MENU_ITEMS_POLICY_PATH = PolicyTest.USER_ACCOUNTS_HOME_FIRSTTIME + "policy_menu_items_editing.n3";

    @Test
    public void testHomeMenuItemsRestrictionPolicy() {
        load(MENU_ITEMS_POLICY_PATH);
        String policyUri = "https://vivoweb.org/ontology/vitro-application/auth/individual/RestrictHomeMenuItemsEditingPolicy";
        DynamicPolicy policy = loader.loadPolicy(policyUri);
        assertTrue(policy != null);
        assertEquals(9000, policy.getPriority());
        assertTrue(policy.getRules().size() > 0);
        final AccessRule rule = policy.getRules().iterator().next();
        assertEquals(false, rule.isAllowMatched());
        assertEquals(4, rule.getAttributesCount());

        AccessObject ao = new ObjectPropertyStatementAccessObject(null, null,
                new Property("http://vitro.mannlib.cornell.edu/ontologies/display/1.1#HomeMenuItem"),
                "http://vitro.mannlib.cornell.edu/ontologies/display/1.1#hasElement");
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(ao, AccessOperation.DROP);
        assertEquals(DecisionResult.UNAUTHORIZED, policy.decide(ar).getDecisionResult());
        ar = new SimpleAuthorizationRequest(ao, AccessOperation.EDIT);
        assertEquals(DecisionResult.UNAUTHORIZED, policy.decide(ar).getDecisionResult());
        ar = new SimpleAuthorizationRequest(ao, AccessOperation.ADD);
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
    }
}
