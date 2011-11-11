/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditN3GeneratorVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;

public abstract class BaseEditConfigurationGenerator implements EditConfigurationGenerator {

    /* constants */
    public static final String DEFAULT_NS_FOR_NEW_RESOURCE= "";
    
    /* Utility Methods */
    
    /**
     * Sets up the things that should be done for just about every form.
     */
    void initBasics(EditConfigurationVTwo editConf, VitroRequest vreq){                        
        String editKey = EditConfigurationUtils.getEditKey(vreq);   
        editConf.setEditKey(editKey);        
        
        String formUrl = EditConfigurationUtils.getFormUrl(vreq);  
        editConf.setFormUrl(formUrl);                                   
    }
    
    /** 
     * Method that setups up a form for basic object or data property editing. 
     */
    void  initPropertyParameters(VitroRequest vreq, HttpSession session, EditConfigurationVTwo editConfiguration) {
        //set up the subject URI based on request
        String subjectUri = EditConfigurationUtils.getSubjectUri(vreq);
        editConfiguration.setSubjectUri(subjectUri);
        
        //set up predicate URI based on request
        String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);                           
        editConfiguration.setPredicateUri(predicateUri);
        
        editConfiguration.setUrlPatternToReturnTo("/individual");        
        editConfiguration.setEntityToReturnTo(subjectUri);
    }
        
    void initObjectPropForm(EditConfigurationVTwo editConfiguration,VitroRequest vreq) {                      
        editConfiguration.setObject( EditConfigurationUtils.getObjectUri(vreq) );        
    }    
    
}
