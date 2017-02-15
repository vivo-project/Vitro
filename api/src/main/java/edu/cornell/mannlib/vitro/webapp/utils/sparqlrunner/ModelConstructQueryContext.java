/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.ConstructQueryContext;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.ExecutingConstructQueryContext;

/**
 * TODO
 */
public class ModelConstructQueryContext implements ConstructQueryContext {
	private static final Log log = LogFactory
			.getLog(ModelConstructQueryContext.class);

	private static final Syntax SYNTAX = Syntax.syntaxARQ;

	private final Model model;
	private final QueryHolder query;

	public ModelConstructQueryContext(Model model, QueryHolder query) {
		this.model = model;
		this.query = query;
	}

	@Override
	public ConstructQueryContext bindVariableToUri(String name, String uri) {
		return new ModelConstructQueryContext(model, query.bindToUri(name, uri));
	}

	@Override
	public ConstructQueryContext bindVariableToPlainLiteral(String name,
			String value) {
		return new ModelConstructQueryContext(model, query.bindToPlainLiteral(
				name, value));
	}

	@Override
	public String toString() {
		return "ModelConstructQueryContext[query=" + query + "]";
	}

	@Override
	public ExecutingConstructQueryContext execute() {
		return new ModelExecutingConstructQueryContext(model, query);
	}

	private static class ModelExecutingConstructQueryContext implements
			ExecutingConstructQueryContext {
		private final Model model;
		private final QueryHolder query;

		public ModelExecutingConstructQueryContext(Model model, QueryHolder query) {
			this.model = model;
			this.query = query;
		}

		@Override
		public Model toModel() {
			QueryExecution qe = null;
			try {
				Query q = QueryFactory.create(query.getQueryString(), SYNTAX);
				qe = QueryExecutionFactory.create(q, model);
				return qe.execConstruct();
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
