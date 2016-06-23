/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.sparql;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

/**
 * A conversational tool for handling SPARQL queries.
 * 
 * {@code
 * Examples:
 *   List<String> values = createQueryContext(rdfService, queryString)
 *                             .bindVariableToUri("uri", uri)
 * 				               .execute()
 * 				               .getStringFields("partner")
 * 				               .flatten();
 * 
 *   SelectQueryHolder q = selectQuery(queryString)
 *                             .bindToUri("uri", uri));
 *   List<Map<String, String> map = createQueryContext(rdfService, q)
 *                             .execute()
 *                             .getStringFields();
 * }
 * 
 * The execute() method does not actually execute the query: it merely sets it
 * up syntactically.
 * 
 * If you don't supply any field names to getStringFields(), you get all of
 * them.
 * 
 * Any string value that returns a blank or empty string is omitted from the
 * results. Any row that returns no values is omitted from the results.
 */
public final class SelectQueryRunner {
	private static final Log log = LogFactory.getLog(SelectQueryRunner.class);

	private SelectQueryRunner() {
		// No need to create an instance.
	}

	public static SelectQueryHolder selectQuery(String queryString) {
		return new SelectQueryHolder(queryString);
	}

	public static SelectQueryContext createQueryContext(RDFService rdfService,
			String queryString) {
		return createQueryContext(rdfService, selectQuery(queryString));
	}

	public static SelectQueryContext createQueryContext(RDFService rdfService,
			SelectQueryHolder query) {
		return new RdfServiceQueryContext(rdfService, query);
	}

	public static interface SelectQueryContext {
		public SelectQueryContext bindVariableToUri(String name, String uri);

		public ExecutingSelectQueryContext execute();
	}

	public static interface ExecutingSelectQueryContext {
		public StringResultsMapping getStringFields(String... fieldNames);
	}

	public static interface StringResultsMapping extends
			List<Map<String, String>> {
		public List<String> flatten();

		public Set<String> flattenToSet();
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	static class StringResultsMappingImpl extends
			ArrayList<Map<String, String>> implements StringResultsMapping {

		@Override
		public List<String> flatten() {
			List<String> flat = new ArrayList<>();
			for (Map<String, String> map : this) {
				flat.addAll(map.values());
			}
			return flat;
		}

		@Override
		public Set<String> flattenToSet() {
			return new HashSet<>(flatten());
		}

	}

}
