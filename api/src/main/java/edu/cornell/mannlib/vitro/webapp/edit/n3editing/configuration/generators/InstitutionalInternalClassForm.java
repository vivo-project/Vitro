/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;

/**
 * Generate the EditConfiguration for the Institutional Internal Class Form.
 * see http://issues.library.cornell.edu/browse/NIHVIVO-2666
 *
 *
 */
public class InstitutionalInternalClassForm extends BaseEditConfigurationGenerator implements EditConfigurationGenerator {

    String INTERNAL_CLASS_ANNOTATION_URI= "<http://example.com/vivo#ChangeMeUniveristy>";

    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq, HttpSession session) {
        EditConfigurationVTwo editConfig = new EditConfigurationVTwo();

        //set up the template for the form
        editConfig.setTemplate("institutionalInternalClassForm.ftl");

        //Set the n3 that is required for the edit
        //bdc34: don't know how the annotation will be structured
        StringList n3ForInternalClass =new StringList( " ?internalClassUri "+INTERNAL_CLASS_ANNOTATION_URI+" \"true\" . " );
        editConfig.setN3Required( n3ForInternalClass );

        //bdc34: maybe this is redundant with the keys of the fields Map?
        editConfig.setUrisOnform( new StringList( "internalClassUri" ));

        //edit config should have URL to submit the form to
        //editConfig.setUrl
        //set the url pattern that the client will return to after a successful edit
        editConfig.setUrlPatternToReturnTo("/siteAdmin");

        editConfig.setSubmitToUrl("/edit/process");
        //prepare
        prepare(vreq, editConfig);
        return editConfig;
    }


    public class StringList extends ArrayList<String>{
        public StringList( String ... strings){
            super();
            Collections.addAll(this, strings);
        }
    }
}
