package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import edu.cornell.mannlib.vitro.webapp.dynapi.ParameterUtils;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.PrimitiveSerializationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.SerializationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.DynapiModelFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.impl.ContextModelAccessImpl;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;

import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletContext;
import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class N3TemplateTest extends ServletContextTest {

    private final static String TEST_DATA_PATH = TEST_PREFIX + "dynamic-api-individuals-n3template-test.n3";
    private final static String TEST_N3TEMPLATE_URI="https://vivoweb.org/ontology/vitro-dynamic-api/N3Template/testN3Template";
    private final static String MODEL_PATH="https://vivoweb.org/ontology/vitro-dynamic-api/model/full_union";

    public static SerializationType anyURI;
    public static SerializationType stringType;
    static SerializationType booleanType;

    DataStore dataStore;

    @Mock
    ContextModelAccessImpl contextModelAccess;

    @Mock
    ServletContext servletContext;

    OntModel writeModel;

    N3Template n3Template;

    private static MockedStatic<DynapiModelFactory> dynapiModelFactory;

    @AfterClass
    public static void after() {
    	dynapiModelFactory.close();    		
    }
    
    @BeforeClass
    public static void setupStaticObjects(){
        anyURI = new PrimitiveSerializationType();
        anyURI.setName("anyURI");
        stringType = new PrimitiveSerializationType();
        stringType.setName("string");
        booleanType = new PrimitiveSerializationType();
        booleanType.setName("boolean");
    	dynapiModelFactory = mockStatic(DynapiModelFactory.class);
    }
    
    @Before
    public void setupTemplate(){
        writeModel = new OntModelImpl(OntModelSpec.OWL_DL_MEM);
        dynapiModelFactory.when(() -> DynapiModelFactory.getModel(any(String.class))).thenReturn(writeModel);
        this.n3Template = new N3Template();
        dataStore = new DataStore();
    }

    @Test
    public void testLoadingAndPropertiesSetup() throws IOException, ConfigurationBeanLoaderException {
        loadDefaultModel();
        loadModels(TEST_DATA_PATH.split("\\.")[1],TEST_DATA_PATH);

        N3Template n3Template = loader.loadInstance(TEST_N3TEMPLATE_URI, N3Template.class);
        assertNotNull(n3Template);
        assertEquals(0, n3Template.getOutputParams().size());
        assertEquals(4, n3Template.getInputParams().size());
        assertEquals("?testSubject <http://has> ?testObject. ?testSubject2 <http://has> ?testObject", n3Template.getN3TextAdditions());
        assertEquals("?testSubject <http://has> ?testObject", n3Template.getN3TextRetractions());
    }

    @Test
    public void testNotAllN3VariablesSubstitutedWithValues() throws Exception{
        n3Template.setN3TextRetractions("?uri1 <http:has> ?literal1");
        loadDefaultModel();
        Parameter model = loader.loadInstance(MODEL_PATH, Parameter.class);
        n3Template.setTemplateModel(model);
        addModel(n3Template);
        Parameter uri1Param = ParameterUtils.createUriParameter("uri1");
        n3Template.addInputParameter(uri1Param);
        addData(n3Template, "uri1", "http://testSubject");

        assertTrue(n3Template.run(dataStore).hasError());
    }


    @Test
    public void testInsertMultipleUris() throws Exception {
        n3Template.setN3TextAdditions("?uri1 <http:has> ?uri2");
    	loadDefaultModel();
        Parameter model = loader.loadInstance(MODEL_PATH, Parameter.class);
        n3Template.setTemplateModel(model);
        addModel(n3Template);
        
        Parameter param1 =  ParameterUtils.createUriParameter("uri1");
        Parameter param2 = ParameterUtils.createUriParameter("uri2");

        n3Template.addInputParameter(param1);
        n3Template.addInputParameter(param2);

        addData(n3Template, "uri1", "http://testSubject");
        addData(n3Template, "uri2", "http://testObject");

        assertFalse(n3Template.run(dataStore).hasError());
        assertNotNull(writeModel.getResource("http://testSubject"));
        assertTrue(writeModel.listObjectsOfProperty(new PropertyImpl("http:has")).next().isResource());
        assertEquals("http://testSubject",
                writeModel.listResourcesWithProperty(new PropertyImpl("http:has")).nextResource().getURI());
    }

    @Test
    public void testInsertOneUriOneLiteral() throws Exception {
       // when(modelComponent.getName()).thenReturn("test");
        n3Template.setN3TextAdditions("?uri1 <http:has> ?literal1");
    	loadDefaultModel();
        Parameter model = loader.loadInstance(MODEL_PATH, Parameter.class);
        n3Template.setTemplateModel(model);
        addModel(n3Template);

        Parameter param1 = ParameterUtils.createUriParameter("uri1");
        Parameter param2 = ParameterUtils.createStringLiteralParameter("literal1");

        n3Template.addInputParameter(param1);
        n3Template.addInputParameter(param2);

        addData(n3Template, "uri1", "http://testSubject");
        addData(n3Template, "literal1", "testLiteral");

        assertFalse(n3Template.run(dataStore).hasError());
        assertNotNull(writeModel.getResource("http://testSubject"));
        assertTrue(writeModel.listObjectsOfProperty(new PropertyImpl("http:has")).next().isLiteral());
        assertEquals("http://testSubject",
                writeModel.listResourcesWithProperty(new PropertyImpl("http:has")).nextResource().getURI());
    }

    @Test
    public void testMultipleStatements() throws Exception{
        n3Template.setN3TextAdditions("?uri1 <http://has> ?literal1 .\n?uri1 <http://was> ?literal2");

        loadDefaultModel();
        Parameter model = loader.loadInstance(MODEL_PATH, Parameter.class);
        n3Template.setTemplateModel(model);
        addModel(n3Template);
        
        Parameter param1 = ParameterUtils.createUriParameter("uri1");
        Parameter param2 = ParameterUtils.createStringLiteralParameter("literal1");
        Parameter param3 = ParameterUtils.createBooleanParameter("literal2");

        n3Template.addInputParameter(param1);
        n3Template.addInputParameter(param2);
        n3Template.addInputParameter(param3);
        
        addData(n3Template, "uri1", "http://testSubject");
        addData(n3Template, "literal1", "testLiteral");
        addData(n3Template, "literal2", true);

        assertFalse(n3Template.run(dataStore).hasError());
        assertNotNull(writeModel.getResource("http://testSubject"));
        assertEquals(2,writeModel.listObjects().toList().size());
        assertEquals(1,writeModel.listSubjects().toList().size());
        assertEquals(2, writeModel.listStatements().toList().size());
        assertTrue(writeModel.containsLiteral(
                new ResourceImpl("http://testSubject"), new PropertyImpl("http://was"),true));
    }

    @Test
    public void testRetractionsWorkWhenModelIsEmpty() throws Exception {
        loadDefaultModel();
    	Parameter model = loader.loadInstance(MODEL_PATH, Parameter.class);
        n3Template.setTemplateModel(model);
        addModel(n3Template);
        n3Template.setN3TextRetractions("<http://testSubject> <http:has> <http://testObject>");

        assertFalse(n3Template.run(dataStore).hasError());
        assertTrue(writeModel.getGraph().isEmpty());
    }

    @Test
    public void removingATriplet() throws Exception {
    	n3Template.setN3TextRetractions("<http://testSubject> <http:has> <http://testObject>");
    	loadDefaultModel();
        Parameter model = loader.loadInstance(MODEL_PATH, Parameter.class);
        n3Template.setTemplateModel(model);
        addModel(n3Template);
        writeModel.add(new StatementImpl(
                new ResourceImpl("http://testSubject"),
                new PropertyImpl("http:has"),
                new ResourceImpl("http://testObject"))
        );
        assertEquals(1,writeModel.getGraph().size());
        assertFalse(n3Template.run(dataStore).hasError());
        assertEquals(0,writeModel.getGraph().size());
    }


    @Test
    public void loadAndExecuteN3operationWithAdditionAndRetraction() throws Exception {
        loadDefaultModel();
        loadModels(TEST_DATA_PATH.split("\\.")[1],TEST_DATA_PATH);
        N3Template n3Template = loader.loadInstance(TEST_N3TEMPLATE_URI, N3Template.class);
        addData(n3Template, "testSubject", "http://Joe");
        addData(n3Template, "testSubject2", "http://Bob");
        addData(n3Template, "testObject", "Bike");
		addModel(n3Template);
        assertFalse(n3Template.run(dataStore).hasError());
        assertEquals(1,writeModel.getGraph().size());
        assertTrue(writeModel.getGraph().contains(
                NodeFactory.createURI("http://Bob"),
                NodeFactory.createURI("http://has"),
                NodeFactory.createLiteral("Bike"))
        );
    }

    @Test
    public void loadAndExecuteN3operationMultipleTimes() throws Exception {
        loadDefaultModel();
        loadModels(TEST_DATA_PATH.split("\\.")[1],TEST_DATA_PATH);

        N3Template n3Template = loader.loadInstance(TEST_N3TEMPLATE_URI, N3Template.class);
        addData(n3Template, "testSubject", "http://Joe");
        addData(n3Template, "testSubject2", "http://Bob");
        addData(n3Template, "testObject", "Bike");
		addModel(n3Template);
		
        assertFalse(n3Template.run(dataStore).hasError());
        assertEquals(1,writeModel.getGraph().size());
        assertTrue(writeModel.getGraph().contains(
                NodeFactory.createURI("http://Bob"),
                NodeFactory.createURI("http://has"),
                NodeFactory.createLiteral("Bike"))
        );
        addData(n3Template, "testSubject2", "http://Mike");

        assertFalse(n3Template.run(dataStore).hasError());
        assertEquals(2,writeModel.getGraph().size());
        assertTrue(writeModel.getGraph().contains(
                NodeFactory.createURI("http://Mike"),
                NodeFactory.createURI("http://has"),
                NodeFactory.createLiteral("Bike"))
        );

    }
    
	private void addModel(N3Template n3Template) throws ConversionException {
		Parameter modelParam = n3Template.getInputParams().get("FULL_UNION");
        final Data data = new Data(modelParam);
        data.earlyInitialization();
		dataStore.addData(modelParam.getName(), data);
	}

	private void addData(N3Template n3Template, String name, Object value) {
		Parameter testSubjectParam = n3Template.getInputParams().get(name);
		Data testSubject = new Data(testSubjectParam);
		TestView.setObject(testSubject, value);
		dataStore.addData(name, testSubject);
	}

}
