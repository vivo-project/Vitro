/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.DateTimeWithPrecisionVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils;
import edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils.EditMode;
import edu.cornell.mannlib.vitro.webapp.utils.generators.EditModeUtils;

/**
 * 
 * @author hjk54
 *
 * There are multiple date time properties in VIVO, all of which have a DateTimeValue individual as an object.
 * I am extending DateTimeValueFormGenerator to enable the predicate to be utilized as the "toDateTimeValue" property, i.e. 
 * the property associating the subject with the date time value individual.  The dateTimeValueForm template has been
 * used as the basis for the general form for this generator that can be applied to multiple properties. 
 */

public class DateTimePropertyGenerator extends DateTimeValueFormGenerator {
	
	String predicate = null;
	@Override
	public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq,
			HttpSession session) {
		predicate = EditConfigurationUtils.getPredicateUri(vreq);  
        EditConfigurationVTwo conf = super.getEditConfiguration(vreq, session);
        return conf; 
	} 	

	//isolating the predicate in this fashion allows this class to be subclassed for other date time value
	//properties
	@Override
	protected String getToDateTimeValuePredicate() {
		return predicate;
	}
	
}