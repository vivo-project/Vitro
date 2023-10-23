package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.DATA_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.FAUX_DATA_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.FAUX_OBJECT_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.OBJECT_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.PUBLISH;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class AccessRelatedAllowedPropertiesPolicyTemplateTest extends PolicyTest {
    private static final String TEMPLATE_PROPERTY_PREFIX =
            "https://vivoweb.org/ontology/vitro-application/auth/individual/template/access-allowed-property/";

    public static final String POLICY_TEMPLATE_PATH =
            USER_ACCOUNTS_HOME_FIRSTTIME + "template_access_related_allowed_property.n3";
    @org.junit.runners.Parameterized.Parameter(0)
    public String dataSetUri;

    @org.junit.runners.Parameterized.Parameter(1)
    public AccessOperation ao;

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
        EntityPolicyController.updateEntityDataSet("test:entity", type, ao, Arrays.asList(roleUri), ROLE_LIST);
        DynamicPolicy policy = null;
        policy = loader.loadPolicyFromTemplateDataSet(TEMPLATE_PROPERTY_PREFIX + dataSetUri);
        countRulesAndAttributes(policy, rulesCount, attrCount);
        Set<String> values = loader.getDataSetValues(ao, type, roleUri);
        assertFalse(values.isEmpty());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {

                { "SelfEditorDisplayObjectPropertyDataSet", DISPLAY, OBJECT_PROPERTY, SELF_EDITOR, 2, num(4) },
                { "SelfEditorDisplayDataPropertyDataSet", DISPLAY, DATA_PROPERTY, SELF_EDITOR, 2, num(4) },
                { "SelfEditorDisplayFauxObjectPropertyDataSet", DISPLAY, FAUX_OBJECT_PROPERTY, SELF_EDITOR, 2, num(4) },
                { "SelfEditorDisplayFauxDataPropertyDataSet", DISPLAY, FAUX_DATA_PROPERTY, SELF_EDITOR, 2, num(4) },
                { "SelfEditorPublishObjectPropertyDataSet", PUBLISH, OBJECT_PROPERTY, SELF_EDITOR, 2, num(4) },
                { "SelfEditorPublishDataPropertyDataSet", PUBLISH, DATA_PROPERTY, SELF_EDITOR, 2, num(4) },
                { "SelfEditorPublishFauxObjectPropertyDataSet", PUBLISH, FAUX_OBJECT_PROPERTY, SELF_EDITOR, 2, num(4) },
                { "SelfEditorPublishFauxDataPropertyDataSet", PUBLISH, FAUX_DATA_PROPERTY, SELF_EDITOR, 2, num(4) }, });
    }

    private static Set<Integer> num(int i) {
        return Collections.singleton(i);
    }

}
