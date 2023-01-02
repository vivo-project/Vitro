package edu.cornell.mannlib.vitro.webapp.dynapi;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

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

    public static final String ABOX_PREFIX = "../home/src/main/resources/rdf/dynapiAbox/everytime/";
    public static final String TBOX_PREFIX = "../home/src/main/resources/rdf/dynapiTbox/everytime/";
    public static final String TEST_PREFIX = "src/test/resources/rdf/abox/filegraph/";

    //public static final String INDIVIDUALS_FILE_PATH = ABOX_PREFIX + "dynamic-api-individuals.n3";
    public static final String IMPLEMENTATION_FILE_PATH = TBOX_PREFIX + "dynamic-api-implementation.n3";
    public static final String ONTOLOGY_FILE_PATH = TBOX_PREFIX + "vitro-dynamic-api.n3";
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

        DynapiModelProvider.getInstance().setModel(ontModel);

        loader = new ConfigurationBeanLoader(ontModel, servletContext);
    }

    protected void loadTestModel() throws IOException {
        // all actions reuse testSparqlQuery1 from testing action
        loadModel(
            new RDFFile("N3", TEST_PREFIX + "dynamic-api-individuals-collection.n3"),
            new RDFFile("N3", TEST_PREFIX + "dynamic-api-individuals-concept.n3"),
            new RDFFile("N3", TEST_PREFIX + "dynamic-api-individuals-document.n3"),
            new RDFFile("N3", TEST_PREFIX + "dynamic-api-individuals-organization.n3"),
            new RDFFile("N3", TEST_PREFIX + "dynamic-api-individuals-person.n3"),
            new RDFFile("N3", TEST_PREFIX + "dynamic-api-individuals-process.n3"),
            new RDFFile("N3", TEST_PREFIX + "dynamic-api-individuals-relationship.n3")
        );
    }
    
    protected void loadDefaultModel() throws IOException {
        loadModel(
            new RDFFile("N3", IMPLEMENTATION_FILE_PATH),
            new RDFFile("N3", ONTOLOGY_FILE_PATH),
            new RDFFile("N3", TEST_PREFIX + "dynamic-api-individuals-testing.n3")
        );
        loadModels("N3", getBaseIndividuals());
    }
    

    private String[] getBaseIndividuals() {
        return new String[]{
                ABOX_PREFIX + "http_methods.n3", 
                ABOX_PREFIX + "paramerter_types.n3", 
                ABOX_PREFIX + "validators.n3",
                ABOX_PREFIX + "user_groups.n3",
                ABOX_PREFIX + "implementation_types.n3",
                ABOX_PREFIX + "rdf_types.n3",
                ABOX_PREFIX + "validators.n3",
                ABOX_PREFIX + "model_parameters.n3",
                ABOX_PREFIX + "serialization_types.n3"
        };
    }

    protected String[] getFileList(String path) {
        File dir = new File(path);
        File[] files = dir.listFiles();
        List<String> paths = new LinkedList<String>();
        for (File file : files) {
            paths.add(file.getAbsolutePath());
        }
        return paths.toArray(new String[0]);
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
            new RDFFile("N3", TEST_PREFIX + "dynamic-api-individuals-person1_1.n3")
        );
    }

    protected void loadPersonVersion2Model() throws IOException {
        // versioning action reuses testSparqlQuery1 from testing action
        loadModel(
            new RDFFile("N3", TEST_PREFIX + "dynamic-api-individuals-person2.n3")
        );
    }

    protected void loadPersonVersion4_3_7Model() throws IOException {
        // versioning action reuses testSparqlQuery1 from testing action
        loadModel(
            new RDFFile("N3", TEST_PREFIX + "dynamic-api-individuals-person4_3_7.n3")
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
