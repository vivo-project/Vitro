/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.tdb;

import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContentModelMakerFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelMakerFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.ListCachingModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.MemoryMappingModelMaker;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

/**
 * In TDB, is there any difference between short-term and long-term connections?
 * For now, use long-term connections for all models, memory-mapping the small
 * ones.
 * 
 * RDFService doesn't support empty models, so support them with ListCaching.
 */
public class ContentModelMakerFactoryTDB extends ContentModelMakerFactory
		implements ModelMakerFactory {

	private final ModelMaker longTermModelMaker;

	public ContentModelMakerFactoryTDB(RDFService longTermRdfService) {
		this.longTermModelMaker = new ListCachingModelMaker(
				new MemoryMappingModelMaker(new RDFServiceModelMaker(
						longTermRdfService), SMALL_CONTENT_MODELS));
	}

	/**
	 * The small content models are memory mapped, for speed.
	 */
	@Override
	public ModelMaker getModelMaker(RDFService longTermRdfService) {
		return addContentDecorators(longTermModelMaker);
	}

	/**
	 * There are no connections or connection pool, so short-term use is the
	 * same as long-term use.
	 */
	@Override
	public ModelMaker getShortTermModelMaker(RDFService shortTermRdfService) {
		return addContentDecorators(longTermModelMaker);
	}

}
