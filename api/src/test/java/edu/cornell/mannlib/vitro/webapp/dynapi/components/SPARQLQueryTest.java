package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.PrimitiveParameterType;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.impl.ContextModelAccessImpl;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@RunWith(MockitoJUnitRunner.class)
public class SPARQLQueryTest extends ServletContextTest{
    private final static String TEST_DATA_PATH="src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-SPARQLQuery-test.n3";
    private final static String TEST_SPARQLQuery_SELECT="https://vivoweb.org/ontology/vitro-dynamic-api/SPARQL/testSPARQLQuery-SELECT";
    private final static String TEST_SPARQLQuery_ASK="https://vivoweb.org/ontology/vitro-dynamic-api/SPARQL/testSPARQLQuery-ASK";
    private final static String TEST_SPARQLQuery_CONSTRUCT="https://vivoweb.org/ontology/vitro-dynamic-api/SPARQL/testSPARQLQuery-CONSTRUCT";

    private static MockedStatic<ModelAccess> modelAccess;

    static ParameterType anyURI;
    static ParameterType stringType;
    static ParameterType integerType;

    static ParameterType booleanType;
    private static final Log log = LogFactory.getLog(SPARQLQueryTest.class);

    @Mock
    OperationData inputOutput;

    @Mock
    ContextModelAccessImpl contextModelAccess;

    @Mock
    ServletContext servletContext;

    @Mock
    ModelComponent modelComponent;

    @Mock
    OntModel queryModel;

    @Mock
    ParameterizedSparqlString select_pss;

    @Mock
    ParameterizedSparqlString ask_pss;

    @Mock
    ParameterizedSparqlString construct_pss;

    @Mock
    QueryExecution qexec;

    SPARQLQuery sparqlQuery;




    @BeforeClass
    public static void setupStaticObjects(){
        modelAccess = mockStatic(ModelAccess.class);

        anyURI = new PrimitiveParameterType();
        anyURI.setName("anyURI");
        stringType = new PrimitiveParameterType();
        stringType.setName("string");
        integerType = new PrimitiveParameterType();
        integerType.setName("integer");
        booleanType = new PrimitiveParameterType();
        booleanType.setName("boolean");
    }

    @AfterClass
    public static void after() {
        modelAccess.close();
    }

    @Before
    public void setupQuery(){
        this.sparqlQuery = new SPARQLQuery();

        when(ModelAccess.on(any(ServletContext.class))).thenReturn(contextModelAccess);
        this.queryModel = new OntModelImpl(OntModelSpec.OWL_DL_MEM);

        when(contextModelAccess.getOntModel(any(String.class))).thenReturn(this.queryModel);
        this.sparqlQuery.setQueryModel(modelComponent);
        this.select_pss = new ParameterizedSparqlString();
        this.ask_pss = new ParameterizedSparqlString();
        this.construct_pss = new ParameterizedSparqlString();

    }


    @Test
    public void testLoadingAndPropertiesSetup() throws IOException, ConfigurationBeanLoaderException {
        loadDefaultModel();
        loadModels(TEST_DATA_PATH.split("\\.")[1],TEST_DATA_PATH);

        SPARQLQuery sparqlQuerySelect = loader.loadInstance(TEST_SPARQLQuery_SELECT, SPARQLQuery.class);
        SPARQLQuery sparqlQueryASK = loader.loadInstance(TEST_SPARQLQuery_ASK, SPARQLQuery.class);
        SPARQLQuery sparqlQueryCONSTRUCT = loader.loadInstance(TEST_SPARQLQuery_CONSTRUCT, SPARQLQuery.class);

        assertNotNull(sparqlQuerySelect);
        assertNotNull(sparqlQueryASK);
        assertNotNull(sparqlQueryCONSTRUCT);

        assertEquals(1, sparqlQuerySelect.getProvidedParams().size());
        assertEquals(3, sparqlQuerySelect.getRequiredParams().size());

        assertEquals(1, sparqlQueryASK.getProvidedParams().size());
        assertEquals(3, sparqlQueryASK.getRequiredParams().size());

        assertEquals(1, sparqlQueryCONSTRUCT.getProvidedParams().size());
        assertEquals(3, sparqlQueryCONSTRUCT.getRequiredParams().size());
    }

    @Test
    public void testNotAllvariablessubstitutedWithValues() {
        // SELECT query
        when(modelComponent.getName()).thenReturn("test");
        sparqlQuery.setQueryModel(modelComponent);
        sparqlQuery.setQueryText("SELECT ?action ?label\nWHERE\n{\n      ?action <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://vivoweb.org/ontology/vitro-dynamic-api#action>\n       OPTIONAL { ?action <http://www.w3.org/2000/01/rdf-schema#label> ?label } \n}\nLIMIT ?limit");

        Parameter param1 = new Parameter();
        param1.setParamType(anyURI);
        param1.setName("action");

        sparqlQuery.addRequiredParameter(param1);

        when(inputOutput.has(any(String.class))).thenReturn(true);
        when(inputOutput.get("action")).thenReturn(new String[]{"http://action"});

        assertTrue(sparqlQuery.run(inputOutput).hasError());


    }

}
