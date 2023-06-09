package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.Lock;
import org.junit.Before;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.Attribute;
import edu.cornell.mannlib.vitro.webapp.auth.rules.AccessRule;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;

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
    
    public static final String ADMIN_DISPLAY_FAUX_OBJECT_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_admin_display_faux_object_property.n3";
    public static final String CURATOR_DISPLAY_FAUX_OBJECT_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_curator_display_faux_object_property.n3";
    public static final String EDITOR_DISPLAY_FAUX_OBJECT_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_editor_display_faux_object_property.n3";
    public static final String SELF_EDITOR_DISPLAY_FAUX_OBJECT_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_self_editor_display_faux_object_property.n3";
    public static final String PUBLIC_DISPLAY_FAUX_OBJECT_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_public_display_faux_object_property.n3";

    public static final String ADMIN_PUBLISH_FAUX_OBJECT_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_admin_publish_faux_object_property.n3";
    public static final String CURATOR_PUBLISH_FAUX_OBJECT_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_curator_publish_faux_object_property.n3";
    public static final String EDITOR_PUBLISH_FAUX_OBJECT_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_editor_publish_faux_object_property.n3";
    public static final String SELF_EDITOR_PUBLISH_FAUX_OBJECT_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_self_editor_publish_faux_object_property.n3";
   
    public static final String ADMIN_UPDATE_FAUX_OBJECT_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_admin_update_faux_object_property.n3";
    public static final String CURATOR_UPDATE_FAUX_OBJECT_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_curator_update_faux_object_property.n3";
    public static final String EDITOR_UPDATE_FAUX_OBJECT_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_editor_update_faux_object_property.n3";
    public static final String SELF_EDITOR_UPDATE_FAUX_OBJECT_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_self_editor_update_faux_object_property.n3";
    
    public static final String ADMIN_DISPLAY_FAUX_DATA_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_admin_display_faux_data_property.n3";
    public static final String CURATOR_DISPLAY_FAUX_DATA_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_curator_display_faux_data_property.n3";
    public static final String EDITOR_DISPLAY_FAUX_DATA_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_editor_display_faux_data_property.n3";
    public static final String SELF_EDITOR_DISPLAY_FAUX_DATA_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_self_editor_display_faux_data_property.n3";
    public static final String PUBLIC_DISPLAY_FAUX_DATA_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_public_display_faux_data_property.n3";

    public static final String ADMIN_PUBLISH_FAUX_DATA_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_admin_publish_faux_data_property.n3";
    public static final String CURATOR_PUBLISH_FAUX_DATA_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_curator_publish_faux_data_property.n3";
    public static final String EDITOR_PUBLISH_FAUX_DATA_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_editor_publish_faux_data_property.n3";
    public static final String SELF_EDITOR_PUBLISH_FAUX_DATA_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_self_editor_publish_faux_data_property.n3";
   
    public static final String ADMIN_UPDATE_FAUX_DATA_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_admin_update_faux_data_property.n3";
    public static final String CURATOR_UPDATE_FAUX_DATA_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_curator_update_faux_data_property.n3";
    public static final String EDITOR_UPDATE_FAUX_DATA_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_editor_update_faux_data_property.n3";
    public static final String SELF_EDITOR_UPDATE_FAUX_DATA_PROPERTY_POLICY_PATH = USER_ACCOUNTS_HOME_EVERYTIME + "policy_self_editor_update_faux_data_property.n3";
    
    protected static final String TEST_RESOURCES_PREFIX = "src/test/resources/edu/cornell/mannlib/vitro/webapp/auth/rules/";

    public static final String VALID_POLICY = TEST_RESOURCES_PREFIX + "test_policy_valid.n3";
    public static final String VALID_POLICY_WITH_SET = TEST_RESOURCES_PREFIX + "test_policy_valid_set.n3";
    public static final String BROKEN_POLICY_WITH_SET = TEST_RESOURCES_PREFIX + "test_policy_broken_set.n3";
    public static final String POLICY_KEY_TEST = TEST_RESOURCES_PREFIX + "test_policy_key.n3";
    
    protected static final List<String> ROLE_LIST = Arrays.asList(ROLE_ADMIN_URI, ROLE_CURATOR_URI, ROLE_EDITOR_URI, ROLE_SELF_EDITOR_URI, ROLE_PUBLIC_URI);
    public static final String PREFIX = "https://vivoweb.org/ontology/vitro-application/auth/individual/";
    public static final String DATASET = "_dataset";
    public static final String EXT = ".n3";
    
    private static final Log log = LogFactory.getLog(PolicyTest.class);

    protected Model userAccountsModel;
    protected PolicyLoader loader;
    protected Dataset ds;

    @Before
    public void init() {
        userAccountsModel = ModelFactory.createDefaultModel();
        ds = DatasetFactory.createTxnMem();
        ds.addNamedModel(ModelNames.USER_ACCOUNTS, userAccountsModel);
        load(ATTRIBUTES_PATH);
        load(OPERATIONS_PATH);
        load(OPERATION_GROUPS);
        load(SUBJECT_TYPES);
        load(OBJECT_TYPES);
        load(ATTRIBUTE_TYPES_PATH);
        load(TEST_TYPES_PATH);
        load(TEST_VALUES_PATH);
        load(TEST_DECISIONS);
        RDFServiceModel rdfService = new RDFServiceModel(ds);
        
        PolicyLoader.initialize(rdfService);
        loader = PolicyLoader.getInstance();
    }
    
    protected void countRulesAndAttributes(DynamicPolicy policy, int ruleCount, Set<Integer> attrCount) {
        assertTrue(policy != null);
        Set<AccessRule> rules = policy.getRules();
        Map<String, AccessRule> ruleMap = rules.stream().collect(Collectors.toMap( r -> r.getRuleUri(), r -> r));
        if(ruleCount != ruleMap.size()) {
            log.error(String.format("Rules count %s doesn't match for policy %s", ruleMap.size(), policy.getUri()));
            for (AccessRule ar : ruleMap.values()) {
                log.error(String.format("Rule uri %s", ar.getRuleUri()));
            }
        }
        assertEquals(ruleCount, ruleMap.size());
        for (AccessRule ar : ruleMap.values()) {
            if(!attrCount.contains(ar.getAttributes().size())) {
                log.error(String.format("Attribute count %s doesn't match for policy %s", ar.getAttributes().size(), policy.getUri()));
                for (String att : ar.getAttributeUris()) {
                    log.error(String.format("Attribute uri %s", att));
                }
            }
            assertTrue(attrCount.contains(ar.getAttributes().size()));
            for (Attribute att : ar.getAttributes().values()) {
                assertTrue(att.getValues().size() > 0); 
            }
        }
    }
    
    protected void loadAllEntityPolicies() {
        load(ADMIN_DISPLAY_OBJ_PROP_POLICY_PATH);
        load(ADMIN_DISPLAY_DATA_PROP_POLICY_PATH);
        load(ADMIN_DISPLAY_CLASS_POLICY_PATH);
        
        load(CURATOR_DISPLAY_OBJ_PROP_POLICY_PATH);
        load(CURATOR_DISPLAY_DATA_PROP_POLICY_PATH);
        load(CURATOR_DISPLAY_CLASS_POLICY_PATH);
        
        load(PUBLIC_DISPLAY_OBJ_PROP_POLICY_PATH);
        load(PUBLIC_DISPLAY_DATA_PROP_POLICY_PATH);
        load(PUBLIC_DISPLAY_CLASS_POLICY_PATH);
        
        load(SELF_EDITOR_DISPLAY_OBJ_PROP_POLICY_PATH);
        load(SELF_EDITOR_DISPLAY_DATA_PROP_POLICY_PATH);
        load(SELF_EDITOR_DISPLAY_CLASS_POLICY_PATH);
        
        load(EDITOR_DISPLAY_OBJ_PROP_POLICY_PATH);
        load(EDITOR_DISPLAY_DATA_PROP_POLICY_PATH);
        load(EDITOR_DISPLAY_CLASS_POLICY_PATH);
        
        load(ADMIN_UPDATE_OBJ_PROP_POLICY_PATH);
        load(ADMIN_UPDATE_DATA_PROP_POLICY_PATH);
        load(ADMIN_UPDATE_CLASS_POLICY_PATH);
        
        load(CURATOR_UPDATE_OBJ_PROP_POLICY_PATH);
        load(CURATOR_UPDATE_DATA_PROP_POLICY_PATH);
        load(CURATOR_UPDATE_CLASS_POLICY_PATH);
        
        load(PUBLIC_UPDATE_OBJ_PROP_POLICY_PATH);
        load(PUBLIC_UPDATE_DATA_PROP_POLICY_PATH);
        load(PUBLIC_UPDATE_CLASS_POLICY_PATH);
        
        load(SELF_EDITOR_UPDATE_OBJ_PROP_POLICY_PATH);
        load(SELF_EDITOR_UPDATE_DATA_PROP_POLICY_PATH);
        load(SELF_EDITOR_UPDATE_CLASS_POLICY_PATH);

        load(EDITOR_UPDATE_OBJ_PROP_POLICY_PATH);
        load(EDITOR_UPDATE_DATA_PROP_POLICY_PATH);
        load(EDITOR_UPDATE_CLASS_POLICY_PATH);
        
        load(ADMIN_PUBLISH_OBJ_PROP_POLICY_PATH);
        load(ADMIN_PUBLISH_DATA_PROP_POLICY_PATH);
        load(ADMIN_PUBLISH_CLASS_POLICY_PATH);
        
        load(CURATOR_PUBLISH_OBJ_PROP_POLICY_PATH);
        load(CURATOR_PUBLISH_DATA_PROP_POLICY_PATH);
        load(CURATOR_PUBLISH_CLASS_POLICY_PATH);
        
        load(SELF_EDITOR_PUBLISH_OBJ_PROP_POLICY_PATH);
        load(SELF_EDITOR_PUBLISH_DATA_PROP_POLICY_PATH);
        load(SELF_EDITOR_PUBLISH_CLASS_POLICY_PATH);
        
        load(EDITOR_PUBLISH_OBJ_PROP_POLICY_PATH);
        load(EDITOR_PUBLISH_DATA_PROP_POLICY_PATH);
        load(EDITOR_PUBLISH_CLASS_POLICY_PATH);
        
        load(ADMIN_DISPLAY_FAUX_OBJECT_PROPERTY_POLICY_PATH);
        load(CURATOR_DISPLAY_FAUX_OBJECT_PROPERTY_POLICY_PATH);
        load(EDITOR_DISPLAY_FAUX_OBJECT_PROPERTY_POLICY_PATH);
        load(SELF_EDITOR_DISPLAY_FAUX_OBJECT_PROPERTY_POLICY_PATH);
        load(PUBLIC_DISPLAY_FAUX_OBJECT_PROPERTY_POLICY_PATH);
        
        load(ADMIN_PUBLISH_FAUX_OBJECT_PROPERTY_POLICY_PATH);
        load(CURATOR_PUBLISH_FAUX_OBJECT_PROPERTY_POLICY_PATH);
        load(EDITOR_PUBLISH_FAUX_OBJECT_PROPERTY_POLICY_PATH);
        load(SELF_EDITOR_PUBLISH_FAUX_OBJECT_PROPERTY_POLICY_PATH);
        
        load(ADMIN_UPDATE_FAUX_OBJECT_PROPERTY_POLICY_PATH);
        load(CURATOR_UPDATE_FAUX_OBJECT_PROPERTY_POLICY_PATH);
        load(EDITOR_UPDATE_FAUX_OBJECT_PROPERTY_POLICY_PATH);
        load(SELF_EDITOR_UPDATE_FAUX_OBJECT_PROPERTY_POLICY_PATH);
        
        load(ADMIN_DISPLAY_FAUX_DATA_PROPERTY_POLICY_PATH);
        load(CURATOR_DISPLAY_FAUX_DATA_PROPERTY_POLICY_PATH);
        load(EDITOR_DISPLAY_FAUX_DATA_PROPERTY_POLICY_PATH);
        load(SELF_EDITOR_DISPLAY_FAUX_DATA_PROPERTY_POLICY_PATH);
        load(PUBLIC_DISPLAY_FAUX_DATA_PROPERTY_POLICY_PATH);
        
        load(ADMIN_PUBLISH_FAUX_DATA_PROPERTY_POLICY_PATH);
        load(CURATOR_PUBLISH_FAUX_DATA_PROPERTY_POLICY_PATH);
        load(EDITOR_PUBLISH_FAUX_DATA_PROPERTY_POLICY_PATH);
        load(SELF_EDITOR_PUBLISH_FAUX_DATA_PROPERTY_POLICY_PATH);
        
        load(ADMIN_UPDATE_FAUX_DATA_PROPERTY_POLICY_PATH);
        load(CURATOR_UPDATE_FAUX_DATA_PROPERTY_POLICY_PATH);
        load(EDITOR_UPDATE_FAUX_DATA_PROPERTY_POLICY_PATH);
        load(SELF_EDITOR_UPDATE_FAUX_DATA_PROPERTY_POLICY_PATH);
    }
    
    protected void load(String filePath) {
        try {
            userAccountsModel.enterCriticalSection(Lock.WRITE);
            userAccountsModel.read(filePath);
        } finally {
            userAccountsModel.leaveCriticalSection();
        }
        ds.replaceNamedModel("http://vitro.mannlib.cornell.edu/default/vitro-kb-userAccounts", userAccountsModel);

    }
    
    protected void load(Model m, String filePath) {
        try {
            m.enterCriticalSection(Lock.WRITE);
            m.read(filePath);
        } finally {
            m.leaveCriticalSection();
        }
    }
}
