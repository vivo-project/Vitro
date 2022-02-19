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
import java.util.concurrent.CompletableFuture;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.DefaultAction;
import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccessStub;
import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccessFactoryStub;
import stubs.javax.servlet.ServletContextStub;

public class ActionPoolTest {

    private final static String TEST_ACTION_NAME = "test_action";
    private final static String TEST_RELOAD_ACTION_NAME = "test_reload";

    private ServletContextStub servletContext;
    private ModelAccessFactoryStub modelAccessFactory;
    private ContextModelAccessStub contentModelAccess;
    private OntModel ontModel;

    @Before
    public void setupContext() {
        servletContext = new ServletContextStub();
        modelAccessFactory = new ModelAccessFactoryStub();

        contentModelAccess = modelAccessFactory.get(servletContext);

        ontModel = ModelFactory.createOntologyModel();

        contentModelAccess.setOntModel(FULL_UNION, ontModel);
    }

    @After
    public void cleanupActionPool() {
        setupContext();

        ActionPool actionPool = ActionPool.getInstance();
        actionPool.init(servletContext);
        actionPool.reload();
    }

    @Test
    public void testGetInstance() {
        ActionPool actionPool = ActionPool.getInstance();
        assertNotNull(actionPool);
        assertEquals(actionPool, ActionPool.getInstance());
    }

    @Test
    public void testGetByNameBeforeInit() {
        ActionPool actionPool = ActionPool.getInstance();
        Action action = actionPool.getByName(TEST_ACTION_NAME);
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
        loadDefaultModel();

        ActionPool actionPool = ActionPool.getInstance();

        actionPool.init(servletContext);

        assertActionByName(actionPool.getByName(TEST_ACTION_NAME), TEST_ACTION_NAME);
    }

    @Test
    public void testReloadBeforeInit() throws IOException {
        ActionPool actionPool = ActionPool.getInstance();
        actionPool.reload();
        // not sure what to assert
    }

    @Test
    public void testReload() throws IOException {
        loadDefaultModel();

        ActionPool actionPool = ActionPool.getInstance();

        actionPool.init(servletContext);

        assertActionByName(actionPool.getByName(TEST_ACTION_NAME), TEST_ACTION_NAME);

        // reloading action reuses testSparqlQuery1 from testing action
        loadModel(
            new RDFFile("N3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-reloading.n3")
        );

        actionPool.reload();

        assertActionByName(actionPool.getByName(TEST_RELOAD_ACTION_NAME), TEST_RELOAD_ACTION_NAME);
    }

    private void assertActionByName(Action action, String name) {
        assertNotNull(action);
        assertFalse(format("%s not loaded!", name), action instanceof DefaultAction);
        assertTrue(action.isValid());
        assertEquals(name, action.getName());
    }

    private void loadDefaultModel() throws IOException {
        loadModel(
            new RDFFile("N3", "../home/src/main/resources/rdf/tbox/filegraph/dynamic-api-implementation.n3"),
            new RDFFile("N3", "../home/src/main/resources/rdf/abox/filegraph/dynamic-api-individuals.n3"),
            new RDFFile("N3", "../home/src/main/resources/rdf/abox/filegraph/dynamic-api-individuals-testing.n3")
        );
    }

    private void loadModel(RDFFile... rdfFiles) throws IOException {
        for (RDFFile rdfFile : rdfFiles) {
            String rdf = readRdf(rdfFile.path);
            ontModel.read(new StringReader(rdf), null, rdfFile.format);
        }
    }

    private String readRdf(String rdfPath) throws IOException {
        Path path = new File(rdfPath).toPath();

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
