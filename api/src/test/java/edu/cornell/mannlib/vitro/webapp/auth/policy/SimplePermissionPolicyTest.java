package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.SimpleAuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.rules.AccessRule;
import org.junit.Test;

public class SimplePermissionPolicyTest extends PolicyTest {

    public static final String ADMIN_SIMPLE_PERMISSIONS_PATH = "simple_permissions_admin";
    public static final String CURATOR_SIMPLE_PERMISSIONS_PATH = "simple_permissions_curator";
    public static final String EDITOR_SIMPLE_PERMISSIONS_PATH = "simple_permissions_editor";
    public static final String SELF_EDITOR_SIMPLE_PERMISSIONS_PATH = "simple_permissions_self_editor";
    public static final String PUBLIC_SIMPLE_PERMISSIONS_PATH = "simple_permissions_public";
    public static final String TEMPLATE_PATH = "template_simple_permissions";

    @Test
    public void testAdminSimplePermissionPolicy() {
        load(PolicyTest.USER_ACCOUNTS_HOME_FIRSTTIME + TEMPLATE_PATH + EXT);
        load(USER_ACCOUNTS_HOME_FIRSTTIME + ADMIN_SIMPLE_PERMISSIONS_PATH + EXT);
        String dataSetUri = "https://vivoweb.org/ontology/vitro-application/auth/individual/template/simple-permissions/AdminSimplePermissionDataSet";
        DynamicPolicy policy = loader.loadPolicyFromTemplateDataSet(dataSetUri);
        assertTrue(policy != null);
        assertEquals(1000, policy.getPriority());
        assertEquals(1, policy.getRules().size());
        final AccessRule rule = policy.getRules().iterator().next();
        assertEquals(true, rule.isAllowMatched());
        assertEquals(3, rule.getAttributesCount());

        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(SimplePermission.NS + "SeeSiteAdminPage");
        ar.setRoleUris(Arrays.asList(CURATOR));
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        ar.setRoleUris(Arrays.asList(ADMIN));
        assertEquals(DecisionResult.AUTHORIZED, policy.decide(ar).getDecisionResult());
    }

    @Test
    public void testCuratorSimplePermissionPolicy() {
        load(PolicyTest.USER_ACCOUNTS_HOME_FIRSTTIME + TEMPLATE_PATH + EXT);
        load(USER_ACCOUNTS_HOME_FIRSTTIME + CURATOR_SIMPLE_PERMISSIONS_PATH + EXT);
        String dataSetUri = "https://vivoweb.org/ontology/vitro-application/auth/individual/template/simple-permissions/CuratorSimplePermissionDataSet";
        DynamicPolicy policy = loader.loadPolicyFromTemplateDataSet(dataSetUri);
        assertTrue(policy != null);
        assertEquals(1000, policy.getPriority());
        assertEquals(1, policy.getRules().size());
        final AccessRule rule = policy.getRules().iterator().next();
        assertEquals(true, rule.isAllowMatched());
        assertEquals(3, rule.getAttributesCount());

        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(SimplePermission.NS + "EditOntology");
        ar.setRoleUris(Arrays.asList(EDITOR));
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        ar.setRoleUris(Arrays.asList(CURATOR));
        assertEquals(DecisionResult.AUTHORIZED, policy.decide(ar).getDecisionResult());
    }

    @Test
    public void testEditorSimplePermissionPolicy() {
        load(PolicyTest.USER_ACCOUNTS_HOME_FIRSTTIME + TEMPLATE_PATH + EXT);
        load(USER_ACCOUNTS_HOME_FIRSTTIME + EDITOR_SIMPLE_PERMISSIONS_PATH + EXT);
        String dataSetUri = "https://vivoweb.org/ontology/vitro-application/auth/individual/template/simple-permissions/EditorSimplePermissionDataSet";
        DynamicPolicy policy = loader.loadPolicyFromTemplateDataSet(dataSetUri);
        assertTrue(policy != null);
        assertEquals(1000, policy.getPriority());
        assertEquals(1, policy.getRules().size());
        final AccessRule rule = policy.getRules().iterator().next();
        assertEquals(true, rule.isAllowMatched());
        assertEquals(3, rule.getAttributesCount());

        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(SimplePermission.NS + "DoBackEndEditing");
        ar.setRoleUris(Arrays.asList(SELF_EDITOR));
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        ar.setRoleUris(Arrays.asList(EDITOR));
        assertEquals(DecisionResult.AUTHORIZED, policy.decide(ar).getDecisionResult());
    }

    @Test
    public void testSelfEditorSimplePermissionPolicy() {
        load(PolicyTest.USER_ACCOUNTS_HOME_FIRSTTIME + TEMPLATE_PATH + EXT);
        load(USER_ACCOUNTS_HOME_FIRSTTIME + SELF_EDITOR_SIMPLE_PERMISSIONS_PATH + EXT);
        String dataSetUri = "https://vivoweb.org/ontology/vitro-application/auth/individual/template/simple-permissions/SelfEditorSimplePermissionDataSet";
        DynamicPolicy policy = loader.loadPolicyFromTemplateDataSet(dataSetUri);
        assertTrue(policy != null);
        assertEquals(1000, policy.getPriority());
        assertEquals(1, policy.getRules().size());
        final AccessRule rule = policy.getRules().iterator().next();
        assertEquals(true, rule.isAllowMatched());
        assertEquals(3, rule.getAttributesCount());

        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(SimplePermission.NS + "DoFrontEndEditing");
        ar.setRoleUris(Arrays.asList(PUBLIC));
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        ar.setRoleUris(Arrays.asList(SELF_EDITOR));
        assertEquals(DecisionResult.AUTHORIZED, policy.decide(ar).getDecisionResult());
    }

    @Test
    public void testPublicSimplePermissionPolicy() {
        load(PolicyTest.USER_ACCOUNTS_HOME_FIRSTTIME + TEMPLATE_PATH + EXT);
        load(USER_ACCOUNTS_HOME_FIRSTTIME + PUBLIC_SIMPLE_PERMISSIONS_PATH + EXT);
        String dataSetUri = "https://vivoweb.org/ontology/vitro-application/auth/individual/template/simple-permissions/PublicSimplePermissionDataSet";
        DynamicPolicy policy = loader.loadPolicyFromTemplateDataSet(dataSetUri);
        assertTrue(policy != null);
        assertEquals(1000, policy.getPriority());
        assertEquals(1, policy.getRules().size());
        final AccessRule rule = policy.getRules().iterator().next();
        assertEquals(true, rule.isAllowMatched());
        assertEquals(3, rule.getAttributesCount());

        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(SimplePermission.NS + "QueryFullModel");
        ar.setRoleUris(Arrays.asList(""));
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        ar.setRoleUris(Arrays.asList(PUBLIC));
        assertEquals(DecisionResult.AUTHORIZED, policy.decide(ar).getDecisionResult());
    }

}
