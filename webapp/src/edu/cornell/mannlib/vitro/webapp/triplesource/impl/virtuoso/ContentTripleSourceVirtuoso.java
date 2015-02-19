/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.triplesource.impl.virtuoso;

import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.virtuoso.RDFServiceVirtuoso;
import edu.cornell.mannlib.vitro.webapp.triplesource.impl.sparql.ContentTripleSourceSPARQL;

/**
 * So far, it's just like a ContentTripleSourceSPARQL but it uses an instance of
 * RDFServiceVirtuoso.
 */
public class ContentTripleSourceVirtuoso extends ContentTripleSourceSPARQL {

	@Override
	protected RDFService createRDFService(ComponentStartupStatus ss,
			String endpoint, String updateEndpoint) {
		if (updateEndpoint == null) {
			ss.info("Using endpoint at " + endpoint);
			return new RDFServiceVirtuoso(endpoint);
		} else {
			ss.info("Using read endpoint at " + endpoint
					+ " and update endpoint at " + updateEndpoint);
			return new RDFServiceVirtuoso(endpoint, updateEndpoint);
		}
	}

}
