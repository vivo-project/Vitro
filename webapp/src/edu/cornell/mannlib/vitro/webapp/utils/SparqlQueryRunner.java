/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;

/**
 * Execute a SPARQL query.
 * 
 * Take the model and a parser in the constructor. Then execute as many queries
 * as desired, with the query contained in a String.
 * 
 * If there is an exception while parsing the query, executing the query, or
 * parsing the results, log the exception and return the parser's default value.
 * The query enbvironment is closed properly in any case.
 */
public class SparqlQueryRunner<T> {
	private static final Log log = LogFactory.getLog(SparqlQueryRunner.class);

	private static final Syntax SYNTAX = Syntax.syntaxARQ;

	private final OntModel model;
	private final QueryParser<T> parser;

	public SparqlQueryRunner(OntModel model, QueryParser<T> parser) {
		this.model = model;
		this.parser = parser;
	}

	/**
	 * Execute the query and parse the results, closing and cleaning up
	 * afterward. If an exception occurs, return the parser's default value.
	 */
	public T executeQuery(String queryStr) {
		log.debug("query is: '" + queryStr + "'");
		QueryExecution qe = null;
		try {
			Query query = QueryFactory.create(queryStr, SYNTAX);
			qe = QueryExecutionFactory.create(query, model);
			return parser.parseResults(queryStr, qe.execSelect());
		} catch (Exception e) {
			log.error("Failed to execute the query: " + queryStr, e);
			return parser.defaultValue();
		} finally {
			if (qe != null) {
				qe.close();
			}
		}
	}

	/**
	 * This template class provides some parsing methods to help in the
	 * parseResults() method.
	 */
	public static abstract class QueryParser<T> {
		protected abstract T parseResults(String queryStr, ResultSet results);

		protected abstract T defaultValue();

		protected String ifLiteralPresent(QuerySolution solution,
				String variableName, String defaultValue) {
			Literal literal = solution.getLiteral(variableName);
			if (literal == null) {
				return defaultValue;
			} else {
				return literal.getString();
			}
		}

		protected long ifLongPresent(QuerySolution solution,
				String variableName, long defaultValue) {
			Literal literal = solution.getLiteral(variableName);
			if (literal == null) {
				return defaultValue;
			} else {
				return literal.getLong();
			}
		}

		protected int ifIntPresent(QuerySolution solution, String variableName,
				int defaultValue) {
			Literal literal = solution.getLiteral(variableName);
			if (literal == null) {
				return defaultValue;
			} else {
				return literal.getInt();
			}
		}

	}

}
