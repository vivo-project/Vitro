package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.Data;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationImpl;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;

@RunWith(MockitoJUnitRunner.class)
public class SolrQueryTest extends ServletContextTest {

    private final static String TEST_DATA_PATH="src/test/resources/rdf/abox/filegraph/dynamic-api-individuals-solr-test.n3";
    private final static String TEST_SOLR_QUERY_URI="https://vivoweb.org/ontology/vitro-dynamic-api/solrQuery/genericSolrTextQuery";

    private static MockedStatic<ApplicationUtils> applicationUtils;

    @Spy
    private SolrQuery solrQuery;

    @Mock
    ApplicationImpl application;

    @Mock
    private Parameter parameter1;

    @Mock
    private Data data;

    @Mock
    private OperationData input;

    @Mock
    private SearchEngine searchEngine;

    @Mock
    private SearchQuery searchQuery;

    @BeforeClass
    public static void setupStaticObjects() {
        applicationUtils = mockStatic(ApplicationUtils.class);
    }

    @AfterClass
    public static void after() {
        applicationUtils.close();
    }

    @Before
    public void setupQuery(){
        when(ApplicationUtils.instance()).thenReturn(application);
        when(application.getSearchEngine()).thenReturn(searchEngine);
        when(searchEngine.createQuery()).thenReturn(searchQuery);
        this.solrQuery = new SolrQuery();
    }

    @Test
    public void testLoadingAndPropertiesSetup() throws IOException, ConfigurationBeanLoaderException {
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
        when(parameter1.getName()).thenReturn("testParameter");
        solrQuery.addRequiredParameter(parameter1);
        when(input.has("testParameter")).thenReturn(false);
        assertTrue(solrQuery.run(input).hasError());
        verify(parameter1, times(1)).getName();
        verify(input,times(1)).has("testParameter");
    }

    @Test
    public void requiredParameterPresentButInvalid(){
        when(parameter1.getName()).thenReturn("testParameter");
        when(parameter1.isValid(any(Data.class))).thenReturn(false);
        solrQuery.addRequiredParameter(parameter1);
        when(input.has("testParameter")).thenReturn(true);
        when(input.getData("testParameter")).thenReturn(data);
        assertTrue(solrQuery.run(input).hasError());

        verify(parameter1,times(1)).getName();
        verify(parameter1,times(1)).isValid(any(Data.class));
    }

    @Test @Ignore
    public void requiredParameterPresentAndValid(){
        when(parameter1.getName()).thenReturn("testParameter");
        when(parameter1.isValid(any(Data.class))).thenReturn(true);
        solrQuery.addRequiredParameter(parameter1);
        when(input.has("testParameter")).thenReturn(true);
        when(input.getData("testParameter")).thenReturn(data);
        assertFalse(solrQuery.run(input).hasError());

        verify(parameter1,times(1)).getName();
        verify(parameter1,times(1)).isValid(any(Data.class));
    }

    @Test
    public void queryWithOnlySimpleTextSearch(){
        when(searchQuery.setQuery("testSearchText")).thenReturn(searchQuery);
        solrQuery.setQueryText("testSearchText");

        assertFalse(solrQuery.run(input).hasError());
        verify(searchQuery,times(1)).setQuery("testSearchText");
    }

    @Test
    public void queryWithVariableInsideTextSearch(){
        when(input.has("testParameter")).thenReturn(false);
        solrQuery.setQueryText("testSearchText OR ?testParameter");

        assertTrue(solrQuery.run(input).hasError());
        verify(searchQuery,times(0)).setQuery(any());
        verify(input,times(1)).has(any());
    }

    @Test
    public void queryWithVariableInsideTextSearchWithSubstitution(){
        when(searchQuery.setQuery(any(String.class))).thenReturn(searchQuery);
        when(input.has("testParameter")).thenReturn(true);
        when(input.get("testParameter")).thenReturn("testValue");
        solrQuery.setQueryText("testSearchText OR ?testParameter");

        assertFalse(solrQuery.run(input).hasError());
        verify(searchQuery,times(1)).setQuery("testSearchText OR testValue");
    }

    @Test
    public void queryWithMultipleVariableInsideTextSearchWithSubstitution(){
        when(searchQuery.setQuery(any(String.class))).thenReturn(searchQuery);
        when(input.has(anyString())).thenReturn(true);
        when(input.get("testParam1")).thenReturn("testValue1");
        when(input.get("testParam2")).thenReturn("testValue2");
        solrQuery.setQueryText("?testParam1 OR ?testParam2");

        assertFalse(solrQuery.run(input).hasError());
        verify(searchQuery,times(1)).setQuery("testValue1 OR testValue2");
    }

    @Test
    public void queryWithLimitAndOffsetWrongValue(){
        when(searchQuery.setStart(anyInt())).thenReturn(searchQuery);
        when(input.has(anyString())).thenReturn(true);
        when(input.get("testParam1")).thenReturn("11");
        when(input.get("testParam2")).thenReturn("testValue2");
        solrQuery.setOffset("?testParam1");
        solrQuery.setLimit("?testParam2");

        assertTrue(solrQuery.run(input).hasError());
        verify(searchQuery,times(0)).setQuery(any());
        verify(searchQuery,times(1)).setStart(11);
        verify(searchQuery,times(0)).setRows(anyInt());
    }

    @Test
    public void queryWithMultipleSortsBadInput(){
        when(input.has(anyString())).thenReturn(true);
        when(input.get("testParam1")).thenReturn("field1");
        when(input.get("testParam2")).thenReturn("field2");
        //Should fail because in this case testParam3 must be keyword desc or asc
        when(input.get("testParam3")).thenReturn("field3");
        //more spaces are added on purpose to simulate RDF input that's not well formatted
        solrQuery.addSort("   ?testParam1    desc ");
        solrQuery.addSort("?testParam2  ?testParam3");

        assertTrue(solrQuery.run(input).hasError());
        assertEquals(solrQuery.getSorts().size(),2);
        verify(searchQuery,times(1)).addSortField(anyString(),any());
    }

    @Test
    public void queryWithMultipleSorts(){
        when(searchQuery.addSortField(anyString(),any())).thenReturn(searchQuery);
        when(input.has(anyString())).thenReturn(true);
        when(input.get("testParam1")).thenReturn("field1");
        when(input.get("testParam2")).thenReturn("field2");
        //Should fail because in this case testParam3 must be keyword desc or asc
        when(input.get("testParam3")).thenReturn("asc");
        //more spaces are added on purpose to simulate RDF input that's not well formatted
        solrQuery.addSort("   ?testParam1    desc ");
        solrQuery.addSort("?testParam2  ?testParam3");

        assertFalse(solrQuery.run(input).hasError());
        assertEquals(solrQuery.getSorts().size(),2);
        verify(searchQuery,times(2)).addSortField(anyString(),any());
    }
}
