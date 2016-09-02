/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.tripleSource;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.OntModelCache;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;

/**
 * A provider of triples and the data structures by which they are accessed.
 */
public interface TripleSource extends Application.Module{
	RDFServiceFactory getRDFServiceFactory();

	RDFService getRDFService();

	Dataset getDataset();

	ModelMaker getModelMaker();

	OntModelCache getShortTermOntModels(RDFService shortTermRdfService,
			OntModelCache longTermOntModelCache);
}
