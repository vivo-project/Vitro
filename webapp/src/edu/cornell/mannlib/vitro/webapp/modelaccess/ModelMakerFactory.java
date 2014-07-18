/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess;

import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils.WhichService;

/**
 * Get model makers for long and short-term use.
 */
public interface ModelMakerFactory {

	/**
	 * Get a model maker that is suitable for long-term use.
	 */
	ModelMaker getModelMaker(RDFService longTermRdfService);

	/**
	 * Get a model maker that should not be left idle for long periods of time.
	 * 
	 * Because it is based (at least in part) on a short-term RDFService, it
	 * should not be stored in the context or the session, but should be deleted
	 * at the end of the request.
	 */
	ModelMaker getShortTermModelMaker(RDFService shortTermRdfService);
	
	/**
	 * Is this factory configured to provide CONTENT models or CONFIGURATION models?
	 */
	WhichService whichModelMaker();

}
