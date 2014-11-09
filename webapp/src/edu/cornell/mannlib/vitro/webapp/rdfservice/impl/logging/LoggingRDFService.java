/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.logging;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;

/**
 * This RDFService wrapper adds instrumentation to the time-consuming methods of
 * the inner RDFService.
 * 
 * For the other methods, it just delegates to the inner RDFService.
 */
public class LoggingRDFService implements RDFService {
	private final RDFService innerService;

	LoggingRDFService(RDFService innerService) {
		this.innerService = innerService;
	}

	// ----------------------------------------------------------------------
	// Timed methods
	// ----------------------------------------------------------------------

	@Override
	public boolean changeSetUpdate(ChangeSet changeSet)
			throws RDFServiceException {
		try (RDFServiceLogger l = new RDFServiceLogger(changeSet)) {
			return innerService.changeSetUpdate(changeSet);
		}
	}

	@Override
	public InputStream sparqlConstructQuery(String query,
			ModelSerializationFormat resultFormat) throws RDFServiceException {
		try (RDFServiceLogger l = new RDFServiceLogger(resultFormat, query)) {
			return innerService.sparqlConstructQuery(query, resultFormat);
		}
	}

	@Override
	public InputStream sparqlDescribeQuery(String query,
			ModelSerializationFormat resultFormat) throws RDFServiceException {
		try (RDFServiceLogger l = new RDFServiceLogger(resultFormat, query)) {
			return innerService.sparqlDescribeQuery(query, resultFormat);
		}
	}

	@Override
	public void sparqlSelectQuery(String query, ResultFormat resultFormat,
			OutputStream outputStream) throws RDFServiceException {
		innerService.sparqlSelectQuery(query, resultFormat,
				outputStream);
	}
	
	@Override
	public InputStream sparqlSelectQuery(String query, ResultFormat resultFormat)
			throws RDFServiceException {
		try (RDFServiceLogger l = new RDFServiceLogger(resultFormat, query)) {
			return innerService.sparqlSelectQuery(query, resultFormat);
		}
	}

	@Override
	public boolean sparqlAskQuery(String query) throws RDFServiceException {
		try (RDFServiceLogger l = new RDFServiceLogger(query)) {
			return innerService.sparqlAskQuery(query);
		}
	}

	// ----------------------------------------------------------------------
	// Untimed methods
	// ----------------------------------------------------------------------

	@Override
	public void newIndividual(String individualURI, String individualTypeURI)
			throws RDFServiceException {
		innerService.newIndividual(individualURI, individualTypeURI);
	}

	@Override
	public void newIndividual(String individualURI, String individualTypeURI,
			String graphURI) throws RDFServiceException {
		innerService.newIndividual(individualURI, individualTypeURI, graphURI);
	}

	@Override
	public List<String> getGraphURIs() throws RDFServiceException {
		return innerService.getGraphURIs();
	}

	@Override
	public void getGraphMetadata() throws RDFServiceException {
		innerService.getGraphMetadata();
	}

	@Override
	public String getDefaultWriteGraphURI() throws RDFServiceException {
		return innerService.getDefaultWriteGraphURI();
	}

	@Override
	public void registerListener(ChangeListener changeListener)
			throws RDFServiceException {
		innerService.registerListener(changeListener);
	}

	@Override
	public void unregisterListener(ChangeListener changeListener)
			throws RDFServiceException {
		innerService.unregisterListener(changeListener);
	}

	@Override
	public ChangeSet manufactureChangeSet() {
		return innerService.manufactureChangeSet();
	}

	@Override
	public void close() {
		innerService.close();
	}

	@Override
	public String toString() {
		return "LoggingRDFService[inner=" + innerService + "]";
	}
}
