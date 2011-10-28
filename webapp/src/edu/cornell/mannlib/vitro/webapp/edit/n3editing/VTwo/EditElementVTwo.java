/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import java.util.Map;
import java.util.List;
import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.MultiValueEditSubmission;
import freemarker.template.Configuration;
/**
 * All classes that implement this interface must have a public constructor that 
 * takes a edu.cornell.mannlib.vitro.webapp.edit.n3editing.Field.  It will be 
 * called with using reflection.
 */
public interface EditElementVTwo {  
    /**
     * This is a method to get a map of variable name to Literal value from the submitted form. 
     */
    public Map<String,List<Literal>> 
        getLiterals(String fieldName, EditConfigurationVTwo editConfig, Map<String,String[]> queryParameters );
    
    /**
     * This is a method to get a map of variable name to URI values from the submitted form. 
     */
    public Map<String,List<String>> 
        getURIs(String fieldName, EditConfigurationVTwo editConfig, Map<String,String[]> queryParameters );
    
    /**
     * Gets validation error messages.  Returns an empty list if there are no errors.
     */
    public Map<String,String>
        getValidationMessages(String fieldName, EditConfigurationVTwo editConfig, Map<String,String[]> queryParameters);
    
    /**
     * This is a method to generate the HTML output for a form element.  It should use a freemarker template
     * to produce the output.  
     */
    public String draw(String fieldName, EditConfigurationVTwo editConfig, MultiValueEditSubmission editSub, Configuration fmConfig);
    
    /**
     * This method gets the map with the data that can then be passed to the template
     */
    public Map getMapForTemplate(EditConfigurationVTwo editConfig, MultiValueEditSubmission editSub);            
    
    /* in the future, we may need to get existing values */
    /*
    public Map<String,Literal> getExistingLiterals(???)
    public Map<String,String> getExistingURIs(???);
    */
}
