/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;

/**
 * Generate the EditConfiguration for the Institutional Internal Class Form.
 * see http://issues.library.cornell.edu/browse/NIHVIVO-2666
 *  
 *
 */
public class InstitutionalInternalClassForm implements EditConfigurationGenerator {

    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq, HttpSession session) { 
        EditConfigurationVTwo editConfig = new EditConfigurationVTwo();
        editConfig.setTemplate("institutionalInternalClassForm.ftl");
        
        return editConfig;
    }

}
