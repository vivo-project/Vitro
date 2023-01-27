package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import edu.cornell.mannlib.vitro.webapp.dynapi.ProcedurePool;
import edu.cornell.mannlib.vitro.webapp.dynapi.Endpoint;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.Converter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer.Type;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.IntegerParam;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.JsonContainerObjectParam;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;

@RunWith(Parameterized.class)
public class ReportEndpointIntegrationTest extends ServletContextTest {

	private static final String OUTPUT_CONTAINER = "output_container";
    private static final String RESOURCES_PATH = "src/test/resources/edu/cornell/mannlib/vitro/webapp/dynapi/components/";
	private static final String TEST_ACTION = RESOURCES_PATH + "loop-operation-test-action.n3";
	private static final String REPORT_ENDPOINT_FILE_NAME = "endpoint_procedure_create_report.n3"; 

	Model storeModel;

	@org.junit.runners.Parameterized.Parameter(0)
	public List<Integer> inputValues;
	
	@org.junit.runners.Parameterized.Parameter(1)
	public String expectedValues;

    @AfterClass
    public static void after() {
        //restoreLogs();
    }
    
    @BeforeClass
    public static void before() {
        //offLogs();
    }

	@Before
	public void beforeEach() {
		storeModel = new OntModelImpl(OntModelSpec.OWL_MEM);
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
        ProcedurePool rpcPool = ProcedurePool.getInstance();
        rpcPool.init(servletContext);
        return rpcPool;
    }
    
    @Test
    public void test() throws ConfigurationBeanLoaderException, IOException, ConversionException, InitializationException {
        loadModel(ontModel, TEST_ACTION);
        ProcedurePool procedurePool = initWithDefaultModel();
        System.out.println(procedurePool.count());
        Procedure procedure = null;
        DataStore store = null;
        try { 
            procedure = procedurePool.getByUri("https://vivoweb.org/procedure/create_report_generator");
            assertFalse(procedure instanceof NullProcedure);
            assertTrue(procedure.isValid());
            Parameters inputs = procedure.getInputParams();
            Parameters outputs = procedure.getOutputParams();
            Parameters internal = procedure.getInternalParams();
            System.out.println(inputs.getNames());
            System.out.println(internal.getNames());
            System.out.println(outputs.getNames());
            store = new DataStore();
            Converter.convertInternalParams(internal, store);
            //addInputContainer(store);

            Endpoint.getDependencies(procedure, store, procedurePool);
            assertTrue(OperationResult.ok().equals(procedure.run(store)));
            Data modelData = store.getData("report_generator_configuration_graph");
            Model model = (Model) TestView.getObject(modelData);
            model.write(System.out,"n3");
            //assertTrue(store.contains(OUTPUT_CONTAINER));
            //Data output = store.getData(OUTPUT_CONTAINER);
            //assertEquals(expectedValues, output.getSerializedValue());
             
        } finally {
            if (procedure != null) {
                procedure.removeClient();    
            }
            if (store != null) {
                store.removeDependencies();    
            }
        }
    }

    private void addInputContainer(DataStore store) {
        String containerParamName = "input_container";
        Parameter container = new JsonContainerObjectParam(containerParamName);
        Data containerData = createContainer(container);
        store.addData(containerParamName, containerData);
    }
    
    private Data createContainer(Parameter containerParam) {
        Data containerData = new Data(containerParam);
        JsonContainer container = new JsonContainer(Type.ARRAY);
        String expectedOutputParamName = "item";
        Parameter expectedOutputParam = new IntegerParam(expectedOutputParamName);
        for (Integer value : inputValues) {
            addValue(container, expectedOutputParam, value);
        }
        TestView.setObject(containerData, container);
        return containerData;
    }

    private void addValue(JsonContainer container, Parameter expectedOutputParam, Integer value) {
        Data data = new Data(expectedOutputParam);
        TestView.setObject(data, value);
        container.addValue(data);
    }

	@Parameterized.Parameters
	public static Collection<Object[]> requests() {
		return Arrays.asList(new Object[][] {
		    {Arrays.asList(20,30,40,50,60), "[\"30\",\"40\",\"50\",\"60\",\"70\"]"},
		});
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
	}
}
