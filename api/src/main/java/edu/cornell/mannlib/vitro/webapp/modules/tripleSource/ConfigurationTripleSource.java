/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.tripleSource;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY_DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY_TBOX;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.USER_ACCOUNTS;

import org.apache.jena.rdf.model.ModelMaker;

/**
 * A triple source for configuration models.
 */
public abstract class ConfigurationTripleSource implements TripleSource {
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
		// Insure that these models are created here, and not in the Content.
		for (String name : CONFIGURATION_MODELS) {
			sourceMM.getModel(name);
		}
		return sourceMM;
	}
}
