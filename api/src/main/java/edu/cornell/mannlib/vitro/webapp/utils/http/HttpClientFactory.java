/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.http;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

public final class HttpClientFactory {
    private static final DefaultHttpClient httpClient;

    static {
        PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
        cm.setDefaultMaxPerRoute(50);
        cm.setMaxTotal(300);
        httpClient = new DefaultHttpClient(cm);
    }

    public static HttpClient getHttpClient() {
        return httpClient;
    }
}
