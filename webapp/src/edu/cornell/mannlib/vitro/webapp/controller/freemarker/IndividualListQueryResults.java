/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.search.solr.SolrSetup;

/**
 * Holds the Individuals that were found in a Solr search query.
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

	public static IndividualListQueryResults runQuery(SolrQuery query,
			IndividualDao indDao, ServletContext context)
			throws SolrServerException {

		SolrServer solr = SolrSetup.getSolrServer(context);
		QueryResponse response = null;
		response = solr.query(query);

		if (response == null) {
			log.debug("response from search query was null");
			return EMPTY_RESULT;
		}

		SolrDocumentList docs = response.getResults();
		if (docs == null) {
			log.debug("results from search query response was null");
			return EMPTY_RESULT;
		}

		// get list of individuals for the search results
		long hitCount = docs.getNumFound();
		log.debug("Number of search results: " + hitCount);

		List<Individual> individuals = new ArrayList<Individual>(docs.size());
		for (SolrDocument doc : docs) {
			String uri = doc.get(VitroSearchTermNames.URI).toString();
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
