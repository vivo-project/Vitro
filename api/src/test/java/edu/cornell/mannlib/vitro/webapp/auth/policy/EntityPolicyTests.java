package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.CLASS;
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
    @org.junit.runners.Parameterized.Parameter(0)
    public String filePath;

    @org.junit.runners.Parameterized.Parameter(1)
    public String uri;

    @org.junit.runners.Parameterized.Parameter(2)
    public OperationGroup group;

    @org.junit.runners.Parameterized.Parameter(3)
    public AccessObjectType type;

    @org.junit.runners.Parameterized.Parameter(4)
    public String roleUri;

    @org.junit.runners.Parameterized.Parameter(5)
    public int rulesCount;

    @org.junit.runners.Parameterized.Parameter(6)
    public Set<Integer> attrCount;

    @Test
    public void testPolicy() {
        load(filePath);
        String policyUri = PREFIX + uri;
        EntityPolicyController.updateEntityPolicy("test:entity", type, group, Arrays.asList(roleUri), ROLE_LIST);
        DynamicPolicy policy = loader.loadPolicy(policyUri);
        countRulesAndAttributes(policy, rulesCount, attrCount);
        Set<String> values = loader.getPolicyDataSetValues(group, type, roleUri);
        assertFalse(values.isEmpty());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
            { EDITOR_DISPLAY_CLASS_POLICY_PATH, "EditorDisplayClassPolicy", DISPLAY_GROUP,
                CLASS, ROLE_EDITOR_URI, 1, Collections.singleton(4) },
            { EDITOR_DISPLAY_DATA_PROP_POLICY_PATH, "EditorDisplayDataPropertyPolicy", DISPLAY_GROUP,
                DATA_PROPERTY, ROLE_EDITOR_URI, 2, Collections.singleton(4) },
            { EDITOR_DISPLAY_OBJ_PROP_POLICY_PATH, "EditorDisplayObjectPropertyPolicy",
                DISPLAY_GROUP, OBJECT_PROPERTY, ROLE_EDITOR_URI, 2,
                Collections.singleton(4) },

            { SELF_EDITOR_DISPLAY_CLASS_POLICY_PATH, "SelfEditorDisplayClassPolicy", DISPLAY_GROUP,
                CLASS, ROLE_SELF_EDITOR_URI, 1, Collections.singleton(4) },
            { SELF_EDITOR_DISPLAY_DATA_PROP_POLICY_PATH, "SelfEditorDisplayDataPropertyPolicy",
                DISPLAY_GROUP, DATA_PROPERTY, ROLE_SELF_EDITOR_URI, 2,
                Collections.singleton(4) },
            { SELF_EDITOR_DISPLAY_OBJ_PROP_POLICY_PATH, "SelfEditorDisplayObjectPropertyPolicy",
                DISPLAY_GROUP, OBJECT_PROPERTY, ROLE_SELF_EDITOR_URI, 2,
                Collections.singleton(4) },

            { PUBLIC_DISPLAY_CLASS_POLICY_PATH, "PublicDisplayClassPolicy", DISPLAY_GROUP,
                CLASS, ROLE_PUBLIC_URI, 1, Collections.singleton(4) },
            { PUBLIC_DISPLAY_DATA_PROP_POLICY_PATH, "PublicDisplayDataPropertyPolicy", DISPLAY_GROUP,
                DATA_PROPERTY, ROLE_PUBLIC_URI, 2, Collections.singleton(4) },
            { PUBLIC_DISPLAY_OBJ_PROP_POLICY_PATH, "PublicDisplayObjectPropertyPolicy",
                DISPLAY_GROUP, OBJECT_PROPERTY, ROLE_PUBLIC_URI, 2,
                Collections.singleton(4) },

            { CURATOR_DISPLAY_CLASS_POLICY_PATH, "CuratorDisplayClassPolicy", DISPLAY_GROUP,
                CLASS, ROLE_CURATOR_URI, 1, Collections.singleton(4) },
            { CURATOR_DISPLAY_DATA_PROP_POLICY_PATH, "CuratorDisplayDataPropertyPolicy",
                DISPLAY_GROUP, DATA_PROPERTY, ROLE_CURATOR_URI, 2,
                Collections.singleton(4) },
            { CURATOR_DISPLAY_OBJ_PROP_POLICY_PATH, "CuratorDisplayObjectPropertyPolicy",
                DISPLAY_GROUP, OBJECT_PROPERTY, ROLE_CURATOR_URI, 2,
                Collections.singleton(4) },

            { ADMIN_DISPLAY_CLASS_POLICY_PATH, "AdminDisplayClassPolicy", DISPLAY_GROUP,
                CLASS, ROLE_ADMIN_URI, 1, Collections.singleton(4) },
            { ADMIN_DISPLAY_DATA_PROP_POLICY_PATH, "AdminDisplayDataPropertyPolicy", DISPLAY_GROUP,
                DATA_PROPERTY, ROLE_ADMIN_URI, 1, Collections.singleton(4) },
            { ADMIN_DISPLAY_OBJ_PROP_POLICY_PATH, "AdminDisplayObjectPropertyPolicy", DISPLAY_GROUP,
                OBJECT_PROPERTY, ROLE_ADMIN_URI, 2, Collections.singleton(4) },

            { EDITOR_PUBLISH_CLASS_POLICY_PATH, "EditorPublishClassPolicy", PUBLISH_GROUP,
                CLASS, ROLE_EDITOR_URI, 1, Collections.singleton(4) },
            { EDITOR_PUBLISH_DATA_PROP_POLICY_PATH, "EditorPublishDataPropertyPolicy", PUBLISH_GROUP,
                DATA_PROPERTY, ROLE_EDITOR_URI, 2, Collections.singleton(4) },
            { EDITOR_PUBLISH_OBJ_PROP_POLICY_PATH, "EditorPublishObjectPropertyPolicy",
                PUBLISH_GROUP, OBJECT_PROPERTY, ROLE_EDITOR_URI, 2,
                Collections.singleton(4) },

            { SELF_EDITOR_PUBLISH_CLASS_POLICY_PATH, "SelfEditorPublishClassPolicy", PUBLISH_GROUP,
                CLASS, ROLE_SELF_EDITOR_URI, 1, Collections.singleton(4) },
            { SELF_EDITOR_PUBLISH_DATA_PROP_POLICY_PATH, "SelfEditorPublishDataPropertyPolicy",
                PUBLISH_GROUP, DATA_PROPERTY, ROLE_SELF_EDITOR_URI, 2,
                Collections.singleton(4) },
            { SELF_EDITOR_PUBLISH_OBJ_PROP_POLICY_PATH, "SelfEditorPublishObjectPropertyPolicy",
                PUBLISH_GROUP, OBJECT_PROPERTY, ROLE_SELF_EDITOR_URI, 2,
                Collections.singleton(4) },

            { CURATOR_PUBLISH_CLASS_POLICY_PATH, "CuratorPublishClassPolicy", PUBLISH_GROUP,
                CLASS, ROLE_CURATOR_URI, 1, Collections.singleton(4) },
            { CURATOR_PUBLISH_DATA_PROP_POLICY_PATH, "CuratorPublishDataPropertyPolicy",
                PUBLISH_GROUP, DATA_PROPERTY, ROLE_CURATOR_URI, 2,
                Collections.singleton(4) },
            { CURATOR_PUBLISH_OBJ_PROP_POLICY_PATH, "CuratorPublishObjectPropertyPolicy",
                PUBLISH_GROUP, OBJECT_PROPERTY, ROLE_CURATOR_URI, 2,
                Collections.singleton(4) },

            { ADMIN_PUBLISH_CLASS_POLICY_PATH, "AdminPublishClassPolicy", PUBLISH_GROUP,
                CLASS, ROLE_ADMIN_URI, 1, Collections.singleton(4) },
            { ADMIN_PUBLISH_DATA_PROP_POLICY_PATH, "AdminPublishDataPropertyPolicy", PUBLISH_GROUP,
                DATA_PROPERTY, ROLE_ADMIN_URI, 2, Collections.singleton(4) },
            { ADMIN_PUBLISH_OBJ_PROP_POLICY_PATH, "AdminPublishObjectPropertyPolicy", PUBLISH_GROUP,
                OBJECT_PROPERTY, ROLE_ADMIN_URI, 2, Collections.singleton(4) },

            { EDITOR_UPDATE_CLASS_POLICY_PATH, "EditorUpdateClassPolicy", UPDATE_GROUP,
                CLASS, ROLE_EDITOR_URI, 1, Collections.singleton(4) },
            { EDITOR_UPDATE_DATA_PROP_POLICY_PATH, "EditorUpdateDataPropertyPolicy", UPDATE_GROUP,
                DATA_PROPERTY, ROLE_EDITOR_URI, 2, Collections.singleton(4) },
            { EDITOR_UPDATE_OBJ_PROP_POLICY_PATH, "EditorUpdateObjectPropertyPolicy", UPDATE_GROUP,
                OBJECT_PROPERTY, ROLE_EDITOR_URI, 2, Collections.singleton(4) },

            { SELF_EDITOR_UPDATE_CLASS_POLICY_PATH, "SelfEditorUpdateClassPolicy", UPDATE_GROUP,
                CLASS, ROLE_SELF_EDITOR_URI, 1, Collections.singleton(4) },
            { SELF_EDITOR_UPDATE_DATA_PROP_POLICY_PATH, "SelfEditorUpdateDataPropertyPolicy",
                UPDATE_GROUP, DATA_PROPERTY, ROLE_SELF_EDITOR_URI, 2,
                new HashSet<>(Arrays.asList(4, 5)) },
            { SELF_EDITOR_UPDATE_OBJ_PROP_POLICY_PATH, "SelfEditorUpdateObjectPropertyPolicy",
                UPDATE_GROUP, OBJECT_PROPERTY, ROLE_SELF_EDITOR_URI, 2,
                new HashSet<>(Arrays.asList(4, 5)) },

            { PUBLIC_UPDATE_CLASS_POLICY_PATH, "PublicUpdateClassPolicy", UPDATE_GROUP,
                CLASS, ROLE_PUBLIC_URI, 1, Collections.singleton(4) },
            { PUBLIC_UPDATE_DATA_PROP_POLICY_PATH, "PublicUpdateDataPropertyPolicy", UPDATE_GROUP,
                DATA_PROPERTY, ROLE_PUBLIC_URI, 2, Collections.singleton(4) },
            { PUBLIC_UPDATE_OBJ_PROP_POLICY_PATH, "PublicUpdateObjectPropertyPolicy", UPDATE_GROUP,
                OBJECT_PROPERTY, ROLE_PUBLIC_URI, 2, Collections.singleton(4) },

            { CURATOR_UPDATE_CLASS_POLICY_PATH, "CuratorUpdateClassPolicy", UPDATE_GROUP,
                CLASS, ROLE_CURATOR_URI, 1, Collections.singleton(4) },
            { CURATOR_UPDATE_DATA_PROP_POLICY_PATH, "CuratorUpdateDataPropertyPolicy", UPDATE_GROUP,
                DATA_PROPERTY, ROLE_CURATOR_URI, 2, Collections.singleton(4) },
            { CURATOR_UPDATE_OBJ_PROP_POLICY_PATH, "CuratorUpdateObjectPropertyPolicy", UPDATE_GROUP,
                OBJECT_PROPERTY, ROLE_CURATOR_URI, 2, Collections.singleton(4) },

            { ADMIN_UPDATE_CLASS_POLICY_PATH, "AdminUpdateClassPolicy", UPDATE_GROUP,
                CLASS, ROLE_ADMIN_URI, 1, Collections.singleton(4) },
            { ADMIN_UPDATE_DATA_PROP_POLICY_PATH, "AdminUpdateDataPropertyPolicy", UPDATE_GROUP,
                DATA_PROPERTY, ROLE_ADMIN_URI, 2, Collections.singleton(4) },
            { ADMIN_UPDATE_OBJ_PROP_POLICY_PATH, "AdminUpdateObjectPropertyPolicy", UPDATE_GROUP,
                OBJECT_PROPERTY, ROLE_ADMIN_URI, 2, Collections.singleton(4) },

            { ADMIN_DISPLAY_FAUX_OBJECT_PROPERTY_POLICY_PATH, "AdminDisplayFauxObjectPropertyPolicy",
                DISPLAY_GROUP, FAUX_OBJECT_PROPERTY, ROLE_ADMIN_URI, 2,
                Collections.singleton(4) },
            { CURATOR_DISPLAY_FAUX_OBJECT_PROPERTY_POLICY_PATH, "CuratorDisplayFauxObjectPropertyPolicy",
                DISPLAY_GROUP, FAUX_OBJECT_PROPERTY, ROLE_CURATOR_URI, 2,
                Collections.singleton(4) },
            { EDITOR_DISPLAY_FAUX_OBJECT_PROPERTY_POLICY_PATH, "EditorDisplayFauxObjectPropertyPolicy",
                DISPLAY_GROUP, FAUX_OBJECT_PROPERTY, ROLE_EDITOR_URI, 2,
                Collections.singleton(4) },
            { SELF_EDITOR_DISPLAY_FAUX_OBJECT_PROPERTY_POLICY_PATH, "SelfEditorDisplayFauxObjectPropertyPolicy",
                DISPLAY_GROUP, FAUX_OBJECT_PROPERTY, ROLE_SELF_EDITOR_URI, 2,
                Collections.singleton(4) },
            { PUBLIC_DISPLAY_FAUX_OBJECT_PROPERTY_POLICY_PATH, "PublicDisplayFauxObjectPropertyPolicy",
                DISPLAY_GROUP, FAUX_OBJECT_PROPERTY, ROLE_PUBLIC_URI, 2,
                Collections.singleton(4) },

            { ADMIN_PUBLISH_FAUX_OBJECT_PROPERTY_POLICY_PATH, "AdminPublishFauxObjectPropertyPolicy",
                PUBLISH_GROUP, FAUX_OBJECT_PROPERTY, ROLE_ADMIN_URI, 2,
                Collections.singleton(4) },
            { CURATOR_PUBLISH_FAUX_OBJECT_PROPERTY_POLICY_PATH, "CuratorPublishFauxObjectPropertyPolicy",
                PUBLISH_GROUP, FAUX_OBJECT_PROPERTY, ROLE_CURATOR_URI, 2,
                Collections.singleton(4) },
            { EDITOR_PUBLISH_FAUX_OBJECT_PROPERTY_POLICY_PATH, "EditorPublishFauxObjectPropertyPolicy",
                PUBLISH_GROUP, FAUX_OBJECT_PROPERTY, ROLE_EDITOR_URI, 2,
                Collections.singleton(4) },
            { SELF_EDITOR_PUBLISH_FAUX_OBJECT_PROPERTY_POLICY_PATH, "SelfEditorPublishFauxObjectPropertyPolicy",
                PUBLISH_GROUP, FAUX_OBJECT_PROPERTY, ROLE_SELF_EDITOR_URI, 2,
                Collections.singleton(4) },

            { ADMIN_UPDATE_FAUX_OBJECT_PROPERTY_POLICY_PATH, "AdminUpdateFauxObjectPropertyPolicy",
                UPDATE_GROUP, FAUX_OBJECT_PROPERTY, ROLE_ADMIN_URI, 2,
                Collections.singleton(4) },
            { CURATOR_UPDATE_FAUX_OBJECT_PROPERTY_POLICY_PATH, "CuratorUpdateFauxObjectPropertyPolicy",
                UPDATE_GROUP, FAUX_OBJECT_PROPERTY, ROLE_CURATOR_URI, 2,
                Collections.singleton(4) },
            { EDITOR_UPDATE_FAUX_OBJECT_PROPERTY_POLICY_PATH, "EditorUpdateFauxObjectPropertyPolicy",
                UPDATE_GROUP, FAUX_OBJECT_PROPERTY, ROLE_EDITOR_URI, 2,
                Collections.singleton(4) },
            { SELF_EDITOR_UPDATE_FAUX_OBJECT_PROPERTY_POLICY_PATH, "SelfEditorUpdateFauxObjectPropertyPolicy",
                UPDATE_GROUP, FAUX_OBJECT_PROPERTY, ROLE_SELF_EDITOR_URI, 2,
                new HashSet<>(Arrays.asList(4, 5)) },

            { ADMIN_DISPLAY_FAUX_DATA_PROPERTY_POLICY_PATH, "AdminDisplayFauxDataPropertyPolicy",
                DISPLAY_GROUP, FAUX_DATA_PROPERTY, ROLE_ADMIN_URI, 2,
                Collections.singleton(4) },
            { CURATOR_DISPLAY_FAUX_DATA_PROPERTY_POLICY_PATH, "CuratorDisplayFauxDataPropertyPolicy",
                DISPLAY_GROUP, FAUX_DATA_PROPERTY, ROLE_CURATOR_URI, 2,
                Collections.singleton(4) },
            { EDITOR_DISPLAY_FAUX_DATA_PROPERTY_POLICY_PATH, "EditorDisplayFauxDataPropertyPolicy",
                DISPLAY_GROUP, FAUX_DATA_PROPERTY, ROLE_EDITOR_URI, 2,
                Collections.singleton(4) },
            { SELF_EDITOR_DISPLAY_FAUX_DATA_PROPERTY_POLICY_PATH, "SelfEditorDisplayFauxDataPropertyPolicy",
                DISPLAY_GROUP, FAUX_DATA_PROPERTY, ROLE_SELF_EDITOR_URI, 2,
                Collections.singleton(4) },
            { PUBLIC_DISPLAY_FAUX_DATA_PROPERTY_POLICY_PATH, "PublicDisplayFauxDataPropertyPolicy",
                DISPLAY_GROUP, FAUX_DATA_PROPERTY, ROLE_PUBLIC_URI, 2,
                Collections.singleton(4) },

            { ADMIN_PUBLISH_FAUX_DATA_PROPERTY_POLICY_PATH, "AdminPublishFauxDataPropertyPolicy",
                PUBLISH_GROUP, FAUX_DATA_PROPERTY, ROLE_ADMIN_URI, 2,
                Collections.singleton(4) },
            { CURATOR_PUBLISH_FAUX_DATA_PROPERTY_POLICY_PATH, "CuratorPublishFauxDataPropertyPolicy",
                PUBLISH_GROUP, FAUX_DATA_PROPERTY, ROLE_CURATOR_URI, 2,
                Collections.singleton(4) },
            { EDITOR_PUBLISH_FAUX_DATA_PROPERTY_POLICY_PATH, "EditorPublishFauxDataPropertyPolicy",
                PUBLISH_GROUP, FAUX_DATA_PROPERTY, ROLE_EDITOR_URI, 2,
                Collections.singleton(4) },
            { SELF_EDITOR_PUBLISH_FAUX_DATA_PROPERTY_POLICY_PATH, "SelfEditorPublishFauxDataPropertyPolicy",
                PUBLISH_GROUP, FAUX_DATA_PROPERTY, ROLE_SELF_EDITOR_URI, 2,
                Collections.singleton(4) },

            { ADMIN_UPDATE_FAUX_DATA_PROPERTY_POLICY_PATH, "AdminUpdateFauxDataPropertyPolicy",
                UPDATE_GROUP, FAUX_DATA_PROPERTY, ROLE_ADMIN_URI, 2,
                Collections.singleton(4) },
            { CURATOR_UPDATE_FAUX_DATA_PROPERTY_POLICY_PATH, "CuratorUpdateFauxDataPropertyPolicy",
                UPDATE_GROUP, FAUX_DATA_PROPERTY, ROLE_CURATOR_URI, 2,
                Collections.singleton(4) },
            { EDITOR_UPDATE_FAUX_DATA_PROPERTY_POLICY_PATH, "EditorUpdateFauxDataPropertyPolicy",
                UPDATE_GROUP, FAUX_DATA_PROPERTY, ROLE_EDITOR_URI, 2,
                Collections.singleton(4) },
            { SELF_EDITOR_UPDATE_FAUX_DATA_PROPERTY_POLICY_PATH, "SelfEditorUpdateFauxDataPropertyPolicy",
                UPDATE_GROUP, FAUX_DATA_PROPERTY, ROLE_SELF_EDITOR_URI, 2,
                new HashSet<>(Arrays.asList(4, 5)) },

        });
    }

}
