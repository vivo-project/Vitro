/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.sparql;

import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ConfigurationModelMakerFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.ListCachingModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.MemoryMappingModelMaker;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

/**
 * On a SPARQL endpoint, is there any difference between short-term and
 * long-term connections?
 * 
 * In any case, memory-map all of the Configuration models, and apply them to
 * both short-term and long-term use.
 * 
 * RDFService doesn't support empty models, so support them with ListCaching
 */
public class ConfigurationModelMakerFactorySPARQL extends
		ConfigurationModelMakerFactory {

	private final ModelMaker longTermModelMaker;

	public ConfigurationModelMakerFactorySPARQL(RDFService longTermRdfService) {
		this.longTermModelMaker = new ListCachingModelMaker(new MemoryMappingModelMaker(
				new RDFServiceModelMaker(longTermRdfService),
				CONFIGURATION_MODELS));
	}

	@Override
	public ModelMaker getModelMaker(RDFService longTermRdfService) {
		return addConfigurationDecorators(longTermModelMaker);
	}

	/**
	 * The long-term models are all memory-mapped, so use them.
	 */
	@Override
	public ModelMaker getShortTermModelMaker(RDFService shortTermRdfService) {
		return addConfigurationDecorators(longTermModelMaker);
	}

}
