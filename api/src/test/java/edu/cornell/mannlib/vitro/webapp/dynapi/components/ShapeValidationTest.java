package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.io.StringReader;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;

import edu.cornell.mannlib.vitro.webapp.dynapi.ProcedurePool;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.Converter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.DynapiModelFactory;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.BooleanParam;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.ModelParam;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.StringParam;

public class ShapeValidationTest extends ServletContextTest {

    private static final String DYNAPI_ABOX_URI = "http://vitro.mannlib.cornell.edu/default/dynamic-api-abox";
    private static final String SHAPES_MODEL_URI = "http://vitro.mannlib.cornell.edu/default/shapes";
    public static final String SHAPES_PREFIX = "../home/src/main/resources/rdf/shapes/everytime/";
    private static final String SHAPES_MODEL_FILE = SHAPES_PREFIX + "dynamic_api_shapes.n3";
    
    private static MockedStatic<DynapiModelFactory> dynapiModelFactory;
    OntModel shapesModel = ModelFactory.createOntologyModel();
    
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
        shapesModel = new OntModelImpl(OntModelSpec.OWL_MEM);
        dynapiModelFactory.when(() -> DynapiModelFactory.getModel(eq(DYNAPI_ABOX_URI))).thenReturn(ontModel);
        dynapiModelFactory.when(() -> DynapiModelFactory.getModel(eq(SHAPES_MODEL_URI))).thenReturn(shapesModel);
    }
    private ProcedurePool initWithDefaultModel() throws IOException {
        loadOntology(ontModel);
        ProcedurePool procedurePool = ProcedurePool.getInstance();
        procedurePool.init(servletContext);
        return procedurePool;
    }
    
    @Test
    public void validationTest() throws InitializationException, ConversionException, IOException {
        
        boolean manualDebugging = false;
        loadOntology(ontModel);

        //ProcedurePool pool = initWithDefaultModel();
        
        ShapeValidation sv = new ShapeValidation();
        Parameter dataModelParam = new ModelParam(DYNAPI_ABOX_URI, true, true);
        Parameter shapesModelParam = new ModelParam(SHAPES_MODEL_URI, true, true);
        Parameter reportParam = new StringParam("report");
        Parameter resultParam = new BooleanParam("result");
        sv.setDataModel(dataModelParam);
        sv.setShapesModel(shapesModelParam);
        sv.addOutputParam(reportParam);
        sv.addOutputParam(resultParam);
        sv.setDetails(true);
        sv.setCache(true);
        sv.setValidateShapes(false);
        DataStore dataStore = new DataStore();
        Converter.convertInternalParams(sv.getInputParams(), dataStore);
        assertTrue(OperationResult.ok().equals(sv.run(dataStore)));
        assertTrue(dataStore.contains("report"));
        assertTrue(dataStore.contains("result"));
        if (manualDebugging) {
            String report = (String) TestView.getObject(dataStore.getData("report"));
            System.out.println(report);
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
        loadModel(ontModel, getFileList(TBOX_PREFIX));
        loadModel(shapesModel, SHAPES_MODEL_FILE);
    }
}
