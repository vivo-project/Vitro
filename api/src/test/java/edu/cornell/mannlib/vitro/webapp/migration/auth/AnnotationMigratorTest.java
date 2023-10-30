package edu.cornell.mannlib.vitro.webapp.migration.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

public class AnnotationMigratorTest extends AuthMigratorTest {

    @Test
    public void testGetAnnotationConfigs() {
        AnnotationMigrator annotationMigrator =
                new AnnotationMigrator(new RDFServiceModel(contentModel), new RDFServiceModel(configurationDataSet));
        Map<String, Map<OperationGroup, Set<String>>> configs = annotationMigrator.getObjectPropertyAnnotations();
        Set<String> ops = configs.keySet();
        assertEquals(1, configs.size());
        configs = annotationMigrator.getDataPropertyAnnotations();
        Set<String> dps = configs.keySet();
        assertEquals(1, configs.size());
        configs = annotationMigrator.getClassAnnotations();
        assertEquals(2, configs.size());
        configs = annotationMigrator.getFauxObjectPropertyAnnotations(ops);
        assertEquals(1, configs.size());
        configs = annotationMigrator.getFauxDataPropertyAnnotations(dps);
        assertEquals(1, configs.size());
    }

    @Test
    public void testConvertAnnotationConfiguration() {
        AnnotationMigrator annotationMigrator =
                new AnnotationMigrator(new RDFServiceModel(contentModel), new RDFServiceModel(configurationDataSet));
        Model tmpModel = ModelFactory.createDefaultModel();
        tmpModel.add(configurationDataSet.getNamedModel(ModelNames.ACCESS_CONTROL));
        long initialSize = accessControlModel.size();
        annotationMigrator.migrateConfiguration();
        assertTrue(configurationDataSet.getNamedModel(ModelNames.ACCESS_CONTROL).size() > initialSize);
        Model diff = configurationDataSet.getNamedModel(ModelNames.ACCESS_CONTROL).difference(tmpModel);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            diff.write(baos, "TTL");
            String newData = baos.toString();
            assertFalse(StringUtils.isBlank(newData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConfigurationVersion() {
        Model tmpModel = ModelFactory.createDefaultModel();
        tmpModel.add(configurationDataSet.getNamedModel(ModelNames.ACCESS_CONTROL));
        assertEquals(0L, migrator.getVersion());
        migrator.setVersion(1);
        assertEquals(1L, migrator.getVersion());
        migrator.removeVersion(1);
        migrator.setVersion(2);
        assertEquals(2L, migrator.getVersion());
    }
}
