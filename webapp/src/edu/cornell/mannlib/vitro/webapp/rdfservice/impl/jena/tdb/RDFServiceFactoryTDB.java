/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.tdb;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;

/**
 * TODO
 */
public class RDFServiceFactoryTDB implements RDFServiceFactory {
	private static final Log log = LogFactory
			.getLog(RDFServiceFactoryTDB.class);
	
	
	private final RDFServiceTDB service;

	public RDFServiceFactoryTDB(String directoryPath) throws IOException {
		this.service = new RDFServiceTDB(directoryPath);
	}

	@Override
	public RDFService getRDFService() {
		return service;
	}

	@Override
	public RDFService getShortTermRDFService() {
		return service;
	}

	@Override
	public void registerListener(ChangeListener changeListener)
			throws RDFServiceException {
		service.registerListener(changeListener);
	}

	@Override
	public void unregisterListener(ChangeListener changeListener)
			throws RDFServiceException {
		service.unregisterListener(changeListener);
	}

}
