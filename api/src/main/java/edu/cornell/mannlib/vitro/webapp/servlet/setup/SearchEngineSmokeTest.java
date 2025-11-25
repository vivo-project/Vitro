/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Objects;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.SearchEngineUtil;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.http.ESHttpBasicClientFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

/**
 * Start up the appropriate search engine smoke test based on the configured URL property.
 */
public class SearchEngineSmokeTest implements ServletContextListener {

    private static final Log log = LogFactory.getLog(SearchEngineSmokeTest.class);

    private static ServiceType identifyService(String url) throws MalformedURLException {
        String baseServiceUrl = getBaseServiceUrl(url);

        ServiceType serviceType = ServiceType.UNKNOWN;
        HttpClient httpClient = ESHttpBasicClientFactory.getHttpClient(baseServiceUrl);
        HttpGet request = new HttpGet(baseServiceUrl);

        try {
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);

                if (result.contains("Solr")) {
                    return ServiceType.SOLR;
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(result);

                if (rootNode.has("version")) {
                    JsonNode versionNode = rootNode.get("version");
                    if (versionNode.has("distribution") &&
                        "opensearch".equals(versionNode.get("distribution").asText())) {
                        serviceType = ServiceType.OPENSEARCH;
                    } else if (versionNode.has("number")) {
                        serviceType = ServiceType.ELASTIC;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Request failed: " + e.getMessage());
        }

        return serviceType;
    }

    private static String getBaseServiceUrl(String url) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            return url.substring(0, lastSlashIndex);
        }
        return url;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        final StartupStatus ss = StartupStatus.getBean(sce.getServletContext());

        String searchEngineUrlString = SearchEngineUtil.getSearchEngineURLProperty();

        if (Objects.isNull(searchEngineUrlString) || searchEngineUrlString.isEmpty()) {
            ss.fatal(this, "No search engine is configured");
        }

        ServiceType service = ServiceType.UNKNOWN;
        try {
            service = identifyService(searchEngineUrlString);
        } catch (MalformedURLException e) {
            ss.fatal(this, "Search engine service URL is malformed.");
        }


        switch (service) {
            case ELASTIC:
                log.debug("Initializing ElasticSearch: " + searchEngineUrlString);
                new ElasticSmokeTest(this).doTest(sce);
                break;
            case OPENSEARCH:
                log.debug("Initializing OpenSearch: " + searchEngineUrlString);
                new ElasticSmokeTest(this).doTest(sce);
                break;
            case SOLR:
                log.debug("Initializing Solr: " + searchEngineUrlString);
                new SolrSmokeTest(this).doTest(sce);
                break;
            default:
                ss.fatal(this, "Unknown search engine service is configured");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nothing to tear down.
    }

    private enum ServiceType {
        ELASTIC,
        OPENSEARCH,
        SOLR,
        UNKNOWN
    }

}
