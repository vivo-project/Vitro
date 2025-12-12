package edu.cornell.mannlib.vitro.webapp.searchengine.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.SearchEngineUtil;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class SearchEngineUtilTest {

    @Test
    public void returnsConfiguredSearchEngineUrl() {
        try (MockedStatic<ConfigurationProperties> mocked = Mockito.mockStatic(ConfigurationProperties.class)) {
            ConfigurationProperties config = Mockito.mock(ConfigurationProperties.class);

            mocked.when(ConfigurationProperties::getInstance).thenReturn(config);
            Mockito.when(config.getProperty("vitro.local.searchengine.url", "")).thenReturn("http://search:8983/solr");

            String result = SearchEngineUtil.getSearchEngineURLProperty();

            assertEquals("http://search:8983/solr", result);
        }
    }

    @Test
    public void fallsBackToLegacySolrUrlAndWarns() {
        try (MockedStatic<ConfigurationProperties> mocked = Mockito.mockStatic(ConfigurationProperties.class)) {
            ConfigurationProperties config = Mockito.mock(ConfigurationProperties.class);

            mocked.when(ConfigurationProperties::getInstance).thenReturn(config);
            Mockito.when(config.getProperty("vitro.local.searchengine.url", "")).thenReturn("");
            Mockito.when(config.getProperty("vitro.local.solr.url", "")).thenReturn("http://legacy:8983/solr");

            String result = SearchEngineUtil.getSearchEngineURLProperty();

            assertEquals("http://legacy:8983/solr", result);
        }
    }

    @Test
    public void returnsNullWhenConfigIsNull() {
        try (MockedStatic<ConfigurationProperties> mocked = Mockito.mockStatic(ConfigurationProperties.class)) {
            mocked.when(ConfigurationProperties::getInstance).thenReturn(null);

            assertNull(SearchEngineUtil.getSearchEngineURLProperty());
        }
    }

    @Test
    public void returnsEmptyWhenNoConfigValues() {
        try (MockedStatic<ConfigurationProperties> mocked = Mockito.mockStatic(ConfigurationProperties.class)) {
            ConfigurationProperties config = Mockito.mock(ConfigurationProperties.class);

            mocked.when(ConfigurationProperties::getInstance).thenReturn(config);
            Mockito.when(config.getProperty("vitro.local.searchengine.url", "")).thenReturn("");
            Mockito.when(config.getProperty("vitro.local.solr.url", "")).thenReturn("");

            String result = SearchEngineUtil.getSearchEngineURLProperty();

            assertEquals("", result);
        }
    }
}

