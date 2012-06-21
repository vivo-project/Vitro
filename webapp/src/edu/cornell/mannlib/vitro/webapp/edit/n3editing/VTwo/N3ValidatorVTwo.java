/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import java.util.Map;

public interface N3ValidatorVTwo {
	public Map<String,String> validate(EditConfigurationVTwo editConfig, MultiValueEditSubmission editSub);	
}
