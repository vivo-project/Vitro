/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONTENT;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.ALLTEXT;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.ALLTEXTUNSTEMMED;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ContextModelsUser;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Validation;

/**
 * If the individual qualifies, execute the SPARQL queries and add the results
 * to the specified search fields.
 * 
 * If there are no specified search fields, ALLTEXT and ALLTEXTUNSTEMMED are
 * assumed.
 * 
 * An individual qualifies if it satisfies any of the type restrictions, or if
 * there are no type restrictions.
 * 
 * Each query should contain a ?uri variable, which will be replaced by the URI
 * of the individual.
 * 
 * All of the result fields of all result rows of all of the queries will be
 * concatenated into a single result, separated by spaces. That result will be
 * added to each of the specified search fields.
 * 
 * A label may be supplied to the instance, for use in logging. If no label is
 * supplied, one will be generated.
 */
public class SimpleSparqlQueryDocumentModifier implements DocumentModifier,
		ContextModelsUser {
	private static final Log log = LogFactory
			.getLog(SimpleSparqlQueryDocumentModifier.class);

	private RDFService rdfService;

	/** A name to be used in logging, to identify this instance. */
	private String label;

	/** The queries to be executed. There must be at least one. */
	private List<String> queries = new ArrayList<>();

	/**
	 * The names of the fields where the results of the queries will be stored.
	 * If empty, it is assumed to be ALLTEXT and ALLTEXTUNSTEMMED.
	 */
	private List<String> fieldNames = new ArrayList<>();

	/**
	 * URIs of the types of individuals to whom these queries apply. If empty,
	 * then the queries apply to all individuals.
	 */
	private Set<String> typeRestrictions = new HashSet<>();

	@Override
	public void setContextModels(ContextModelAccess models) {
		this.rdfService = models.getRDFService(CONTENT);
	}

	@Property(uri = "http://www.w3.org/2000/01/rdf-schema#label")
	public void setLabel(String l) {
		label = l;
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasSparqlQuery")
	public void addQuery(String query) {
		queries.add(query);
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasTargetField")
	public void addTargetField(String fieldName) {
		fieldNames.add(fieldName);
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasTypeRestriction")
	public void addTypeRestriction(String typeUri) {
		typeRestrictions.add(typeUri);
	}

	@Validation
	public void validate() {
		if (label == null) {
			label = this.getClass().getSimpleName() + ":" + this.hashCode();
		}
		if (fieldNames.isEmpty()) {
			fieldNames.add(ALLTEXT);
			fieldNames.add(ALLTEXTUNSTEMMED);
		}
		if (queries.isEmpty()) {
			throw new IllegalStateException(
					"Configuration contains no queries for " + label);
		}
	}

	@Override
	public String toString() {
		return (label == null) ? super.toString() : label;
	}

	@Override
	public void modifyDocument(Individual ind, SearchInputDocument doc) {
		if (!passesTypeRestrictions(ind)) {
			return;
		}

		String text = getTextForQueries(ind);

		for (String fieldName : fieldNames) {
			doc.addField(fieldName, text);
		}
	}

	private boolean passesTypeRestrictions(Individual ind) {
		if (typeRestrictions.isEmpty()) {
			return true;
		} else {
			for (VClass type : ind.getVClasses()) {
				if (typeRestrictions.contains(type.getURI())) {
					return true;
				}
			}
		}
		return false;
	}

	private String getTextForQueries(Individual ind) {
		List<String> list = new ArrayList<>();
		for (String query : queries) {
			String text = getTextForQuery(substituteUri(ind, query));
			if (StringUtils.isNotBlank(text)) {
				list.add(text);
			}
		}
		return StringUtils.join(list, " ");
	}

	private String substituteUri(Individual ind, String query) {
		return query.replace("?uri", "<" + ind.getURI() + "> ");
	}

	private String getTextForQuery(String query) {
		List<String> list = new ArrayList<>();
		try {
			ResultSet results = RDFServiceUtils.sparqlSelectQuery(query,
					rdfService);
			while (results.hasNext()) {
				String text = getTextForRow(results.nextSolution());
				if (StringUtils.isNotBlank(text)) {
					list.add(text);
				}
			}
		} catch (Throwable t) {
			log.error("problem while running query '" + query + "'", t);
		}
		log.debug(label + " - query: '" + query + "' returns " + list);
		return StringUtils.join(list, " ");
	}

	private String getTextForRow(QuerySolution row) {
		List<String> list = new ArrayList<>();
		Iterator<String> names = row.varNames();
		while (names.hasNext()) {
			RDFNode node = row.get(names.next());
			String text = getTextForNode(node);
			if (StringUtils.isNotBlank(text)) {
				list.add(text);
			}
		}
		return StringUtils.join(list, " ");
	}

	private String getTextForNode(RDFNode node) {
		if (node == null) {
			return "";
		} else if (node.isLiteral()) {
			return node.asLiteral().getString().trim();
		} else {
			return node.toString().trim();
		}
	}

	@Override
	public void shutdown() {
		// Nothing to do.
	}

}
