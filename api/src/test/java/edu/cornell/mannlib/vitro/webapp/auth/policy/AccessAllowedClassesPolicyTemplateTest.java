package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.CLASS;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.PUBLISH;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.UPDATE;
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
public class AccessAllowedClassesPolicyTemplateTest extends PolicyTest {

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
        load(TEMPLATE_CLASS_PATH);
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
            { DISPLAY, CLASS, PUBLIC, 1, Collections.singleton(4) },
            { DISPLAY, CLASS, SELF_EDITOR, 1, Collections.singleton(4) },
            { DISPLAY, CLASS, EDITOR, 1, Collections.singleton(4) },
            { DISPLAY, CLASS, CURATOR, 1, Collections.singleton(4) },
            { DISPLAY, CLASS, ADMIN, 1, Collections.singleton(4) },
            { DISPLAY, CLASS, CUSTOM, 1, Collections.singleton(4) },

            { PUBLISH, CLASS, SELF_EDITOR, 1, Collections.singleton(4) },
            { PUBLISH, CLASS, EDITOR, 1, Collections.singleton(4) },
            { PUBLISH, CLASS, CURATOR, 1, Collections.singleton(4) },
            { PUBLISH, CLASS, ADMIN, 1, Collections.singleton(4) },
            { PUBLISH, CLASS, CUSTOM, 1, Collections.singleton(4) },

            { UPDATE, CLASS, PUBLIC, 1, Collections.singleton(4) },
            { UPDATE, CLASS, SELF_EDITOR, 1, Collections.singleton(4) },
            { UPDATE, CLASS, EDITOR, 1, Collections.singleton(4) },
            { UPDATE, CLASS, CURATOR, 1, Collections.singleton(4) },
            { UPDATE, CLASS, ADMIN, 1, Collections.singleton(4) },
            { UPDATE, CLASS, CUSTOM, 1, Collections.singleton(4) }, });

    }

}
