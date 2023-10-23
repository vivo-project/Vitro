package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
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
        assertEquals(PREFIX + "DataSets" , templates.get(PREFIX + "RoleDataSetTemplate1"));
        assertTrue(templates.containsKey(PREFIX + "RoleDataSetTemplate2"));
        assertEquals(PREFIX + "DataSets" , templates.get(PREFIX + "RoleDataSetTemplate2"));
    }

    @Test
    public void getRoleDataSetKeyTemplateTest() {
        load(DATA_SET);
        List<String> keys = PolicyLoader.getInstance().getDataSetKeysFromTemplate(PREFIX + "RoleDataSetTemplate1");
        assertEquals(2, keys.size());
        assertTrue(keys.contains("https://vivoweb.org/ontology/vitro-application/auth/individual/NamedObject"));
        assertTrue(keys.contains("https://vivoweb.org/ontology/vitro-application/auth/individual/ExecuteOperation"));
    }

    @Test
    public void getRoleDataSetDraftKeyTemplateTest() {
        load(DATA_SET);
        List<String> keys = PolicyLoader.getInstance().getDataSetKeyTemplatesFromTemplate(PREFIX + "RoleDataSetTemplate1");
        assertEquals(1, keys.size());
        assertTrue(keys.contains("https://vivoweb.org/ontology/vitro-application/auth/individual/SubjectRole"));
    }

    @Test
    public void getDataSetUriByKeyTest() {
        load(DATA_SET);
        String uri = PolicyLoader.getInstance().getDataSetUriByKey(
                new String[] { "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#PUBLIC" },
                new String[] { AccessObjectType.NAMED_OBJECT.toString() });
        assertEquals(PREFIX + "PublicDataSet", uri);
    }
    
    @Test
    public void getRoleDataSetValuesTemplateTest() {
        load(DATA_SET);
        List<String> values = PolicyLoader.getInstance().getDataSetValuesFromTemplate(PREFIX + "RoleDataSetTemplate1");
        assertEquals(1, values.size());
        assertEquals(values.get(0), "https://vivoweb.org/ontology/vitro-application/auth/individual/PublicRoleValueContainer");
    }
}
