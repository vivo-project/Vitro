package edu.cornell.mannlib.vitro.webapp.dynapi.components.conditions;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.StringData;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.impl.ContextModelAccessImpl;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;
import stubs.javax.servlet.ServletContextStub;

@RunWith(Parameterized.class)
public class ConditionTest extends ServletContextTest{

    private static final String RESOURCES_PATH = "src/test/resources/edu/cornell/mannlib/vitro/webapp/dynapi/components/conditions/";
    private static final String TEST_ACTION = RESOURCES_PATH + "conditions-test-action.n3";
    private static final String TEST_STORE = RESOURCES_PATH + "conditions-test-store.n3";

    
    private static MockedStatic<ModelAccess> modelAccess;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    ContextModelAccessImpl contextModelAccess;
    
    @Mock
    ServletContext servletContext;
  
    OntModel storeModel;
    
    @Parameter(0)
    public String input;
    
    @Parameter(1)
    public String result;
    
    @Before
    public void beforeEach() {
        MockitoAnnotations.openMocks(this);
        when(ModelAccess.on(any(ServletContext.class))).thenReturn(contextModelAccess);
        storeModel = new OntModelImpl(OntModelSpec.OWL_MEM);
        when(contextModelAccess.getOntModel(any(String.class))).thenReturn(storeModel);
    }
    
    @BeforeClass
    public static void beforeClass() {
        modelAccess = mockStatic(ModelAccess.class);
    }
    
    @AfterClass
    public static void after() {
        modelAccess.close();
    }
    
    @Test
    public void test() throws ConfigurationBeanLoaderException, IOException {
        loadOntology(ontModel);
        loadModel(ontModel, TEST_ACTION);
        loadModel(storeModel, TEST_STORE);
        servletContext = new ServletContextStub();
        when(request.getServletContext()).thenReturn(servletContext);
        Action action = loader.loadInstance("test:action", Action.class);
        Map<String, String[]> params = new HashMap<String, String[]>();
        when(request.getParameterMap()).thenReturn(params);
        OperationData data = new OperationData(request);
        
        data.add("input_param", new StringData(input));
        OperationResult opResult = action.run(data);
        assertFalse(opResult.hasError());
        assertEquals(result, data.getData("0.label").toString());
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
        loadModel(ontModel, "../home/src/main/resources/rdf/tbox/filegraph/dynamic-api-implementation.n3");
        loadModel(ontModel, "../home/src/main/resources/rdf/abox/filegraph/dynamic-api-individuals.n3");
    }

}
