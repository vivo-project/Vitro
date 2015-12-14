/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.impl.keys;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.PolicyOption.POLICY_NEUTRAL;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.PolicyOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ReasoningOption;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WebappDaoFactoryOption;

/**
 * An immutable key for storing RDFService objects in the ModelAccess maps.
 */
public final class WebappDaoFactoryKey extends ModelAccessKey {
	public WebappDaoFactoryKey(WebappDaoFactoryOption... options) {
		super(findLanguageOption(options), findReasoningOption(options),
				findPolicyOption(options));
	}

	@Override
	public LanguageOption getLanguageOption() {
		return super.getLanguageOption();
	}

	@Override
	public ReasoningOption getReasoningOption() {
		return super.getReasoningOption();
	}

	@Override
	public PolicyOption getPolicyOption() {
		return super.getPolicyOption();
	}

	public WebappDaoFactoryKey policyNeutral() {
		return new WebappDaoFactoryKey(getLanguageOption(),
				getReasoningOption(), POLICY_NEUTRAL);
	}

	public RDFServiceKey rdfServiceKey() {
		return new RDFServiceKey(getWhichService(), getLanguageOption());
	}

	public OntModelSelectorKey ontModelSelectorKey() {
		return new OntModelSelectorKey(getLanguageOption(),
				getReasoningOption());
	}
}
