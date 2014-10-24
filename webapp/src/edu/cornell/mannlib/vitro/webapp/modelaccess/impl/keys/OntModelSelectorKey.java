/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.impl.keys;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_UNION;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_UNION;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_UNION;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.OntModelSelectorOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ReasoningOption;

/**
 * An immutable key for storing OntModelSelectors in the ModelAccess maps.
 */
public final class OntModelSelectorKey extends ModelAccessKey {
	public OntModelSelectorKey(OntModelSelectorOption... options) {
		super(findLanguageOption(options), findReasoningOption(options));
	}

	@Override
	public LanguageOption getLanguageOption() {
		return super.getLanguageOption();
	}

	@Override
	public ReasoningOption getReasoningOption() {
		return super.getReasoningOption();
	}

	public OntModelKey aboxKey() {
		switch (getReasoningOption()) {
		case ASSERTIONS_ONLY:
			return new OntModelKey(ABOX_ASSERTIONS, getLanguageOption());
		case INFERENCES_ONLY:
			return new OntModelKey(ABOX_INFERENCES, getLanguageOption());
		default: // ASSERTIONS_AND_INFERENCES
			return new OntModelKey(ABOX_UNION, getLanguageOption());
		}
	}

	public OntModelKey tboxKey() {
		switch (getReasoningOption()) {
		case ASSERTIONS_ONLY:
			return new OntModelKey(TBOX_ASSERTIONS, getLanguageOption());
		case INFERENCES_ONLY:
			return new OntModelKey(TBOX_INFERENCES, getLanguageOption());
		default: // ASSERTIONS_AND_INFERENCES
			return new OntModelKey(TBOX_UNION, getLanguageOption());
		}
	}

	public OntModelKey fullKey() {
		switch (getReasoningOption()) {
		case ASSERTIONS_ONLY:
			return new OntModelKey(FULL_ASSERTIONS, getLanguageOption());
		case INFERENCES_ONLY:
			return new OntModelKey(FULL_INFERENCES, getLanguageOption());
		default: // ASSERTIONS_AND_INFERENCES
			return new OntModelKey(FULL_UNION, getLanguageOption());
		}
	}

	/**
	 * Get appropriate keys for the DISPLAY, USER_ACCOUNTS, and
	 * APPLICATION_METADATA models.
	 */
	public OntModelKey ontModelKey(String name) {
		return new OntModelKey(name, getLanguageOption());
	}

}
