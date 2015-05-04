/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.virtuoso;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.sparql.RDFServiceSparql;

/**
 * For now, at least, it is just like an RDFServiceSparql except:
 * 
 * A username and password are required. These should refer to a Virtuoso user
 * that posesses the SPARQL_UPDATE role.
 * 
 * The endpoint URI and the update endpoint URI are derived from the base URI.
 *   You provide: http://localhost:8890
 *   endpoint is: http://localhost:8890/sparql/
 *   update is:   http://localhost:8890/DAV/home/username/rdf_sink/vitro_update
 * 
 * A change in the syntax of an UPDATE request: "INSERT DATA" becomes "INSERT".
 * This fixes a problem with inserting blank nodes.
 * 
 * The HTTP request is equipped with the username and password, to answer a
 * challenge for basic authentication.
 * 
 * Allow for the nonNegativeInteger bug when checking to see whether a graph has
 * changed.
 */
public class RDFServiceVirtuoso extends RDFServiceSparql {
	private static final Log log = LogFactory.getLog(RDFServiceVirtuoso.class);

	private final String username;
	private final String password;

	public RDFServiceVirtuoso(String baseURI, String username, String password) {
		super(figureReadEndpointUri(baseURI), figureUpdateEndpointUri(baseURI,
				username));
		this.username = username;
		this.password = password;
	}

	private static String figureReadEndpointUri(String baseUri) {
		return noTrailingSlash(baseUri) + "/sparql/";
	}

	private static String figureUpdateEndpointUri(String baseUri,
			String username) {
		return noTrailingSlash(baseUri) + "/DAV/home/" + username
				+ "/rdf_sink/vitro_update";
	}

	private static String noTrailingSlash(String uri) {
		return uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
	}

	@Override
	protected void executeUpdate(String updateString)
			throws RDFServiceException {
		updateString = tweakUpdateStringSyntax(updateString);
		log.debug("UPDATE STRING: " + updateString);

		try {
			HttpResponse response = httpClient.execute(
					createHttpRequest(updateString), createHttpContext());
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode > 399) {
				log.error("response " + response.getStatusLine()
						+ " to update. \n");

				try (InputStream content = response.getEntity().getContent()) {
					for (String line : IOUtils.readLines(content)) {
						log.error("response-line >>" + line);
					}
				}

				throw new RDFServiceException(
						"Unable to perform SPARQL UPDATE: status code = "
								+ statusCode);
			}
		} catch (Exception e) {
			log.error("Failed to update: " + updateString, e);
			throw new RDFServiceException(
					"Unable to perform change set update", e);
		}
	}

	private String tweakUpdateStringSyntax(String updateString) {
		if (updateString.startsWith("INSERT DATA")) {
			return updateString.replaceFirst("INSERT DATA", "INSERT");
		}
		return updateString;
	}

	// TODO entity.setContentType("application/sparql-query");
	private HttpPost createHttpRequest(String updateString) {
		HttpPost meth = new HttpPost(updateEndpointURI);
		meth.addHeader("Content-Type", "application/sparql-query");
		try {
			meth.setEntity(new StringEntity(updateString, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// UTF-8 is unsupported?
			throw new RuntimeException(e);
		}
		return meth;
	}

	/**
	 * We need an HttpContext that will provide username and password in
	 * response to a basic authentication challenge.
	 */
	private HttpContext createHttpContext() {
		CredentialsProvider provider = new BasicCredentialsProvider();
		provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
				username, password));

		BasicHttpContext context = new BasicHttpContext();
		context.setAttribute(ClientContext.CREDS_PROVIDER, provider);
		return context;
	}

	/**
	 * Virtuoso has a bug which it shares with TDB: if given a literal of type
	 * xsd:nonNegativeInteger, it stores a literal of type xsd:integer.
	 * 
	 * To determine whether this serialized graph is equivalent to what is
	 * already in Virtuoso, we need to do the same.
	 */
	@Override
	public boolean isEquivalentGraph(String graphURI,
			InputStream serializedGraph,
			ModelSerializationFormat serializationFormat)
			throws RDFServiceException {
		return super.isEquivalentGraph(graphURI,
				adjustForNonNegativeIntegers(serializedGraph),
				serializationFormat);
	}

	/**
	 * Convert all of the references to "nonNegativeInteger" to "integer" in
	 * this serialized graph.
	 * 
	 * This isn't rigorous: it could fail if another property contained the text
	 * "nonNegativeInteger" in its name, or if that text were used as part of a
	 * string literal. If that happens before this Virtuoso bug is fixed, we'll
	 * need to improve this method.
	 * 
	 * It also isn't scalable: if we wanted real scalability, we would write to
	 * a temporary file as we converted.
	 */
	private InputStream adjustForNonNegativeIntegers(InputStream serializedGraph)
			throws RDFServiceException {
		try {
			String raw = IOUtils.toString(serializedGraph, "UTF-8");
			String modified = raw.replace("nonNegativeInteger", "integer");
			return new ByteArrayInputStream(modified.getBytes("UTF-8"));
		} catch (IOException e) {
			throw new RDFServiceException(e);
		}
	}

}
