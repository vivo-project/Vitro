/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.distribute.modelbuilder;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.mannlib.vitro.webapp.controller.api.distribute.DataDistributorContext;

/**
 * Creates a local model as the basis for later queries.
 */
public interface ModelBuilder {
	/** Call once, after instantiating */
	void init(DataDistributorContext ddContext)
			throws DataDistributorException;

	/** Call once, after init. */
	Model buildModel() throws DataDistributorException;

	/** Call once, after buildModel. */
	void close();
}
