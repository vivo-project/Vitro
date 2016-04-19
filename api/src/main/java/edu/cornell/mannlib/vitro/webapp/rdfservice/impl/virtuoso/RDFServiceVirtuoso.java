/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.virtuoso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.sparql.RDFServiceSparql;
import org.apache.http.util.EntityUtils;

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
		super(figureReadEndpointUri(baseURI), figureUpdateEndpointUri(baseURI, username));
		this.username = username;
		this.password = password;
		testConnection();
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
			HttpPost request = createHttpRequest(updateString);
			HttpContext context = getContext(request);
			HttpResponse response = context != null ? httpClient.execute(request, context) : httpClient.execute(request);
			try {
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
			} finally {
				EntityUtils.consume(response.getEntity());
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

	protected UsernamePasswordCredentials getCredentials() {
		if (username != null && password != null) {
			return new UsernamePasswordCredentials(username, password);
		}

		return null;
	}

	private static boolean isNumeric(String typeUri) {
		return typeUri != null && (typeUri.endsWith("decimal") ||
				typeUri.endsWith("int") ||
				typeUri.endsWith("integer") ||
				typeUri.endsWith("float") ||
				typeUri.endsWith("long") ||
				typeUri.endsWith("negativeInteger") ||
				typeUri.endsWith("nonNegativeInteger") ||
				typeUri.endsWith("nonPositiveInteger") ||
				typeUri.endsWith("positiveInteger") ||
				typeUri.endsWith("short") ||
				typeUri.endsWith("unsignedLong") ||
				typeUri.endsWith("unsignedInt") ||
				typeUri.endsWith("unsignedShort") ||
				typeUri.endsWith("unsignedByte"));
	}

	/**
	 * Virtuoso has a bug which it shares with TDB: if given a literal of type
	 * xsd:nonNegativeInteger, it stores a literal of type xsd:integer.
	 * 
	 * To determine whether this serialized graph is equivalent to what is
	 * already in Virtuoso, we need to do the same.
	 */
	public boolean isEquivalentGraph(String graphURI, InputStream serializedGraph,
									 ModelSerializationFormat serializationFormat) throws RDFServiceException {
		Model fileModel = RDFServiceUtils.parseModel(serializedGraph, serializationFormat);
		Model tripleStoreModel = new RDFServiceDataset(this).getNamedModel(graphURI);
		Model fromTripleStoreModel = ModelFactory.createDefaultModel().add(tripleStoreModel);

		// Compare the models
		Model difference = fileModel.difference(fromTripleStoreModel);

		// If there is a difference
		if (difference.size() > 0) {
			// First, normalize the numeric values, as Virtuoso likes to mess with the datatypes
			// Iterate over the differences
			StmtIterator stmtIterator = difference.listStatements();
			while (stmtIterator.hasNext()) {
				final Statement stmt = stmtIterator.next();
				final RDFNode subject = stmt.getSubject();
				final Property predicate = stmt.getPredicate();
				final RDFNode object = stmt.getObject();

				// If the object is a numeric literal
				if (object.isLiteral() &&  isNumeric(object.asLiteral().getDatatypeURI())) {
					// Find a matching statement in the triple store, based on normalized numeric values
					StmtIterator matching = fromTripleStoreModel.listStatements(new Selector() {
						@Override
						public boolean test(Statement statement) {
							RDFNode objectToMatch = statement.getObject();

							// Both values are numeric, so compare them as parsed doubles
							if (objectToMatch.isLiteral()) {
								String num1 = object.asLiteral().getString();
								String num2 = objectToMatch.asLiteral().getString();

								return Double.parseDouble(num1) == Double.parseDouble(num2);
							}

							return false;
						}

						@Override
						public boolean isSimple() {
							return false;
						}

						@Override
						public Resource getSubject() {
							return subject.asResource();
						}

						@Override
						public Property getPredicate() {
							return predicate;
						}

						@Override
						public RDFNode getObject() {
							return null;
						}
					});

					// For every matching statement
					// Rewrite the object as the one in the file model (they are the same, just differ in datatype)
					List<Statement> toModify = new ArrayList<Statement>();
					while (matching.hasNext()) {
						toModify.add(matching.next());
					}

					for (Statement stmtToModify : toModify) {
						stmtToModify.changeObject(object);
					}
				}
			}

			// Now we've normalized the datatypes, check the graphs are isomorphic
			return fileModel.isIsomorphicWith(fromTripleStoreModel);
		}

		return true;
	}

	/**
	 * TDB has a bug: if given a literal of type xsd:nonNegativeInteger, it
	 * stores a literal of type xsd:integer.
	 *
	 * To determine whether this serialized graph is equivalent to what's in
	 * TDB, we need to do the same.
	 */
	@Override
	public boolean isEquivalentGraph(String graphURI,
									 Model graph)
			throws RDFServiceException {

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		graph.write(buffer, "N-TRIPLE");
		InputStream inStream = new ByteArrayInputStream(buffer.toByteArray());
		return isEquivalentGraph(graphURI, inStream, ModelSerializationFormat.NTRIPLE);
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
