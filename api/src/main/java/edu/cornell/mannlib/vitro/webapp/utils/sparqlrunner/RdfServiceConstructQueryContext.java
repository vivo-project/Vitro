/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner;

import static edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ModelSerializationFormat.NTRIPLE;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.ConstructQueryContext;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.ExecutingConstructQueryContext;

/**
 * TODO
 */
public class RdfServiceConstructQueryContext implements ConstructQueryContext {
	private static final Log log = LogFactory
			.getLog(RdfServiceConstructQueryContext.class);

	private final RDFService rdfService;
	private final QueryHolder query;

	public RdfServiceConstructQueryContext(RDFService rdfService,
			QueryHolder query) {
		this.rdfService = rdfService;
		this.query = query;
	}

	@Override
	public ConstructQueryContext bindVariableToUri(String name, String uri) {
		return new RdfServiceConstructQueryContext(rdfService, query.bindToUri(
				name, uri));
	}

	@Override
	public ConstructQueryContext bindVariableToPlainLiteral(String name,
			String value) {
		return new RdfServiceConstructQueryContext(rdfService,
				query.bindToPlainLiteral(name, value));
	}

	@Override
	public ExecutingConstructQueryContext execute() {
		return new RdfServiceExecutingConstructQueryContext(rdfService, query);
	}

	@Override
	public String toString() {
		return "RdfServiceConstructQueryContext[query=" + query + "]";
	}

	private static class RdfServiceExecutingConstructQueryContext implements
			ExecutingConstructQueryContext {
		private final RDFService rdfService;
		private final QueryHolder query;

		public RdfServiceExecutingConstructQueryContext(RDFService rdfService,
				QueryHolder query) {
			this.rdfService = rdfService;
			this.query = query;
		}

		@Override
		public Model toModel() {
			QueryExecution qe = null;
			try {
				return RDFServiceUtils.parseModel(rdfService
						.sparqlConstructQuery(query.getQueryString(), NTRIPLE),
						NTRIPLE);
			} catch (Exception e) {
				log.error(
						"problem while running query '"
								+ query.getQueryString() + "'", e);
				return ModelFactory.createDefaultModel();
			} finally {
				if (qe != null) {
					qe.close();
				}
			}
		}

	}
}
