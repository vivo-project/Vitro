/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.Arrays;

import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.DateTimeWithPrecisionVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.FieldVTwo;

public class DateTimeValueFormGenerator extends BaseEditConfigurationGenerator
        implements EditConfigurationGenerator {
	
	final static String vivoCore = "http://vivoweb.org/ontology/core#";
	final static String toDateTimeValue = vivoCore + "dateTimeValue";
	final static String valueType = vivoCore + "DateTimeValue";
	final static String dateTimeValue = vivoCore + "dateTime";
	final static String dateTimePrecision = vivoCore + "dateTimePrecision";

	@Override
	public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq,
			HttpSession session) {
        EditConfigurationVTwo conf = new EditConfigurationVTwo();
        
        initBasics(conf, vreq);
        initPropertyParameters(vreq, session, conf);
        initObjectPropForm(conf, vreq);               
        
        conf.setTemplate("dateTimeValueForm.ftl");
        
        conf.setVarNameForSubject("subject");
        conf.setVarNameForPredicate("toDateTimeValue");
        conf.setVarNameForObject("valueNode");
        
        conf.setN3Optional(Arrays.asList(n3ForValue));
        
        conf.addNewResource("valueNode", DEFAULT_NS_FOR_NEW_RESOURCE);
        
        conf.addSparqlForExistingLiteral(
        		"dateTimeField-value", existingDateTimeValueQuery);
        conf.addSparqlForExistingUris(
        		"dateTimeField-precision", existingPrecisionQuery);
        conf.addSparqlForExistingUris("valueNode", existingNodeQuery);
        
        conf.addField(new FieldVTwo().setName("dateTimeField").
        		setEditElement(new DateTimeWithPrecisionVTwo(null, 
        				VitroVocabulary.Precision.SECOND.uri(), 
        				VitroVocabulary.Precision.NONE.uri())));
        
        return conf;
	}
	
	final static String n3ForValue = 
        "?subject <" + toDateTimeValue + "> ?valueNode . \n" +
        "?valueNode a <" + valueType + "> . \n" +
        "?valueNode  <" + dateTimeValue + "> ?dateTimeField-value . \n" +
        "?valueNode  <" + dateTimePrecision + "> ?dateTimeField-precision .";
	
	final static String existingDateTimeValueQuery = 
        "SELECT ?existingDateTimeValue WHERE { \n" +
        "?subject <" + toDateTimeValue + "> ?existingValueNode . \n" +
        "?existingValueNode a <" + valueType + " . \n" +
        "?existingValueNode <" + dateTimeValue + "> ?existingDateTimeValue }";
	
	final static String existingPrecisionQuery = 
        "SELECT ?existingPrecision WHERE { \n" +
        "?subject <" + toDateTimeValue + "> ?existingValueNode . \n" +
        "?existingValueNode a <" + valueType + "> . \n" +
        "?existingValueNode <"  + dateTimePrecision + "> ?existingPrecision }";
	
	final static String existingNodeQuery =
        "SELECT ?existingNode WHERE { \n" +
        "?subject <" + toDateTimeValue + "> ?existingNode . \n" +
        "?existingNode a <" + valueType + "> }";
}
