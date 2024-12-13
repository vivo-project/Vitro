package edu.cornell.mannlib.vitro.webapp.searchengine.base;

import java.util.Objects;

import javax.annotation.Nullable;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SearchEngineUtil {

    private static final Log log = LogFactory.getLog(SearchEngineUtil.class);

    @Nullable
    public static String getSearchEngineURLProperty() {
        ConfigurationProperties config = ConfigurationProperties.getInstance();
        if (Objects.isNull(config)) {
            return null;
        }

        if (config.getProperty("vitro.local.searchengine.url", "").isEmpty()) {
            return tryFetchLegacySolrConfiguration(config);
        }

        return config.getProperty("vitro.local.searchengine.url", "");
    }

    private static String tryFetchLegacySolrConfiguration(ConfigurationProperties config) {
        String legacyConfigValue = config.getProperty("vitro.local.solr.url", "");
        if (!legacyConfigValue.isEmpty()) {
            log.warn(
                "vitro.local.solr.url is deprecated, switch to using" +
                    " vitro.local.searchengine.url as soon as possible.");
        }

        return legacyConfigValue;
    }
}
