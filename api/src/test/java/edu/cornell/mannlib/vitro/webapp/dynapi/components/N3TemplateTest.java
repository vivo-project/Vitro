package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.PrimitiveSerializationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.SerializationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.RDFType;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
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

    private final static String TEST_DATA_PATH="src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-n3template-test.n3";
    private final static String TEST_N3TEMPLATE_URI="https://vivoweb.org/ontology/vitro-dynamic-api/N3Template/testN3Template";

    private static MockedStatic<ModelAccess> modelAccess;

    static SerializationType anyURI;
    static SerializationType stringType;
    static SerializationType booleanType;

    @Mock
    OperationData input;

    @Mock
    ContextModelAccessImpl contextModelAccess;

    @Mock
    ServletContext servletContext;

    @Mock
    ModelComponent modelComponent;

    OntModel writeModel;

    N3Template n3Template;

    @BeforeClass
    public static void setupStaticObjects(){
        modelAccess = mockStatic(ModelAccess.class);

        anyURI = new PrimitiveSerializationType();
        anyURI.setName("anyURI");
        stringType = new PrimitiveSerializationType();
        stringType.setName("string");
        booleanType = new PrimitiveSerializationType();
        booleanType.setName("boolean");
    }

    @AfterClass
    public static void after() {
        modelAccess.close();
    }

    @Before
    public void setupTemplate(){
        when(ModelAccess.on(any(ServletContext.class))).thenReturn(contextModelAccess);
        writeModel = new OntModelImpl(OntModelSpec.OWL_DL_MEM);
        when(contextModelAccess.getOntModel(any(String.class))).thenReturn(writeModel);
        this.n3Template = new N3Template();
    }


    @Test
    public void testLoadingAndPropertiesSetup() throws IOException, ConfigurationBeanLoaderException {
        loadDefaultModel();
        loadModels(TEST_DATA_PATH.split("\\.")[1],TEST_DATA_PATH);

        N3Template n3Template = loader.loadInstance(TEST_N3TEMPLATE_URI, N3Template.class);
        assertNotNull(n3Template);
        assertEquals(0, n3Template.getProvidedParams().size());
        assertEquals(3, n3Template.getRequiredParams().size());
        assertEquals("?testSubject <http://has> ?testObject. ?testSubject2 <http://has> ?testObject", n3Template.getN3TextAdditions());
        assertEquals("?testSubject <http://has> ?testObject", n3Template.getN3TextRetractions());
    }

    @Test
    public void testNotAllN3VariablesSubstitutedWithValues(){
        n3Template.setN3TextRetractions("?uri1 <http:has> ?literal1");
        n3Template.setTemplateModel(modelComponent);

        Parameter param1 = new Parameter();
        ParameterType type = new ParameterType();
        param1.setType(type);
        type.setSerializationType(anyURI);
        param1.setName("uri1");

        n3Template.addRequiredParameter(param1);

        when(input.has(any(String.class))).thenReturn(true);
        when(input.get("uri1")).thenReturn(new String[]{"http://testSubject"});

        assertTrue(n3Template.run(input).hasError());
    }

    @Test
    public void testInsertMultipleUris() {
        when(modelComponent.getName()).thenReturn("test");
        n3Template.setN3TextAdditions("?uri1 <http:has> ?uri2");
        n3Template.setTemplateModel(modelComponent);

        Parameter param1 = new Parameter();
        ParameterType type = new ParameterType();
        param1.setType(type);
        type.setSerializationType(anyURI);
        RDFType rdfType1 = new RDFType();
        rdfType1.setName("anyURI");
        type.setRdfType(rdfType1);
        param1.setName("uri1");
        Parameter param2 = new Parameter();
        ParameterType type2 = new ParameterType();
        param2.setType(type2);
        type2.setSerializationType(anyURI);
        RDFType rdfType2 = new RDFType();
        rdfType2.setName("anyURI");
        type2.setRdfType(rdfType2);
        param2.setName("uri2");

        n3Template.addRequiredParameter(param1);
        n3Template.addRequiredParameter(param2);

        when(input.has(any(String.class))).thenReturn(true);
        when(input.getContext()).thenReturn(servletContext);
        when(input.get("uri1")).thenReturn(new String[]{"http://testSubject"});
        when(input.get("uri2")).thenReturn(new String[]{"http://testObject"});

        assertFalse(n3Template.run(input).hasError());
        assertNotNull(writeModel.getResource("http://testSubject"));
        assertTrue(writeModel.listObjectsOfProperty(new PropertyImpl("http:has")).next().isResource());
        assertEquals("http://testSubject",
                writeModel.listResourcesWithProperty(new PropertyImpl("http:has")).nextResource().getURI());
    }

    @Test
    public void testInsertOneUriOneLiteral(){
        when(modelComponent.getName()).thenReturn("test");
        n3Template.setN3TextAdditions("?uri1 <http:has> ?literal1");
        n3Template.setTemplateModel(modelComponent);

        Parameter param1 = new Parameter();
        ParameterType type1 = new ParameterType();
        param1.setType(type1);
        type1.setSerializationType(anyURI);
        RDFType rdfType1 = new RDFType();
        rdfType1.setName("anyURI");
        type1.setRdfType(rdfType1);
        param1.setName("uri1");
        Parameter param2 = new Parameter();
        ParameterType type2 = new ParameterType();
        param2.setType(type2);
        type2.setSerializationType(stringType);
        
        RDFType rdfType2 = new RDFType();
        rdfType2.setName("string");
        type2.setRdfType(rdfType2);
        
        param2.setName("literal1");

        n3Template.addRequiredParameter(param1);
        n3Template.addRequiredParameter(param2);

        when(input.has(any(String.class))).thenReturn(true);
        when(input.getContext()).thenReturn(servletContext);
        when(input.get("uri1")).thenReturn(new String[]{"http://testSubject"});
        when(input.get("literal1")).thenReturn(new String[]{"testLiteral"});

        assertFalse(n3Template.run(input).hasError());
        assertNotNull(writeModel.getResource("http://testSubject"));
        assertTrue(writeModel.listObjectsOfProperty(new PropertyImpl("http:has")).next().isLiteral());
        assertEquals("http://testSubject",
                writeModel.listResourcesWithProperty(new PropertyImpl("http:has")).nextResource().getURI());
    }

    @Test
    public void testMultipleStatements(){
        when(modelComponent.getName()).thenReturn("test");
        n3Template.setN3TextAdditions("?uri1 <http://has> ?literal1 .\n?uri1 <http://was> ?literal2");
        n3Template.setTemplateModel(modelComponent);

        Parameter param1 = new Parameter();
        ParameterType type1 = new ParameterType();
        param1.setType(type1);
        type1.setSerializationType(anyURI);
        RDFType rdfType1 = new RDFType();
        rdfType1.setName("anyURI");
        type1.setRdfType(rdfType1);
        param1.setName("uri1");
        Parameter param2 = new Parameter();
        ParameterType type2 = new ParameterType();
        param2.setType(type2);
        type2.setSerializationType(stringType);
        RDFType rdfType2 = new RDFType();
        rdfType2.setName("string");
        type2.setRdfType(rdfType2);
        
        param2.setName("literal1");
        Parameter param3 = new Parameter();
        ParameterType type3 = new ParameterType();
        param3.setType(type3);
        type3.setSerializationType(booleanType);
        
        RDFType rdfType3 = new RDFType();
        rdfType3.setName("boolean");
        type3.setRdfType(rdfType3);
        
        param3.setName("literal2");

        n3Template.addRequiredParameter(param1);
        n3Template.addRequiredParameter(param2);
        n3Template.addRequiredParameter(param3);

        when(input.has(any(String.class))).thenReturn(true);
        when(input.getContext()).thenReturn(servletContext);
        when(input.get("uri1")).thenReturn(new String[]{"http://testSubject"});
        when(input.get("literal1")).thenReturn(new String[]{"testLiteral"});
        when(input.get("literal2")).thenReturn(new String[]{"true"});

        assertFalse(n3Template.run(input).hasError());
        assertNotNull(writeModel.getResource("http://testSubject"));
        assertEquals(2,writeModel.listObjects().toList().size());
        assertEquals(1,writeModel.listSubjects().toList().size());
        assertEquals(2, writeModel.listStatements().toList().size());
        assertTrue(writeModel.containsLiteral(
                new ResourceImpl("http://testSubject"), new PropertyImpl("http://was"),true));
    }

    @Test
    public void testRetractionsWorkWhenModelIsEmpty() {
        when(modelComponent.getName()).thenReturn("test");
        n3Template.setN3TextRetractions("<http://testSubject> <http:has> <http://testObject>");
        n3Template.setTemplateModel(modelComponent);

        when(input.getContext()).thenReturn(servletContext);

        assertFalse(n3Template.run(input).hasError());
        assertTrue(writeModel.getGraph().isEmpty());
    }

    @Test
    public void removingATriplet() {
        when(modelComponent.getName()).thenReturn("test");
        n3Template.setN3TextRetractions("<http://testSubject> <http:has> <http://testObject>");
        n3Template.setTemplateModel(modelComponent);

        when(input.getContext()).thenReturn(servletContext);

        writeModel.add(new StatementImpl(
                new ResourceImpl("http://testSubject"),
                new PropertyImpl("http:has"),
                new ResourceImpl("http://testObject"))
        );
        assertEquals(1,writeModel.getGraph().size());
        assertFalse(n3Template.run(input).hasError());
        assertEquals(0,writeModel.getGraph().size());
    }


    @Test
    public void loadAndExecuteN3operationWithAdditionAndRetraction() throws IOException, ConfigurationBeanLoaderException {
        loadDefaultModel();
        loadModels(TEST_DATA_PATH.split("\\.")[1],TEST_DATA_PATH);

        N3Template n3Template = loader.loadInstance(TEST_N3TEMPLATE_URI, N3Template.class);

        when(input.has("testSubject")).thenReturn(true);
        when(input.has("testSubject2")).thenReturn(true);
        when(input.has("testObject")).thenReturn(true);
        when(input.get("testSubject")).thenReturn(new String[]{"http://Joe"});
        when(input.get("testSubject2")).thenReturn(new String[]{"http://Bob"});
        when(input.get("testObject")).thenReturn(new String[]{"Bike"});
        when(input.getContext()).thenReturn(servletContext);

        assertFalse(n3Template.run(input).hasError());
        assertEquals(1,writeModel.getGraph().size());
        assertTrue(writeModel.getGraph().contains(
                NodeFactory.createURI("http://Bob"),
                NodeFactory.createURI("http://has"),
                NodeFactory.createLiteral("Bike"))
        );
    }

    @Test
    public void loadAndExecuteN3operationMultipleTimes() throws IOException, ConfigurationBeanLoaderException {
        loadDefaultModel();
        loadModels(TEST_DATA_PATH.split("\\.")[1],TEST_DATA_PATH);

        N3Template n3Template = loader.loadInstance(TEST_N3TEMPLATE_URI, N3Template.class);

        when(input.has("testSubject")).thenReturn(true);
        when(input.has("testSubject2")).thenReturn(true);
        when(input.has("testObject")).thenReturn(true);
        when(input.get("testSubject")).thenReturn(new String[]{"http://Joe"});
        when(input.get("testSubject2")).thenReturn(new String[]{"http://Bob"});
        when(input.get("testObject")).thenReturn(new String[]{"Bike"});
        when(input.getContext()).thenReturn(servletContext);

        assertFalse(n3Template.run(input).hasError());
        assertEquals(1,writeModel.getGraph().size());
        assertTrue(writeModel.getGraph().contains(
                NodeFactory.createURI("http://Bob"),
                NodeFactory.createURI("http://has"),
                NodeFactory.createLiteral("Bike"))
        );

        when(input.get("testSubject")).thenReturn(new String[]{"http://Bob"});
        when(input.get("testSubject2")).thenReturn(new String[]{"http://Mike"});
        when(input.get("testObject")).thenReturn(new String[]{"Bike"});

        assertFalse(n3Template.run(input).hasError());
        assertEquals(1,writeModel.getGraph().size());
        assertTrue(writeModel.getGraph().contains(
                NodeFactory.createURI("http://Mike"),
                NodeFactory.createURI("http://has"),
                NodeFactory.createLiteral("Bike"))
        );

    }
}
