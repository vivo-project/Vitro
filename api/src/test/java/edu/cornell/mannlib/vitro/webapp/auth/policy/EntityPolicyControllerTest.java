package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import org.junit.Test;

public class EntityPolicyControllerTest extends PolicyTest {

    @Test
    public void testGetGrantedRoles() {
        load( USER_ACCOUNTS_HOME_FIRSTTIME + "template_access_allowed_class.n3");
        List<String> allowedRoles = Arrays.asList(ADMIN, SELF_EDITOR);
        EntityPolicyController.updateEntityDataSet("test:newClass", AccessObjectType.CLASS,
                AccessOperation.DISPLAY, allowedRoles, ROLE_LIST);
        List<String> roles = EntityPolicyController.getGrantedRoles("test:newClass", AccessOperation.DISPLAY,
                AccessObjectType.CLASS, ROLE_LIST);
        assertEquals(2, roles.size());
        roles.contains(ADMIN);
        roles.contains(EDITOR);
    }

    @Test
    public void testPolicyDataSetModification() {
        load( USER_ACCOUNTS_HOME_FIRSTTIME + "template_access_allowed_class.n3");
        EntityPolicyController.updateEntityDataSet("test:newClass", AccessObjectType.CLASS,
                AccessOperation.DISPLAY, Arrays.asList(ADMIN), ROLE_LIST);
        EntityPolicyController.updateEntityDataSet("test:newClass2", AccessObjectType.CLASS,
                AccessOperation.DISPLAY, Arrays.asList(ADMIN), ROLE_LIST);
        List<String> roles = EntityPolicyController.getGrantedRoles("test:newClass", AccessOperation.DISPLAY,
                AccessObjectType.CLASS, ROLE_LIST);
        assertEquals(1, roles.size());
        roles.contains(ADMIN);
        EntityPolicyController.updateEntityDataSet("test:newClass", AccessObjectType.CLASS,
                AccessOperation.DISPLAY, Collections.emptyList(), ROLE_LIST);
        roles = EntityPolicyController.getGrantedRoles("test:newClass", AccessOperation.DISPLAY, AccessObjectType.CLASS,
                ROLE_LIST);
        assertEquals(0, roles.size());
    }
}
