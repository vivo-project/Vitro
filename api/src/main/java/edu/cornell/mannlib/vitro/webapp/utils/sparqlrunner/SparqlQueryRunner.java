/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner;

import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

/**
 * A conversational tool for handling SPARQL queries.
 * 
 * <pre>
 * Examples:
 *   List<String> values = createSelectQueryContext(rdfService, queryString)
 *                             .bindVariableToUri("uri", uri)
 * 				               .execute()
 * 				               .toStringFields("partner")
 * 				               .flatten();
 * 
 *   QueryHolder qh = queryHolder(queryString)
 *                             .bindToUri("uri", uri));
 *   List<Map<String, String> map = createSelectQueryContext(model, qh)
 *                             .execute()
 *                             .toStringFields();
 * </pre>
 * 
 * The query context can come from either an RDFService or a Model.
 * 
 * The execute() method does not actually execute the query: it merely sets it
 * up syntactically.
 * 
 * If you don't supply any field names to toStringFields(), you get all of
 * them.
 * 
 * Any string value that returns a blank or empty string is omitted from the
 * results. Any row that returns no values is omitted from the results.
 */
public final class SparqlQueryRunner {
	private static final Log log = LogFactory.getLog(SparqlQueryRunner.class);

	private SparqlQueryRunner() {
		// No need to create an instance.
	}

	public static QueryHolder queryHolder(String queryString) {
		return new QueryHolder(queryString);
	}

	// ------------- SELECT ----------- //
	
	public static SelectQueryContext createSelectQueryContext(RDFService rdfService,
			String queryString) {
		return createSelectQueryContext(rdfService, queryHolder(queryString));
	}

	public static SelectQueryContext createSelectQueryContext(RDFService rdfService,
			QueryHolder query) {
		return new RdfServiceSelectQueryContext(rdfService, query);
	}

	public static SelectQueryContext createSelectQueryContext(Model model,
			String queryString) {
		return createSelectQueryContext(model, queryHolder(queryString));
	}

	public static SelectQueryContext createSelectQueryContext(Model model,
			QueryHolder query) {
		return new ModelSelectQueryContext(model, query);
	}

	public static interface SelectQueryContext  {
		public SelectQueryContext bindVariableToUri(String name, String uri);

		public SelectQueryContext bindVariableToPlainLiteral(String name,
				String value);

		public ExecutingSelectQueryContext execute();
	}
	
	public static interface ExecutingSelectQueryContext {
		public StringResultsMapping toStringFields(String... fieldNames);
		
		public <T> T parse(ResultSetParser<T> parser);
		
		public void writeToOutput(OutputStream output);
	}
	
	// ------------- CONSTRUCT ----------- //
	
	public static ConstructQueryContext createConstructQueryContext(RDFService rdfService,
			String queryString) {
		return createConstructQueryContext(rdfService, queryHolder(queryString));
	}
	
	public static ConstructQueryContext createConstructQueryContext(RDFService rdfService,
			QueryHolder query) {
		return new RdfServiceConstructQueryContext(rdfService, query);
	}
	
	public static ConstructQueryContext createConstructQueryContext(Model model,
			String queryString) {
		return createConstructQueryContext(model, queryHolder(queryString));
	}
	
	public static ConstructQueryContext createConstructQueryContext(Model model,
			QueryHolder query) {
		return new ModelConstructQueryContext(model, query);
	}
	
	public static interface ConstructQueryContext{
		public ConstructQueryContext bindVariableToUri(String name, String uri);

		public ConstructQueryContext bindVariableToPlainLiteral(String name,
				String value);
		
		public ExecutingConstructQueryContext execute();
	}
	
	public static interface ExecutingConstructQueryContext {
		public Model toModel();
	}
	
}
