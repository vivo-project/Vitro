/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;

//For use with classes that explicitly modify configurations through AJAX requests
public interface EditConfigurationAJAXGenerator {
    public void modifyEditConfiguration( EditConfigurationVTwo config, VitroRequest vreq ) throws Exception;
}
