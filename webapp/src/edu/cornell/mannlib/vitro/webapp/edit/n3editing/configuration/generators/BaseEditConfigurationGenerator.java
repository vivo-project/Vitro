/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;

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
    
    /**
     * Method to turn Strings or multiple List<String> to List<String>. 
     * Only accepts String and List<String> as multi args.  
     */
    static List<String> list( Object ... objs){
        List<String> rv = new ArrayList<String>();        
        for( Object obj: objs){
            if( obj instanceof String)
                rv.add((String)obj);
            else if( obj instanceof Iterable){
                for( Object innerObj: (Iterable)obj){
                    if( innerObj instanceof String){
                        rv.add((String)innerObj);
                    }else{
                        throw new Error("list may only take String " +
                        		"and List<String>. It does not accept List<" 
                                + innerObj.getClass().getName() + ">");
                    }
                }                
            }else{
                throw new Error("list may only take String " +
                        "and List<String>. It does not accept " 
                        + obj.getClass().getName() );            
            }
        }
        return rv;
    }
}
