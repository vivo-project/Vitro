/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.impl.keys;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.RdfServiceOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;

/**
 * An immutable key for storing RDFService objects in the ModelAccess maps.
 */
public final class RDFServiceKey extends ModelAccessKey {
	public RDFServiceKey(RdfServiceOption... options) {
		super(findWhichService(options), findLanguageOption(options));
	}

	@Override
	public WhichService getWhichService() {
		return super.getWhichService();
	}

	@Override
	public LanguageOption getLanguageOption() {
		return super.getLanguageOption();
	}

}
