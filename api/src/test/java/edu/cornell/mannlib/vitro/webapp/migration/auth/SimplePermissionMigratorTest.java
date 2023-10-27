package edu.cornell.mannlib.vitro.webapp.migration.auth;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.NAMED_OBJECT;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.EXECUTE;
import static edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission.ACCESS_SPECIAL_DATA_MODELS;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.PERMISSIONSET;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.PERMISSIONSET_HAS_PERMISSION;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.RDF_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyLoader;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyTemplateController;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyTest;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.junit.Before;
import org.junit.Test;


public class SimplePermissionMigratorTest extends AuthMigratorTest {

    private OntModel userAccountsModel;
    private SimplePermissionMigrator spm;
    private static final String TEMPLATE_PATH = "template_simple_permissions";
    public static final String ADMIN_SIMPLE_PERMISSIONS_PATH = "simple_permissions_admin";
    public static final String CURATOR_SIMPLE_PERMISSIONS_PATH = "simple_permissions_curator";
    public static final String EDITOR_SIMPLE_PERMISSIONS_PATH = "simple_permissions_editor";
    public static final String SELF_EDITOR_SIMPLE_PERMISSIONS_PATH = "simple_permissions_self_editor";
    public static final String PUBLIC_SIMPLE_PERMISSIONS_PATH = "simple_permissions_public";

    @Before
    public void initMigration() {
        userAccountsModel = ModelFactory.createOntologyModel();
        configurationDataSet.addNamedModel(ModelNames.USER_ACCOUNTS, userAccountsModel);
        spm = new SimplePermissionMigrator(userAccountsModel);
        load(PolicyTest.USER_ACCOUNTS_HOME_FIRSTTIME + TEMPLATE_PATH + EXT);
    }

    private void addUserAccountsStatement(String subjUri, String pUri, String objUri) {
        Statement statement =
                new StatementImpl(new ResourceImpl(subjUri), new PropertyImpl(pUri), new ResourceImpl(objUri));
        userAccountsModel.add(statement);
    }

    @Test
    public void getPermissionSetsTest() {
        addPermissionSet(PolicyTest.CUSTOM);
        addPermissionSet(PolicyTest.ADMIN);
        addPermissionSet(PolicyTest.CURATOR);
        addPermissionSet(PolicyTest.EDITOR);
        addPermissionSet(PolicyTest.SELF_EDITOR);
        addPermissionSet(PolicyTest.PUBLIC);
        Set<String> sets = spm.getPermissionSets();
        assertEquals(6, sets.size());
        assertTrue(sets.contains(PolicyTest.CUSTOM));
        assertTrue(sets.contains(PolicyTest.ADMIN));
        assertTrue(sets.contains(PolicyTest.CURATOR));
        assertTrue(sets.contains(PolicyTest.EDITOR));
        assertTrue(sets.contains(PolicyTest.SELF_EDITOR));
        assertTrue(sets.contains(PolicyTest.PUBLIC));
    }

    @Test
    public void getUserAccountPermissionsTest() {
        addUserAccountsStatement(PolicyTest.CUSTOM, RDF_TYPE, PERMISSIONSET);
        addPermissionSet(PolicyTest.ADMIN);
        addUserAccountsStatement(PolicyTest.CUSTOM, PERMISSIONSET_HAS_PERMISSION, ACCESS_SPECIAL_DATA_MODELS.getUri());
        addUserAccountsStatement(PolicyTest.CUSTOM, PERMISSIONSET_HAS_PERMISSION,
                SimplePermission.DO_BACK_END_EDITING.getUri());
        Set<String> permissions = spm.getUserAccountPermissions(PolicyTest.CUSTOM);
        assertEquals(2, permissions.size());
        assertTrue(permissions.contains(ACCESS_SPECIAL_DATA_MODELS.getUri()));
        assertTrue(permissions.contains(SimplePermission.DO_BACK_END_EDITING.getUri()));
        permissions = spm.getUserAccountPermissions(PolicyTest.ADMIN);
        assertEquals(0, permissions.size());
    }

    @Test
    public void migrateConfigurationTest() {
        addPermissionSet(PolicyTest.ADMIN);
        addPermissionSet(PolicyTest.CURATOR);
        addPermissionSet(PolicyTest.EDITOR);
        addPermissionSet(PolicyTest.SELF_EDITOR);
        addPermissionSet(PolicyTest.PUBLIC);
        addPermissionSet(PolicyTest.CUSTOM);
        addUserAccountsStatement(PolicyTest.CUSTOM, PERMISSIONSET_HAS_PERMISSION, ACCESS_SPECIAL_DATA_MODELS.getUri());
        addUserAccountsStatement(PolicyTest.PUBLIC, PERMISSIONSET_HAS_PERMISSION, ACCESS_SPECIAL_DATA_MODELS.getUri());
        load(USER_ACCOUNTS_HOME_FIRSTTIME + PUBLIC_SIMPLE_PERMISSIONS_PATH + EXT);
        PolicyTemplateController.createRoleDataSets(CUSTOM);
        spm.migrateConfiguration();
        PolicyLoader policyLoader = PolicyLoader.getInstance();
        Set<String> entities = policyLoader.getDataSetValues(EXECUTE, NAMED_OBJECT, PolicyTest.CUSTOM);
        assertEquals(1, entities.size());
        assertTrue(entities.contains(ACCESS_SPECIAL_DATA_MODELS.getUri()));
        entities = policyLoader.getDataSetValues(EXECUTE, NAMED_OBJECT, PolicyTest.PUBLIC);
        assertEquals(1, entities.size());
        assertTrue(entities.contains(ACCESS_SPECIAL_DATA_MODELS.getUri()));
        entities = policyLoader.getDataSetValues(EXECUTE, NAMED_OBJECT, PolicyTest.ADMIN);
        assertEquals(1, entities.size());
        assertTrue(entities.contains(ACCESS_SPECIAL_DATA_MODELS.getUri()));
    }

    private void addPermissionSet(String role) {
        addUserAccountsStatement(role, RDF_TYPE, PERMISSIONSET);
    }

}
