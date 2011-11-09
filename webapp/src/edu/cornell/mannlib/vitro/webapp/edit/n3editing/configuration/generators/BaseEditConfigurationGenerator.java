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

//    @Override
//    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq,
//            HttpSession session) {
//    EditConfigurationVTwo editConfiguration = new EditConfigurationVTwo();
//    //Set n3 generator
//    editConfiguration.setN3Generator(new EditN3GeneratorVTwo(editConfiguration));
//    
//    //process subject, predicate, object parameters
//    this.initPropertyParameters(vreq, session, editConfiguration);
//    
//    //Assumes this is a simple case of subject predicate var
//    editConfiguration.setN3Required(generateN3Required(vreq));
//            
//    //n3 optional
//    editConfiguration.setN3Optional(generateN3Optional());
//    
//    //Todo: what do new resources depend on here?
//    //In original form, these variables start off empty
//    editConfiguration.setNewResources(generateNewResources(vreq));
//    //In scope
//    setUrisAndLiteralsInScope(editConfiguration, vreq);
//    
//    //on Form
//    setUrisAndLiteralsOnForm(editConfiguration, vreq);
//    
//    editConfiguration.setFilesOnForm(new ArrayList<String>());
//    
//    //Sparql queries
//    setSparqlQueries(editConfiguration, vreq);
//    
//    //set fields
//    setFields(editConfiguration, vreq, EditConfigurationUtils.getPredicateUri(vreq));
//    
//    prepareForUpdate(vreq, session, editConfiguration);
//    
//    //Form title and submit label now moved to edit configuration template
//    //TODO: check if edit configuration template correct place to set those or whether
//    //additional methods here should be used and reference instead, e.g. edit configuration template could call
//    //default obj property form.populateTemplate or some such method
//    //Select from existing also set within template itself
//    setTemplate(editConfiguration, vreq);
//    
//    //Set edit key
//    setEditKey(editConfiguration, vreq);
//    
//    //Add validator
//    setValidators(editConfiguration, vreq); 
//    
//    //Add preprocessors
//    addPreprocessors(editConfiguration, vreq.getWebappDaoFactory());
//    
//    //Adding additional data, specifically edit mode
//    addFormSpecificData(editConfiguration, vreq);
//    
//    return editConfiguration;
//
//}
//
//    abstract void setValidators(EditConfigurationVTwo editConfiguration, VitroRequest vreq) ;
//
//    abstract void addFormSpecificData(EditConfigurationVTwo editConfiguration,
//            VitroRequest vreq) ;
//
//    abstract void addPreprocessors(EditConfigurationVTwo editConfiguration,
//            WebappDaoFactory webappDaoFactory) ;
//
//    abstract void setEditKey(EditConfigurationVTwo editConfiguration,
//            VitroRequest vreq) ;
//
//    abstract void setTemplate(EditConfigurationVTwo editConfiguration,
//            VitroRequest vreq) ;
//
//    abstract void  prepareForUpdate(VitroRequest vreq, HttpSession session,
//            EditConfigurationVTwo editConfiguration) ;
//
//    abstract void  setFields(EditConfigurationVTwo editConfiguration,
//            VitroRequest vreq, String predicateUri) ;
//
//    abstract void  setSparqlQueries(EditConfigurationVTwo editConfiguration,
//            VitroRequest vreq) ;
//
//    abstract void  setUrisAndLiteralsOnForm(
//            EditConfigurationVTwo editConfiguration, VitroRequest vreq) ;
//
//    abstract void  setUrisAndLiteralsInScope(
//            EditConfigurationVTwo editConfiguration, VitroRequest vreq) ;
//
//    abstract Map<String, String> generateNewResources(VitroRequest vreq) ;
//
//    abstract  List<String> generateN3Optional() ;
//
//    abstract  List<String> generateN3Required(VitroRequest vreq) ;

    /* constants */
    public static final String DEFAULT_NS_FOR_NEW_RESOURCE= "";
    
    /* Utility Methods */
    
    /**
     * Sets up the things that should be done for just about every form.
     */
    void initBasics(EditConfigurationVTwo editConf, VitroRequest vreq){
        editConf.setN3Generator( new EditN3GeneratorVTwo(editConf) );
                        
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
        
        //this needs to be set for the editing to be triggered properly, otherwise the 'prepare' method
        //pretends this is a data property editing statement and throws an error
        //TODO: Check if null in case no object uri exists but this is still an object property
        if(editConfiguration.getObject() != null ) {
            editConfiguration.setObjectResource(true);
        }
    }    
    
}
