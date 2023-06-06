package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.SimpleAuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.rules.AccessRule;

public class SimplePermissionPolicyTest extends PolicyTest {

    public static final String ADMIN_SIMPLE_PERMISSIONS_POLICY_PATH = "policy_admin_simple_permissions";
    public static final String CURATOR_SIMPLE_PERMISSIONS_POLICY_PATH = "policy_curator_simple_permissions";
    public static final String EDITOR_SIMPLE_PERMISSIONS_POLICY_PATH = "policy_editor_simple_permissions";
    public static final String SELF_EDITOR_SIMPLE_PERMISSIONS_POLICY_PATH = "policy_self_editor_simple_permissions";
    public static final String PUBLIC_SIMPLE_PERMISSIONS_POLICY_PATH = "policy_public_simple_permissions";
    @Test
    public void testAdminSimplePermissionPolicy() {        
        load(USER_ACCOUNTS_HOME_EVERYTIME + ADMIN_SIMPLE_PERMISSIONS_POLICY_PATH + EXT);
        load(USER_ACCOUNTS_HOME_FIRSTTIME + ADMIN_SIMPLE_PERMISSIONS_POLICY_PATH + DATASET + EXT);
        String policyUri = "https://vivoweb.org/ontology/vitro-application/auth/individual/AdminSimplePermissionsPolicy";
        DynamicPolicy policy = loader.loadPolicy(policyUri);
        assertTrue(policy != null);
        assertEquals(1000, policy.getPriority());
        assertEquals(1, policy.getRules().size());
        final AccessRule rule = policy.getRules().iterator().next();
        assertEquals(true, rule.isAllowMatched());
        assertEquals(3, rule.getAttributesCount());
        
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(SimplePermission.NS + "SeeSiteAdminPage");
        ar.setRoleUris(Arrays.asList(ROLE_CURATOR_URI));
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        ar.setRoleUris(Arrays.asList(ROLE_ADMIN_URI));
        assertEquals(DecisionResult.AUTHORIZED, policy.decide(ar).getDecisionResult());
    }
    
    @Test
    public void testCuratorSimplePermissionPolicy() {        
        load(USER_ACCOUNTS_HOME_EVERYTIME + CURATOR_SIMPLE_PERMISSIONS_POLICY_PATH + EXT);
        load(USER_ACCOUNTS_HOME_FIRSTTIME + CURATOR_SIMPLE_PERMISSIONS_POLICY_PATH + DATASET + EXT);
        String policyUri = "https://vivoweb.org/ontology/vitro-application/auth/individual/CuratorSimplePermissionsPolicy";
        DynamicPolicy policy = loader.loadPolicy(policyUri);
        assertTrue(policy != null);
        assertEquals(1000, policy.getPriority());
        assertEquals(1, policy.getRules().size());
        final AccessRule rule = policy.getRules().iterator().next();
        assertEquals(true, rule.isAllowMatched());
        assertEquals(3, rule.getAttributesCount());
        
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(SimplePermission.NS + "EditOntology");
        ar.setRoleUris(Arrays.asList(ROLE_EDITOR_URI));
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        ar.setRoleUris(Arrays.asList(ROLE_CURATOR_URI));
        assertEquals(DecisionResult.AUTHORIZED, policy.decide(ar).getDecisionResult());
    }
    
    @Test
    public void testEditorSimplePermissionPolicy() {        
        load(USER_ACCOUNTS_HOME_EVERYTIME + EDITOR_SIMPLE_PERMISSIONS_POLICY_PATH + EXT);
        load(USER_ACCOUNTS_HOME_FIRSTTIME + EDITOR_SIMPLE_PERMISSIONS_POLICY_PATH + DATASET + EXT);
        String policyUri = "https://vivoweb.org/ontology/vitro-application/auth/individual/EditorSimplePermissionsPolicy";
        DynamicPolicy policy = loader.loadPolicy(policyUri);
        assertTrue(policy != null);
        assertEquals(1000, policy.getPriority());
        assertEquals(1, policy.getRules().size());
        final AccessRule rule = policy.getRules().iterator().next();
        assertEquals(true, rule.isAllowMatched());
        assertEquals(3, rule.getAttributesCount());
        
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(SimplePermission.NS + "DoBackEndEditing");
        ar.setRoleUris(Arrays.asList(ROLE_SELF_EDITOR_URI));
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        ar.setRoleUris(Arrays.asList(ROLE_EDITOR_URI));
        assertEquals(DecisionResult.AUTHORIZED, policy.decide(ar).getDecisionResult());
    }

    @Test
    public void testSelfEditorSimplePermissionPolicy() {        
        load(USER_ACCOUNTS_HOME_EVERYTIME + SELF_EDITOR_SIMPLE_PERMISSIONS_POLICY_PATH + EXT);
        load(USER_ACCOUNTS_HOME_FIRSTTIME + SELF_EDITOR_SIMPLE_PERMISSIONS_POLICY_PATH + DATASET + EXT);
        String policyUri = "https://vivoweb.org/ontology/vitro-application/auth/individual/SelfEditorSimplePermissionsPolicy";
        DynamicPolicy policy = loader.loadPolicy(policyUri);
        assertTrue(policy != null);
        assertEquals(1000, policy.getPriority());
        assertEquals(1, policy.getRules().size());
        final AccessRule rule = policy.getRules().iterator().next();
        assertEquals(true, rule.isAllowMatched());
        assertEquals(3, rule.getAttributesCount());
        
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(SimplePermission.NS + "DoFrontEndEditing");
        ar.setRoleUris(Arrays.asList(ROLE_PUBLIC_URI));
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        ar.setRoleUris(Arrays.asList(ROLE_SELF_EDITOR_URI));
        assertEquals(DecisionResult.AUTHORIZED, policy.decide(ar).getDecisionResult());
    }
    
    @Test
    public void testPublicSimplePermissionPolicy() {        
        load(USER_ACCOUNTS_HOME_EVERYTIME + PUBLIC_SIMPLE_PERMISSIONS_POLICY_PATH + EXT);
        load(USER_ACCOUNTS_HOME_FIRSTTIME + PUBLIC_SIMPLE_PERMISSIONS_POLICY_PATH + DATASET + EXT);
        String policyUri = "https://vivoweb.org/ontology/vitro-application/auth/individual/PublicSimplePermissionsPolicy";
        DynamicPolicy policy = loader.loadPolicy(policyUri);
        assertTrue(policy != null);
        assertEquals(1000, policy.getPriority());
        assertEquals(1, policy.getRules().size());
        final AccessRule rule = policy.getRules().iterator().next();
        assertEquals(true, rule.isAllowMatched());
        assertEquals(3, rule.getAttributesCount());
        
        SimpleAuthorizationRequest ar = new SimpleAuthorizationRequest(SimplePermission.NS + "QueryFullModel");
        ar.setRoleUris(Arrays.asList(""));
        assertEquals(DecisionResult.INCONCLUSIVE, policy.decide(ar).getDecisionResult());
        ar.setRoleUris(Arrays.asList(ROLE_PUBLIC_URI));
        assertEquals(DecisionResult.AUTHORIZED, policy.decide(ar).getDecisionResult());
    }
   
    
}
