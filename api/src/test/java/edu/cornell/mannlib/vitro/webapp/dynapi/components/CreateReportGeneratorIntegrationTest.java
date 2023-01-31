package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Base64;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;

import edu.cornell.mannlib.vitro.webapp.dynapi.Endpoint;
import edu.cornell.mannlib.vitro.webapp.dynapi.ProcedurePool;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.Converter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.DynapiModelFactory;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;

public class CreateReportGeneratorIntegrationTest extends ServletContextTest {

    private static final String RESOURCES_PATH = "src/test/resources/edu/cornell/mannlib/vitro/webapp/dynapi/components/";
	private static final String CREATE_REPORT_ENDPOINT = "endpoint_procedure_create_report_generator.n3";
	private static final String EXECUTE_REPORT_ENDPOINT = "endpoint_procedure_execute_report_generator.n3"; 
	private static final String REPORT_ENDPOINT_INPUT_FILE = RESOURCES_PATH + "endpoint_procedure_create_report_generator_input_new.n3";
	private static final String REPORT_ENDPOINT_DATA_FILE = RESOURCES_PATH + "endpoint_procedure_create_report_generator_demo_data.n3" ; 

	private static MockedStatic<DynapiModelFactory> dynapiModelFactory;

	OntModel storeModel = ModelFactory.createOntologyModel();

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
        dynapiModelFactory.when(() -> DynapiModelFactory.getModel(eq("http://vitro.mannlib.cornell.edu/default/dynamic-api-abox"))).thenReturn(ontModel);
        dynapiModelFactory.when(() -> DynapiModelFactory.getModel(eq("vitro:jenaOntModel"))).thenReturn(storeModel);

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
        DataStore store = null;
        
        boolean manualDebugging = false;
        
        try(Procedure procedure = procedurePool.getByUri("https://vivoweb.org/procedure/create_report_generator")) { 
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
            
            if (manualDebugging) {
                Data modelData = store.getData("report_generator_configuration_graph");
                Model model = (Model) TestView.getObject(modelData);
                File file = new File(RESOURCES_PATH + "create-report-generator-integration-test-report-generator.n3");
                FileWriter fw = new FileWriter(file);
                model.write(fw, "n3");
            }
            DataStore reportStore = new DataStore() ;
            Data uriData = store.getData("report_generator_uri");
            reportStore.addData(uriData.getParam().getName(), uriData);
            try(Procedure reportGenerator = procedurePool.getByUri("https://vivoweb.org/procedure/execute_report_generator");){
                Parameters reportInternalParams = reportGenerator.getInternalParams();
                Converter.convertInternalParams(reportInternalParams, reportStore);
                assertTrue(OperationResult.ok().equals(reportGenerator.run(reportStore)));
                Data reportData = reportStore.getData("report");
                String base64EncodedReport = reportData.getSerializedValue();
                assertFalse(base64EncodedReport.isEmpty());
                if (manualDebugging) {
                    byte[] reportBytes = Base64.getDecoder().decode(base64EncodedReport);
                    File file = new File(RESOURCES_PATH + "create-report-generator-integration-test-report.xlsx");
                    try (OutputStream os = new FileOutputStream(file)) {
                        os.write(reportBytes);
                    }   
                }
            }

        } finally {
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
        loadModel(ontModel, ABOX_PREFIX + CREATE_REPORT_ENDPOINT);
        loadModel(ontModel, ABOX_PREFIX + EXECUTE_REPORT_ENDPOINT);
        loadModel(ontModel, REPORT_ENDPOINT_INPUT_FILE);
        loadModel(storeModel, REPORT_ENDPOINT_DATA_FILE);
	}
}
