/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.logging;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;

/**
 * If the RDFServiceFactory is wrapped in this, then all RDFServices will be
 * wrapped in a LoggingRDFService.
 */
public class LoggingRDFServiceFactory implements RDFServiceFactory {
	private final ServletContext ctx;
	private final RDFServiceFactory factory;

	public LoggingRDFServiceFactory(ServletContext ctx,
			RDFServiceFactory factory) {
		this.ctx = ctx;
		this.factory = factory;
	}

	@Override
	public RDFService getRDFService() {
		return new LoggingRDFService(ctx, factory.getRDFService());
	}

	@Override
	public RDFService getShortTermRDFService() {
		return new LoggingRDFService(ctx, factory.getShortTermRDFService());
	}

	@Override
	public void registerListener(ChangeListener changeListener)
			throws RDFServiceException {
		factory.registerListener(changeListener);
	}

	@Override
	public void unregisterListener(ChangeListener changeListener)
			throws RDFServiceException {
		factory.unregisterListener(changeListener);
	}

}
