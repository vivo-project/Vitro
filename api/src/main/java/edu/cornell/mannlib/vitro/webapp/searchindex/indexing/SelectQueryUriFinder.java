/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.indexing;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONTENT;
import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createSelectQueryContext;
import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.queryHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ContextModelsUser;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Validation;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.QueryHolder;

/**
 * Find URIs based on one or more select queries.
 * 
 * If the statement qualifies, execute the queries and return the accumulated
 * results.
 * 
 * A statement qualifies if the predicate matches any of the restrictions, or if
 * there are no restrictions.
 * 
 * If a query contains a ?subject, ?predicate, or ?object variable, it will be
 * bound to the URI of the subject, predicate, or object of the statement,
 * respectively. If the subject or object has no URI and the query expects one,
 * then the query will be ignored. (Predicates always have URIs.)
 * 
 * All of the result fields of all result rows of all of the queries will be
 * returned.
 * 
 * A label may be supplied to the instance, for use in logging. If no label is
 * supplied, one will be generated.
 */
public class SelectQueryUriFinder implements IndexingUriFinder,
		ContextModelsUser {
	private static final Log log = LogFactory
			.getLog(SelectQueryUriFinder.class);

	private RDFService rdfService;

	/** A name to be used in logging, to identify this instance. */
	private String label;

	/** The queries to be executed. There must be at least one. */
	private List<String> queries = new ArrayList<>();

	/**
	 * URIs of the predicates that will trigger these queries. If empty, then
	 * the queries apply to all statements.
	 */
	private Set<String> predicateRestrictions = new HashSet<>();

	@Override
	public void setContextModels(ContextModelAccess models) {
		this.rdfService = models.getRDFService(CONTENT);
	}

	@Property(uri = "http://www.w3.org/2000/01/rdf-schema#label")
	public void setLabel(String l) {
		label = l;
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasSelectQuery", minOccurs = 1)
	public void addQuery(String query) {
		queries.add(query);
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasPredicateRestriction")
	public void addPredicateRestriction(String predicateUri) {
		predicateRestrictions.add(predicateUri);
	}

	@Validation
	public void validate() {
		if (label == null) {
			label = this.getClass().getSimpleName() + ":" + this.hashCode();
		}
	}

	@Override
	public String toString() {
		return (label == null) ? super.toString() : label;
	}

	@Override
	public void startIndexing() {
		// Nothing to do.
	}

	@Override
	public List<String> findAdditionalURIsToIndex(Statement stmt) {
		List<String> list = new ArrayList<>();
		if (passesTypePredicateRestrictions(stmt)) {
			for (String query : queries) {
				list.addAll(getUrisForQuery(stmt, query));
			}
		}
		return list;
	}

	private boolean passesTypePredicateRestrictions(Statement stmt) {
		return predicateRestrictions.isEmpty()
				|| predicateRestrictions.contains(stmt.getPredicate().getURI());
	}

	private List<String> getUrisForQuery(Statement stmt, String queryString) {
		QueryHolder query = queryHolder(queryString);
		query = query.bindToUri("predicate", stmt.getPredicate().getURI());

		query = tryToBindUri(query, "subject", stmt.getSubject());
		query = tryToBindUri(query, "object", stmt.getObject());
		if (query == null) {
			return Collections.emptyList();
		}

		return createSelectQueryContext(rdfService, query).execute()
				.toStringFields().flatten();
	}

	private QueryHolder tryToBindUri(QueryHolder query, String name,
			RDFNode node) {
		if (query == null) {
			return null;
		}
		if (!query.hasVariable(name)) {
			return query;
		}
		if (!node.isURIResource()) {
			return null;
		}
		return query.bindToUri(name, node.asResource().getURI());
	}

	@Override
	public void endIndexing() {
		// Nothing to do.
	}

}
