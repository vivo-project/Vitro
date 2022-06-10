package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_UNION;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.Lock;
import org.junit.Before;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPIKey;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader;
import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccessStub;
import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccessFactoryStub;
import stubs.javax.servlet.ServletContextStub;

public abstract class ServletContextTest {

    protected final static String TEST_ACTION_NAME = "test_action";
    protected final static ResourceAPIKey TEST_RESOURCE_KEY = ResourceAPIKey.of("test_resource", "0.1.0");

    protected final static String TEST_PERSON_ACTION_NAME = "test_person";
    protected final static ResourceAPIKey TEST_PERSON_RESOURCE_KEY = ResourceAPIKey.of("test_person_resource", "1.0.0");

    protected ServletContextStub servletContext;
    protected ModelAccessFactoryStub modelAccessFactory;
    protected ContextModelAccessStub contentModelAccess;
    protected OntModel ontModel;

    protected ConfigurationBeanLoader loader;

    @Before
    public void setup() {
        servletContext = new ServletContextStub();
        modelAccessFactory = new ModelAccessFactoryStub();

        contentModelAccess = modelAccessFactory.get(servletContext);

        ontModel = ModelFactory.createOntologyModel();

        contentModelAccess.setOntModel(FULL_UNION, ontModel);

        loader = new ConfigurationBeanLoader(ontModel, servletContext);
    }

    protected void loadTestModel() throws IOException {
        // all actions reuse testSparqlQuery1 from testing action
        loadModel(
            new RDFFile("N3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-collection.n3"),
            new RDFFile("N3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-concept.n3"),
            new RDFFile("N3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-document.n3"),
            new RDFFile("N3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-organization.n3"),
            new RDFFile("N3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-person.n3"),
            new RDFFile("N3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-process.n3"),
            new RDFFile("N3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-relationship.n3")
        );
    }
    
    protected void loadDefaultModel() throws IOException {
        loadModel(
            new RDFFile("N3", "../home/src/main/resources/rdf/tbox/filegraph/dynamic-api-implementation.n3"),
            new RDFFile("N3", "../home/src/main/resources/rdf/abox/filegraph/dynamic-api-individuals.n3"),
            new RDFFile("N3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-testing.n3")
        );
    }

    protected void loadModel(RDFFile... rdfFiles) throws IOException {
        for (RDFFile rdfFile : rdfFiles) {
            String rdf = readFile(rdfFile.path);
            ontModel.enterCriticalSection(Lock.WRITE);
            try {
                ontModel.read(new StringReader(rdf), null, rdfFile.format);
            } finally {
                if (ontModel != null) {
                    ontModel.leaveCriticalSection();
                }
            }
        }
    }

    protected void loadModels(String fileFormat, String... paths) throws IOException {
        for (String path : paths) {
            loadModel(new RDFFile(fileFormat, path));
        }
    }

    protected void loadPersonVersion1_1Model() throws IOException {
        // versioning action reuses testSparqlQuery1 from testing action
        loadModel(
            new RDFFile("N3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-person1_1.n3")
        );
    }

    protected void loadPersonVersion2Model() throws IOException {
        // versioning action reuses testSparqlQuery1 from testing action
        loadModel(
            new RDFFile("N3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-person2.n3")
        );
    }

    protected void loadPersonVersion4_3_7Model() throws IOException {
        // versioning action reuses testSparqlQuery1 from testing action
        loadModel(
            new RDFFile("N3", "src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-person4_3_7.n3")
        );
    }

    protected String readFile(String path) throws IOException {
        Path p = new File(path).toPath();

        return new String(Files.readAllBytes(p));
    }

    protected InputStream readFileAsInputStream(String path) throws IOException {
        File file = new File(path);

        return new FileInputStream(file);
    }

    protected class RDFFile {
        private final String format;
        private final String path;

        protected RDFFile(String format, String path) {
            this.format = format;
            this.path = path;
        }
    }

}
