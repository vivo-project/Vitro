/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY_DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY_TBOX;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.USER_ACCOUNTS;
import static edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils.WhichService.CONFIGURATION;

import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils.WhichService;

/**
 * Common functionality among the Configuration-based ModelMakerFactorys
 */
public abstract class ConfigurationModelMakerFactory implements
		ModelMakerFactory {

	/**
	 * A list of all Configuration models, in case the implementation wants to
	 * add memory-mapping.
	 */
	protected static final String[] CONFIGURATION_MODELS = { DISPLAY,
			DISPLAY_TBOX, DISPLAY_DISPLAY, USER_ACCOUNTS };

	/**
	 * These decorators are added to a Configuration ModelMaker, regardless of
	 * the source.
	 */
	protected ModelMaker addConfigurationDecorators(ModelMaker sourceMM) {
		// No decorators yet.
		return sourceMM;
	}

	@Override
	public WhichService whichModelMaker() {
		return CONFIGURATION;
	}

}
