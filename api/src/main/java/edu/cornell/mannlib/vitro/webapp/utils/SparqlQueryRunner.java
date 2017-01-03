/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

/**
 * Execute SPARQL queries against a model.
 * 
 * Take the model in the constructor. Then execute as many queries as desired,
 * with the query contained in a String. Exceptions are handled in a tidy
 * manner, and the query environment is closed properly in any case.
 */
public class SparqlQueryRunner {
	private static final Log log = LogFactory.getLog(SparqlQueryRunner.class);

	private static final Syntax SYNTAX = Syntax.syntaxARQ;

	private final Model model;

	public SparqlQueryRunner(Model model) {
		if (model == null) {
			throw new NullPointerException("model may not be null.");
		}
		this.model = model;
	}

	/**
	 * Execute the SELECT query and parse the results, closing and cleaning up
	 * afterward. If an exception occurs, return the parser's default value.
	 */
	public <T> T executeSelect(QueryParser<T> parser, String queryStr) {
		if (parser == null) {
			throw new NullPointerException("parser may not be null.");
		}
		if (queryStr == null) {
			throw new NullPointerException("queryStr may not be null.");
		}

		log.debug("select query is: '" + queryStr + "'");
		QueryExecution qe = null;
		try {
			Query query = QueryFactory.create(queryStr, SYNTAX);
			qe = QueryExecutionFactory.create(query, model);
			return parser.parseResults(queryStr, qe.execSelect());
		} catch (Exception e) {
			log.error("Failed to execute the Select query: " + queryStr, e);
			return parser.defaultValue();
		} finally {
			if (qe != null) {
				qe.close();
			}
		}
	}

	/**
	 * Execute the CONSTRUCT query and return the resulting model. If an
	 * exception occurs, return an empty model.
	 */
	public Model executeConstruct(String queryStr) {
		log.debug("construct query is: '" + queryStr + "'");
		QueryExecution qe = null;
		try {
			Query query = QueryFactory.create(queryStr, SYNTAX);
			qe = QueryExecutionFactory.create(query, model);
			return qe.execConstruct();
		} catch (Exception e) {
			log.error("Failed to execute the Construct query: " + queryStr, e);
			return ModelFactory.createDefaultModel();
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

		protected String ifResourcePresent(QuerySolution solution,
				String variableName, String defaultValue) {
			RDFNode node = solution.get(variableName);
			if (node == null || !node.isURIResource()) {
				return defaultValue;
			}
			return node.asResource().getURI();
		}

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
	
	public static String bindValues(String rawString, VariableValue... values) {
		String queryString = rawString;
		for (VariableValue value: values) {
			queryString = value.bind(queryString);
		}
		return queryString;
	}
	
	public static UriValue uriValue(String name, String uri) {
		return new UriValue(name, uri);
	}

	public interface VariableValue {
		String bind(String rawString);
	}
	
	private static class UriValue implements VariableValue {
		private final String name;
		private final String uri;
		
		public UriValue(String name, String uri) {
			this.name = name;
			this.uri = uri;
		}

		@Override
		public String bind(String rawString) {
			return rawString.replaceAll("\\?" + name + "\\b", "<" + uri + ">");
		}
		
	}
}
