/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;

/**
 * Generates the edit configuration for a default property form.
 *
 */
public class DefaultPropertyFormGenerator implements EditConfigurationGenerator {

    @Override
    public EditConfiguration getEditConfiguration(VitroRequest vreq,
            HttpSession session) {
        // TODO Generate a edit conf for the default object property form and return it.
        return null;
    }

}
