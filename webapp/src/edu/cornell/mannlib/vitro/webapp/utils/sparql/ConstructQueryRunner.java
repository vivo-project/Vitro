/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.sparql;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

/**
 * A conversational tool for handling SPARQL CONSTRUCT queries.
 * 
 * <pre>
 * Examples:
 *   Model m = createQueryContext(rdfService, queryString)
 *                             .bindVariableToUri("uri", uri)
 * 				               .execute();
 * 
 *   ConstructQueryHolder q = constructQuery(queryString)
 *                             .bindToUri("uri", uri));
 *   List<Map<String, String> map = createQueryContext(rdfService, q)
 *                             .execute();
 * </pre>
 */
public class ConstructQueryRunner {
	public static ConstructQueryHolder constructQuery(String queryString) {
		return new ConstructQueryHolder(queryString);
	}

	public static ConstructQueryContext createQueryContext(RDFService rdfService,
			String queryString) {
		return createQueryContext(rdfService, constructQuery(queryString));
	}

	public static ConstructQueryContext createQueryContext(RDFService rdfService,
			ConstructQueryHolder query) {
		return new RdfServiceConstructQueryContext(rdfService, query);
	}

	public static interface ConstructQueryContext {
		public ConstructQueryContext bindVariableToUri(String name, String uri);

		public Model execute();
	}

}
