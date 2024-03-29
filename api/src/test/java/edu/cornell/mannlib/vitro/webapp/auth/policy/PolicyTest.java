package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueSetRegistry;
import edu.cornell.mannlib.vitro.webapp.auth.checks.Check;
import edu.cornell.mannlib.vitro.webapp.auth.rules.AccessRule;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.Lock;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;

public class PolicyTest {
    public static final String USER_ACCOUNTS_HOME_FIRSTTIME = "../home/src/main/resources/rdf/accessControl/firsttime/";

    public static final String ADMIN = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#ADMIN";
    public static final String EDITOR = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#EDITOR";
    public static final String SELF_EDITOR = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#SELF_EDITOR";
    public static final String CURATOR = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#CURATOR";
    public static final String PUBLIC = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#PUBLIC";
    protected static final String CUSTOM = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#CUSTOM";

    public static final String ONTOLOGY_PATH = USER_ACCOUNTS_HOME_FIRSTTIME + "ontology.n3";
    public static final String OPERATIONS_PATH = USER_ACCOUNTS_HOME_FIRSTTIME + "operations.n3";
    public static final String SUBJECT_TYPES = USER_ACCOUNTS_HOME_FIRSTTIME + "subject_types.n3";
    public static final String OBJECT_TYPES = USER_ACCOUNTS_HOME_FIRSTTIME + "object_types.n3";
    public static final String ATTRIBUTES_PATH = USER_ACCOUNTS_HOME_FIRSTTIME + "attributes.n3";
    public static final String OPERATORS_PATH = USER_ACCOUNTS_HOME_FIRSTTIME + "operators.n3";
    public static final String NAMED_KEY_COMPONENTS_PATH = USER_ACCOUNTS_HOME_FIRSTTIME + "named_key_components.n3";
    public static final String PROFILE_PROXIMITY_QUERY = USER_ACCOUNTS_HOME_FIRSTTIME + "profile_proximity_query.n3";
    public static final String TEST_DECISIONS = USER_ACCOUNTS_HOME_FIRSTTIME + "decisions.n3";
    public static final String ROLES = USER_ACCOUNTS_HOME_FIRSTTIME + "roles.n3";
    protected static final String RESOURCES_PREFIX = "src/test/resources/edu/cornell/mannlib/vitro/webapp/auth/";
    protected static final String RESOURCES_RULES_PREFIX = RESOURCES_PREFIX + "rules/";

    public static final String VALID_POLICY = RESOURCES_RULES_PREFIX + "test_policy_valid.n3";
    public static final String VALID_POLICY_TEMPLATE = RESOURCES_RULES_PREFIX + "test_policy_valid_set.n3";
    public static final String BROKEN_POLICY_TEMPLATE = RESOURCES_RULES_PREFIX + "test_policy_broken_set.n3";
    // TODO:Implement public static final String POLICY_KEY_TEST = RESOURCES_RULES_PREFIX + "test_policy_key.n3";

    public static final String TEMPLATE_CLASS_PATH = USER_ACCOUNTS_HOME_FIRSTTIME + "template_access_allowed_class.n3";

    public static final String TEMPLATE_PROPERTIES_PATH =
            USER_ACCOUNTS_HOME_FIRSTTIME + "template_access_allowed_property.n3";

    public static final String TEMPLATE_RELATED_PROPERTIES_PATH =
            USER_ACCOUNTS_HOME_FIRSTTIME + "template_access_related_allowed_property.n3";

    public static final String TEMPLATE_RELATED_UPDATE_PATH =
            USER_ACCOUNTS_HOME_FIRSTTIME + "template_update_related_allowed_property.n3";

    protected static final List<String> ROLE_LIST = Arrays.asList(ADMIN, CURATOR, EDITOR, SELF_EDITOR, PUBLIC);
    public static final String PREFIX = "https://vivoweb.org/ontology/vitro-application/auth/individual/";
    public static final String DATASET = "_dataset";
    public static final String EXT = ".n3";

    private static final Log log = LogFactory.getLog(PolicyTest.class);

    protected static final List<String> entityPolicies = Arrays.asList(TEMPLATE_CLASS_PATH, TEMPLATE_PROPERTIES_PATH,
            TEMPLATE_RELATED_PROPERTIES_PATH, TEMPLATE_RELATED_UPDATE_PATH);

    protected Model accessControlModel;
    protected PolicyLoader loader;
    protected Dataset configurationDataSet;

    @Before
    public void init() {
        accessControlModel = ModelFactory.createDefaultModel();
        configurationDataSet = DatasetFactory.createTxnMem();
        configurationDataSet.addNamedModel(ModelNames.ACCESS_CONTROL, accessControlModel);
        load(OPERATIONS_PATH);
        load(ROLES);
        load(SUBJECT_TYPES);
        load(OBJECT_TYPES);
        load(ATTRIBUTES_PATH);
        load(OPERATORS_PATH);
        load(PROFILE_PROXIMITY_QUERY);
        load(TEST_DECISIONS);
        load(NAMED_KEY_COMPONENTS_PATH);
        RDFServiceModel rdfService = new RDFServiceModel(configurationDataSet);
        AttributeValueSetRegistry.getInstance().clear();
        PolicyLoader.initialize(rdfService);
        loader = PolicyLoader.getInstance();
        Logger logger = LogManager.getLogger(PolicyLoader.class);
        logger.setLevel(Level.ERROR);
    }

    @After
    public void finish() {
        AttributeValueSetRegistry.getInstance().clear();
    }

    protected void countRulesAndAttributes(DynamicPolicy policy, int ruleCount, Set<Integer> checksCount) {
        assertTrue(policy != null);
        Set<AccessRule> rules = policy.getRules();
        Map<String, AccessRule> ruleMap = rules.stream().collect(Collectors.toMap(r -> r.getRuleUri(), r -> r));
        if (ruleCount != ruleMap.size()) {
            log.error(String.format("Rules count %s doesn't match for policy %s", ruleMap.size(), policy.getUri()));
            for (AccessRule ar : ruleMap.values()) {
                log.error(String.format("Rule uri %s", ar.getRuleUri()));
            }
        }
        assertEquals(ruleCount, ruleMap.size());
        for (AccessRule ar : ruleMap.values()) {
            if (!checksCount.contains(ar.getChecks().size())) {
                log.error(String.format("Checks count %s doesn't match for policy %s", ar.getChecks().size(),
                        policy.getUri()));
                for (String checkUri : ar.getCheckUris()) {
                    log.error(String.format("Checks uri %s", checkUri));
                }
            }
            assertTrue(checksCount.contains(ar.getChecks().size()));
            for (Check att : ar.getChecks()) {
                assertTrue(!att.getValues().isEmpty());
            }
        }
    }

    protected void loadAllEntityPolicies() {
        for (String policyPath : entityPolicies) {
            load(policyPath);
        }
    }

    protected void load(String filePath) {
        try {
            accessControlModel.enterCriticalSection(Lock.WRITE);
            accessControlModel.read(filePath);
        } finally {
            accessControlModel.leaveCriticalSection();
        }
        configurationDataSet.replaceNamedModel(ModelNames.ACCESS_CONTROL, accessControlModel);

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
