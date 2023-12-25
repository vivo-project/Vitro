package edu.cornell.mannlib.vitro.webapp.migration.auth;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueSetRegistry;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyTest;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.After;
import org.junit.Before;

public class AuthMigratorTest extends PolicyTest {
    protected static final String TEST_MIGRATION_RESOURCES_PREFIX =
            "src/test/resources/edu/cornell/mannlib/vitro/webapp/auth/migration/";
    protected static final String CONTENT = TEST_MIGRATION_RESOURCES_PREFIX + "content.n3";
    protected static final String CONFIGURATION = TEST_MIGRATION_RESOURCES_PREFIX + "configuration.n3";

    protected Model contentModel;
    protected Model configurationModel;
    protected AuthMigrator migrator;

    @Before
    public void initAuthMigration() {
        contentModel = ModelFactory.createDefaultModel();
        configurationModel = ModelFactory.createDefaultModel();
        migrator = new AuthMigrator();
        configurationDataSet.addNamedModel(ModelNames.DISPLAY, configurationModel);
        migrator.initialize(new RDFServiceModel(contentModel), new RDFServiceModel(configurationDataSet));
        loadAllEntityPolicies();
        load(configurationModel, CONFIGURATION);
        configurationDataSet.replaceNamedModel(ModelNames.DISPLAY, configurationModel);
        load(contentModel, CONTENT);
        AttributeValueSetRegistry.getInstance().clear();
        LogManager.getLogger(AnnotationMigrator.class).setLevel(Level.ERROR);
        LogManager.getLogger(ArmMigrator.class).setLevel(Level.ERROR);
    }

    @After
    public void finish() {
        LogManager.getLogger(AnnotationMigrator.class).setLevel(Level.INFO);
        LogManager.getLogger(ArmMigrator.class).setLevel(Level.INFO);
    }

}
