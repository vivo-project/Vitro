package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_UNION;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.DefaultAction;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccessStub;
import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccessFactoryStub;
import stubs.javax.servlet.ServletContextStub;

public class ActionPoolTest {

    private ServletContextStub servletContext;
    private ModelAccessFactoryStub modelAccessFactory;
    private ContextModelAccessStub contentModelAccess;
    private OntModel ontModel;

    @Before
    public void setupContext() throws IOException {
        servletContext = new ServletContextStub();
        modelAccessFactory = new ModelAccessFactoryStub();

        ModelAccess.on(servletContext);

        contentModelAccess = modelAccessFactory.get(servletContext);

        ontModel = ModelFactory.createOntologyModel();

        contentModelAccess.setOntModel(FULL_UNION, ontModel);
    }

    @Test
    public void testGetInstance() {
        ActionPool actionPool = ActionPool.getInstance();
        assertNotNull(actionPool);
    }

    @Test
    public void testGetByNameBeforeInit() {
        ActionPool actionPool = ActionPool.getInstance();
        Action action = actionPool.getByName("test");
        assertNotNull(action);
        assertTrue(action instanceof DefaultAction);
    }

    @Test
    public void testPrintActionNamesBeforeInit() {
        ActionPool actionPool = ActionPool.getInstance();
        actionPool.printActionNames();
    }

    @Test
    public void testInit() throws IOException {

        loadModel(
            new RDFFile("N3", "../home/src/main/resources/rdf/tbox/filegraph/dynamic-api-implementation.n3"),
            new RDFFile("N3", "../home/src/main/resources/rdf/abox/filegraph/dynamic-api-individuals.n3"),
            new RDFFile("N3", "../home/src/main/resources/rdf/abox/filegraph/dynamic-api-individuals-testing.n3")
        );

        ActionPool actionPool = ActionPool.getInstance();

        actionPool.init(servletContext);

        String name = "test_action";
        assertActionByName(actionPool.getByName(name), name);
    }

    @Test
    public void testReloadBeforeInit() throws IOException {
        ActionPool actionPool = ActionPool.getInstance();
        actionPool.reload();
        // not sure what to assert
    }

    @Test
    public void testReload() throws IOException {
        loadModel(
            new RDFFile("N3", "../home/src/main/resources/rdf/tbox/filegraph/dynamic-api-implementation.n3"),
            new RDFFile("N3", "../home/src/main/resources/rdf/abox/filegraph/dynamic-api-individuals.n3"),
            new RDFFile("N3", "../home/src/main/resources/rdf/abox/filegraph/dynamic-api-individuals-testing.n3")
        );

        ActionPool actionPool = ActionPool.getInstance();

        actionPool.init(servletContext);

        String testName = "test_action";
        assertActionByName(actionPool.getByName(testName), testName);

        // reloading action reuses testSparqlQuery1 from testing action
        loadModel(
            new RDFFile("N3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-reloading.n3")
        );

        actionPool.reload();

        String name = "test_reload";
        assertActionByName(actionPool.getByName(name), name);
    }

    private void assertActionByName(Action action, String name) {
        assertNotNull(action);
        assertFalse(format("%s not loaded!", name), action instanceof DefaultAction);
        assertTrue(action.isValid());
        assertEquals(name, action.getName());
    }

    private void loadModel(RDFFile... rdfFiles) throws IOException {
        for (RDFFile rdfFile : rdfFiles) {
            String rdf = readN3(rdfFile.path);
            ontModel.read(new StringReader(rdf), null, rdfFile.format);
        }
    }

    private String readN3(String n3Path) throws IOException {
        Path path = new File(n3Path).toPath();

        return new String(Files.readAllBytes(path));
    }

    private class RDFFile {
        private final String format;
        private final String path;

        private RDFFile(String format, String path) {
            this.format = format;
            this.path = path;
        }
    }

}
