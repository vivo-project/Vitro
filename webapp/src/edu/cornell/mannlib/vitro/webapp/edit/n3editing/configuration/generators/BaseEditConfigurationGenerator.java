/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.hp.hpl.jena.rdf.model.Model;

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
        
        String formUrl = EditConfigurationUtils.getFormUrlWithoutContext(vreq);  
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
    
    //Prepare for update or non-update
    //Originally included in edit request dispatch controller but moved here due to
    //exceptions such as default add missing individual form
    void prepare(VitroRequest vreq, EditConfigurationVTwo editConfig) {
    	//This used to get the model from the servlet context
    	//        Model model = (Model) getServletContext().getAttribute("jenaOntModel");
        Model model = vreq.getJenaOntModel();
        
        if( editConfig.getSubjectUri() == null)
            editConfig.setSubjectUri( EditConfigurationUtils.getSubjectUri(vreq));
        if( editConfig.getPredicateUri() == null )
            editConfig.setPredicateUri( EditConfigurationUtils.getPredicateUri(vreq));
        
        String objectUri = EditConfigurationUtils.getObjectUri(vreq);
        Integer dataKey = EditConfigurationUtils.getDataHash(vreq);
        if (objectUri != null && ! objectUri.trim().isEmpty()) { 
            // editing existing object
            if( editConfig.getObject() == null)
                editConfig.setObject( EditConfigurationUtils.getObjectUri(vreq));
            editConfig.prepareForObjPropUpdate(model);
        } else if( dataKey != null ) { // edit of a data prop statement
            //do nothing since the data prop form generator must take care of it
            editConfig.prepareForDataPropUpdate(model, vreq.getWebappDaoFactory().getDataPropertyDao());
        } else{
            //this might be a create new or a form
            editConfig.prepareForNonUpdate(model);
        }
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
