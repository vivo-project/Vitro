package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

public class BasicPolicyTest extends PolicyTest {

    public static final String BROKEN_POLICY_BROKEN_TEST = RESOURCES_PREFIX + "test_policy_broken1.n3";
    public static final String BROKEN_POLICY_BROKEN_TYPE = RESOURCES_PREFIX + "test_policy_broken2.n3";
    public static final String BROKEN_POLICY_BROKEN_TEST_ID = RESOURCES_PREFIX + "test_policy_broken3.n3";
    public static final String BROKEN_POLICY_BROKEN_TYPE_ID = RESOURCES_PREFIX + "test_policy_broken4.n3";
    public static final String BROKEN_POLICY_BROKEN_ATTRIBUTE_VALUES = RESOURCES_PREFIX + "test_policy_broken5.n3";

    @Test
    public void testGetPolicyUris() {
        load(VALID_POLICY);
        assertTrue(!loader.getPolicyUris().isEmpty());
    }

    @Test
    public void testValidPolicy() {
        load(VALID_POLICY);
        assertTrue(!loader.getPolicyUris().isEmpty());
    }

    @Test
    public void testValidPolicyWithSet() {
        load(VALID_POLICY_WITH_SET);
        DynamicPolicy policy = loader
                .loadPolicy("https://vivoweb.org/ontology/vitro-application/auth/individual/ValidTestSetPolicy");
        countRulesAndAttributes(policy, 2, Collections.singleton(2));
    }

    @Test
    public void testBrokenTestId() {
        load(BROKEN_POLICY_BROKEN_TEST_ID);
        DynamicPolicy policy = loader.loadPolicy(
                "https://vivoweb.org/ontology/vitro-application/auth/individual/BrokenPolicyBrokenTestTypeId");
        assertTrue(policy == null);
    }

    @Test
    public void testBrokenTypeId() {
        load(BROKEN_POLICY_BROKEN_TYPE_ID);
        DynamicPolicy policy = loader
                .loadPolicy("https://vivoweb.org/ontology/vitro-application/auth/individual/BrokenPolicyTypeId");
        assertTrue(policy == null);
    }

    @Test
    public void testBrokenTest() {
        load(BROKEN_POLICY_BROKEN_TEST);
        DynamicPolicy policy = loader.loadPolicy(
                "https://vivoweb.org/ontology/vitro-application/auth/individual/BrokenPolicyBrokenTestType");
        assertTrue(policy == null);
    }

    @Test
    public void testBrokenType() {
        load(BROKEN_POLICY_BROKEN_TYPE);
        DynamicPolicy policy = loader
                .loadPolicy("https://vivoweb.org/ontology/vitro-application/auth/individual/BrokenPolicyType");
        assertTrue(policy == null);
    }

    @Test
    public void testBrokenAttributeValue() {
        load(BROKEN_POLICY_BROKEN_ATTRIBUTE_VALUES);
        DynamicPolicy policy = loader
                .loadPolicy("https://vivoweb.org/ontology/vitro-application/auth/individual/BrokenTestSetPolicy");
        assertTrue(policy == null);
    }

    @Test
    public void testBrokenSet() {
        load(BROKEN_POLICY_WITH_SET);
        DynamicPolicy policy = loader
                .loadPolicy("https://vivoweb.org/ontology/vitro-application/auth/individual/BrokenTestSetPolicy");
        assertTrue(policy == null);
    }

    @Test
    public void testDuplicateTriplesInPolicies() {
        Model m = ModelFactory.createDefaultModel();
        Map<String, Long> pathToSizeMap = new HashMap<>();
        for (String entityPolicyPath : entityPolicies) {
            load(m, entityPolicyPath);
            pathToSizeMap.put(entityPolicyPath, m.size());
            m.removeAll();
        }
        long cumulativeSize = 0;
        for (String entityPolicyPath : entityPolicies) {
            load(m, entityPolicyPath);
            cumulativeSize += pathToSizeMap.get(entityPolicyPath);
            if (cumulativeSize != m.size()) {
                System.out.println(entityPolicyPath);
            }
            assertEquals(cumulativeSize, m.size());
        }
    }
}
