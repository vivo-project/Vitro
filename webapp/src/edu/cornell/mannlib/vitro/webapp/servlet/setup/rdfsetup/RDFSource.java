/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelMakerFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;

/**
 * The interface for a triple-store implementation. It returns an
 * RDFServiceFactory and either or both of the ModelMakerFactories.
 * 
 * You should call close() when shutting down the triple-store.
 */
public interface RDFSource extends AutoCloseable {

	RDFServiceFactory getRDFServiceFactory();

	ModelMakerFactory getContentModelMakerFactory();

	ModelMakerFactory getConfigurationModelMakerFactory();

	@Override
	void close();

}
