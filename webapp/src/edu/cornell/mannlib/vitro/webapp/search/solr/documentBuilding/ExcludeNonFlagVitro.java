/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding;

import static edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding.IndividualToSearchDocument.DONT_EXCLUDE;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * Exclude individuals with most specific types from the Vitro namespace from
 * the search index. (Other than old vitro Flag types).
 */
public class ExcludeNonFlagVitro implements SearchIndexExcluder {
	private static final Log log = LogFactory.getLog(ExcludeNonFlagVitro.class);

	@Override
	public String checkForExclusion(Individual ind) {
		if (ind == null) {
			return DONT_EXCLUDE;
		}

		List<String> mostSpecificTypeUris = ind.getMostSpecificTypeURIs();
		if (mostSpecificTypeUris == null) {
			return DONT_EXCLUDE;
		}

		String message = skipIfVitro(ind, mostSpecificTypeUris);
		if (!StringUtils.equals(DONT_EXCLUDE, message)) {
			log.debug("msg=" + message + ", individual=" + ind.getURI() + " ("
					+ ind.getLabel() + "), types=" + mostSpecificTypeUris);
		}
		return message;
	}

	String skipIfVitro(Individual ind, List<String> mostSpecificTypeUris) {
		for (String typeUri : mostSpecificTypeUris) {
			if (typeUri == null) {
				continue;
			}
			if (typeUri.startsWith(VitroVocabulary.vitroURI + "Flag")) {
				continue;
			}
			if (typeUri.startsWith(VitroVocabulary.vitroURI)) {
				return "Skipped " + ind.getURI() + " because in "
						+ VitroVocabulary.vitroURI + " namespace";
			}
		}
		return DONT_EXCLUDE;
	}

}
