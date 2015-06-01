/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.caching;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryConfig;
import edu.cornell.mannlib.vitro.webapp.dao.jena.DatasetWrapperFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

/**
 * A plain WebappDaoFactorySDB, except that the IndividualDao uses caching.
 * Also, this exposes the DatasetWrapperFactory for the IndividualDao to use.
 */
public class IndividualCachingWebappDaoFactorySDB extends WebappDaoFactorySDB {

	public IndividualCachingWebappDaoFactorySDB(RDFService rdfService,
			OntModelSelector ontModelSelector, WebappDaoFactoryConfig config,
			SDBDatasetMode datasetMode) {
		super(rdfService, ontModelSelector, config, datasetMode);
		constructDao();
	}

	public IndividualCachingWebappDaoFactorySDB(RDFService rdfService,
			OntModelSelector ontModelSelector, WebappDaoFactoryConfig config) {
		super(rdfService, ontModelSelector, config);
		constructDao();
	}

	public IndividualCachingWebappDaoFactorySDB(RDFService rdfService,
			OntModelSelector ontModelSelector) {
		super(rdfService, ontModelSelector);
		constructDao();
	}

	private void constructDao() {
		this.entityWebappDao = new IndividualDaoCaching(this);
	}

	DatasetWrapperFactory getDatasetWrapperFactory() {
		return this.dwf;
	}

}
