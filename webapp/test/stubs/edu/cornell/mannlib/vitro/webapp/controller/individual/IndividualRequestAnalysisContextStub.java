/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.controller.individual;

import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.individual.IndividualRequestAnalysisContext;

/**
 * A simple implementation of the Analysis Context for the Individual Request.
 */
public class IndividualRequestAnalysisContextStub implements
		IndividualRequestAnalysisContext {

	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private final String defaultNamespace;
	private final Map<String, Individual> individualsByUri = new HashMap<String, Individual>();
	private final Map<String, Individual> profilePages = new HashMap<String, Individual>();
	private final Map<String, String> aliasUrlsByIndividual = new HashMap<String, String>();

	public IndividualRequestAnalysisContextStub(String defaultNamespace) {
		this.defaultNamespace = defaultNamespace;
	}

	public void addIndividual(Individual individual) {
		individualsByUri.put(individual.getURI(), individual);
	}

	public void addProfilePage(String netId, Individual individual) {
		profilePages.put(netId, individual);
	}

	public void setAliasUrl(String individualUri, String aliasUrl) {
		aliasUrlsByIndividual.put(individualUri, aliasUrl);
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public String getDefaultNamespace() {
		return defaultNamespace;
	}

	@Override
	public Individual getIndividualByURI(String individualUri) {
		if (individualUri == null) {
			return null;

		}
		return individualsByUri.get(individualUri);
	}

	@Override
	public Individual getIndividualByNetId(String netId) {
		if (netId == null) {
			return null;
		}
		return profilePages.get(netId);
	}

	@Override
	public String getAliasUrlForBytestreamIndividual(Individual individual) {
		if (individual == null) {
			return null;
		}
		return aliasUrlsByIndividual.get(individual.getURI());
	}
}
