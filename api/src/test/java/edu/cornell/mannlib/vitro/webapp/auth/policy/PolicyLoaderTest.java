package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.NAMED_OBJECT;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.EXECUTE;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.AUTH_INDIVIDUAL_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueKey;
import org.junit.Test;

public class PolicyLoaderTest extends PolicyTest {

    private static final String PREFIX =
            "https://vivoweb.org/ontology/vitro-application/auth/individual/template/test-data-set-templates/";
    public static final String DATA_SET = RESOURCES_RULES_PREFIX + "data_set_templates.n3";

    @Test
    public void getRoleDataSetTemplatesTest() {
        load(DATA_SET);
        Map<String, String> templates = PolicyLoader.getInstance().getRoleDataSetTemplates();
        assertEquals(2, templates.size());
        assertTrue(templates.containsKey(PREFIX + "RoleDataSetTemplate1"));
        assertEquals(PREFIX + "PolicyTemplate", templates.get(PREFIX + "RoleDataSetTemplate1"));
        assertTrue(templates.containsKey(PREFIX + "RoleDataSetTemplate2"));
        assertEquals(PREFIX + "PolicyTemplate", templates.get(PREFIX + "RoleDataSetTemplate2"));
    }

    @Test
    public void getRoleDataSetKeyTemplateTest() {
        load(DATA_SET);
        List<String> keys = PolicyLoader.getInstance().getDataSetKeysFromTemplate(PREFIX + "RoleDataSetTemplate1");
        assertEquals(2, keys.size());
        assertTrue(keys.contains(AUTH_INDIVIDUAL_PREFIX + "NamedObject"));
        assertTrue(keys.contains(AUTH_INDIVIDUAL_PREFIX + "ExecuteOperation"));
    }

    @Test
    public void getRoleDataSetDraftKeyTemplateTest() {
        load(DATA_SET);
        List<String> keys =
                PolicyLoader.getInstance().getDataSetKeyTemplatesFromTemplate(PREFIX + "RoleDataSetTemplate1");
        assertEquals(1, keys.size());
        assertTrue(keys.contains(AUTH_INDIVIDUAL_PREFIX + "SubjectRole"));
    }

    @Test
    public void getDataSetUriByKeyTest() {
        load(DATA_SET);
        String uri = PolicyLoader.getInstance().getDataSetUriByKey(new String[] { PUBLIC },
                new String[] { NAMED_OBJECT.toString(), EXECUTE.toString() });
        assertEquals(PREFIX + "PublicDataSet", uri);
    }

    @Test
    public void getRoleDataSetValuesTemplateTest() {
        load(DATA_SET);
        List<String> values = PolicyLoader.getInstance().getDataSetValuesFromTemplate(PREFIX + "RoleDataSetTemplate1");
        assertEquals(1, values.size());
        assertEquals(values.get(0), AUTH_INDIVIDUAL_PREFIX + "PublicRoleValueContainer");
    }

    @Test
    public void getDataSetKeyTest() {
        load(DATA_SET);
        AttributeValueKey expectedKey = new AttributeValueKey();
        expectedKey.setOperation(EXECUTE);
        expectedKey.setRole(PUBLIC);
        expectedKey.setObjectType(NAMED_OBJECT);
        AttributeValueKey compositeKey = PolicyLoader.getInstance().getDataSetKey(PREFIX + "PublicDataSet");
        assertEquals(expectedKey, compositeKey);

    }
}
