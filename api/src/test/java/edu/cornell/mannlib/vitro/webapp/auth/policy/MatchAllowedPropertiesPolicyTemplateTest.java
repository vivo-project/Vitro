package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.DATA_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.FAUX_DATA_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.FAUX_OBJECT_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.OBJECT_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup.DISPLAY_GROUP;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup.PUBLISH_GROUP;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class MatchAllowedPropertiesPolicyTemplateTest extends PolicyTest {
    private static final String TEMPLATE_PROPERTY_PREFIX =
            "https://vivoweb.org/ontology/vitro-application/auth/individual/template/simple-match-allowed-property/";

    @org.junit.runners.Parameterized.Parameter(0)
    public String dataSetUri;

    @org.junit.runners.Parameterized.Parameter(1)
    public OperationGroup group;

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
        load(POLICY_TEMPLATE_MATCH_PROPERTY_PATH);
        EntityPolicyController.updateEntityPolicyDataSet("test:entity", type, group, Arrays.asList(roleUri), ROLE_LIST);
        DynamicPolicy policy = null;
        policy = loader.loadPolicyFromTemplateDataSet(TEMPLATE_PROPERTY_PREFIX + dataSetUri);
        countRulesAndAttributes(policy, rulesCount, attrCount);
        Set<String> values = loader.getDataSetValues(group, type, roleUri);
        assertFalse(values.isEmpty());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
                { "EditorDisplayDataPropertyDataSet", DISPLAY_GROUP, DATA_PROPERTY, ROLE_EDITOR_URI, 2,
                        Collections.singleton(4) },
                { "EditorDisplayObjectPropertyDataSet", DISPLAY_GROUP, OBJECT_PROPERTY, ROLE_EDITOR_URI, 2,
                        Collections.singleton(4) },
                { "PublicDisplayDataPropertyDataSet", DISPLAY_GROUP, DATA_PROPERTY, ROLE_PUBLIC_URI, 2,
                        Collections.singleton(4) },
                { "PublicDisplayObjectPropertyDataSet", DISPLAY_GROUP, OBJECT_PROPERTY, ROLE_PUBLIC_URI, 2,
                        Collections.singleton(4) },
                { "CuratorDisplayDataPropertyDataSet", DISPLAY_GROUP, DATA_PROPERTY, ROLE_CURATOR_URI, 2,
                        Collections.singleton(4) },
                { "CuratorDisplayObjectPropertyDataSet", DISPLAY_GROUP, OBJECT_PROPERTY, ROLE_CURATOR_URI, 2,
                        Collections.singleton(4) },
                { "AdminDisplayDataPropertyDataSet", DISPLAY_GROUP, DATA_PROPERTY, ROLE_ADMIN_URI, 2,
                        Collections.singleton(4) },
                { "AdminDisplayObjectPropertyDataSet", DISPLAY_GROUP, OBJECT_PROPERTY, ROLE_ADMIN_URI, 2,
                        Collections.singleton(4) },
                { "EditorPublishDataPropertyDataSet", PUBLISH_GROUP, DATA_PROPERTY, ROLE_EDITOR_URI, 2,
                        Collections.singleton(4) },
                { "EditorPublishObjectPropertyDataSet", PUBLISH_GROUP, OBJECT_PROPERTY, ROLE_EDITOR_URI, 2,
                        Collections.singleton(4) },
                { "CuratorPublishDataPropertyDataSet", PUBLISH_GROUP, DATA_PROPERTY, ROLE_CURATOR_URI, 2,
                        Collections.singleton(4) },
                { "CuratorPublishObjectPropertyDataSet", PUBLISH_GROUP, OBJECT_PROPERTY, ROLE_CURATOR_URI, 2,
                        Collections.singleton(4) },
                { "AdminPublishDataPropertyDataSet", PUBLISH_GROUP, DATA_PROPERTY, ROLE_ADMIN_URI, 2,
                        Collections.singleton(4) },
                { "AdminPublishObjectPropertyDataSet", PUBLISH_GROUP, OBJECT_PROPERTY, ROLE_ADMIN_URI, 2,
                        Collections.singleton(4) },
                { "AdminDisplayFauxObjectPropertyDataSet", DISPLAY_GROUP, FAUX_OBJECT_PROPERTY, ROLE_ADMIN_URI, 2,
                        Collections.singleton(4) },
                { "CuratorDisplayFauxObjectPropertyDataSet", DISPLAY_GROUP, FAUX_OBJECT_PROPERTY, ROLE_CURATOR_URI, 2,
                        Collections.singleton(4) },
                { "EditorDisplayFauxObjectPropertyDataSet", DISPLAY_GROUP, FAUX_OBJECT_PROPERTY, ROLE_EDITOR_URI, 2,
                        Collections.singleton(4) },
                { "PublicDisplayFauxObjectPropertyDataSet", DISPLAY_GROUP, FAUX_OBJECT_PROPERTY, ROLE_PUBLIC_URI, 2,
                        Collections.singleton(4) },
                { "AdminPublishFauxObjectPropertyDataSet", PUBLISH_GROUP, FAUX_OBJECT_PROPERTY, ROLE_ADMIN_URI, 2,
                        Collections.singleton(4) },
                { "CuratorPublishFauxObjectPropertyDataSet", PUBLISH_GROUP, FAUX_OBJECT_PROPERTY, ROLE_CURATOR_URI, 2,
                        Collections.singleton(4) },
                { "EditorPublishFauxObjectPropertyDataSet", PUBLISH_GROUP, FAUX_OBJECT_PROPERTY, ROLE_EDITOR_URI, 2,
                        Collections.singleton(4) },
                { "AdminDisplayFauxDataPropertyDataSet", DISPLAY_GROUP, FAUX_DATA_PROPERTY, ROLE_ADMIN_URI, 2,
                        Collections.singleton(4) },
                { "CuratorDisplayFauxDataPropertyDataSet", DISPLAY_GROUP, FAUX_DATA_PROPERTY, ROLE_CURATOR_URI, 2,
                        Collections.singleton(4) },
                { "EditorDisplayFauxDataPropertyDataSet", DISPLAY_GROUP, FAUX_DATA_PROPERTY, ROLE_EDITOR_URI, 2,
                        Collections.singleton(4) },
                { "PublicDisplayFauxDataPropertyDataSet", DISPLAY_GROUP, FAUX_DATA_PROPERTY, ROLE_PUBLIC_URI, 2,
                        Collections.singleton(4) },
                { "AdminPublishFauxDataPropertyDataSet", PUBLISH_GROUP, FAUX_DATA_PROPERTY, ROLE_ADMIN_URI, 2,
                        Collections.singleton(4) },
                { "CuratorPublishFauxDataPropertyDataSet", PUBLISH_GROUP, FAUX_DATA_PROPERTY, ROLE_CURATOR_URI, 2,
                        Collections.singleton(4) },
                { "EditorPublishFauxDataPropertyDataSet", PUBLISH_GROUP, FAUX_DATA_PROPERTY, ROLE_EDITOR_URI, 2,
                        Collections.singleton(4) },
        });
    }

}
