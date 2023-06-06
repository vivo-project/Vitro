package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.Lock;
import org.junit.Before;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.Attribute;
import edu.cornell.mannlib.vitro.webapp.auth.rules.AccessRule;

public class PolicyTest {
    public static final String USER_ACCOUNTS_HOME_EVERYTIME = "../home/src/main/resources/rdf/auth/everytime/";
    public static final String USER_ACCOUNTS_HOME_FIRSTTIME = "../home/src/main/resources/rdf/auth/firsttime/";

    protected static final String ROLE_ADMIN_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#ADMIN";
    protected static final String ROLE_EDITOR_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#EDITOR";
    protected static final String ROLE_SELF_EDITOR_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#SELF_EDITOR";
    protected static final String ROLE_CURATOR_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#CURATOR";
    protected static final String ROLE_PUBLIC_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#PUBLIC";

    public static final String ONTOLOGY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "ontology.n3";
    public static final String ATTRIBUTES_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "attributes.n3";
    public static final String OPERATIONS_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "operations.n3";
    public static final String OPERATION_GROUPS = USER_ACCOUNTS_HOME_EVERYTIME + "operation_groups.n3";
    public static final String SUBJECT_TYPES = USER_ACCOUNTS_HOME_EVERYTIME + "subject_types.n3";
    public static final String OBJECT_TYPES = USER_ACCOUNTS_HOME_EVERYTIME + "object_types.n3";
    public static final String ATTRIBUTE_TYPES_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "attribute_types.n3";
    public static final String TEST_TYPES_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "test_types.n3";
    public static final String TEST_VALUES_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "test_values.n3";
    public static final String TEST_DECISIONS = USER_ACCOUNTS_HOME_EVERYTIME + "decisions.n3";
    
    public static final String ADMIN_DISPLAY_OBJ_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_admin_display_object_property.n3";
    public static final String ADMIN_DISPLAY_DATA_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_admin_display_data_property.n3";
    public static final String ADMIN_DISPLAY_CLASS_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_admin_display_class.n3";

    public static final String CURATOR_DISPLAY_OBJ_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_curator_display_object_property.n3";
    public static final String CURATOR_DISPLAY_DATA_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_curator_display_data_property.n3";
    public static final String CURATOR_DISPLAY_CLASS_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_curator_display_class.n3";

    public static final String PUBLIC_DISPLAY_OBJ_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_public_display_object_property.n3";
    public static final String PUBLIC_DISPLAY_DATA_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_public_display_data_property.n3";
    public static final String PUBLIC_DISPLAY_CLASS_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_public_display_class.n3";
    
    public static final String SELF_EDITOR_DISPLAY_OBJ_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_self_editor_display_object_property.n3";
    public static final String SELF_EDITOR_DISPLAY_DATA_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_self_editor_display_data_property.n3";
    public static final String SELF_EDITOR_DISPLAY_CLASS_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_self_editor_display_class.n3";
    
    public static final String EDITOR_DISPLAY_OBJ_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_editor_display_object_property.n3";
    public static final String EDITOR_DISPLAY_DATA_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_editor_display_data_property.n3";
    public static final String EDITOR_DISPLAY_CLASS_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_editor_display_class.n3";
    //Update
    public static final String ADMIN_UPDATE_OBJ_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_admin_update_object_property.n3";
    public static final String ADMIN_UPDATE_DATA_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_admin_update_data_property.n3";
    public static final String ADMIN_UPDATE_CLASS_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_admin_update_class.n3";

    public static final String CURATOR_UPDATE_OBJ_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_curator_update_object_property.n3";
    public static final String CURATOR_UPDATE_DATA_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_curator_update_data_property.n3";
    public static final String CURATOR_UPDATE_CLASS_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_curator_update_class.n3";

    public static final String PUBLIC_UPDATE_OBJ_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_public_update_object_property.n3";
    public static final String PUBLIC_UPDATE_DATA_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_public_update_data_property.n3";
    public static final String PUBLIC_UPDATE_CLASS_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_public_update_class.n3";
    
    public static final String SELF_EDITOR_UPDATE_OBJ_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_self_editor_update_object_property.n3";
    public static final String SELF_EDITOR_UPDATE_DATA_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_self_editor_update_data_property.n3";
    public static final String SELF_EDITOR_UPDATE_CLASS_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_self_editor_update_class.n3";
    
    public static final String EDITOR_UPDATE_OBJ_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_editor_update_object_property.n3";
    public static final String EDITOR_UPDATE_DATA_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_editor_update_data_property.n3";
    public static final String EDITOR_UPDATE_CLASS_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_editor_update_class.n3"; 
    //Publish
    public static final String ADMIN_PUBLISH_OBJ_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_admin_publish_object_property.n3";
    public static final String ADMIN_PUBLISH_DATA_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_admin_publish_data_property.n3";
    public static final String ADMIN_PUBLISH_CLASS_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_admin_publish_class.n3";

    public static final String CURATOR_PUBLISH_OBJ_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_curator_publish_object_property.n3";
    public static final String CURATOR_PUBLISH_DATA_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_curator_publish_data_property.n3";
    public static final String CURATOR_PUBLISH_CLASS_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_curator_publish_class.n3";

    public static final String SELF_EDITOR_PUBLISH_OBJ_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_self_editor_publish_object_property.n3";
    public static final String SELF_EDITOR_PUBLISH_DATA_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_self_editor_publish_data_property.n3";
    public static final String SELF_EDITOR_PUBLISH_CLASS_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_self_editor_publish_class.n3";
    
    public static final String EDITOR_PUBLISH_OBJ_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_editor_publish_object_property.n3";
    public static final String EDITOR_PUBLISH_DATA_PROP_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_editor_publish_data_property.n3";
    public static final String EDITOR_PUBLISH_CLASS_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_editor_publish_class.n3";
    
    protected static final String TEST_RESOURCES_PREFIX = "src/test/resources/edu/cornell/mannlib/vitro/webapp/auth/rules/";
    protected static final List<String> ROLE_LIST = Arrays.asList(ROLE_ADMIN_URI, ROLE_CURATOR_URI, ROLE_EDITOR_URI, ROLE_SELF_EDITOR_URI, ROLE_PUBLIC_URI);
    public static final String PREFIX = "https://vivoweb.org/ontology/vitro-application/auth/individual/";
    public static final String DATASET = "_dataset";
    public static final String EXT = ".n3";
    
    private Model model;
    protected PolicyLoader loader;

    @Before
    public void init() {
        model = ModelFactory.createDefaultModel();
        load(ATTRIBUTES_PATH);
        load(OPERATIONS_PATH);
        load(OPERATION_GROUPS);
        load(SUBJECT_TYPES);
        load(OBJECT_TYPES);
        load(ATTRIBUTE_TYPES_PATH);
        load(TEST_TYPES_PATH);
        load(TEST_VALUES_PATH);
        load(TEST_DECISIONS);
        
        PolicyLoader.initialize(model);
        loader = PolicyLoader.getInstance();
    }
    
    protected void countRulesAndAttributes(DynamicPolicy policy, int ruleCount, int attCount) {
        assertTrue(policy != null);
        Set<AccessRule> rules = policy.getRules();
        Map<String, AccessRule> ruleMap = rules.stream().collect(Collectors.toMap( r -> r.getRuleUri(), r -> r));
        assertEquals(ruleCount, ruleMap.size());
        for (AccessRule ar : ruleMap.values()) {
            assertEquals(attCount, ar.getAttributes().size());
            for (Attribute att : ar.getAttributes().values()) {
                assertTrue(att.getValues().size() > 0); 
            }
        }
    }
    
    protected void load(String filePath) {
        try {
            model.enterCriticalSection(Lock.WRITE);
            model.read(filePath);
        } finally {
            model.leaveCriticalSection();
        }
    }
}
