package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.io.StringReader;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;

import edu.cornell.mannlib.vitro.webapp.dynapi.Endpoint;
import edu.cornell.mannlib.vitro.webapp.dynapi.ProcedurePool;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.Converter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.DynapiModelFactory;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;

public class CreateReportGeneratorIntegrationTest extends ServletContextTest {

    private static final String RESOURCES_PATH = "src/test/resources/edu/cornell/mannlib/vitro/webapp/dynapi/components/";
	private static final String REPORT_ENDPOINT_FILE_NAME = "endpoint_procedure_create_report_generator.n3"; 
	private static final String REPORT_ENDPOINT_INPUT_FILE_NAME = RESOURCES_PATH + "endpoint_procedure_create_report_generator_input.n3";

	private static MockedStatic<DynapiModelFactory> dynapiModelFactory;

	Model storeModel;

    @AfterClass
    public static void after() {
        restoreLogs();
        dynapiModelFactory.close();
    }
    
    @BeforeClass
    public static void before() {
        offLogs();
        dynapiModelFactory = mockStatic(DynapiModelFactory.class);

    }

	@Before
	public void beforeEach() {
		storeModel = new OntModelImpl(OntModelSpec.OWL_MEM);
        dynapiModelFactory.when(() -> DynapiModelFactory.getModel(any(String.class))).thenReturn(ontModel);
	}
	
    @After
    public void reset() {
        setup();
        ProcedurePool procedurePool = ProcedurePool.getInstance();
        procedurePool.init(servletContext);
        procedurePool.reload();
        assertEquals(0, procedurePool.count());
    }

    private ProcedurePool initWithDefaultModel() throws IOException {
        loadOntology(ontModel);
        ProcedurePool procedurePool = ProcedurePool.getInstance();
        procedurePool.init(servletContext);
        return procedurePool;
    }
    
    @Test
    public void test() throws ConfigurationBeanLoaderException, IOException, ConversionException, InitializationException {
        ProcedurePool procedurePool = initWithDefaultModel();
        Procedure procedure = null;
        DataStore store = null;
        try { 
            procedure = procedurePool.getByUri("https://vivoweb.org/procedure/create_report_generator");
            assertFalse(procedure instanceof NullProcedure);
            assertTrue(procedure.isValid());
            long initialModelSize = ontModel.size();
            long initialProcedureCount = procedurePool.count();
            Parameters internal = procedure.getInternalParams();
            store = new DataStore();
            Converter.convertInternalParams(internal, store);
            Endpoint.getDependencies(procedure, store, procedurePool);
            assertTrue(OperationResult.ok().equals(procedure.run(store)));
            assertTrue(ontModel.size() > initialModelSize);
            assertTrue(procedurePool.count() > initialProcedureCount);
             
        } finally {
            if (procedure != null) {
                procedure.removeClient();    
            }
            if (store != null) {
                store.removeDependencies();    
            }
        }
    }

	protected void loadModel(Model model, String... files) throws IOException {
		for (String file : files) {
			String rdf = readFile(file);
			model.read(new StringReader(rdf), null, "n3");
		}
	}

	public void loadOntology(OntModel ontModel) throws IOException {
        loadModel(ontModel, IMPLEMENTATION_FILE_PATH);
        loadModel(ontModel, ONTOLOGY_FILE_PATH);
        loadModel(ontModel, getFileList(ABOX_PREFIX));
        loadModel(ontModel, ABOX_PREFIX + REPORT_ENDPOINT_FILE_NAME);
        loadModel(ontModel, REPORT_ENDPOINT_INPUT_FILE_NAME);

	}
}
