package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.CLASS;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup.DISPLAY_GROUP;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup.PUBLISH_GROUP;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup.UPDATE_GROUP;
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
public class MatchAllowedClassesPolicyTemplateTest extends PolicyTest {
    private static final String TEMPLATE_CLASS_PREFIX =
            "https://vivoweb.org/ontology/vitro-application/auth/individual/template/simple-match-allowed-class/";

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
        load(POLICY_TEMPLATE_MATCH_CLASS_PATH);
        EntityPolicyController.updateEntityPolicyDataSet("test:entity", type, group, Arrays.asList(roleUri), ROLE_LIST);
        DynamicPolicy policy = null;
        policy = loader.loadPolicyFromTemplateDataSet(TEMPLATE_CLASS_PREFIX + dataSetUri);
        countRulesAndAttributes(policy, rulesCount, attrCount);
        Set<String> values = loader.getDataSetValues(group, type, roleUri);
        assertFalse(values.isEmpty());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
                { "EditorDisplayClassUriDataSet", DISPLAY_GROUP, CLASS, ROLE_EDITOR_URI, 1, Collections.singleton(4) },
                { "PublicDisplayClassUriDataSet", DISPLAY_GROUP, CLASS, ROLE_PUBLIC_URI, 1, Collections.singleton(4) },
                { "EditorUpdateClassUriDataSet", UPDATE_GROUP, CLASS, ROLE_EDITOR_URI, 1, Collections.singleton(4) },
                { "SelfEditorDisplayClassUriDataSet", DISPLAY_GROUP, CLASS, ROLE_SELF_EDITOR_URI, 1,
                        Collections.singleton(4) },
                { "CuratorDisplayClassUriDataSet", DISPLAY_GROUP, CLASS, ROLE_CURATOR_URI, 1,
                        Collections.singleton(4) },
                { "AdminDisplayClassUriDataSet", DISPLAY_GROUP, CLASS, ROLE_ADMIN_URI, 1, Collections.singleton(4) },
                { "EditorPublishClassUriDataSet", PUBLISH_GROUP, CLASS, ROLE_EDITOR_URI, 1, Collections.singleton(4) },
                { "SelfEditorPublishClassUriDataSet", PUBLISH_GROUP, CLASS, ROLE_SELF_EDITOR_URI, 1,
                        Collections.singleton(4) },
                { "CuratorPublishClassUriDataSet", PUBLISH_GROUP, CLASS, ROLE_CURATOR_URI, 1,
                        Collections.singleton(4) },
                { "AdminPublishClassUriDataSet", PUBLISH_GROUP, CLASS, ROLE_ADMIN_URI, 1, Collections.singleton(4) },
                { "SelfEditorUpdateClassUriDataSet", UPDATE_GROUP, CLASS, ROLE_SELF_EDITOR_URI, 1,
                        Collections.singleton(4) },
                { "PublicUpdateClassUriDataSet", UPDATE_GROUP, CLASS, ROLE_PUBLIC_URI, 1, Collections.singleton(4) },
                { "CuratorUpdateClassUriDataSet", UPDATE_GROUP, CLASS, ROLE_CURATOR_URI, 1, Collections.singleton(4) },
                { "AdminUpdateClassUriDataSet", UPDATE_GROUP, CLASS, ROLE_ADMIN_URI, 1, Collections.singleton(4) }, });
    }

}
