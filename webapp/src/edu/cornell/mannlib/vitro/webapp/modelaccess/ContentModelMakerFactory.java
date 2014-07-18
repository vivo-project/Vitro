/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_UNION;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.APPLICATION_METADATA;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_UNION;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_UNION;
import static edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils.WhichService.CONTENT;

import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.NamedDefaultModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.UnionModelsModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.UnionModelsModelMaker.UnionSpec;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils.WhichService;

/**
 * Common functionality among the Content-based ModelMakerFactorys
 */
public abstract class ContentModelMakerFactory implements ModelMakerFactory {

	/**
	 * These are the small content models that we want to keep in memory.
	 */
	protected static final String[] SMALL_CONTENT_MODELS = {
			APPLICATION_METADATA, TBOX_ASSERTIONS, TBOX_INFERENCES };


	private static final UnionSpec[] CONTENT_UNIONS = new UnionSpec[] {
			UnionSpec.base(ABOX_ASSERTIONS).plus(ABOX_INFERENCES)
					.yields(ABOX_UNION),
			UnionSpec.base(TBOX_ASSERTIONS).plus(TBOX_INFERENCES)
					.yields(TBOX_UNION),
			UnionSpec.base(ABOX_ASSERTIONS).plus(TBOX_ASSERTIONS)
					.yields(FULL_ASSERTIONS),
			UnionSpec.base(ABOX_INFERENCES).plus(TBOX_INFERENCES)
					.yields(FULL_INFERENCES) };

	private static final String CONTENT_DEFAULT_MODEL_NAME = FULL_UNION;

	/**
	 * These decorations are added to a Content ModelMaker, regardless of the
	 * source.
	 * 
	 * Create the union models and full models. Use the default model as the
	 * full union.
	 */
	protected ModelMaker addContentDecorators(ModelMaker sourceMM) {
		ModelMaker unions = new UnionModelsModelMaker(sourceMM, CONTENT_UNIONS);
		return new NamedDefaultModelMaker(unions, CONTENT_DEFAULT_MODEL_NAME);
	}

	@Override
	public WhichService whichModelMaker() {
		return CONTENT;
	}

}
