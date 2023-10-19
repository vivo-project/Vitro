package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.CLASS;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.PUBLISH;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.UPDATE;
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
public class AccessAllowedClassesPolicyTemplateTest extends PolicyTest {
    private static final String TEMPLATE_CLASS_PREFIX =
            "https://vivoweb.org/ontology/vitro-application/auth/individual/template/access-allowed-class/";

    public static final String POLICY_TEMPLATE_MATCH_CLASS_PATH =
            USER_ACCOUNTS_HOME_FIRSTTIME + "template_access_allowed_class.n3";

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
                { "EditorDisplayClassUriDataSet", DISPLAY, CLASS, EDITOR, 1, Collections.singleton(4) },
                { "PublicDisplayClassUriDataSet", DISPLAY, CLASS, PUBLIC, 1, Collections.singleton(4) },
                { "EditorUpdateClassUriDataSet", UPDATE, CLASS, EDITOR, 1, Collections.singleton(4) },
                { "SelfEditorDisplayClassUriDataSet", DISPLAY, CLASS, SELF_EDITOR, 1, Collections.singleton(4) },
                { "CuratorDisplayClassUriDataSet", DISPLAY, CLASS, CURATOR, 1, Collections.singleton(4) },
                { "AdminDisplayClassUriDataSet", DISPLAY, CLASS, ADMIN, 1, Collections.singleton(4) },
                { "EditorPublishClassUriDataSet", PUBLISH, CLASS, EDITOR, 1, Collections.singleton(4) },
                { "SelfEditorPublishClassUriDataSet", PUBLISH, CLASS, SELF_EDITOR, 1, Collections.singleton(4) },
                { "CuratorPublishClassUriDataSet", PUBLISH, CLASS, CURATOR, 1, Collections.singleton(4) },
                { "AdminPublishClassUriDataSet", PUBLISH, CLASS, ADMIN, 1, Collections.singleton(4) },
                { "SelfEditorUpdateClassUriDataSet", UPDATE, CLASS, SELF_EDITOR, 1, Collections.singleton(4) },
                { "PublicUpdateClassUriDataSet", UPDATE, CLASS, PUBLIC, 1, Collections.singleton(4) },
                { "CuratorUpdateClassUriDataSet", UPDATE, CLASS, CURATOR, 1, Collections.singleton(4) },
                { "AdminUpdateClassUriDataSet", UPDATE, CLASS, ADMIN, 1, Collections.singleton(4) }, });
    }

}
