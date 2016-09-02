/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.tripleSource;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.APPLICATION_METADATA;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_UNION;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_INFERENCES;

import org.apache.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.NamedDefaultModelMaker;

/**
 * A triple source for content models.
 */
public abstract class ContentTripleSource implements TripleSource {
	/**
	 * These are the small content models that we want to keep in memory.
	 */
	protected static final String[] SMALL_CONTENT_MODELS = {
			APPLICATION_METADATA, TBOX_ASSERTIONS, TBOX_INFERENCES };

	/**
	 * These are the small content OntModels for which we don't need short-term instances.
	 */
	protected static final String[] MEMORY_MAPPED_CONTENT_MODELS = {
		APPLICATION_METADATA, TBOX_ASSERTIONS, TBOX_INFERENCES };
	
	private static final String CONTENT_DEFAULT_MODEL_NAME = FULL_UNION;

	/**
	 * These decorations are added to a Content ModelMaker, regardless of the
	 * source.
	 * 
	 * Use the default model as the full union.
	 */
	protected static ModelMaker addContentDecorators(ModelMaker sourceMM) {
		return new NamedDefaultModelMaker(sourceMM, CONTENT_DEFAULT_MODEL_NAME);
	}
}
