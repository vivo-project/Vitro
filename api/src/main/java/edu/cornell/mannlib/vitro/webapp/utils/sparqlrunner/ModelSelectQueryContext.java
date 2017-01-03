/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.ExecutingSelectQueryContext;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.SelectQueryContext;

/**
 * An implementation of QueryContext based on a Model.
 * 
 * Package access. Instances should be created only by SparqlQueryRunner, or by
 * a method on this class.
 */
class ModelSelectQueryContext implements SelectQueryContext {
	private static final Log log = LogFactory
			.getLog(ModelSelectQueryContext.class);

	private static final Syntax SYNTAX = Syntax.syntaxARQ;

	private final Model model;
	private final QueryHolder query;

	public ModelSelectQueryContext(Model model, QueryHolder query) {
		this.model = model;
		this.query = query;
	}

	@Override
	public ModelSelectQueryContext bindVariableToUri(String name, String uri) {
		return new ModelSelectQueryContext(model, query.bindToUri(name, uri));
	}

	@Override
	public ModelSelectQueryContext bindVariableToPlainLiteral(String name,
			String value) {
		return new ModelSelectQueryContext(model, query.bindToPlainLiteral(
				name, value));
	}

	@Override
	public String toString() {
		return "ModelSelectQueryContext[query=" + query + "]";
	}

	@Override
	public ExecutingSelectQueryContext execute() {
		return new ModelExecutingQueryContext(model, query);
	}

	private static class ModelExecutingQueryContext implements
			ExecutingSelectQueryContext {
		private final Model model;
		private final QueryHolder query;

		public ModelExecutingQueryContext(Model model, QueryHolder query) {
			this.model = model;
			this.query = query;
		}

		@Override
		public StringResultsMapping toStringFields(String... names) {
			String qString = query.getQueryString();
			Set<String> fieldNames = new HashSet<>(Arrays.asList(names));
			try {
				Query q = QueryFactory.create(qString, SYNTAX);
				QueryExecution qexec = QueryExecutionFactory.create(q, model);
				try {
					ResultSet results = qexec.execSelect();
					return new StringResultsMapping(results, fieldNames);
				} finally {
					qexec.close();
				}
			} catch (Exception e) {
				log.error("problem while running query '" + qString + "'", e);
				return StringResultsMapping.EMPTY;
			}
		}

		@Override
		public <T> T parse(ResultSetParser<T> parser) {
			String qString = query.getQueryString();
			try {
				Query q = QueryFactory.create(qString, SYNTAX);
				QueryExecution qexec = QueryExecutionFactory.create(q, model);
				try {
					return parser.parseResults(qString, qexec.execSelect());
				} finally {
					qexec.close();
				}
			} catch (Exception e) {
				log.error("problem while running query '" + qString + "'", e);
				return parser.defaultValue();
			}
		}

		@Override
		public void writeToOutput(OutputStream output) {
			String qString = query.getQueryString();
			try {
				Query q = QueryFactory.create(qString, SYNTAX);
				QueryExecution qexec = QueryExecutionFactory.create(q, model);
				try {
					ResultSetFormatter.outputAsJSON(output, qexec.execSelect());
				} finally {
					qexec.close();
				}
			} catch (Exception e) {
				log.error("problem while running query '" + qString + "'", e);
			}
		}

	}
}
