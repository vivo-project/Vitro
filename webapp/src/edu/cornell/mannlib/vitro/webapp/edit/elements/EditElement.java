/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.elements;

import java.util.Map;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditSubmission;
import freemarker.template.Configuration;
/**
 * All classes that implement this interface must have a public empty constructor that
 * will be called with using reflection.
 */
public interface EditElement {  
    /**
     * This is a method to get a map of variable name to Literal value from the submitted form. 
     */
    public Map<String,Literal> 
        getLiterals(String fieldName, EditConfiguration editConfig, Map<String,String[]> queryParameters );
    
    /**
     * This is a method to generate the HTML output for a form element.  It should use a freemarker template
     * to produce the output.  
     */
    public String draw(String fieldName, EditConfiguration editConfig, EditSubmission editSub, Configuration fmConfig);        
}
