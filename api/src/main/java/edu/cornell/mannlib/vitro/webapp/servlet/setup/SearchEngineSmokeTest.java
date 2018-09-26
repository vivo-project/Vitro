/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Start up the appropriate search engine smoke test based on the configured URL property.
 */
public class SearchEngineSmokeTest implements ServletContextListener {

    private static final Log log = LogFactory.getLog(SearchEngineSmokeTest.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        final StartupStatus ss = StartupStatus.getBean(sce.getServletContext());

        String solrUrlString = ConfigurationProperties.getBean(sce).getProperty("vitro.local.solr.url", "");
        String elasticUrlString = ConfigurationProperties.getBean(sce).getProperty("vitro.local.elastic.url", "");

        if (!solrUrlString.isEmpty() && !elasticUrlString.isEmpty()) {
            ss.fatal(this, "More than one search engine is configured: " + solrUrlString + ", and " + elasticUrlString);

        } else if (solrUrlString.isEmpty() && elasticUrlString.isEmpty()) {
            ss.fatal(this, "No search engine is configured");

        } else if (!solrUrlString.isEmpty()) {
            log.debug("Initializing Solr: " + solrUrlString);
            new SolrSmokeTest(this).doTest(sce);

        } else {
            log.debug("Initializing ElasticSearch: " + elasticUrlString);
            new ElasticSmokeTest(this).doTest(sce);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nothing to tear down.
    }

}
