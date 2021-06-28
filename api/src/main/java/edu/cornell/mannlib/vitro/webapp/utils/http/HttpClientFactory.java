/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.utils.http;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public final class HttpClientFactory {
    
	private static final HttpClient httpClient;

    static {
    	
    	PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(300);
    	cm.setDefaultMaxPerRoute(50);
    	
    	httpClient = HttpClients.custom().setConnectionManager(cm).build();
   	
    }

    public static HttpClient getHttpClient() {
        return httpClient;
    }
}
