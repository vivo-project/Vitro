/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.OntModelCache;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;

/**
 * An RDFSource that provides Content models or Configuration models, but not
 * both.
 * 
 * Repeated calls for the same data structure should yield the same instance,
 * except for the short-term OntModelCache.
 */
public interface SingleSourceDataStructuresProvider extends AutoCloseable {
	RDFServiceFactory getRDFServiceFactory();

	RDFService getRDFService();

	Dataset getDataset();

	ModelMaker getModelMaker();

	OntModelCache getShortTermOntModels(RDFService shortTermRdfService,
			OntModelCache longTermOntModelCache);
}
