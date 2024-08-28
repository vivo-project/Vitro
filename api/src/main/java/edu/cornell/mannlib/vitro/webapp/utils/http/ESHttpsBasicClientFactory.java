package edu.cornell.mannlib.vitro.webapp.utils.http;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

public class ESHttpsBasicClientFactory {

    private static final Log log = LogFactory.getLog(ESHttpsBasicClientFactory.class);

    private static volatile CloseableHttpClient httpsClient;

    private static void initializeClient() {
        String elasticUsername =
            ConfigurationProperties.getInstance().getProperty("vitro.local.searchengine.username", "");
        String elasticPassword =
            ConfigurationProperties.getInstance().getProperty("vitro.local.searchengine.password", "");

        if (elasticUsername.isEmpty() && elasticPassword.isEmpty()) {
            log.warn(
                "You haven't set up username and password for your ES/OS client. " +
                    "If this is intentional, it is strongly recommended to switch to " +
                    "basic authentication as well as https.)");
        }

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
            new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
            new UsernamePasswordCredentials(elasticUsername, elasticPassword)
        );

        try {
            SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();

            sslContextBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContextBuilder.build(),
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
            );

            RequestConfig requestConfig = RequestConfig.custom()
                .setCircularRedirectsAllowed(true)
                .build();

            httpsClient = HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .setDefaultCredentialsProvider(credsProvider)
                .setDefaultRequestConfig(requestConfig)
                .build();

        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new RuntimeException("Failed to initialize SSL context", e);
        }
    }

    public static CloseableHttpClient getHttpClient() {
        if (httpsClient == null) {
            synchronized (ESHttpsBasicClientFactory.class) {
                if (httpsClient == null) {
                    initializeClient();
                }
            }
        }
        return httpsClient;
    }
}
