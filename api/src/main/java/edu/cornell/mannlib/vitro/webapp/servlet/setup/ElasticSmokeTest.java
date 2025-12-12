/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.http.ESHttpBasicClientFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * If we can't connect to ElasticSearch, add a Warning item to the StartupStatus.
 */
public class ElasticSmokeTest {

    private static final Log log = LogFactory.getLog(ElasticSmokeTest.class);

    private final ServletContextListener listener;

    public ElasticSmokeTest(ServletContextListener listener) {
        this.listener = listener;
    }

    public void doTest(ServletContextEvent sce) {
        final StartupStatus ss = StartupStatus.getBean(sce.getServletContext());

        String elasticUrlString = ConfigurationProperties.getBean(sce).getProperty("vitro.local.searchengine.url", "");
        if (elasticUrlString.isEmpty()) {
            ss.fatal(listener, "Can't connect to ElasticSearch engine. "
                + "runtime.properties must contain a value for "
                + "vitro.local.searchengine.url");
            return;
        }

        URL elasticUrl = null;

        try {
            elasticUrl = new URL(elasticUrlString);
        } catch (MalformedURLException e) {
            ss.fatal(listener, "Can't connect to ElasticSearch engine. "
                + "The value for vitro.local.searchengine.url "
                + "in runtime.properties is not a valid URL: '"
                + elasticUrlString + "'", e);
        }

        ss.info(listener, "Starting ElasticSearch test.");

        checkConnection(elasticUrl, ss);
    }

    private void checkConnection(URL elasticUrl, StartupStatus ss) {
        try {
            new ElasticPinger(elasticUrl).ping();
            reportGoodPing(ss);
        } catch (ElasticProblemException e) {
            reportPingProblem(ss, e);
        }
    }

    private void reportGoodPing(StartupStatus ss) {
        ss.info(listener, "The ElasticSearch server responded to a 'ping'.");
    }

    private void reportPingProblem(StartupStatus ss, ElasticProblemException e) {
        ss.warning(listener, "The ElasticSearch engine did not respond to a 'ping' request", e);
    }

    /**
     * Issue a "ping" to ElasticSearch. If we get here, we've already established
     * contact, so any error is a fatal one.
     */
    private static class ElasticPinger {
        private final URL elasticUrl;
        private final HttpClient httpClient;

        public ElasticPinger(URL elasticUrl) {
            this.elasticUrl = elasticUrl;
            this.httpClient = ESHttpBasicClientFactory.getHttpClient(elasticUrl.toString());
        }

        public void ping() throws ElasticProblemException {
            try {
                HttpGet method = new HttpGet(elasticUrl.toExternalForm());
                log.debug("Trying to ping ElasticSearch");
                HttpResponse response = httpClient.execute(method);
                try {
                    log.debug("Finished pinging ElasticSearch");
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != HttpStatus.SC_OK) {
                        throw new ElasticProblemException(statusCode);
                    }
                } finally {
                    EntityUtils.consume(response.getEntity());
                }
            } catch (IOException e) {
                throw new ElasticProblemException(e);
            }
        }
    }

    private static class ElasticProblemException extends Exception {
        private final int statusCode;

        ElasticProblemException(int statusCode) {
            super("HTTP status code = " + statusCode);
            this.statusCode = statusCode;
        }

        ElasticProblemException(Throwable cause) {
            super(cause);
            this.statusCode = 0;
        }

        int getStatusCode() {
            return this.statusCode;
        }
    }
}
