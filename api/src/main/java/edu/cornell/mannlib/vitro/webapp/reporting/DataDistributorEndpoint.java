/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.reporting;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

/**
 * Defines a data distributor endpoint to access the distributors Can be used in
 * the future to define multiple endpoints and configure datasources from
 * multiple instances
 */
public class DataDistributorEndpoint {
    private String url;

    public DataDistributorEndpoint(String url) {
        this.url = url;
    }

    public URI generateUri(String actionName, List<NameValuePair> parameters) {
        try {
            URIBuilder builder = new URIBuilder(url + actionName);
            if (parameters != null) {
                builder.addParameters(parameters);
            }
            return builder.build();
        } catch (URISyntaxException e) {
        }

        return null;
    }

    private static DataDistributorEndpoint defaultEndpoint;

    public static DataDistributorEndpoint getDefault() {
        return defaultEndpoint;
    }

    public static void setDefault(DataDistributorEndpoint endpoint) {
        defaultEndpoint = endpoint;
    }
}
