/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.individual;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfiguration;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.FileInfo;

/**
 * Implement all of the fiddly-bits that we need for analyzing the request for
 * an individual, but that we do not want to do in unit tests.
 */
public class IndividualRequestAnalysisContextImpl implements
		IndividualRequestAnalysisContext {
		
	private final VitroRequest vreq;	
	private final WebappDaoFactory wadf;
	private final IndividualDao iDao;

	public IndividualRequestAnalysisContextImpl(VitroRequest vreq) {
		this.vreq = vreq;	
		this.wadf = vreq.getWebappDaoFactory();
		this.iDao = wadf.getIndividualDao();
	}

	@Override
	public String getDefaultNamespace() {
		return wadf.getDefaultNamespace();
	}

	@Override
	public Individual getIndividualByURI(String individualUri) {
		if (individualUri == null) {
			return null;
		}
		return iDao.getIndividualByURI(individualUri);
	}

	@Override
	public Individual getIndividualByNetId(String netId) {
		if (netId == null) {
			return null;
		}

		SelfEditingConfiguration sec = SelfEditingConfiguration.getBean(vreq);
		List<Individual> assocInds = sec.getAssociatedIndividuals(iDao, netId);
		if (!assocInds.isEmpty()) {
			return assocInds.get(0);
		} else {
			return null;
		}
	}

	@Override
	public String getAliasUrlForBytestreamIndividual(Individual individual) {
		if (individual == null) {
			return null;
		}

		FileInfo fileInfo = FileInfo.instanceFromBytestreamUri(wadf,
				individual.getURI());
		if (fileInfo == null) {
			return null;
		}

		return fileInfo.getBytestreamAliasUrl();
	}

}
