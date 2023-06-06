package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup;

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
    public int attrCount;
    
    @Test
    public void testPolicy() {
        load(filePath);
        String policyUri =  PREFIX + uri;
        EntityPolicyController.updateEntityPolicy("test:entity", type, group, Arrays.asList(roleUri), ROLE_LIST);
        DynamicPolicy policy = loader.loadPolicy(policyUri);
        countRulesAndAttributes(policy, rulesCount, attrCount);
        Set<String> values = loader.getPolicyDataSetValues(group, type, roleUri);
        assertFalse(values.isEmpty());
    }
    
    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
            { EDITOR_DISPLAY_CLASS_POLICY_PATH, "EditorDisplayClassPolicy", OperationGroup.DISPLAY_GROUP, AccessObjectType.CLASS, ROLE_EDITOR_URI, 1, 4 },
            { EDITOR_DISPLAY_DATA_PROP_POLICY_PATH, "EditorDisplayDataPropertyPolicy", OperationGroup.DISPLAY_GROUP, AccessObjectType.DATA_PROPERTY, ROLE_EDITOR_URI, 2, 4 },
            { EDITOR_DISPLAY_OBJ_PROP_POLICY_PATH, "EditorDisplayObjectPropertyPolicy", OperationGroup.DISPLAY_GROUP, AccessObjectType.OBJECT_PROPERTY, ROLE_EDITOR_URI, 2, 4 },
            
            { SELF_EDITOR_DISPLAY_CLASS_POLICY_PATH, "SelfEditorDisplayClassPolicy", OperationGroup.DISPLAY_GROUP, AccessObjectType.CLASS, ROLE_SELF_EDITOR_URI, 1, 4 },
            { SELF_EDITOR_DISPLAY_DATA_PROP_POLICY_PATH, "SelfEditorDisplayDataPropertyPolicy", OperationGroup.DISPLAY_GROUP, AccessObjectType.DATA_PROPERTY, ROLE_SELF_EDITOR_URI, 2, 4 },
            { SELF_EDITOR_DISPLAY_OBJ_PROP_POLICY_PATH, "SelfEditorDisplayObjectPropertyPolicy", OperationGroup.DISPLAY_GROUP, AccessObjectType.OBJECT_PROPERTY, ROLE_SELF_EDITOR_URI, 2, 4 },

            { PUBLIC_DISPLAY_CLASS_POLICY_PATH, "PublicDisplayClassPolicy", OperationGroup.DISPLAY_GROUP, AccessObjectType.CLASS, ROLE_PUBLIC_URI, 1, 4 },
            { PUBLIC_DISPLAY_DATA_PROP_POLICY_PATH, "PublicDisplayDataPropertyPolicy", OperationGroup.DISPLAY_GROUP, AccessObjectType.DATA_PROPERTY, ROLE_PUBLIC_URI, 2, 4 },
            { PUBLIC_DISPLAY_OBJ_PROP_POLICY_PATH, "PublicDisplayObjectPropertyPolicy", OperationGroup.DISPLAY_GROUP, AccessObjectType.OBJECT_PROPERTY, ROLE_PUBLIC_URI, 2, 4 },
            
            { CURATOR_DISPLAY_CLASS_POLICY_PATH, "CuratorDisplayClassPolicy", OperationGroup.DISPLAY_GROUP, AccessObjectType.CLASS, ROLE_CURATOR_URI, 1, 4 },
            { CURATOR_DISPLAY_DATA_PROP_POLICY_PATH, "CuratorDisplayDataPropertyPolicy", OperationGroup.DISPLAY_GROUP, AccessObjectType.DATA_PROPERTY, ROLE_CURATOR_URI, 2, 4 },
            { CURATOR_DISPLAY_OBJ_PROP_POLICY_PATH, "CuratorDisplayObjectPropertyPolicy", OperationGroup.DISPLAY_GROUP, AccessObjectType.OBJECT_PROPERTY, ROLE_CURATOR_URI, 2, 4 },
            
            { ADMIN_DISPLAY_CLASS_POLICY_PATH, "AdminDisplayClassPolicy", OperationGroup.DISPLAY_GROUP, AccessObjectType.CLASS, ROLE_ADMIN_URI, 1, 4 },
            { ADMIN_DISPLAY_DATA_PROP_POLICY_PATH, "AdminDisplayDataPropertyPolicy", OperationGroup.DISPLAY_GROUP, AccessObjectType.DATA_PROPERTY, ROLE_ADMIN_URI, 1, 4 },
            { ADMIN_DISPLAY_OBJ_PROP_POLICY_PATH, "AdminDisplayObjectPropertyPolicy", OperationGroup.DISPLAY_GROUP, AccessObjectType.OBJECT_PROPERTY, ROLE_ADMIN_URI, 2, 4 },
            
            { EDITOR_PUBLISH_CLASS_POLICY_PATH, "EditorPublishClassPolicy", OperationGroup.PUBLISH_GROUP, AccessObjectType.CLASS, ROLE_EDITOR_URI, 1, 4 },
            { EDITOR_PUBLISH_DATA_PROP_POLICY_PATH, "EditorPublishDataPropertyPolicy", OperationGroup.PUBLISH_GROUP, AccessObjectType.DATA_PROPERTY, ROLE_EDITOR_URI, 2, 4 },
            { EDITOR_PUBLISH_OBJ_PROP_POLICY_PATH, "EditorPublishObjectPropertyPolicy", OperationGroup.PUBLISH_GROUP, AccessObjectType.OBJECT_PROPERTY, ROLE_EDITOR_URI, 2, 4 },
            
            { SELF_EDITOR_PUBLISH_CLASS_POLICY_PATH, "SelfEditorPublishClassPolicy", OperationGroup.PUBLISH_GROUP, AccessObjectType.CLASS, ROLE_SELF_EDITOR_URI, 1, 4 },
            { SELF_EDITOR_PUBLISH_DATA_PROP_POLICY_PATH, "SelfEditorPublishDataPropertyPolicy", OperationGroup.PUBLISH_GROUP, AccessObjectType.DATA_PROPERTY, ROLE_SELF_EDITOR_URI, 2, 4 },
            { SELF_EDITOR_PUBLISH_OBJ_PROP_POLICY_PATH, "SelfEditorPublishObjectPropertyPolicy", OperationGroup.PUBLISH_GROUP, AccessObjectType.OBJECT_PROPERTY, ROLE_SELF_EDITOR_URI, 2, 4 },

            { CURATOR_PUBLISH_CLASS_POLICY_PATH, "CuratorPublishClassPolicy", OperationGroup.PUBLISH_GROUP, AccessObjectType.CLASS, ROLE_CURATOR_URI, 1, 4 },
            { CURATOR_PUBLISH_DATA_PROP_POLICY_PATH, "CuratorPublishDataPropertyPolicy", OperationGroup.PUBLISH_GROUP, AccessObjectType.DATA_PROPERTY, ROLE_CURATOR_URI, 2, 4 },
            { CURATOR_PUBLISH_OBJ_PROP_POLICY_PATH, "CuratorPublishObjectPropertyPolicy", OperationGroup.PUBLISH_GROUP, AccessObjectType.OBJECT_PROPERTY, ROLE_CURATOR_URI, 2, 4 },

            { ADMIN_PUBLISH_CLASS_POLICY_PATH, "AdminPublishClassPolicy", OperationGroup.PUBLISH_GROUP, AccessObjectType.CLASS, ROLE_ADMIN_URI, 1, 4 },
            { ADMIN_PUBLISH_DATA_PROP_POLICY_PATH, "AdminPublishDataPropertyPolicy", OperationGroup.PUBLISH_GROUP, AccessObjectType.DATA_PROPERTY, ROLE_ADMIN_URI, 2, 4 },
            { ADMIN_PUBLISH_OBJ_PROP_POLICY_PATH, "AdminPublishObjectPropertyPolicy", OperationGroup.PUBLISH_GROUP, AccessObjectType.OBJECT_PROPERTY, ROLE_ADMIN_URI, 2, 4 },
            
            { EDITOR_UPDATE_CLASS_POLICY_PATH, "EditorUpdateClassPolicy", OperationGroup.UPDATE_GROUP, AccessObjectType.CLASS, ROLE_EDITOR_URI, 1, 4 },
            { EDITOR_UPDATE_DATA_PROP_POLICY_PATH, "EditorUpdateDataPropertyPolicy", OperationGroup.UPDATE_GROUP, AccessObjectType.DATA_PROPERTY, ROLE_EDITOR_URI, 2, 4 },
            { EDITOR_UPDATE_OBJ_PROP_POLICY_PATH, "EditorUpdateObjectPropertyPolicy", OperationGroup.UPDATE_GROUP, AccessObjectType.OBJECT_PROPERTY, ROLE_EDITOR_URI, 2, 4 },
            
            { SELF_EDITOR_UPDATE_CLASS_POLICY_PATH, "SelfEditorUpdateClassPolicy", OperationGroup.UPDATE_GROUP, AccessObjectType.CLASS, ROLE_SELF_EDITOR_URI, 1, 4 },
            { SELF_EDITOR_UPDATE_DATA_PROP_POLICY_PATH, "SelfEditorUpdateDataPropertyPolicy", OperationGroup.UPDATE_GROUP, AccessObjectType.DATA_PROPERTY, ROLE_SELF_EDITOR_URI, 2, 4 },
            { SELF_EDITOR_UPDATE_OBJ_PROP_POLICY_PATH, "SelfEditorUpdateObjectPropertyPolicy", OperationGroup.UPDATE_GROUP, AccessObjectType.OBJECT_PROPERTY, ROLE_SELF_EDITOR_URI, 2, 4 },

            { PUBLIC_UPDATE_CLASS_POLICY_PATH, "PublicUpdateClassPolicy", OperationGroup.UPDATE_GROUP, AccessObjectType.CLASS, ROLE_PUBLIC_URI, 1, 4 },
            { PUBLIC_UPDATE_DATA_PROP_POLICY_PATH, "PublicUpdateDataPropertyPolicy", OperationGroup.UPDATE_GROUP, AccessObjectType.DATA_PROPERTY, ROLE_PUBLIC_URI, 2, 4 },
            { PUBLIC_UPDATE_OBJ_PROP_POLICY_PATH, "PublicUpdateObjectPropertyPolicy", OperationGroup.UPDATE_GROUP, AccessObjectType.OBJECT_PROPERTY, ROLE_PUBLIC_URI, 2, 4 },
            
            { CURATOR_UPDATE_CLASS_POLICY_PATH, "CuratorUpdateClassPolicy", OperationGroup.UPDATE_GROUP, AccessObjectType.CLASS, ROLE_CURATOR_URI, 1, 4 },
            { CURATOR_UPDATE_DATA_PROP_POLICY_PATH, "CuratorUpdateDataPropertyPolicy", OperationGroup.UPDATE_GROUP, AccessObjectType.DATA_PROPERTY, ROLE_CURATOR_URI, 2, 4 },
            { CURATOR_UPDATE_OBJ_PROP_POLICY_PATH, "CuratorUpdateObjectPropertyPolicy", OperationGroup.UPDATE_GROUP, AccessObjectType.OBJECT_PROPERTY, ROLE_CURATOR_URI, 2, 4 },
            
            { ADMIN_UPDATE_CLASS_POLICY_PATH, "AdminUpdateClassPolicy", OperationGroup.UPDATE_GROUP, AccessObjectType.CLASS, ROLE_ADMIN_URI, 1, 4 },
            { ADMIN_UPDATE_DATA_PROP_POLICY_PATH, "AdminUpdateDataPropertyPolicy", OperationGroup.UPDATE_GROUP, AccessObjectType.DATA_PROPERTY, ROLE_ADMIN_URI, 2, 4 },
            { ADMIN_UPDATE_OBJ_PROP_POLICY_PATH, "AdminUpdateObjectPropertyPolicy", OperationGroup.UPDATE_GROUP, AccessObjectType.OBJECT_PROPERTY, ROLE_ADMIN_URI, 2, 4 },
            
        });
    }
    
}
