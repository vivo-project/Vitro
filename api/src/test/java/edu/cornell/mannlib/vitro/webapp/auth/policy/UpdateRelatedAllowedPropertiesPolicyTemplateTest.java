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
    private static final String TEMPLATE_PROPERTY_PREFIX =
            "https://vivoweb.org/ontology/vitro-application/auth/individual/template/update-related-allowed-property/";

    public static final String POLICY_TEMPLATE_PATH =
            USER_ACCOUNTS_HOME_FIRSTTIME + "template_update_related_allowed_property.n3";
    @org.junit.runners.Parameterized.Parameter(0)
    public String dataSetUri;

    @org.junit.runners.Parameterized.Parameter(1)
    public AccessOperation group;

    @org.junit.runners.Parameterized.Parameter(2)
    public AccessObjectType type;

    @org.junit.runners.Parameterized.Parameter(3)
    public String roleUri;

    @org.junit.runners.Parameterized.Parameter(4)
    public int rulesCount;

    @org.junit.runners.Parameterized.Parameter(5)
    public Set<Integer> attrCount;

    @Test
    public void testPolicy() {
        load(POLICY_TEMPLATE_PATH);
        EntityPolicyController.updateEntityPolicyDataSet("test:entity", type, group, Arrays.asList(roleUri), ROLE_LIST);
        DynamicPolicy policy = null;
        policy = loader.loadPolicyFromTemplateDataSet(TEMPLATE_PROPERTY_PREFIX + "SelfEditor" + dataSetUri + "DataSet");
        countRulesAndAttributes(policy, rulesCount, attrCount);
        Set<String> values = loader.getDataSetValues(group, type, roleUri);
        assertFalse(values.isEmpty());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {

                { "AddObjectProperty", ADD, OBJECT_PROPERTY, SELF_EDITOR, 2, num(4, 5) },
                { "AddDataProperty", ADD, DATA_PROPERTY, SELF_EDITOR, 2, num(4, 5) },
                { "AddFauxObjectProperty", ADD, FAUX_OBJECT_PROPERTY, SELF_EDITOR, 2, num(4, 5) },
                { "AddFauxDataProperty", ADD, FAUX_DATA_PROPERTY, SELF_EDITOR, 2, num(4, 5) },

                { "DropObjectProperty", DROP, OBJECT_PROPERTY, SELF_EDITOR, 2, num(4, 5) },
                { "DropDataProperty", DROP, DATA_PROPERTY, SELF_EDITOR, 2, num(4, 5) },
                { "DropFauxObjectProperty", DROP, FAUX_OBJECT_PROPERTY, SELF_EDITOR, 2, num(4, 5) },
                { "DropFauxDataProperty", DROP, FAUX_DATA_PROPERTY, SELF_EDITOR, 2, num(4, 5) },

                { "EditObjectProperty", EDIT, OBJECT_PROPERTY, SELF_EDITOR, 2, num(4, 5) },
                { "EditDataProperty", EDIT, DATA_PROPERTY, SELF_EDITOR, 2, num(4, 5) },
                { "EditFauxObjectProperty", EDIT, FAUX_OBJECT_PROPERTY, SELF_EDITOR, 2, num(4, 5) },
                { "EditFauxDataProperty", EDIT, FAUX_DATA_PROPERTY, SELF_EDITOR, 2, num(4, 5) }, });

    }

    private static Set<Integer> num(Integer... i) {
        return new HashSet<>(Arrays.asList(i));
    }

}
