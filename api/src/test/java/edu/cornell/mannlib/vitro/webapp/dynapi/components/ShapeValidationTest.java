/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.dynapi.AbstractTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.LoggingControl;
import edu.cornell.mannlib.vitro.webapp.dynapi.ShapesGraphPool;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.operations.ShapeValidation;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.Converter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.DynapiModelFactory;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.BooleanParam;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.ModelParam;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.StringParam;
import org.apache.commons.lang3.ArrayUtils;
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.MockedStatic;

@RunWith(Parameterized.class)
public class ShapeValidationTest extends AbstractTest {

    private static final String RESULT_PARAM_NAME = "result";
    private static final String REPORT_PARAM_NAME = "report";
    private static final String PROCEDURE_PROVIDES_SHAPES_URI = "test:procedure-provides-shapes";

    // private static final String TEST_RESOURCES_PATH =
    // "src/test/resources/edu/cornell/mannlib/vitro/webapp/dynapi/components/";

    private static final String DYNAPI_ABOX_URI = "http://vitro.mannlib.cornell.edu/default/dynamic-api-abox";
    private static final String SHAPES_MODEL_URI = "http://vitro.mannlib.cornell.edu/default/shapes";
    public static final String SHAPES_PREFIX = "../home/src/main/resources/rdf/shapes/everytime/";
    private static final String SHAPES_MODEL_FILE = SHAPES_PREFIX + "dynamic_api_shapes.n3";
    private static final List<String> shapeFile = Arrays.asList(new String[] { SHAPES_MODEL_FILE });

    @org.junit.runners.Parameterized.Parameter(0)
    public List<String> shapeFiles;

    @org.junit.runners.Parameterized.Parameter(1)
    public List<String> modelFiles;

    @org.junit.runners.Parameterized.Parameter(2)
    public boolean cache;

    @org.junit.runners.Parameterized.Parameter(3)
    public boolean validateShapes;

    @org.junit.runners.Parameterized.Parameter(4)
    public boolean details;

    static List<String> dynapiFiles = Arrays.asList(ArrayUtils.addAll(ArrayUtils.addAll(getFileList(ABOX_PREFIX),
            getFileList(TBOX_PREFIX)), new String[] { IMPLEMENTATION_FILE_PATH, ONTOLOGY_FILE_PATH }));

    boolean manualDebugging = false;

    private static MockedStatic<DynapiModelFactory> dynapiModelFactory;
    OntModel shapesModel = ModelFactory.createOntologyModel();

    @After
    public void reset() {
        LoggingControl.restoreLogs();
    }

    @AfterClass
    public static void after() {
        dynapiModelFactory.close();
    }

    @BeforeClass
    public static void before() {
        dynapiModelFactory = mockStatic(DynapiModelFactory.class);
    }

    @Before
    public void beforeEach() {
        LoggingControl.offLogs();
        shapesModel = new OntModelImpl(OntModelSpec.OWL_MEM);
        dynapiModelFactory.when(() -> DynapiModelFactory.getModel(eq(DYNAPI_ABOX_URI))).thenReturn(ontModel);
        dynapiModelFactory.when(() -> DynapiModelFactory.getModel(eq(SHAPES_MODEL_URI))).thenReturn(shapesModel);
        assertEquals(0, ShapesGraphPool.getInstance().count());
    }

    @Test
    public void validationWithProvidedShapes() throws InitializationException, ConversionException, IOException {
        loadOntology(ontModel);
        ShapeValidation sv = new ShapeValidation();
        Parameter dataModelParam = new ModelParam(DYNAPI_ABOX_URI, true, true);
        Parameter shapesModelParam = new ModelParam(SHAPES_MODEL_URI, true, true);
        Parameter reportParam = new StringParam(REPORT_PARAM_NAME);
        Parameter resultParam = new BooleanParam(RESULT_PARAM_NAME);
        sv.setDataModel(dataModelParam);
        sv.setShapesModel(shapesModelParam);
        sv.addOutputParam(reportParam);
        sv.addOutputParam(resultParam);
        sv.setDetails(details);
        sv.setCache(cache);
        sv.setValidateShapes(validateShapes);
        DataStore dataStore = new DataStore();
        Converter.convertInternalParams(sv.getInputParams(), dataStore);
        assertTrue(OperationResult.ok().equals(sv.run(dataStore)));
        assertTrue(dataStore.contains(REPORT_PARAM_NAME));
        assertTrue(dataStore.contains(RESULT_PARAM_NAME));
        if (manualDebugging) {
            String report = (String) TestView.getObject(dataStore.getData(REPORT_PARAM_NAME));
            System.out.println(report);
        }
        if (cache) {
            assertEquals(1, ShapesGraphPool.getInstance().count());
        } else {
            assertEquals(0, ShapesGraphPool.getInstance().count());
        }
        ShapesGraphPool.getInstance().clear();
    }

    @Test
    public void validationWithProcedureInvocation() throws InitializationException, ConversionException, IOException {
        loadOntology(ontModel);
        ShapeValidation sv = new ShapeValidation();
        Parameter dataModelParam = new ModelParam(DYNAPI_ABOX_URI, true, true);
        Parameter shapesModelParam = new ModelParam(SHAPES_MODEL_URI, true, true);
        ProcedureDescriptor pd = new ProcedureDescriptor();
        pd.addOutputParameter(shapesModelParam);
        pd.setCallUri(PROCEDURE_PROVIDES_SHAPES_URI);
        sv.setShapesProcedureDescriptor(pd);
        Parameter reportParam = new StringParam(REPORT_PARAM_NAME);
        Parameter resultParam = new BooleanParam(RESULT_PARAM_NAME);
        sv.setDataModel(dataModelParam);
        sv.addOutputParam(reportParam);
        sv.addOutputParam(resultParam);
        sv.setDetails(details);
        sv.setCache(cache);
        sv.setValidateShapes(validateShapes);
        DataStore dataStore = new DataStore();
        Converter.convertInternalParams(sv.getInputParams(), dataStore);
        Procedure shapesProcedure = new Procedure();
        shapesProcedure.setPublicAccess(true);
        shapesProcedure.setUri(PROCEDURE_PROVIDES_SHAPES_URI);
        shapesProcedure.addProvidedParameter(shapesModelParam);
        assertTrue(shapesProcedure.isValid());
        dataStore.putDependency(PROCEDURE_PROVIDES_SHAPES_URI, shapesProcedure);
        assertTrue(OperationResult.ok().equals(sv.run(dataStore)));
        assertTrue(dataStore.contains(REPORT_PARAM_NAME));
        assertTrue(dataStore.contains(RESULT_PARAM_NAME));
        if (manualDebugging) {
            String report = (String) TestView.getObject(dataStore.getData(REPORT_PARAM_NAME));
            System.out.println(report);
        }
        if (cache) {
            assertEquals(1, ShapesGraphPool.getInstance().count());
        } else {
            assertEquals(0, ShapesGraphPool.getInstance().count());
        }
        ShapesGraphPool.getInstance().clear();
    }

    protected void loadModel(Model model, String... files) throws IOException {
        for (String file : files) {
            String rdf = readFile(file);
            model.read(new StringReader(rdf), null, "n3");
        }
    }

    public void loadOntology(OntModel ontModel) throws IOException {
        for (String filePath : modelFiles) {
            loadModel(ontModel, filePath);
        }
        loadModel(ontModel, getFileList(ABOX_PREFIX));
        loadModel(ontModel, getFileList(TBOX_PREFIX));
        for (String filePath : shapeFiles) {
            loadModel(shapesModel, filePath);
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
                // Shape files, data files, cache, shape validation, details
                { shapeFile, dynapiFiles, true, true, true },
                { shapeFile, dynapiFiles, false, true, true },
                { shapeFile, dynapiFiles, true, false, true },
                { shapeFile, dynapiFiles, false, false, true },
                { shapeFile, dynapiFiles, true, false, false },
                { shapeFile, dynapiFiles, false, false, false }, });
    }
}
