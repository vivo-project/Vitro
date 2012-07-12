/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfiguration;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;

/**
 * TODO
 */
public class SelfEditingConfigurationStub extends SelfEditingConfiguration {

	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private Map<String, List<Individual>> associatedIndividuals = new HashMap<String, List<Individual>>();

	public SelfEditingConfigurationStub() {
		super("bogusMatchingProperty");
	}

	public void addAssociatedIndividual(String externalAuthId,
			Individual individual) {
		if (!associatedIndividuals.containsKey(externalAuthId)) {
			associatedIndividuals.put(externalAuthId,
					new ArrayList<Individual>());
		}
		associatedIndividuals.get(externalAuthId).add(individual);
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public List<Individual> getAssociatedIndividuals(IndividualDao indDao,
			String externalAuthId) {
		if (associatedIndividuals.containsKey(externalAuthId)) {
			return associatedIndividuals.get(externalAuthId);
		} else {
			return Collections.emptyList();
		}
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public boolean isConfigured() {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"SelfEditingConfigurationStub.isConfigured() not implemented.");
	}

	@Override
	public String getMatchingPropertyUri() {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"SelfEditingConfigurationStub.getMatchingPropertyUri() not implemented.");
	}

	@Override
	public List<Individual> getAssociatedIndividuals(IndividualDao indDao,
			UserAccount user) {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"SelfEditingConfigurationStub.getAssociatedIndividuals() not implemented.");
	}

	@Override
	public void associateIndividualWithUserAccount(IndividualDao indDao,
			DataPropertyStatementDao dpsDao, UserAccount user,
			String associatedIndividualUri) {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"SelfEditingConfigurationStub.associateIndividualWithUserAccount() not implemented.");
	}

}
