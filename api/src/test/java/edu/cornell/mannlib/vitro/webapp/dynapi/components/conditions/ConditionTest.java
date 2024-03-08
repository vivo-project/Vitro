package edu.cornell.mannlib.vitro.webapp.dynapi.components.conditions;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.MockitoAnnotations;

import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.StringView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;
import stubs.javax.servlet.ServletContextStub;

@RunWith(Parameterized.class)
public class ConditionTest extends ServletContextTest{

    private static final String MODEL_PARAM = "FULL_UNION";
	private static final String INPUT_PARAM = "input_param";
	private static final String RESOURCES_PATH = "src/test/resources/edu/cornell/mannlib/vitro/webapp/dynapi/components/conditions/";
    private static final String TEST_ACTION = RESOURCES_PATH + "conditions-test-action.n3";
    private static final String TEST_STORE = RESOURCES_PATH + "conditions-test-store.n3";

    
    Model storeModel;
    
    @org.junit.runners.Parameterized.Parameter(0)
    public String input;
    
    @org.junit.runners.Parameterized.Parameter(1)
    public String result;
    
    @Before
    public void beforeEach() {
        MockitoAnnotations.openMocks(this);
        storeModel = new OntModelImpl(OntModelSpec.OWL_MEM);
    }
    
    @Test
    public void test() throws ConfigurationBeanLoaderException, IOException, ConversionException {
        loadOntology(ontModel);
        loadModel(ontModel, TEST_ACTION);
        loadModel(storeModel, TEST_STORE);
        servletContext = new ServletContextStub();
        Action action = loader.loadInstance("test:action", Action.class);
        assertTrue(action.isValid());
        Parameters parameters = action.getInputParams();
        DataStore store = new DataStore();
        Data inputData = new Data(parameters.get(INPUT_PARAM));
        inputData.setRawString(input);
        inputData.earlyInitialization();
        store.addData(INPUT_PARAM, inputData);
        Data modelData = new Data(action.getInternalParams().get(MODEL_PARAM));
        TestView.setObject(modelData, storeModel);
        
        store.addData(MODEL_PARAM, modelData);
        //data.add("input_param", new StringData(input));
        OperationResult opResult = action.run(store);
        assertFalse(opResult.hasError());
        assertEquals(result, StringView.getFirstStringValue(store, "label"));
    }
    
    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
            { "", "label3@en-US"},
            { "text", "label2@en-US"},
            { " ", "label3@en-US"},
            { " ", "label3@en-US"}
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
        loadModel(ontModel, INDIVIDUALS_FILE_PATH);
    }

}
