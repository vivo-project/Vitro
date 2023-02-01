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
	private static final String CREATE_REPORT_GENERATOR_PROCEDURE = "endpoint_procedure_create_report_generator.n3";
	private static final String EXECUTE_REPORT_GENERATOR_PROCEDURE = "endpoint_procedure_execute_report_generator.n3";
    private static final String DELETE_REPORT_GENERATOR_PROCEDURE = "endpoint_procedure_delete_report_generator.n3";
    private static final String LIST_REPORT_GENERATORS_PROCEDURE = "endpoint_procedure_list_report_generators.n3";
    private static final String GET_REPORT_GENERATOR_PROCEDURE = "endpoint_procedure_get_report_generator.n3"; 

	private static final String REPORT_ENDPOINT_INPUT = RESOURCES_PATH + "endpoint_procedure_create_report_generator_input_new.n3";
	private static final String REPORT_ENDPOINT_DATA = RESOURCES_PATH + "endpoint_procedure_create_report_generator_demo_data.n3" ; 

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
        long initialModelSize ;
        long initialProcedureCount ;
        Model generatorConfiguration ;
        
        boolean manualDebugging = false;
        
        try(Procedure procedure = procedurePool.getByUri("https://vivoweb.org/procedure/create_report_generator")) { 
            assertFalse(procedure instanceof NullProcedure);
            assertTrue(procedure.isValid());
            initialModelSize = ontModel.size();
            initialProcedureCount = procedurePool.count();
            Parameters internal = procedure.getInternalParams();
            store = new DataStore();
            Converter.convertInternalParams(internal, store);
            Endpoint.getDependencies(procedure, store, procedurePool);
            assertTrue(OperationResult.ok().equals(procedure.run(store)));
            assertTrue(ontModel.size() > initialModelSize);
            assertTrue(procedurePool.count() > initialProcedureCount);
            
            Data modelData = store.getData("report_generator_configuration_graph");
            generatorConfiguration = (Model) TestView.getObject(modelData);
            assertFalse(generatorConfiguration.isEmpty());
            if (manualDebugging) {
                File file = new File(RESOURCES_PATH + "create-report-generator-integration-test-report-generator.n3");
                FileWriter fw = new FileWriter(file);
                generatorConfiguration.write(fw, "n3");
            }
        } finally {
            if (store != null) {
                store.removeDependencies();    
            }
        }
        
        DataStore reportStore = new DataStore() ;
        Data uriData = store.getData("report_generator_uri");
        assertTrue(uriData != null);
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
        DataStore listReportStore = new DataStore() ;
        listReportStore.addData(uriData.getParam().getName(), uriData);

        try(Procedure listReportGenerators = procedurePool.getByUri("https://vivoweb.org/procedure/list_report_generators");){
            Parameters internalParams = listReportGenerators.getInternalParams();
            Converter.convertInternalParams(internalParams, listReportStore);
            assertTrue(OperationResult.ok().equals(listReportGenerators.run(listReportStore)));
            Data reportsData = listReportStore.getData("reports");
            String reports = reportsData.getSerializedValue();
            assertTrue(reports.contains(uriData.getSerializedValue()));
        }
        
        DataStore getReportStore = new DataStore() ;
        getReportStore.addData(uriData.getParam().getName(), uriData);

        try(Procedure getReportGenerator = procedurePool.getByUri("https://vivoweb.org/procedure/get_report_generator");){
            Parameters internalParams = getReportGenerator.getInternalParams();
            Converter.convertInternalParams(internalParams, getReportStore);
            assertTrue(OperationResult.ok().equals(getReportGenerator.run(getReportStore)));
            Data reportData = getReportStore.getData("result");
            String report = reportData.getSerializedValue();
            assertTrue(report.contains(uriData.getSerializedValue()));
            assertTrue(report.contains("selectQuery"));
            assertTrue(report.contains("constructQuery"));
            assertTrue(report.contains("template"));
        }
        
        DataStore deleteReportStore = new DataStore() ;
        deleteReportStore.addData(uriData.getParam().getName(), uriData);
        try(Procedure deleteReportGenerator = procedurePool.getByUri("https://vivoweb.org/procedure/delete_report_generator");){
            Parameters internalParams = deleteReportGenerator.getInternalParams();
            Converter.convertInternalParams(internalParams, deleteReportStore);
            assertTrue(OperationResult.ok().equals(deleteReportGenerator.run(deleteReportStore)));
            Data removeData = deleteReportStore.getData("report_generator_configuration_graph");
            Model removeModel = (Model) TestView.getObject(removeData);
            Model notRemoved = generatorConfiguration.difference(removeModel);
            Model excessivelyRemoved = removeModel.difference(generatorConfiguration);
            if (manualDebugging) {
                System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++" + excessivelyRemoved.size());
                excessivelyRemoved.write(System.out,"n3");
                System.out.println("------------------------------------------------------------" + notRemoved.size());
                notRemoved.write(System.out,"n3");
            }
            assertTrue(notRemoved.isEmpty());
            assertTrue(excessivelyRemoved.isEmpty());
            assertTrue(ontModel.size() == initialModelSize);
            assertTrue(procedurePool.count() == initialProcedureCount);
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
        loadModel(ontModel, ABOX_PREFIX + CREATE_REPORT_GENERATOR_PROCEDURE);
        loadModel(ontModel, ABOX_PREFIX + EXECUTE_REPORT_GENERATOR_PROCEDURE);
        loadModel(ontModel, ABOX_PREFIX + DELETE_REPORT_GENERATOR_PROCEDURE);
        loadModel(ontModel, ABOX_PREFIX + LIST_REPORT_GENERATORS_PROCEDURE);
        loadModel(ontModel, ABOX_PREFIX + GET_REPORT_GENERATOR_PROCEDURE);

        loadModel(ontModel, REPORT_ENDPOINT_INPUT);
        loadModel(storeModel, REPORT_ENDPOINT_DATA);
	}
}
