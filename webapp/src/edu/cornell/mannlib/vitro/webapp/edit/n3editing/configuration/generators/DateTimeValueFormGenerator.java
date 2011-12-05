/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.DateTimeWithPrecisionVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils;
import edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils.EditMode;
import edu.cornell.mannlib.vitro.webapp.utils.generators.EditModeUtils;


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
        
        FieldVTwo dateTimeField = new FieldVTwo().setName("dateTimeField");
        		dateTimeField.setEditElement(new DateTimeWithPrecisionVTwo(dateTimeField, 
        				VitroVocabulary.Precision.SECOND.uri(), 
        				VitroVocabulary.Precision.NONE.uri()));
        
        conf.addField(dateTimeField);
        
        //Adding additional data, specifically edit mode
        addFormSpecificData(conf, vreq);
        //prepare
        prepare(vreq, conf);
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
        "?existingValueNode a <" + valueType + "> . \n" +
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


//Adding form specific data such as edit mode
	public void addFormSpecificData(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
		HashMap<String, Object> formSpecificData = new HashMap<String, Object>();
		formSpecificData.put("editMode", getEditMode(vreq).name().toLowerCase());
		editConfiguration.setFormSpecificData(formSpecificData);
	}
	
	public EditMode getEditMode(VitroRequest vreq) {
		//In this case, the original jsp didn't rely on FrontEndEditingUtils
		//but instead relied on whether or not the object Uri existed
		String objectUri = EditConfigurationUtils.getObjectUri(vreq);
		EditMode editMode = FrontEndEditingUtils.EditMode.ADD;
		if(objectUri != null && !objectUri.isEmpty()) {
			editMode = FrontEndEditingUtils.EditMode.EDIT;
			
		}
		return editMode;
	}
}