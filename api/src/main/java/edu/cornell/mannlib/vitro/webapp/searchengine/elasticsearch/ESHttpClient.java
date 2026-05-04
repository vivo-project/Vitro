/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

@WebListener
public class ESHttpClient implements ServletContextListener {

    private static final Log log = LogFactory.getLog(ESHttpClient.class);

    private static ESHttpClient INSTANCE = new ESHttpClient();

    private volatile CloseableHttpClient httpClient;

    private volatile PoolingHttpClientConnectionManager connectionManager;

    public static ESHttpClient getInstance() {
        return INSTANCE;
    }

    public static CloseableHttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
        if (INSTANCE.httpClient == null) {
            throw new RuntimeException("ESHttpClient wasn't initialized.");
        }
        return INSTANCE.httpClient.execute(request);
    }

    static void initialize(String baseUrl) {
        boolean isHttps = baseUrl.startsWith("https");
        getInstance().createHttpClient(isHttps);
    }

    private void createHttpClient(boolean isHttps) {
        String elasticUsername = ConfigurationProperties.getInstance()
            .getProperty("vitro.local.searchengine.username", "");
        String elasticPassword = ConfigurationProperties.getInstance()
            .getProperty("vitro.local.searchengine.password", "");

        boolean hasCredentials = !elasticUsername.isEmpty() && !elasticPassword.isEmpty();

        if (isHttps && !hasCredentials) {
            log.warn("Using HTTPS without authentication. This is not recommended for production.");
        }

        HttpClientBuilder builder = HttpClients.custom()
            .setConnectionManager(getConnectionManager(isHttps))
            .setConnectionManagerShared(true);

        if (hasCredentials) {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(elasticUsername, elasticPassword)
            );
            builder.setDefaultCredentialsProvider(credsProvider);
        }

        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(30000) // 30 seconds
            .setSocketTimeout(60000)  // 60 seconds
            .setConnectionRequestTimeout(30000) // 30 seconds
            .setCircularRedirectsAllowed(true)
            .build();

        httpClient = builder
            .setDefaultRequestConfig(requestConfig)
            .setConnectionTimeToLive(60, TimeUnit.SECONDS) // TTL for persistent connections
            .build();
    }

    private PoolingHttpClientConnectionManager getConnectionManager(boolean isHttps) {
        if (connectionManager != null) {
            return connectionManager;
        }
        if (isHttps) {
            try {
                SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(null, (chain, authType) -> true) // Trust all certificates
                    .build();

                connectionManager = new PoolingHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("https", new SSLConnectionSocketFactory(sslContext,
                            (hostname, session) -> true)) // Allow all hostnames
                        .build()
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to create SSL connection manager", e);
            }
        } else {
            connectionManager = new PoolingHttpClientConnectionManager();
        }

        connectionManager.setDefaultMaxPerRoute(50);
        connectionManager.setMaxTotal(300);
        connectionManager.setValidateAfterInactivity(30000);
        return connectionManager;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
            if (connectionManager != null) {
                connectionManager.close();
            }
        } catch (Exception e) {
            log.error(e, e);
        }
    }
}
