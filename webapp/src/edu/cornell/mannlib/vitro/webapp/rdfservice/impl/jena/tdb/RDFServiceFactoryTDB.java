/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.tdb;

import java.io.IOException;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;

/**
 * TODO
 */
public class RDFServiceFactoryTDB implements RDFServiceFactory {
	private final String tdbPath;
	private final RDFServiceTDB longTermRdfService;

	public RDFServiceFactoryTDB(String tdbPath) throws IOException {
		this.tdbPath = tdbPath;
		this.longTermRdfService = new RDFServiceTDB(tdbPath);
	}

	@Override
	public RDFService getRDFService() {
		return this.longTermRdfService;
	}

	@Override
	public RDFService getShortTermRDFService() {
		try {
			return new RDFServiceTDB(tdbPath);
		} catch (IOException e) {
			throw new RuntimeException(
					"Failed to create short-term RDFServiceTDB", e);
		}
	}

	@Override
	public void registerListener(ChangeListener changeListener)
			throws RDFServiceException {
        this.longTermRdfService.registerListener(changeListener);
	}

	@Override
	public void unregisterListener(ChangeListener changeListener)
			throws RDFServiceException {
        this.longTermRdfService.unregisterListener(changeListener);
	}

	public void close() {
		this.longTermRdfService.close();
	}
}
