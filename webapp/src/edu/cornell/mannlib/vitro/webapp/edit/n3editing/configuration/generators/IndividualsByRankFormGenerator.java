/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.IndividualsViaObjectPropertyByRankOptions;

/**
 * Generates the edit configuration for a default property form.
 * This handles the default object property auto complete.
 * 
 * If a default property form is request and the number of individuals
 * found in the range is too large, the the auto complete setup and
 * template will be used instead.
 */
public class IndividualsByRankFormGenerator extends DefaultObjectPropertyFormGenerator implements EditConfigurationGenerator {

	private Log log = LogFactory.getLog(IndividualsByRankFormGenerator.class);	
	
	/*
	 *   (non-Javadoc)
	 * @see edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.DefaultObjectPropertyFormGenerator#setFields(edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo, edu.cornell.mannlib.vitro.webapp.controller.VitroRequest, java.lang.String)
	 *
	 * Updates here include using different field options that enable sorting of individuals for the property by display rank
	 */
   @Override    
    protected void setFields(EditConfigurationVTwo editConfiguration, VitroRequest vreq, String predicateUri) throws Exception {    	
		FieldVTwo field = new FieldVTwo();
    	field.setName("objectVar");    	
    	
    	List<String> validators = new ArrayList<String>();
    	validators.add("nonempty");
    	field.setValidators(validators);
    	    	
    	if( ! doAutoComplete ){
    		field.setOptions( new IndividualsViaObjectPropertyByRankOptions(
    	        super.getSubjectUri(),
    	        super.getPredicateUri(), 
    	        super.getObjectUri(),
    	        vreq.getWebappDaoFactory(), 
    	        vreq.getJenaOntModel()));
    	}else{
    		field.setOptions(null);
    	}
    	
    	Map<String, FieldVTwo> fields = new HashMap<String, FieldVTwo>();
    	fields.put(field.getName(), field);    	
    	    	    	
    	editConfiguration.setFields(fields);
    }       
}
