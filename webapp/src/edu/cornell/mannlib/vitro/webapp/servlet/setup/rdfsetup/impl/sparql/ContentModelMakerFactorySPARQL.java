/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.sparql;

import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContentModelMakerFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelMakerFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.ListCachingModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.MemoryMappingModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.ShadowingModelMaker;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

/**
 * On a SPARQL endpoint, is there any difference between short-term and
 * long-term connections?
 * 
 * Anyway, memory-map the small models, and use a short-term connection for the
 * others (when available).
 * 
 * RDFService doesn't support empty models, so support them with ListCaching
 */
public class ContentModelMakerFactorySPARQL extends ContentModelMakerFactory
		implements ModelMakerFactory {

	private final ModelMaker longTermModelMaker;

	public ContentModelMakerFactorySPARQL(RDFService longTermRdfService) {
		this.longTermModelMaker =  new ListCachingModelMaker(new MemoryMappingModelMaker(
				new RDFServiceModelMaker(longTermRdfService),
				SMALL_CONTENT_MODELS));
	}

	/**
	 * The small content models (tbox, app_metadata) are memory mapped, for
	 * speed.
	 */
	@Override
	public ModelMaker getModelMaker(RDFService longTermRdfService) {
		return addContentDecorators(longTermModelMaker);
	}

	/**
	 * For short-term use, the large models (abox) will come from a short-term
	 * service. The small models can be the memory-mapped ones that we created
	 * for long-term use.
	 */
	@Override
	public ModelMaker getShortTermModelMaker(RDFService shortTermRdfService) {
		ModelMaker shortTermModelMaker = new RDFServiceModelMaker(
				shortTermRdfService);

		// No need to create a fresh memory map of the small models: use the
		// long-term ones.
		return addContentDecorators(new ShadowingModelMaker(
				shortTermModelMaker, longTermModelMaker, SMALL_CONTENT_MODELS));
	}

}
