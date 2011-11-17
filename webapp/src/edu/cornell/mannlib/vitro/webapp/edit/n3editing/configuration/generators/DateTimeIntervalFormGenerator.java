/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.Arrays;

import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.DateTimeWithPrecisionVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.FieldVTwo;

public class DateTimeIntervalFormGenerator extends
		BaseEditConfigurationGenerator implements EditConfigurationGenerator {

	final static String vivoCore = "http://vivoweb.org/ontology/core#";
	final static String toDateTimeInterval = vivoCore + "dateTimeInterval";
	final static String intervalType = vivoCore + "DateTimeInterval";
	final static String intervalToStart = vivoCore+"start";
	final static String intervalToEnd = vivoCore + "end";
	final static String dateTimeValue = vivoCore + "dateTime";
	final static String dateTimeValueType = vivoCore + "DateTimeValue";
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
        conf.setVarNameForPredicate("toDateTimeInterval");
        conf.setVarNameForObject("intervalNode");
        
        conf.setN3Optional(Arrays.asList(n3ForStart, n3ForEnd));
        
        conf.addNewResource("intervalNode", DEFAULT_NS_FOR_NEW_RESOURCE);
        conf.addNewResource("startNode", DEFAULT_NS_FOR_NEW_RESOURCE);
        conf.addNewResource("endNode", DEFAULT_NS_FOR_NEW_RESOURCE);
        
        conf.addSparqlForExistingLiteral(
        		"startField-value", existingStartDateQuery);
        conf.addSparqlForExistingLiteral(
        		"endField-value", existingEndDateQuery);
        conf.addSparqlForExistingUris(
        		"intervalNode", existingIntervalNodeQuery);
        conf.addSparqlForExistingUris("startNode", existingStartNodeQuery);
        conf.addSparqlForExistingUris("endNode", existingEndNodeQuery);
        conf.addSparqlForExistingUris(
        		"startField-precision", existingStartPrecisionQuery);
        conf.addSparqlForExistingUris(
        		"endField-precision", existingEndPrecisionQuery);
        
        conf.addField(new FieldVTwo().setName("startField").
        		setEditElement(new DateTimeWithPrecisionVTwo(null, 
        				VitroVocabulary.Precision.SECOND.uri(), 
        				VitroVocabulary.Precision.NONE.uri())));
        conf.addField(new FieldVTwo().setName("endField").
        		setEditElement(new DateTimeWithPrecisionVTwo(null, 
				        VitroVocabulary.Precision.SECOND.uri(), 
				        VitroVocabulary.Precision.NONE.uri())));
        
        return conf;
        
	}
	
	final static String n3ForStart = 
	    "?subject <" + toDateTimeInterval + " ?intervalNode . \n" +    
	    "?intervalNode  a <" + intervalType + "> . \n" + 
	    "?intervalNode <" + intervalToStart + "> ?startNode . \n" +    
	    "?startNode a <" + dateTimeValueType + "> . \n" +
	    "?startNode  <" + dateTimeValue + "> ?startField-value . \n" +
	    "?startNode  <" + dateTimePrecision + "> ?startField-precision . \n";
	
	final static String n3ForEnd = 
        "?subject <" + toDateTimeInterval + "> ?intervalNode . \n" +       
        "?intervalNode a <" + intervalType + "> . \n" +
        "?intervalNode <" + intervalToEnd + "> ?endNode . \n" +
        "?endNode a <" + dateTimeValueType + "> . \n" +
        "?endNode  <" + dateTimeValue + "> ?endField-value . \n" +
        "?endNode  <" + dateTimePrecision + "> ?endField-precision .";
	
	final static String existingStartDateQuery =
        "SELECT ?existingDateStart WHERE { \n" +     
        "?subject <" + toDateTimeInterval + "> ?existingIntervalNode . \n" +     
        "?intervalNode a <" + intervalType + "> . \n" +
        "?intervalNode <" + intervalToStart + "> ?startNode . \n" +
        "?startNode a <" + dateTimeValueType + "> . \n" +
        "?startNode <" + dateTimeValue + "> ?existingDateStart }";

	final static String existingEndDateQuery = 
        "SELECT ?existingEndDate WHERE { \n" +
        "?subject <" + toDateTimeInterval + "> ?existingIntervalNode . \n" +
        "?intervalNode a <" + intervalType + "> . \n" +
        "?intervalNode <" + intervalToEnd + "> ?endNode . \n" +
        "?endNode a <" + dateTimeValueType + "> . \n " +
        "?endNode <" + dateTimeValue + "> ?existingEndDate . }";
	
	final static String existingIntervalNodeQuery = 
        "SELECT ?existingIntervalNode WHERE { \n" +
        "?subject <" + toDateTimeInterval + "> ?existingIntervalNode . \n" +
        "?existingIntervalNode a <" + intervalType + "> . }";
	
	final static String existingStartNodeQuery =
		"SELECT ?existingStartNode WHERE { \n" +
        "?subject <" + toDateTimeInterval + "> ?existingIntervalNode . \n" +
        "?intervalNode a <" + intervalType + "> . \n" +
        "?intervalNode <" + intervalToStart + "> ?existingStartNode . \n" + 
        "?existingStartNode a <" + dateTimeValueType + "> .}  ";
	
	final static String existingEndNodeQuery = 
        "SELECT ?existingEndNode WHERE { \n" + 
        "?subject <" + toDateTimeInterval + "> ?existingIntervalNode . \n" +
        "?intervalNode a <" + intervalType + "> . \n" +
        "?intervalNode <" + intervalToEnd + "> ?existingEndNode . \n" + 
        "?existingEndNode a <" + dateTimeValueType + "> .} ";
	
	final static String existingStartPrecisionQuery = 
        "SELECT ?existingStartPrecision WHERE { \n" +    
        "?subject <" + toDateTimeInterval + "> ?existingIntervalNode . \n" +      
        "?intervalNode a <" + intervalType + "> . \n" +
        "?intervalNode <" + intervalToStart + "> ?startNode . \n" +
        "?startNode a <" + dateTimeValueType + "> . \n" +          
        "?startNode <" + dateTimePrecision + "> ?existingStartPrecision . }";
	
	final static String existingEndPrecisionQuery =
		"SELECT ?existingEndPrecision WHERE { \n" +
        "?subject <" + toDateTimeInterval + "> ?existingIntervalNode . \n" +      
        "?intervalNode a <" + intervalType + "> . \n" +
        "intervalNode <" + intervalToEnd + "> ?endNode . \n" +
        "?endNode a <" + dateTimeValueType + "> . \n" +          
        "?endNode <" + dateTimePrecision + "> ?existingEndPrecision . }";

}
