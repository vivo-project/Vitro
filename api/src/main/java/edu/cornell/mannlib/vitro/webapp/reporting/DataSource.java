/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reporting;

import static edu.cornell.mannlib.vitro.webapp.dao.ReportingDao.PROPERTY_DISTRIBUTORNAME;
import static edu.cornell.mannlib.vitro.webapp.dao.ReportingDao.PROPERTY_DISTRIBUTORRANK;
import static edu.cornell.mannlib.vitro.webapp.dao.ReportingDao.PROPERTY_OUTPUTNAME;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.net.ssl.SSLContext;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

/**
 * Defines a datasource for the report
 */
public class DataSource {
    private String distributorName;
    private String outputName;
    private int rank;

    public String getBody(Map<String, String[]> parameterMap) {
        // Currently, only supporting the default endpoint. Eventually, this might be
        // configurable
        // so that the api of other installations can be used in a report
        DataDistributorEndpoint endpoint = DataDistributorEndpoint.getDefault();
        URI myUri = endpoint.generateUri(distributorName, null);

        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (certificate, authType) -> true)
                    .build();
            HttpClient httpClient = HttpClients.custom().setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
            HttpResponse sourceResponse = httpClient.execute(new HttpGet(myUri));
            return IOUtils.toString(sourceResponse.getEntity().getContent(), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    @Property(uri = PROPERTY_DISTRIBUTORNAME, minOccurs = 1, maxOccurs = 1)
    public void setDistributorName(String name) {
        distributorName = name;
    }

    @Property(uri = PROPERTY_DISTRIBUTORRANK, minOccurs = 0, maxOccurs = 1)
    public void setRank(Integer rank) {
        if (rank != null) {
            this.rank = rank;
        }
    }

    @Property(uri = PROPERTY_OUTPUTNAME, minOccurs = 1, maxOccurs = 1)
    public void setOutputName(String name) {
        outputName = name;
    }

    public String getOutputName() {
        return outputName;
    }

    public String getDistributorName() {
        return distributorName;
    }

    public int getRank() {
        return rank;
    }
}
