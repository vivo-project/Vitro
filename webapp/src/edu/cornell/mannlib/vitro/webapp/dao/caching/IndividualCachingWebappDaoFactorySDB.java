/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.caching;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryConfig;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

/**
 * TODO
 */
public class IndividualCachingWebappDaoFactorySDB extends WebappDaoFactorySDB {

	public IndividualCachingWebappDaoFactorySDB(RDFService rdfService,
			OntModelSelector ontModelSelector, WebappDaoFactoryConfig config,
			SDBDatasetMode datasetMode) {
		super(rdfService, ontModelSelector, config, datasetMode);
		this.entityWebappDao = new IndividualDaoCaching(super.getIndividualDao());
	}

	public IndividualCachingWebappDaoFactorySDB(RDFService rdfService,
			OntModelSelector ontModelSelector, WebappDaoFactoryConfig config) {
		super(rdfService, ontModelSelector, config);
		this.entityWebappDao = new IndividualDaoCaching(super.getIndividualDao());
	}

	public IndividualCachingWebappDaoFactorySDB(RDFService rdfService,
			OntModelSelector ontModelSelector) {
		super(rdfService, ontModelSelector);
		this.entityWebappDao = new IndividualDaoCaching(super.getIndividualDao());
	}

	public IndividualCachingWebappDaoFactorySDB(WebappDaoFactorySDB base,
			String userURI) {
		super(base, userURI);
		this.entityWebappDao = new IndividualDaoCaching(super.getIndividualDao());
	}

}
