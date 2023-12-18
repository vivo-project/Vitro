package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.AUTH_INDIVIDUAL_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class HidePropertiesNotRelatedToSelfEditorTemplateTest extends PolicyTest {

    public static final String POLICY_PATH = USER_ACCOUNTS_HOME_FIRSTTIME + "template_hide_not_related_property.n3";

    @org.junit.runners.Parameterized.Parameter(0)
    public String dataSetName;

    @Test
    public void testLoadPolicy() {
        load(POLICY_PATH);
        load(RESOURCES_RULES_PREFIX + "hide_entities_value_set.n3");
        String policyPrefix = AUTH_INDIVIDUAL_PREFIX + "hide-not-related-property/";
        String dataSetUri = policyPrefix + dataSetName;
        DynamicPolicy policy = loader.loadPolicyFromTemplateDataSet(dataSetUri);
        assertTrue(policy != null);
        assertEquals(5000, policy.getPriority());
        countRulesAndAttributes(policy, 1, Collections.singleton(5));
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
            { "SelfEditorHideNotRelatedObjectPropertyDataSet" },
            { "SelfEditorHideNotRelatedDataPropertyDataSet" },
            { "SelfEditorHideNotRelatedFauxObjectPropertyDataSet" },
            { "SelfEditorHideNotRelatedFauxDataPropertyDataSet" }, });
    }
}
