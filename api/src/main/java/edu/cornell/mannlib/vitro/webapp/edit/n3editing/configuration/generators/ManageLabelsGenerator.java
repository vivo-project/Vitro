/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;

/**
 *This generator selects the actual generator to be employed.  This generator can be overwritten in applications
 *that use/extend Vitro to allow for class-based label handling.  
 */
public class ManageLabelsGenerator extends BaseEditConfigurationGenerator implements EditConfigurationGenerator {
    public static Log log = LogFactory.getLog(ManageLabelsForIndividualGenerator.class);
    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq, HttpSession session) {
    	ManageLabelsForIndividualGenerator g = new ManageLabelsForIndividualGenerator();
    	return g.getEditConfiguration(vreq, session);
    }

}
