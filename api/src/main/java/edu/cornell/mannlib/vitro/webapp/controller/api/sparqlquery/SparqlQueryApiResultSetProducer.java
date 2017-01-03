/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery;

import static edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery.ResultSetMediaType.TSV;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.apache.commons.io.IOUtils;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.resultset.ResultsFormat;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.utils.http.AcceptHeaderParsingException;
import edu.cornell.mannlib.vitro.webapp.utils.http.ContentTypeUtil;
import edu.cornell.mannlib.vitro.webapp.utils.http.NotAcceptableException;

/**
 * Base class for processing SPARQL queries that produce Result Sets: SELECT and
 * ASK.
 */
abstract class SparqlQueryApiResultSetProducer extends SparqlQueryApiExecutor {
	protected final ResultSetMediaType mediaType;

	public SparqlQueryApiResultSetProducer(RDFService rdfService,
			String queryString, String acceptHeader)
			throws AcceptHeaderParsingException, NotAcceptableException {
		super(rdfService, queryString);

		Collection<String> contentTypes = ResultSetMediaType.contentTypes();
		String bestType = ContentTypeUtil.bestContentType(acceptHeader,
				contentTypes);
		this.mediaType = ResultSetMediaType.fromContentType(bestType);
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
		} else if (mediaType == TSV) {
			// ARQ doesn't support TSV, so we will do the translation.
			pipeWithReplacement(rawResult, out);
		} else {
			ResultSet rs = ResultSetFactory.fromJSON(rawResult);
			ResultsFormat format = ResultsFormat.lookup(mediaType
					.getJenaResponseFormat());
			ResultSetFormatter.output(out, rs, format);
		}
	}

	private void pipeWithReplacement(InputStream in, OutputStream out)
			throws IOException {
		int size;
		byte[] buffer = new byte[4096];
		while ((size = in.read(buffer)) > -1) {
			for (int i = 0; i < size; i++) {
				if (buffer[i] == ',') {
					buffer[i] = '\t';
				}
			}
			out.write(buffer, 0, size);
		}
	}

	/**
	 * Ask the RDFService to run the query, and get the resulting stream.
	 */
	protected abstract InputStream getRawResultStream()
			throws RDFServiceException;
}
