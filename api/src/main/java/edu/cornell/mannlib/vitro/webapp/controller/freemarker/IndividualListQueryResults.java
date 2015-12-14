/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocumentList;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;

/**
 * Holds the Individuals that were found in a search query.
 * 
 * Provides a convenience method to run the query and to find the Individuals.
 */
public class IndividualListQueryResults {
	private static final Log log = LogFactory
			.getLog(IndividualListQueryResults.class);

	private static final IndividualListQueryResults EMPTY_RESULT = new IndividualListQueryResults(
			0, new ArrayList<Individual>());

	// ----------------------------------------------------------------------
	// Convenience method
	// ----------------------------------------------------------------------

	public static IndividualListQueryResults runQuery(SearchQuery query,
			IndividualDao indDao)
			throws SearchEngineException {

		SearchEngine search = ApplicationUtils.instance().getSearchEngine();
		SearchResponse response = search.query(query);

		if (response == null) {
			log.debug("response from search query was null");
			return EMPTY_RESULT;
		}

		SearchResultDocumentList docs = response.getResults();
		if (docs == null) {
			log.debug("results from search query response was null");
			return EMPTY_RESULT;
		}

		// get list of individuals for the search results
		long hitCount = docs.getNumFound();
		log.debug("Number of search results: " + hitCount);

		List<Individual> individuals = new ArrayList<Individual>(docs.size());
		for (SearchResultDocument doc : docs) {
			String uri = doc.getStringValue(VitroSearchTermNames.URI);
			Individual individual = indDao.getIndividualByURI(uri);
			if (individual == null) {
				log.debug("No individual for search document with uri = " + uri);
			} else {
				individuals.add(individual);
				log.debug("Adding individual " + uri + " to individual list");
			}
		}

		return new IndividualListQueryResults((int) hitCount, individuals);
	}

	// ----------------------------------------------------------------------
	// The instance
	// ----------------------------------------------------------------------

	private final int hitCount;
	private final List<Individual> individuals;

	public IndividualListQueryResults(int hitCount, List<Individual> individuals) {
		this.hitCount = hitCount;
		this.individuals = individuals;
	}

	public int getHitCount() {
		return hitCount;
	}

	public List<Individual> getIndividuals() {
		return individuals;
	}

}
