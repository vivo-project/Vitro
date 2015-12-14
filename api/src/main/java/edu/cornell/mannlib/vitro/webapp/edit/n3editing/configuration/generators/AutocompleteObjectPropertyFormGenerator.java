/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;


/**
 * Auto complete object property form generator folded into DefualtObjectPropertyFormGenerator.java
 *
 */
public class AutocompleteObjectPropertyFormGenerator extends DefaultObjectPropertyFormGenerator {

	@Override
	public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq,
			HttpSession session) throws Exception {
		//force auto complete 
		doAutoComplete = true;
		
		return super.getEditConfiguration(vreq, session);
	}


	
}
