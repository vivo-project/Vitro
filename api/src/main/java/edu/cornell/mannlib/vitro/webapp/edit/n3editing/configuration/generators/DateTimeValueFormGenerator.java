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


public class DateTimeValueFormGenerator extends BaseEditConfigurationGenerator
        implements EditConfigurationGenerator {
	
	final static String vivoCore = "http://vivoweb.org/ontology/core#";
	final  String toDateTimeValue = vivoCore + "dateTimeValue";
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
        
        conf.setTemplate(this.getTemplate());
        
        conf.setVarNameForSubject("subject");
        conf.setVarNameForPredicate("toDateTimeValue");
        conf.setVarNameForObject("valueNode");
        //Value node value will be in scope if we have an object uri that exists for editing
        conf.setN3Optional(Arrays.asList(getN3ForValue()));
        
        conf.addNewResource("valueNode", DEFAULT_NS_FOR_NEW_RESOURCE);
        
        conf.addSparqlForExistingLiteral(
        		"dateTimeField-value", getExistingDateTimeValueQuery());
        conf.addSparqlForExistingUris(
        		"dateTimeField-precision", getExistingPrecisionQuery());
        
        FieldVTwo dateTimeField = new FieldVTwo().setName(this.getDateTimeFieldName());
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
	
	
	//Writing these as methods instead of static strings allows the method getToDateTimeValuePredicate
	//to be called after the class has been initialized - this is important for subclasses of this generator
	//that rely on vreq for predicate 
	protected String getN3ForValue() {
        return "?subject <" + this.getToDateTimeValuePredicate() + "> ?valueNode . \n" +
        "?valueNode a <" + valueType + "> . \n" +
        "?valueNode  <" + dateTimeValue + "> ?dateTimeField-value . \n" +
        "?valueNode  <" + dateTimePrecision + "> ?dateTimeField-precision ."; 
	}
	
	protected String getExistingDateTimeValueQuery () {
        return "SELECT ?existingDateTimeValue WHERE { \n" +
        "?subject <" + this.getToDateTimeValuePredicate() + "> ?valueNode . \n" +
        "?valueNode a <" + valueType + "> . \n" +
        "?valueNode <" + dateTimeValue + "> ?existingDateTimeValue }";
	}
	
	protected String getExistingPrecisionQuery() {
        return "SELECT ?existingPrecision WHERE { \n" +
        "?subject <" + this.getToDateTimeValuePredicate() + "> ?valueNode . \n" +
        "?valueNode a <" + valueType + "> . \n" +
        "?valueNode <"  + dateTimePrecision + "> ?existingPrecision }";
	}
	
	public static String getNodeVar() {
		return "valueNode";
	}
	
	public static String getNodeN3Var() {
		return "?" + getNodeVar();
	}

	//isolating the predicate in this fashion allows this class to be subclassed for other date time value
	//properties
	protected String getToDateTimeValuePredicate() {
		return this.toDateTimeValue;
	}
	
	protected String getDateTimeFieldName() {
		return "dateTimeField";
	}
	
	protected String getTemplate() {
		return "dateTimeValueForm.ftl";
	}
//Adding form specific data such as edit mode
	public void addFormSpecificData(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
		HashMap<String, Object> formSpecificData = new HashMap<String, Object>();
		formSpecificData.put("editMode", getEditMode(vreq).name().toLowerCase());
		formSpecificData.put("domainUri", getDomainUri(vreq));
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

	private String getDomainUri(VitroRequest vreq) {
        String domainUri = vreq.getParameter("domainUri"); 
        
		return domainUri;
	}
}