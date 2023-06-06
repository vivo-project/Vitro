package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup;

public class EntityPolicyControllerTest extends PolicyTest {

    @Test
    public void testGetGrantedRoles() {
        load(ADMIN_DISPLAY_CLASS_POLICY_PATH);
        load(SELF_EDITOR_DISPLAY_CLASS_POLICY_PATH);
        List<String> allowedRoles = Arrays.asList(ROLE_ADMIN_URI, ROLE_SELF_EDITOR_URI);
        EntityPolicyController.updateEntityPolicy("test:newClass", AccessObjectType.CLASS, OperationGroup.DISPLAY_GROUP, allowedRoles, ROLE_LIST);
        List<String> roles = EntityPolicyController.getGrantedRoles("test:newClass", OperationGroup.DISPLAY_GROUP, AccessObjectType.CLASS, ROLE_LIST);
        assertEquals(2, roles.size());
        roles.contains(ROLE_ADMIN_URI);
        roles.contains(ROLE_EDITOR_URI);
    }
    
    @Test
    public void testPolicyDataSetModification() {
        load(ADMIN_DISPLAY_CLASS_POLICY_PATH);
        EntityPolicyController.updateEntityPolicy("test:newClass", AccessObjectType.CLASS, OperationGroup.DISPLAY_GROUP, Arrays.asList(ROLE_ADMIN_URI), ROLE_LIST);
        List<String> roles = EntityPolicyController.getGrantedRoles("test:newClass", OperationGroup.DISPLAY_GROUP, AccessObjectType.CLASS, ROLE_LIST);
        assertEquals(1, roles.size());
        roles.contains(ROLE_ADMIN_URI);
        EntityPolicyController.updateEntityPolicy("test:newClass", AccessObjectType.CLASS, OperationGroup.DISPLAY_GROUP, Collections.emptyList(), ROLE_LIST);
        roles = EntityPolicyController.getGrantedRoles("test:newClass", OperationGroup.DISPLAY_GROUP, AccessObjectType.CLASS, ROLE_LIST);
        assertEquals(0, roles.size());
    }
}
    
