/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.modules.tripleSource;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.*;



import org.apache.jena.rdf.model.ModelMaker;

/**
 * A triple source for configuration models.
 */
public abstract class ConfigurationTripleSource implements TripleSource {
	/**
	 * A list of all Configuration models, in case the implementation wants to
	 * add memory-mapping.
	 */
	protected static final String[] CONFIGURATION_MODELS = { 
			DISPLAY,
			DISPLAY_TBOX, 
			DISPLAY_DISPLAY, 
			USER_ACCOUNTS, 
			ABOX_ASSERTIONS_FIRSTTIME_BACKUP,
			TBOX_ASSERTIONS_FIRSTTIME_BACKUP, 
			APPLICATION_METADATA_FIRSTTIME_BACKUP,
			USER_ACCOUNTS_FIRSTTIME_BACKUP, 
			DISPLAY_FIRSTTIME_BACKUP, 
			DISPLAY_TBOX_FIRSTTIME_BACKUP, 
			DISPLAY_DISPLAY_FIRSTTIME_BACKUP, 
			DYNAMIC_API_ABOX,
			DYNAMIC_API_ABOX_FIRSTTIME_BACKUP,
			DYNAMIC_API_TBOX,
			DYNAMIC_API_TBOX_FIRSTTIME_BACKUP,
	        SHAPES,
	        SHAPES_FIRSTTIME_BACKUP,
			};

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
