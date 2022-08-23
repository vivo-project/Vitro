package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import edu.cornell.mannlib.vitro.webapp.dynapi.ParameterUtils;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;

@RunWith(Parameterized.class)
public class ModelWriterIntegrationTest extends ServletContextTest {
	private static final String TARGET_NAME = "target";
	private static final String ADDITIONS_NAME = "addition";
	private static final String RETRACTIONS_NAME = "retraction";
	private static final String RESOURCES_PATH = "src/test/resources/edu/cornell/mannlib/vitro/webapp/dynapi/components/";
    private static final String TEST_ACTION = RESOURCES_PATH + "modelwriter-test-action.n3";
    private static final String TEST_STORE = RESOURCES_PATH + "modelwriter-test-store.n3";

    
    Model storeModel;
    
    @org.junit.runners.Parameterized.Parameter(0)
    public String additionLiteral;
    
    @org.junit.runners.Parameterized.Parameter(1)
    public String retractionLiteral;
    
    @org.junit.runners.Parameterized.Parameter(2)
    public String size;
    
    @Before
    public void beforeEach() {
        storeModel = new OntModelImpl(OntModelSpec.OWL_MEM);
    }
    
    @Test
    public void test() throws ConfigurationBeanLoaderException, IOException, ConversionException {
        loadOntology(ontModel);
        loadModel(ontModel, TEST_ACTION);
        loadModel(storeModel, TEST_STORE);
       // servletContext = new ServletContextStub();
        Action action = loader.loadInstance("test:action", Action.class);
        assertTrue(action.isValid());
        Parameters parameters = action.getInputParams();

        DataStore store = new DataStore();

        OntModelImpl additionModel = new OntModelImpl(OntModelSpec.OWL_DL_MEM);
        if(!StringUtils.isBlank(additionLiteral)){
        	ParameterUtils.addStatement(additionModel, "test:uri1", "test:property", additionLiteral);
        }

        Data additionsData = new Data(parameters.get(ADDITIONS_NAME));
        TestView.setObject(additionsData, additionModel);
        store.addData(ADDITIONS_NAME, additionsData);
        
        
        OntModelImpl retractionModel = new OntModelImpl(OntModelSpec.OWL_DL_MEM);
        
        if(!StringUtils.isBlank(retractionLiteral)){
        	ParameterUtils.addStatement(retractionModel, "test:uri1", "test:property", retractionLiteral);
        }
        	
        Data retractionData = new Data(parameters.get(RETRACTIONS_NAME));
        TestView.setObject(retractionData, retractionModel);
        store.addData(RETRACTIONS_NAME, retractionData);
        
        Data targetData = new Data(parameters.get(TARGET_NAME));
        TestView.setObject(targetData, storeModel);
        store.addData(TARGET_NAME, targetData);
        
        //data.add("input_param", new StringData(input));
        assertTrue(storeModel.size() == 1);
        OperationResult opResult = action.run(store);
        assertFalse(opResult.hasError());
        assertEquals(Integer.parseInt(size), storeModel.size());
    }
    
    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
            { "", "literal1", "0"},
            { "", "literal2", "1"},
            { "literal2", "literal2", "1" },
            { "literal2", "", "2" },
            { " ", "literal2", "1" }
        });
    }
    
    protected void loadModel(Model model, String... files) throws IOException {
        for (String file : files) {
            String rdf = readFile(file);
            model.read(new StringReader(rdf), null, "n3");
        }
    }
    
    public void loadOntology(OntModel ontModel) throws IOException {
        loadModel(ontModel, "../home/src/main/resources/rdf/tbox/filegraph/dynamic-api-implementation.n3");
        loadModel(ontModel, "../home/src/main/resources/rdf/abox/filegraph/dynamic-api-individuals.n3");
    }
}
