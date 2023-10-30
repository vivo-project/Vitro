package edu.cornell.mannlib.vitro.webapp.migration.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.policy.EntityPolicyController;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyTest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.junit.Before;
import org.junit.Test;

public class ArmMigratorTest extends AuthMigratorTest {

    private static final String FAUX_DATA_PROPERTY_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/siteConfig/fp396";
    private static final String FAUX_OBJECT_PROPERTY_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/siteConfig/orgAdministersGrantConfig";
    private static final String DATA_PROPERTY_URI = "http://vivoweb.org/ontology/core#abbreviation";
    private static final String CLASS_URI = "http://xmlns.com/foaf/0.1/Organization";
    private static final String OBJECT_PROPERTY_URI = "http://purl.obolibrary.org/obo/RO_0000053";
    private OntModel userAccountsModel;
    private ArmMigrator armMigrator;
    private String propertyUri = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#forEntity";
    private String value = "https://vivoweb.org/ontology/vitro-application/auth/vocabulary/value";

    @Before
    public void initArmMigration() {
        userAccountsModel = ModelFactory.createOntologyModel();
        configurationDataSet.addNamedModel(ModelNames.USER_ACCOUNTS, userAccountsModel);
        addUserAccountsStatement(PolicyTest.CUSTOM, VitroVocabulary.PERMISSIONSET, VitroVocabulary.RDF_TYPE);
        armMigrator = new ArmMigrator(new RDFServiceModel(contentModel), new RDFServiceModel(configurationDataSet),
                userAccountsModel);
    }

    @Test
    public void isArmConfigurationTest() {
        assertFalse(armMigrator.isArmConfiguation());
        addArmStatement(ArmMigrator.ARM_ADMIN, ArmMigrator.DISPLAY, OBJECT_PROPERTY_URI);
        assertTrue(armMigrator.isArmConfiguation());
    }

    private void addArmStatement(String role, String operation, String uri) {
        String persmissionUri = ArmMigrator.getArmPermissionSubject(operation, role);
        String objectUri = uri;
        addUserAccountsStatement(persmissionUri, objectUri, propertyUri);
    }

    private void addUserAccountsStatement(String subjUri, String objUri, String pUri) {
        Statement statement =
                new StatementImpl(new ResourceImpl(subjUri), new PropertyImpl(pUri), new ResourceImpl(objUri));
        userAccountsModel.add(statement);
        configurationDataSet.replaceNamedModel(ModelNames.USER_ACCOUNTS, userAccountsModel);
    }

    @Test
    public void getEntityMapTest() {
        Map<AccessObjectType, Set<String>> map = armMigrator.getEntityMap();
        assertFalse(map.isEmpty());
        assertEquals(1, map.get(AccessObjectType.OBJECT_PROPERTY).size());
        assertEquals(1, map.get(AccessObjectType.DATA_PROPERTY).size());
        assertEquals(2, map.get(AccessObjectType.CLASS).size());
        assertEquals(1, map.get(AccessObjectType.FAUX_DATA_PROPERTY).size());
        assertEquals(1, map.get(AccessObjectType.FAUX_DATA_PROPERTY).size());
    }

    @Test
    public void getStatementsToRemoveTest() {
        StringBuilder removals = armMigrator.getStatementsToRemove();
        assertTrue(StringUtils.isBlank(removals.toString()));
        EntityPolicyController.updateEntityDataSet("test:entity", AccessObjectType.CLASS,
                AccessOperation.DISPLAY, ROLE_LIST, ROLE_LIST);
        removals = armMigrator.getStatementsToRemove();
        assertEquals(5, getCount("\n", removals.toString()));
    }

    @Test
    public void migrateConfigurationTest() {
        addArmStatement(ArmMigrator.ARM_ADMIN, ArmMigrator.DISPLAY, OBJECT_PROPERTY_URI);
        addArmStatement(ArmMigrator.ARM_EDITOR, ArmMigrator.PUBLISH, OBJECT_PROPERTY_URI);
        addArmStatement(ArmMigrator.ARM_CURATOR, ArmMigrator.UPDATE, OBJECT_PROPERTY_URI);
        addArmStatement(ArmMigrator.ARM_SELF_EDITOR, ArmMigrator.DISPLAY, CLASS_URI);
        addArmStatement(ArmMigrator.ARM_EDITOR, ArmMigrator.PUBLISH, CLASS_URI);

        addArmStatement(ArmMigrator.ARM_ADMIN, ArmMigrator.DISPLAY, DATA_PROPERTY_URI);
        addArmStatement(ArmMigrator.ARM_EDITOR, ArmMigrator.PUBLISH, DATA_PROPERTY_URI);
        addArmStatement(ArmMigrator.ARM_CURATOR, ArmMigrator.UPDATE, DATA_PROPERTY_URI);
        addArmStatement(ArmMigrator.ARM_SELF_EDITOR, ArmMigrator.DISPLAY, FAUX_DATA_PROPERTY_URI);
        addArmStatement(ArmMigrator.ARM_EDITOR, ArmMigrator.PUBLISH, FAUX_DATA_PROPERTY_URI);
        addArmStatement(ArmMigrator.ARM_ADMIN, ArmMigrator.UPDATE, FAUX_DATA_PROPERTY_URI);
        addArmStatement(ArmMigrator.ARM_PUBLIC, ArmMigrator.DISPLAY, FAUX_OBJECT_PROPERTY_URI);
        addArmStatement(ArmMigrator.ARM_EDITOR, ArmMigrator.PUBLISH, FAUX_OBJECT_PROPERTY_URI);
        addArmStatement(ArmMigrator.ARM_CURATOR, ArmMigrator.UPDATE, FAUX_OBJECT_PROPERTY_URI);
        addArmStatement(ArmMigrator.ARM_CUSTOM, ArmMigrator.DISPLAY, OBJECT_PROPERTY_URI);
        addArmStatement(ArmMigrator.ARM_CUSTOM, ArmMigrator.UPDATE, FAUX_DATA_PROPERTY_URI);
        addArmStatement(ArmMigrator.ARM_CUSTOM, ArmMigrator.DISPLAY, CLASS_URI);

        // TODO Class UPDATE permissions migration.
        // addArmStatement(ArmMigrator.ARM_EDITOR, ArmMigrator.UPDATE, CLASS_URI);
        // addArmStatement(ArmMigrator.ARM_EDITOR, ArmMigrator.UPDATE, CLASS_URI);

        Map<AccessObjectType, Set<String>> entityTypeMap = armMigrator.getEntityMap();
        armMigrator.collectAdditions(entityTypeMap);
        String stringResult = armMigrator.additions.toString();
        assertFalse(stringResult.isEmpty());
        assertEquals(33, getCount("\n", stringResult));
        assertEquals(33, getCount(value, stringResult));
        assertEquals(6, getCount(OBJECT_PROPERTY_URI, stringResult));
        assertEquals(3, getCount(CLASS_URI, stringResult));
        assertEquals(5, getCount(DATA_PROPERTY_URI, stringResult));
        assertEquals(12, getCount(FAUX_DATA_PROPERTY_URI, stringResult));
        assertEquals(7, getCount(FAUX_OBJECT_PROPERTY_URI, stringResult));
    }

    private static long getCount(String pattern, String lines) {
        Matcher matcher = Pattern.compile(pattern).matcher(lines);
        long i = 0;
        while (matcher.find()) {
            i++;
        }
        return i;
    }
}
