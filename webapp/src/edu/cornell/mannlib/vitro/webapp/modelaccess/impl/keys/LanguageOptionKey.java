/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.impl.keys;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption;

/**
 * An immutable key that distills a list of LanguageOptions into a single
 * result.
 */
public final class LanguageOptionKey extends ModelAccessKey {
	public LanguageOptionKey(LanguageOption... options) {
		super(options);
	}

	@Override
	public LanguageOption getLanguageOption() {
		return super.getLanguageOption();
	}

}
