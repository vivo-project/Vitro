/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;

import org.apache.commons.io.IOUtils;

import com.github.jsonldjava.core.JSONLD;
import com.github.jsonldjava.core.JSONLDProcessingError;
import com.github.jsonldjava.impl.JenaRDFParser;
import com.github.jsonldjava.utils.JSONUtils;
import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ModelSerializationFormat;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.utils.http.AcceptHeaderParsingException;
import edu.cornell.mannlib.vitro.webapp.utils.http.ContentTypeUtil;
import edu.cornell.mannlib.vitro.webapp.utils.http.NotAcceptableException;

/**
 * Base class for processing SPARQL queries that produce RDF: CONSTRUCT and
 * DESCRIBE.
 */
abstract class SparqlQueryApiRdfProducer extends SparqlQueryApiExecutor {
	protected final RdfResultMediaType mediaType;

	public SparqlQueryApiRdfProducer(RDFService rdfService, String queryString,
			String acceptHeader) throws AcceptHeaderParsingException,
			NotAcceptableException {
		super(rdfService, queryString);

		Collection<String> contentTypes = RdfResultMediaType.contentTypes();
		String bestType = ContentTypeUtil.bestContentType(acceptHeader,
				contentTypes);
		this.mediaType = RdfResultMediaType.fromContentType(bestType);
	}

	@Override
	public String getMediaType() {
		return mediaType.getContentType();
	}

	@Override
	public void executeAndFormat(OutputStream out) throws RDFServiceException,
			IOException {

		InputStream rawResult = getRawResultStream();

		if (mediaType.isNativeFormat()) {
			IOUtils.copy(rawResult, out);
		} else if (mediaType.getJenaResponseFormat().equals("JSON")) {
			// JSON-LD is a special case, since jena 2.6.4 doesn't support it.
			try {
				JenaRDFParser parser = new JenaRDFParser();
				Object json = JSONLD.fromRDF(parseToModel(rawResult), parser);
				JSONUtils.write(new OutputStreamWriter(out, "UTF-8"), json);
			} catch (JSONLDProcessingError e) {
				throw new RDFServiceException(
						"Could not convert from Jena model to JSON-LD", e);
			}
		} else {
			parseToModel(rawResult).write(out,
					mediaType.getJenaResponseFormat());
		}
	}

	private Model parseToModel(InputStream rawResult) {
		ModelSerializationFormat format = ModelSerializationFormat
				.valueOf(mediaType.getSerializationFormat());
		return RDFServiceUtils.parseModel(rawResult, format);
	}

	/**
	 * Ask the RDFService to run the query, and get the resulting stream.
	 */
	protected abstract InputStream getRawResultStream()
			throws RDFServiceException;

}
