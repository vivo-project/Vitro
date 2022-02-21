package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_UNION;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Before;

import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccessStub;
import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccessFactoryStub;
import stubs.javax.servlet.ServletContextStub;

public abstract class ServletContextTest {

    protected final static String TEST_ACTION_NAME = "test_action";
    protected final static String TEST_RELOAD_ACTION_NAME = "test_reload";

    protected final static String TEST_RESOURCE_NAME = "test_resource";
    protected final static String TEST_RELOAD_RESOURCE_NAME = "test_reload_resource";

    protected ServletContextStub servletContext;
    protected ModelAccessFactoryStub modelAccessFactory;
    protected ContextModelAccessStub contentModelAccess;
    protected OntModel ontModel;

    @Before
    public void setup() {
        servletContext = new ServletContextStub();
        modelAccessFactory = new ModelAccessFactoryStub();

        contentModelAccess = modelAccessFactory.get(servletContext);

        ontModel = ModelFactory.createOntologyModel();

        contentModelAccess.setOntModel(FULL_UNION, ontModel);
    }

    protected void loadReloadModel() throws IOException {
        // reloading action reuses testSparqlQuery1 from testing action
        loadModel(
            new RDFFile("N3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-reloading.n3")
        );
    }

    protected void loadDefaultModel() throws IOException {
        loadModel(
            new RDFFile("N3", "../home/src/main/resources/rdf/tbox/filegraph/dynamic-api-implementation.n3"),
            new RDFFile("N3", "../home/src/main/resources/rdf/abox/filegraph/dynamic-api-individuals.n3"),
            new RDFFile("N3", "../home/src/main/resources/rdf/abox/filegraph/dynamic-api-individuals-testing.n3")
        );
    }

    protected void loadModel(RDFFile... rdfFiles) throws IOException {
        for (RDFFile rdfFile : rdfFiles) {
            String rdf = readRdf(rdfFile.path);
            ontModel.read(new StringReader(rdf), null, rdfFile.format);
        }
    }

    protected String readRdf(String rdfPath) throws IOException {
        Path path = new File(rdfPath).toPath();

        return new String(Files.readAllBytes(path));
    }

    protected class RDFFile {
        private final String format;
        private final String path;

        private RDFFile(String format, String path) {
            this.format = format;
            this.path = path;
        }
    }

}
