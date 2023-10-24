package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.DATA_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.FAUX_DATA_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.FAUX_OBJECT_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.OBJECT_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.ADD;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.DROP;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.EDIT;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class UpdateRelatedAllowedPropertiesPolicyTemplateTest extends PolicyTest {

    public static final String POLICY_TEMPLATE_PATH =
            USER_ACCOUNTS_HOME_FIRSTTIME + "template_update_related_allowed_property.n3";

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
        load(POLICY_TEMPLATE_PATH);
        EntityPolicyController.updateEntityDataSet("test:entity", type, ao, Arrays.asList(roleUri), ROLE_LIST);
        DynamicPolicy policy = null;
        String dataSet = loader.getDataSetUriByKey(new String[] { roleUri }, new String[] { ao.toString(), type.toString() });

        policy = loader.loadPolicyFromTemplateDataSet(dataSet);
        countRulesAndAttributes(policy, rulesCount, attrCount);
        Set<String> values = loader.getDataSetValues(ao, type, roleUri);
        assertFalse(values.isEmpty());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {

                { ADD, OBJECT_PROPERTY, SELF_EDITOR, 2, num(4, 5) },
                { ADD, DATA_PROPERTY, SELF_EDITOR, 2, num(4, 5) },
                { ADD, FAUX_OBJECT_PROPERTY, SELF_EDITOR, 2, num(4, 5) },
                { ADD, FAUX_DATA_PROPERTY, SELF_EDITOR, 2, num(4, 5) },

                { DROP, OBJECT_PROPERTY, SELF_EDITOR, 2, num(4, 5) },
                { DROP, DATA_PROPERTY, SELF_EDITOR, 2, num(4, 5) },
                { DROP, FAUX_OBJECT_PROPERTY, SELF_EDITOR, 2, num(4, 5) },
                { DROP, FAUX_DATA_PROPERTY, SELF_EDITOR, 2, num(4, 5) },

                { EDIT, OBJECT_PROPERTY, SELF_EDITOR, 2, num(4, 5) },
                { EDIT, DATA_PROPERTY, SELF_EDITOR, 2, num(4, 5) },
                { EDIT, FAUX_OBJECT_PROPERTY, SELF_EDITOR, 2, num(4, 5) },
                { EDIT, FAUX_DATA_PROPERTY, SELF_EDITOR, 2, num(4, 5) }, });

    }

    private static Set<Integer> num(Integer... i) {
        return new HashSet<>(Arrays.asList(i));
    }

}
