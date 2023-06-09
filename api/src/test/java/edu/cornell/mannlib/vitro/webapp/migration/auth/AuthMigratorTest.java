package edu.cornell.mannlib.vitro.webapp.migration.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyTest;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;

public class AuthMigratorTest extends PolicyTest {
    
    private static final String TEST_MIGRATION_RESOURCES_PREFIX = "src/test/resources/edu/cornell/mannlib/vitro/webapp/auth/migration/";
    private static final String CONTENT = TEST_MIGRATION_RESOURCES_PREFIX + "content.n3";
    private static final String CONFIGURATRION = TEST_MIGRATION_RESOURCES_PREFIX + "configuration.n3";

    private Model contentModel;
    private Model configurationModel;
    private AuthMigrator migrator;

    @Before 
    public void initMigrationTest() {
        contentModel = ModelFactory.createDefaultModel();
        configurationModel = ModelFactory.createDefaultModel();
        migrator = new AuthMigrator();
        migrator.initialize(new RDFServiceModel(contentModel), new RDFServiceModel(configurationModel));
        loadAllEntityPolicies();
        load(configurationModel, CONFIGURATRION);
        load(contentModel, CONTENT);
    }
    
    
    @Test
    public void testGetAnnotationConfigs() {
        Map<String, Map<OperationGroup, Set<String>>> configs = migrator.getObjectPropertyAnnotations();
        Set<String> ops = configs.keySet();
        assertEquals(1, configs.size());
        configs = migrator.getDataPropertyAnnotations();
        Set<String> dps = configs.keySet();
        assertEquals(1, configs.size());
        configs = migrator.getClassAnnotations();
        assertEquals(2, configs.size());
        configs = migrator.getFauxObjectPropertyAnnotations(ops);
        assertEquals(1, configs.size());
        configs = migrator.getFauxDataPropertyAnnotations(dps);
        assertEquals(1, configs.size());
    }
    
    @Test
    public void testConvertAnnotationConfiguration() {
        Model tmpModel = ModelFactory.createDefaultModel();
        tmpModel.add(ds.getNamedModel(ModelNames.USER_ACCOUNTS));
        long initialSize = userAccountsModel.size();
        migrator.convertAnnotationConfiguration();
        assertTrue(ds.getNamedModel(ModelNames.USER_ACCOUNTS).size() > initialSize);
        Model diff = ds.getNamedModel(ModelNames.USER_ACCOUNTS).difference(tmpModel);
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            diff.write(baos, "TTL");
            String newData = baos.toString();
            //System.out.println(newData);
        } catch (IOException e) {
            e.printStackTrace();
        }
       
    }
}
