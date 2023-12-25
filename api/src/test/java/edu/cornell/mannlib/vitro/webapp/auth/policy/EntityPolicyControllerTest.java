package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.CLASS;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.DISPLAY;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class EntityPolicyControllerTest extends PolicyTest {

    @Test
    public void testGetGrantedRoles() {
        load(USER_ACCOUNTS_HOME_FIRSTTIME + "template_access_allowed_class.n3");
        List<String> allowedRoles = Arrays.asList(ADMIN, SELF_EDITOR);
        EntityPolicyController.updateEntityDataSet("test:newClass", CLASS, DISPLAY, allowedRoles, ROLE_LIST);
        List<String> roles = EntityPolicyController.getGrantedRoles("test:newClass", DISPLAY, CLASS, ROLE_LIST);
        assertEquals(2, roles.size());
        roles.contains(ADMIN);
        roles.contains(EDITOR);
    }

    @Test
    public void testPolicyDataSetModification() {
        load(USER_ACCOUNTS_HOME_FIRSTTIME + "template_access_allowed_class.n3");
        EntityPolicyController.grantAccess("test:newClass", CLASS, DISPLAY, ADMIN);
        EntityPolicyController.grantAccess("test:newClass2", CLASS, DISPLAY, ADMIN);
        List<String> roles = EntityPolicyController.getGrantedRoles("test:newClass", DISPLAY, CLASS, ROLE_LIST);
        assertEquals(1, roles.size());
        roles.contains(ADMIN);
        EntityPolicyController.updateEntityDataSet("test:newClass", CLASS, DISPLAY, Collections.emptyList(), ROLE_LIST);
        roles = EntityPolicyController.getGrantedRoles("test:newClass", DISPLAY, CLASS, ROLE_LIST);
        assertEquals(0, roles.size());
    }
}
