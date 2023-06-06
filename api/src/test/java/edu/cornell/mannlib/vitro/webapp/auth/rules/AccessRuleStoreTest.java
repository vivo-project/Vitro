package edu.cornell.mannlib.vitro.webapp.auth.rules;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.Lock;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;

public class AccessRuleStoreTest {

    private static final String RDFS_LABEL_URI = "http://www.w3.org/2000/01/rdf-schema#label";
    private static final String ROLE_ADMIN_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#ADMIN";
    private static final String ROLE_EDITOR_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#EDITOR";
    private static final String ROLE_CURATOR_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#CURATOR";

    public static final String RULES_PATH = "src/test/resources/edu/cornell/mannlib/vitro/webapp/auth/rules/rules.n3";
    private Model model;
    private AccessRuleStore store;

    @Before
    public void init() {
        model = ModelFactory.createDefaultModel();
        try {
            model.enterCriticalSection(Lock.WRITE);
            model.read(RULES_PATH);    
        } finally {
            model.leaveCriticalSection();
        }
        AccessRuleStore.initialize(model);
        store = AccessRuleStore.getInstance();
    }
    
    public void testInitilization() {
        assertEquals(3, store.getRulesCount());
    }
    
    public void testGetGrantedRoleUris() {
        List<String> grantedRoles = store.getGrantedRoleUris(RDFS_LABEL_URI, AccessOperation.DISPLAY);
        assertEquals(1, grantedRoles.size());
        assertTrue(grantedRoles.contains(ROLE_ADMIN_URI));
    }
    
    public void testGetGrantedRoleUrisNotExists() {
        List<String> grantedRoles = store.getGrantedRoleUris(RDFS_LABEL_URI + "_NOT_EXISTS", AccessOperation.DISPLAY);
        assertEquals(0, grantedRoles.size());
    }
    
    public void testDeleteRule() {
        long initialSize = store.getModelSize();
        long initialRulesCount = store.getRulesCount();
        store.removeEntityRule(RDFS_LABEL_URI, AccessObjectType.OBJECT_PROPERTY, AccessOperation.PUBLISH, ROLE_ADMIN_URI);
        assertTrue(store.getModelSize() < initialSize);
        assertTrue(store.getRulesCount() < initialRulesCount);
    }
    
    public void testCreateRule() {
        long initialSize = store.getModelSize();
        long initialRulesCount = store.getRulesCount();
        store.createEntityRule(RDFS_LABEL_URI, AccessObjectType.OBJECT_PROPERTY, AccessOperation.PUBLISH, ROLE_EDITOR_URI);
        assertTrue(store.getModelSize() > initialSize);
        assertTrue(store.getRulesCount() > initialRulesCount);
    }
    
    public void testGrantedRolesModification() {
        List<String> grantedRoles = store.getGrantedRoleUris(RDFS_LABEL_URI, AccessOperation.PUBLISH);
        int prevRolesCount = grantedRoles.size();
        store.createEntityRule(RDFS_LABEL_URI, AccessObjectType.OBJECT_PROPERTY, AccessOperation.PUBLISH, ROLE_EDITOR_URI);
        grantedRoles = store.getGrantedRoleUris(RDFS_LABEL_URI, AccessOperation.PUBLISH);
        assertTrue(grantedRoles.size() > prevRolesCount);
        prevRolesCount = grantedRoles.size();
        store.createEntityRule(RDFS_LABEL_URI, AccessObjectType.OBJECT_PROPERTY, AccessOperation.PUBLISH, ROLE_CURATOR_URI);
        grantedRoles = store.getGrantedRoleUris(RDFS_LABEL_URI, AccessOperation.PUBLISH);
        assertTrue(grantedRoles.size() > prevRolesCount);
        prevRolesCount = grantedRoles.size();
        store.removeEntityRule(RDFS_LABEL_URI, AccessObjectType.OBJECT_PROPERTY, AccessOperation.PUBLISH, ROLE_EDITOR_URI);
        grantedRoles = store.getGrantedRoleUris(RDFS_LABEL_URI, AccessOperation.PUBLISH);
        assertTrue(grantedRoles.size() < prevRolesCount);
    }
    
    
    public void getGetAttributeUri() {
        assertEquals("https://vivoweb.org/ontology/vitro-application/auth/individual/PublishOperationAttribute", store.getAttributeUriFromModel("EQUALS","OPERATION","PUBLISH",true));
        assertEquals("", store.getAttributeUriFromModel("EQUALS","OPERATION","DO_SOMETHING_NEW", true));
    }
    
    public void testUpdateEntityRules() {
        HashSet<String> roles = new HashSet<String>(Arrays.asList(ROLE_ADMIN_URI, ROLE_EDITOR_URI ));
        HashSet<String> newRoles = new HashSet<String>(Arrays.asList(ROLE_CURATOR_URI));
        store.updateEntityRules(RDFS_LABEL_URI, AccessObjectType.OBJECT_PROPERTY, AccessOperation.PUBLISH, roles);
        HashSet<String> grantedRoles = new HashSet<String>(store.getGrantedRoleUris(RDFS_LABEL_URI, AccessOperation.PUBLISH));
        assertEquals(roles, grantedRoles);
        store.updateEntityRules(RDFS_LABEL_URI, AccessObjectType.OBJECT_PROPERTY, AccessOperation.PUBLISH, newRoles);
        grantedRoles = new HashSet<String>(store.getGrantedRoleUris(RDFS_LABEL_URI, AccessOperation.PUBLISH));
        assertEquals(newRoles, grantedRoles);
    }
}
