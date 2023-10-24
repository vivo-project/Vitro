package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.DATA_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.FAUX_DATA_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.FAUX_OBJECT_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.OBJECT_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.ADD;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.DROP;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.EDIT;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.PUBLISH;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class AccessAllowedPropertiesPolicyTemplateTest extends PolicyTest {

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
    public void testPolicy() {
        load(TEMPLATE_PROPERTIES_PATH);
        List<String> roles = new ArrayList<>();
        roles.addAll(ROLE_LIST);
        if (roleUri.equals(CUSTOM)) {
            PolicyTemplateController.createRoleDataSets(CUSTOM);
            roles.add(CUSTOM);
        }
        EntityPolicyController.updateEntityDataSet("test:entity", type, ao, Arrays.asList(roleUri), roles);
        DynamicPolicy policy = null;
        String dataSet =
                loader.getDataSetUriByKey(new String[] { roleUri }, new String[] { ao.toString(), type.toString() });
        policy = loader.loadPolicyFromTemplateDataSet(dataSet);
        countRulesAndAttributes(policy, rulesCount, attrCount);
        Set<String> values = loader.getDataSetValues(ao, type, roleUri);
        assertFalse(values.isEmpty());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {

                { DISPLAY, OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { DISPLAY, OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { DISPLAY, OBJECT_PROPERTY, EDITOR, 2, num(4) },
                { DISPLAY, OBJECT_PROPERTY, PUBLIC, 2, num(4) },
                { DISPLAY, OBJECT_PROPERTY, CUSTOM, 2, num(4) },

                { DISPLAY, DATA_PROPERTY, ADMIN, 2, num(4) },
                { DISPLAY, DATA_PROPERTY, CURATOR, 2, num(4) },
                { DISPLAY, DATA_PROPERTY, EDITOR, 2, num(4) },
                { DISPLAY, DATA_PROPERTY, PUBLIC, 2, num(4) },
                { DISPLAY, DATA_PROPERTY, CUSTOM, 2, num(4) },

                { DISPLAY, FAUX_OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { DISPLAY, FAUX_OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { DISPLAY, FAUX_OBJECT_PROPERTY, EDITOR, 2, num(4) },
                { DISPLAY, FAUX_OBJECT_PROPERTY, PUBLIC, 2, num(4) },
                { DISPLAY, FAUX_OBJECT_PROPERTY, CUSTOM, 2, num(4) },

                { DISPLAY, FAUX_DATA_PROPERTY, ADMIN, 2, num(4) },
                { DISPLAY, FAUX_DATA_PROPERTY, CURATOR, 2, num(4) },
                { DISPLAY, FAUX_DATA_PROPERTY, EDITOR, 2, num(4) },
                { DISPLAY, FAUX_DATA_PROPERTY, PUBLIC, 2, num(4) },
                { DISPLAY, FAUX_DATA_PROPERTY, CUSTOM, 2, num(4) },

                { PUBLISH, OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { PUBLISH, OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { PUBLISH, OBJECT_PROPERTY, EDITOR, 2, num(4) },
                { PUBLISH, OBJECT_PROPERTY, CUSTOM, 2, num(4) },

                { PUBLISH, DATA_PROPERTY, ADMIN, 2, num(4) },
                { PUBLISH, DATA_PROPERTY, CURATOR, 2, num(4) },
                { PUBLISH, DATA_PROPERTY, EDITOR, 2, num(4) },
                { PUBLISH, DATA_PROPERTY, CUSTOM, 2, num(4) },

                { PUBLISH, FAUX_OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { PUBLISH, FAUX_OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { PUBLISH, FAUX_OBJECT_PROPERTY, EDITOR, 2, num(4) },
                { PUBLISH, FAUX_OBJECT_PROPERTY, CUSTOM, 2, num(4) },

                { PUBLISH, FAUX_DATA_PROPERTY, ADMIN, 2, num(4) },
                { PUBLISH, FAUX_DATA_PROPERTY, CURATOR, 2, num(4) },
                { PUBLISH, FAUX_DATA_PROPERTY, EDITOR, 2, num(4) },
                { PUBLISH, FAUX_DATA_PROPERTY, CUSTOM, 2, num(4) },

                { EDIT, OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { EDIT, OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { EDIT, OBJECT_PROPERTY, EDITOR, 2, num(4) },
                { EDIT, OBJECT_PROPERTY, PUBLIC, 2, num(4) },
                { EDIT, OBJECT_PROPERTY, CUSTOM, 2, num(4) },

                { ADD, OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { ADD, OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { ADD, OBJECT_PROPERTY, EDITOR, 2, num(4) },
                { ADD, OBJECT_PROPERTY, PUBLIC, 2, num(4) },
                { ADD, OBJECT_PROPERTY, CUSTOM, 2, num(4) },

                { DROP, OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { DROP, OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { DROP, OBJECT_PROPERTY, EDITOR, 2, num(4) },
                { DROP, OBJECT_PROPERTY, PUBLIC, 2, num(4) },
                { DROP, OBJECT_PROPERTY, CUSTOM, 2, num(4) },

                { EDIT, DATA_PROPERTY, ADMIN, 2, num(4) },
                { EDIT, DATA_PROPERTY, CURATOR, 2, num(4) },
                { EDIT, DATA_PROPERTY, EDITOR, 2, num(4) },
                { EDIT, DATA_PROPERTY, PUBLIC, 2, num(4) },
                { EDIT, DATA_PROPERTY, CUSTOM, 2, num(4) },

                { ADD, DATA_PROPERTY, ADMIN, 2, num(4) },
                { ADD, DATA_PROPERTY, CURATOR, 2, num(4) },
                { ADD, DATA_PROPERTY, EDITOR, 2, num(4) },
                { ADD, DATA_PROPERTY, PUBLIC, 2, num(4) },
                { ADD, DATA_PROPERTY, CUSTOM, 2, num(4) },

                { DROP, DATA_PROPERTY, ADMIN, 2, num(4) },
                { DROP, DATA_PROPERTY, CURATOR, 2, num(4) },
                { DROP, DATA_PROPERTY, EDITOR, 2, num(4) },
                { DROP, DATA_PROPERTY, PUBLIC, 2, num(4) },
                { DROP, DATA_PROPERTY, CUSTOM, 2, num(4) },

                { EDIT, FAUX_OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { EDIT, FAUX_OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { EDIT, FAUX_OBJECT_PROPERTY, EDITOR, 2, num(4) },
                { EDIT, FAUX_OBJECT_PROPERTY, PUBLIC, 2, num(4) },
                { EDIT, FAUX_OBJECT_PROPERTY, CUSTOM, 2, num(4) },

                { ADD, FAUX_OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { ADD, FAUX_OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { ADD, FAUX_OBJECT_PROPERTY, EDITOR, 2, num(4) },
                { ADD, FAUX_OBJECT_PROPERTY, PUBLIC, 2, num(4) },
                { ADD, FAUX_OBJECT_PROPERTY, CUSTOM, 2, num(4) },

                { DROP, FAUX_OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { DROP, FAUX_OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { DROP, FAUX_OBJECT_PROPERTY, EDITOR, 2, num(4) },
                { DROP, FAUX_OBJECT_PROPERTY, PUBLIC, 2, num(4) },
                { DROP, FAUX_OBJECT_PROPERTY, CUSTOM, 2, num(4) },

                { EDIT, FAUX_DATA_PROPERTY, ADMIN, 2, num(4) },
                { EDIT, FAUX_DATA_PROPERTY, CURATOR, 2, num(4) },
                { EDIT, FAUX_DATA_PROPERTY, EDITOR, 2, num(4) },
                { EDIT, FAUX_DATA_PROPERTY, PUBLIC, 2, num(4) },
                { EDIT, FAUX_DATA_PROPERTY, CUSTOM, 2, num(4) },

                { ADD, FAUX_DATA_PROPERTY, ADMIN, 2, num(4) },
                { ADD, FAUX_DATA_PROPERTY, CURATOR, 2, num(4) },
                { ADD, FAUX_DATA_PROPERTY, EDITOR, 2, num(4) },
                { ADD, FAUX_DATA_PROPERTY, PUBLIC, 2, num(4) },
                { ADD, FAUX_DATA_PROPERTY, CUSTOM, 2, num(4) },

                { DROP, FAUX_DATA_PROPERTY, ADMIN, 2, num(4) },
                { DROP, FAUX_DATA_PROPERTY, CURATOR, 2, num(4) },
                { DROP, FAUX_DATA_PROPERTY, EDITOR, 2, num(4) },
                { DROP, FAUX_DATA_PROPERTY, PUBLIC, 2, num(4) },
                { DROP, FAUX_DATA_PROPERTY, CUSTOM, 2, num(4) },});
    }

    private static Set<Integer> num(int i) {
        return Collections.singleton(i);
    }

}
