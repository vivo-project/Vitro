/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.triplesource;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryConfig;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.OntModelCache;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

/**
 * Provide the short-term data structures. Should be closed when no longer
 * needed.
 * 
 * Repeated calls for the same data structure should yield the same instance.
 * 
 * Repeated calls for the WebappDaoFactoryConfig need not yield the same
 * instance.
 */
public interface ShortTermCombinedTripleSource {
	RDFService getRDFService(WhichService whichService);

	OntModelCache getOntModelCache();

	WebappDaoFactoryConfig getWebappDaoFactoryConfig();

	void close();
}
