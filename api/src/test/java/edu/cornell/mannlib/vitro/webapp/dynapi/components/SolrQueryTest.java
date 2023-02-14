package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationImpl;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.operations.SolrQuery;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.PrimitiveSerializationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationConfig;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.RDFType;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;

@RunWith(MockitoJUnitRunner.class)
public class SolrQueryTest extends ServletContextTest {

    private static final String JAVA_LANG_STRING = String.class.getCanonicalName();
    private final static String TEST_DATA_PATH = TEST_PREFIX + "dynamic-api-individuals-solr-test.n3";
    private final static String TEST_SOLR_QUERY_URI="https://vivoweb.org/ontology/vitro-dynamic-api/solrQuery/genericSolrTextQuery";

    private static MockedStatic<ApplicationUtils> applicationUtils;

    private SolrQuery solrQuery;

    @Mock
    ApplicationImpl application;

    private Parameter parameter1;

    private DataStore dataStore;

    @Mock
    private SearchEngine searchEngine;

    @Mock
    private SearchQuery searchQuery;

    @BeforeClass
    public static void setupStaticObjects() {
        offLogs();
        applicationUtils = mockStatic(ApplicationUtils.class);
    }

    @AfterClass
    public static void after() {
        applicationUtils.close();
        restoreLogs();
    }

    @Before
    public void setupQuery() throws Exception{
        when(ApplicationUtils.instance()).thenReturn(application);
        when(application.getSearchEngine()).thenReturn(searchEngine);
        when(searchEngine.createQuery()).thenReturn(searchQuery);
        this.solrQuery = new SolrQuery();
        dataStore = new DataStore();
        parameter1 = createStringParameter("testParameter");
    }
    
    @After
    public void reset() {
    }

    @Test
    public void testLoadingAndPropertiesSetup() throws Exception {
        loadDefaultModel();
        loadModels(TEST_DATA_PATH.split("\\.")[1], TEST_DATA_PATH);

        SolrQuery query = loader.loadInstance(TEST_SOLR_QUERY_URI, SolrQuery.class);
        assertNotNull(query);
        assertEquals(2, query.getSorts().size());
        assertEquals("http", query.getQueryText());
        assertEquals(1, query.getFacets().size());
        assertEquals(2, query.getFields().size());
        assertEquals("10", query.getLimit());
        assertEquals("3", query.getOffset());
    }

    @Test
    public void requiredParameterMissingFromInput(){
        solrQuery.addInputParameter(parameter1);
        assertTrue(solrQuery.run(dataStore).hasError());
    }

    @Test
    public void requiredParameterPresentButInvalid(){
    	Data data = new Data(parameter1);
    	dataStore.addData("anotherParameter", data);
        solrQuery.addInputParameter(parameter1);
        assertTrue(solrQuery.run(dataStore).hasError());
    }

    @Test
    public void requiredParameterPresentAndValid() throws ClassNotFoundException{
        Data data = new Data(parameter1);
        TestView.setObject(data, "testValue");
    	dataStore.addData(parameter1.getName(), data);
    	solrQuery.addInputParameter(parameter1);
        assertFalse(solrQuery.run(dataStore).hasError());
    }

    @Test
    public void queryWithOnlySimpleTextSearch(){
        when(searchQuery.setQuery("testSearchText")).thenReturn(searchQuery);
        solrQuery.setQueryText("testSearchText");

        assertFalse(solrQuery.run(dataStore).hasError());
        verify(searchQuery,times(1)).setQuery("testSearchText");
    }

    @Test
    public void queryWithVariableInsideTextSearch(){
        solrQuery.setQueryText("testSearchText OR ?testParameter");
        assertTrue(solrQuery.run(dataStore).hasError());
    }

    @Test
    public void queryWithVariableInsideTextSearchWithSubstitution(){
        solrQuery.setQueryText("testSearchText OR ?testParameter");
        Data data = new Data(parameter1);
        TestView.setObject(data, "testValue");
    	dataStore.addData(parameter1.getName(), data);
    	solrQuery.addInputParameter(parameter1);
        assertFalse(solrQuery.run(dataStore).hasError());
        verify(searchQuery,times(1)).setQuery("testSearchText OR testValue");
    }

    @Test
    public void queryWithMultipleVariableInsideTextSearchWithSubstitution() throws Exception{
        when(searchQuery.setQuery(any(String.class))).thenReturn(searchQuery);
        solrQuery.setQueryText("?testParam1 OR ?testParam2");
        Parameter param = createStringParameter("testParam1");
        Data data = new Data(param);
        TestView.setObject(data, "testValue1");
    	dataStore.addData(param.getName(), data);
    	
    	Parameter param2 = createStringParameter("testParam2");
        Data data2 = new Data(param2);
        TestView.setObject(data2, "testValue2");
    	dataStore.addData(param2.getName(), data2);

        assertFalse(solrQuery.run(dataStore).hasError());
        verify(searchQuery,times(1)).setQuery("testValue1 OR testValue2");
    }

    @Test
    public void queryWithLimitAndOffsetWrongValue() throws Exception{
        when(searchQuery.setStart(anyInt())).thenReturn(searchQuery);
        
        Parameter param = createStringParameter("testParam1");
        Data data = new Data(param);
        TestView.setObject(data, "11");
    	dataStore.addData(param.getName(), data);
    	
    	Parameter param2 = createStringParameter("testParam2");
        Data data2 = new Data(param2);
        TestView.setObject(data2, "testValue2");
    	dataStore.addData(param2.getName(), data2);
        
        solrQuery.setOffset("?testParam1");
        solrQuery.setLimit("?testParam2");

        assertTrue(solrQuery.run(dataStore).hasError());
        verify(searchQuery,times(0)).setQuery(any());
    }

    @Test
    public void queryWithMultipleSortsBadInput() throws Exception{
        solrQuery.addSort("   ?testParam1    desc ");
        solrQuery.addSort("?testParam2  ?testParam3");

    	Parameter param = createStringParameter("testParam1");
        Data data = new Data(param);
        TestView.setObject(data, "field1");
    	dataStore.addData(param.getName(), data);
    	
    	Parameter param2 = createStringParameter("testParam2");
        Data data2 = new Data(param2);
        TestView.setObject(data2, "field2");
    	dataStore.addData(param2.getName(), data2);
    	
    	Parameter param3 = createStringParameter("testParam3");
        Data data3 = new Data(param3);
        TestView.setObject(data3, "field3");
    	dataStore.addData(param3.getName(), data3);
    	
    	solrQuery.addInputParameter(param);
    	solrQuery.addInputParameter(param2);
    	solrQuery.addInputParameter(param3);

        assertTrue(solrQuery.run(dataStore).hasError());
        assertEquals(solrQuery.getSorts().size(),2);
    }

    @Test
    public void queryWithMultipleSorts() throws Exception{
        when(searchQuery.addSortField(anyString(),any())).thenReturn(searchQuery);
        
    	Parameter param = createStringParameter("testParam1");
        Data data = new Data(param);
        TestView.setObject(data,"field1");
    	dataStore.addData(param.getName(), data);
    	
    	Parameter param2 = createStringParameter("testParam2");
        Data data2 = new Data(param2);
        TestView.setObject(data2, "field2");
    	dataStore.addData(param2.getName(), data2);
    	
    	Parameter param3 = createStringParameter("testParam3");
        Data data3 = new Data(param3);
        TestView.setObject(data3,"asc");
    	dataStore.addData(param3.getName(), data3);
        //more spaces are added on purpose to simulate RDF input that's not well formatted
        solrQuery.addSort("   ?testParam1    desc ");
        solrQuery.addSort("?testParam2  ?testParam3");

        assertFalse(solrQuery.run(dataStore).hasError());
        assertEquals(solrQuery.getSorts().size(),2);
        verify(searchQuery,times(2)).addSortField(anyString(),any());
    }
    
    private Parameter createStringParameter(String name) throws Exception {
		Parameter uri1Param = new Parameter();
        ParameterType paramType = new ParameterType();
        ImplementationType impltype = new ImplementationType();
        paramType.setImplementationType(impltype);
        
        ImplementationConfig config = new ImplementationConfig();
		
		config.setClassName(JAVA_LANG_STRING);
		config.setMethodArguments("");
		config.setMethodName("toString");
		config.setStaticMethod(false);
		impltype.setDeserializationConfig(config);
		impltype.setSerializationConfig(config);
		impltype.setClassName(JAVA_LANG_STRING);
		
		RDFType rdfType = new RDFType();
		rdfType.setName("string");
		paramType.setRdfType(rdfType);
		
		PrimitiveSerializationType anyURI = new PrimitiveSerializationType();
        anyURI.setName("anyURI");
        uri1Param.setType(paramType);
        paramType.setSerializationType(anyURI);
        uri1Param.setName(name);
		return uri1Param;
	}
}
