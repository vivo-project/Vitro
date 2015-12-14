/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.impl.keys;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.DatasetOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;

/**
 * An immutable key for storing Dataset objects in the ModelAccess maps.
 */
public final class DatasetKey extends ModelAccessKey {
	public DatasetKey(DatasetOption... options) {
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

	/**
	 * How would we access an RDFService that is the basis for the Dataset
	 * accessed by this key?
	 */
	public RDFServiceKey rdfServiceKey() {
		return new RDFServiceKey(getWhichService(), getLanguageOption());
	}

}
