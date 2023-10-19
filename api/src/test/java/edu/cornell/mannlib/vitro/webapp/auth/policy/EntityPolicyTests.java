package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.DATA_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.FAUX_DATA_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.FAUX_OBJECT_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.OBJECT_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup.DISPLAY_GROUP;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup.PUBLISH_GROUP;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup.UPDATE_GROUP;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class EntityPolicyTests extends PolicyTest {
    private static final String TEMPLATE_SIMPLE_MATCH_PROPERTY = "template/simple-match-allowed-property/PolicyTemplate";
    private static final String TEMPLATE_SIMPLE_MATCH_CLASS = "template/simple-match-allowed-class/PolicyTemplate";

    private static final String TEMPLATE_MATCH_ALLOWED_DATASET =
            RESOURCES_PREFIX + "policy_template_match_allowed_dataset.n3";
    private static final String TEMPLATE_PROPERTY_PREFIX =
            "https://vivoweb.org/ontology/vitro-application/auth/individual/template/simple-match-allowed-property/";
    private static final String TEMPLATE_CLASS_PREFIX =
            "https://vivoweb.org/ontology/vitro-application/auth/individual/template/simple-match-allowed-class/";

    @org.junit.runners.Parameterized.Parameter(0)
    public String policyFilePath;

    @org.junit.runners.Parameterized.Parameter(1)
    public String testDataSetPath;

    @org.junit.runners.Parameterized.Parameter(2)
    public String uri;

    @org.junit.runners.Parameterized.Parameter(3)
    public String dataSetUri;

    @org.junit.runners.Parameterized.Parameter(4)
    public OperationGroup group;

    @org.junit.runners.Parameterized.Parameter(5)
    public AccessObjectType type;

    @org.junit.runners.Parameterized.Parameter(6)
    public String roleUri;

    @org.junit.runners.Parameterized.Parameter(7)
    public int rulesCount;

    @org.junit.runners.Parameterized.Parameter(8)
    public Set<Integer> attrCount;

    @Test
    public void testPolicy() {
        load(policyFilePath);
        load(testDataSetPath);
        String policyUri = PREFIX + uri;
        EntityPolicyController.updateEntityPolicyDataSet("test:entity", type, group, Arrays.asList(roleUri), ROLE_LIST);
        DynamicPolicy policy = null;
        if (dataSetUri != null) {
            policy = loader.loadPolicyFromTemplateDataSet(dataSetUri);
        } else {
            policy = loader.loadPolicy(policyUri);
        }
        countRulesAndAttributes(policy, rulesCount, attrCount);
        Set<String> values = loader.getDataSetValues(group, type, roleUri);
        assertFalse(values.isEmpty());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
                { SELF_EDITOR_DISPLAY_DATA_PROP_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "SelfEditorDisplayDataPropertyPolicy", null, DISPLAY_GROUP, DATA_PROPERTY, ROLE_SELF_EDITOR_URI, 2, Collections.singleton(4) },
                { SELF_EDITOR_DISPLAY_OBJ_PROP_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "SelfEditorDisplayObjectPropertyPolicy", null, DISPLAY_GROUP, OBJECT_PROPERTY, ROLE_SELF_EDITOR_URI, 2, Collections.singleton(4) },
                { SELF_EDITOR_PUBLISH_DATA_PROP_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "SelfEditorPublishDataPropertyPolicy", null, PUBLISH_GROUP, DATA_PROPERTY, ROLE_SELF_EDITOR_URI, 2, Collections.singleton(4) },
                { SELF_EDITOR_PUBLISH_OBJ_PROP_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "SelfEditorPublishObjectPropertyPolicy", null, PUBLISH_GROUP, OBJECT_PROPERTY, ROLE_SELF_EDITOR_URI, 2, Collections.singleton(4) },

                { EDITOR_UPDATE_DATA_PROP_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "EditorUpdateDataPropertyPolicy", null, UPDATE_GROUP, DATA_PROPERTY, ROLE_EDITOR_URI, 2, Collections.singleton(4) },
                { EDITOR_UPDATE_OBJ_PROP_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "EditorUpdateObjectPropertyPolicy", null, UPDATE_GROUP, OBJECT_PROPERTY, ROLE_EDITOR_URI, 2, Collections.singleton(4) },

                { SELF_EDITOR_UPDATE_DATA_PROP_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "SelfEditorUpdateDataPropertyPolicy", null, UPDATE_GROUP, DATA_PROPERTY, ROLE_SELF_EDITOR_URI, 2, new HashSet<>(Arrays.asList(4, 5)) },
                { SELF_EDITOR_UPDATE_OBJ_PROP_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "SelfEditorUpdateObjectPropertyPolicy", null, UPDATE_GROUP, OBJECT_PROPERTY, ROLE_SELF_EDITOR_URI, 2, new HashSet<>(Arrays.asList(4, 5)) },

                { PUBLIC_UPDATE_DATA_PROP_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "PublicUpdateDataPropertyPolicy", null, UPDATE_GROUP, DATA_PROPERTY, ROLE_PUBLIC_URI, 2, Collections.singleton(4) },
                { PUBLIC_UPDATE_OBJ_PROP_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "PublicUpdateObjectPropertyPolicy", null, UPDATE_GROUP, OBJECT_PROPERTY, ROLE_PUBLIC_URI, 2, Collections.singleton(4) },

                { CURATOR_UPDATE_DATA_PROP_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "CuratorUpdateDataPropertyPolicy", null, UPDATE_GROUP, DATA_PROPERTY, ROLE_CURATOR_URI, 2, Collections.singleton(4) },
                { CURATOR_UPDATE_OBJ_PROP_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "CuratorUpdateObjectPropertyPolicy", null, UPDATE_GROUP, OBJECT_PROPERTY, ROLE_CURATOR_URI, 2, Collections.singleton(4) },

                { ADMIN_UPDATE_DATA_PROP_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "AdminUpdateDataPropertyPolicy", null, UPDATE_GROUP, DATA_PROPERTY, ROLE_ADMIN_URI, 2, Collections.singleton(4) },
                { ADMIN_UPDATE_OBJ_PROP_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "AdminUpdateObjectPropertyPolicy", null, UPDATE_GROUP, OBJECT_PROPERTY, ROLE_ADMIN_URI, 2, Collections.singleton(4) },
                
                { SELF_EDITOR_DISPLAY_FAUX_OBJECT_PROPERTY_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "SelfEditorDisplayFauxObjectPropertyPolicy", null, DISPLAY_GROUP, FAUX_OBJECT_PROPERTY, ROLE_SELF_EDITOR_URI, 2, Collections.singleton(4) },
                { SELF_EDITOR_PUBLISH_FAUX_OBJECT_PROPERTY_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, TEMPLATE_SIMPLE_MATCH_PROPERTY, TEMPLATE_PROPERTY_PREFIX + "SelfEditorPublishFauxObjectPropertyDataSet", PUBLISH_GROUP, FAUX_OBJECT_PROPERTY, ROLE_SELF_EDITOR_URI, 2, Collections.singleton(4) },

                { ADMIN_UPDATE_FAUX_OBJECT_PROPERTY_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "AdminUpdateFauxObjectPropertyPolicy", null, UPDATE_GROUP, FAUX_OBJECT_PROPERTY, ROLE_ADMIN_URI, 2, Collections.singleton(4) },
                { CURATOR_UPDATE_FAUX_OBJECT_PROPERTY_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "CuratorUpdateFauxObjectPropertyPolicy", null, UPDATE_GROUP, FAUX_OBJECT_PROPERTY, ROLE_CURATOR_URI, 2, Collections.singleton(4) },
                { EDITOR_UPDATE_FAUX_OBJECT_PROPERTY_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "EditorUpdateFauxObjectPropertyPolicy", null, UPDATE_GROUP, FAUX_OBJECT_PROPERTY, ROLE_EDITOR_URI, 2, Collections.singleton(4) },
                { SELF_EDITOR_UPDATE_FAUX_OBJECT_PROPERTY_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "SelfEditorUpdateFauxObjectPropertyPolicy", null, UPDATE_GROUP, FAUX_OBJECT_PROPERTY, ROLE_SELF_EDITOR_URI, 2, new HashSet<>(Arrays.asList(4, 5)) },

                { SELF_EDITOR_DISPLAY_FAUX_DATA_PROPERTY_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "SelfEditorDisplayFauxDataPropertyPolicy", null, DISPLAY_GROUP, FAUX_DATA_PROPERTY, ROLE_SELF_EDITOR_URI, 2, Collections.singleton(4) },
                { SELF_EDITOR_PUBLISH_FAUX_DATA_PROPERTY_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "SelfEditorPublishFauxDataPropertyPolicy", null, PUBLISH_GROUP, FAUX_DATA_PROPERTY, ROLE_SELF_EDITOR_URI, 2, Collections.singleton(4) },

                { ADMIN_UPDATE_FAUX_DATA_PROPERTY_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "AdminUpdateFauxDataPropertyPolicy", null, UPDATE_GROUP, FAUX_DATA_PROPERTY, ROLE_ADMIN_URI, 2, Collections.singleton(4) },
                { CURATOR_UPDATE_FAUX_DATA_PROPERTY_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "CuratorUpdateFauxDataPropertyPolicy", null, UPDATE_GROUP, FAUX_DATA_PROPERTY, ROLE_CURATOR_URI, 2, Collections.singleton(4) },
                { EDITOR_UPDATE_FAUX_DATA_PROPERTY_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "EditorUpdateFauxDataPropertyPolicy", null, UPDATE_GROUP, FAUX_DATA_PROPERTY, ROLE_EDITOR_URI, 2, Collections.singleton(4) },
                { SELF_EDITOR_UPDATE_FAUX_DATA_PROPERTY_POLICY_PATH, TEMPLATE_MATCH_ALLOWED_DATASET, "SelfEditorUpdateFauxDataPropertyPolicy", null, UPDATE_GROUP, FAUX_DATA_PROPERTY, ROLE_SELF_EDITOR_URI, 2, new HashSet<>(Arrays.asList(4, 5)) },

        });
    }

}
