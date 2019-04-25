/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.HashMap;

import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.DateTimeIntervalValidationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.DateTimeWithPrecisionVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils;
import edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils.EditMode;

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
        this.setupEditConfiguration(conf, vreq, session);
        //Prepare
        prepare(vreq, conf);
        return conf;

	}

	//Enables refactoring by other generators, as this code can be used to partially setup an
	//edit configuration object which can then be extended - prepare can then be called independently
	//enabling sparql queries to be evaluated using the final edit configuration object
	public void setupEditConfiguration(EditConfigurationVTwo conf, VitroRequest vreq, HttpSession session) {
        initBasics(conf, vreq);
        initPropertyParameters(vreq, session, conf);
        initObjectPropForm(conf, vreq);

        conf.setTemplate("dateTimeIntervalForm.ftl");

        conf.setVarNameForSubject("subject");
        conf.setVarNameForPredicate("toDateTimeInterval");
        conf.setVarNameForObject(getNodeVar());

        conf.setN3Optional(n3ForStart, n3ForEnd);

        conf.addNewResource(getNodeVar(), DEFAULT_NS_FOR_NEW_RESOURCE);
        conf.addNewResource("startNode", DEFAULT_NS_FOR_NEW_RESOURCE);
        conf.addNewResource("endNode", DEFAULT_NS_FOR_NEW_RESOURCE);

        conf.addSparqlForExistingLiteral(
        		"startField-value", existingStartDateQuery);
        conf.addSparqlForExistingLiteral(
        		"endField-value", existingEndDateQuery);

        conf.addSparqlForExistingUris("startNode", existingStartNodeQuery);
        conf.addSparqlForExistingUris("endNode", existingEndNodeQuery);
        conf.addSparqlForExistingUris(
        		"startField-precision", existingStartPrecisionQuery);
        conf.addSparqlForExistingUris(
        		"endField-precision", existingEndPrecisionQuery);

        FieldVTwo startField = new FieldVTwo().setName("startField");
        		startField.setEditElement(new DateTimeWithPrecisionVTwo(startField,
        				VitroVocabulary.Precision.SECOND.uri(),
        				VitroVocabulary.Precision.NONE.uri()));

        FieldVTwo endField = new FieldVTwo().setName("endField");
        		endField.setEditElement(new DateTimeWithPrecisionVTwo(endField,
        		        VitroVocabulary.Precision.SECOND.uri(),
            			VitroVocabulary.Precision.NONE.uri()));

        conf.addField(startField);
        conf.addField(endField);
        //Need to add validators
        conf.addValidator(new DateTimeIntervalValidationVTwo("startField","endField","dateTimeIntervalForm.ftl"));
        //Adding additional data, specifically edit mode
        addFormSpecificData(conf, vreq);
	}

	final String n3ForStart =
	    "?subject <" + getToDateTimeIntervalPredicate() + "> " + getNodeN3Var() + " . \n" +
	    getNodeN3Var() + "  a <" + intervalType + "> . \n" +
	    getNodeN3Var() + " <" + intervalToStart + "> ?startNode . \n" +
	    "?startNode a <" + dateTimeValueType + "> . \n" +
	    "?startNode  <" + dateTimeValue + "> ?startField-value . \n" +
	    "?startNode  <" + dateTimePrecision + "> ?startField-precision . \n";

	final  String n3ForEnd =
        "?subject <" + getToDateTimeIntervalPredicate() + "> " + getNodeN3Var() + " . \n" +
        getNodeN3Var() + " a <" + intervalType + "> . \n" +
        getNodeN3Var() + " <" + intervalToEnd + "> ?endNode . \n" +
        "?endNode a <" + dateTimeValueType + "> . \n" +
        "?endNode  <" + dateTimeValue + "> ?endField-value . \n" +
        "?endNode  <" + dateTimePrecision + "> ?endField-precision .";

	final  String existingStartDateQuery =
        "SELECT ?existingDateStart WHERE { \n" +
        "?subject <" + getToDateTimeIntervalPredicate() + "> " + getNodeN3Var() + " . \n" +
        getNodeN3Var() + " a <" + intervalType + "> . \n" +
        getNodeN3Var() + " <" + intervalToStart + "> ?startNode . \n" +
        "?startNode a <" + dateTimeValueType + "> . \n" +
        "?startNode <" + dateTimeValue + "> ?existingDateStart }";

	final  String existingEndDateQuery =
        "SELECT ?existingEndDate WHERE { \n" +
        "?subject <" + getToDateTimeIntervalPredicate() + "> " + getNodeN3Var() + " . \n" +
        getNodeN3Var() + " a <" + intervalType + "> . \n" +
        getNodeN3Var() + " <" + intervalToEnd + "> ?endNode . \n" +
        "?endNode a <" + dateTimeValueType + "> . \n " +
        "?endNode <" + dateTimeValue + "> ?existingEndDate . }";


	final  String existingStartNodeQuery =
		"SELECT ?existingStartNode WHERE { \n" +
        "?subject <" + getToDateTimeIntervalPredicate() + "> " + getNodeN3Var() + " . \n" +
        getNodeN3Var() + " a <" + intervalType + "> . \n" +
        getNodeN3Var() + " <" + intervalToStart + "> ?existingStartNode . \n" +
        "?existingStartNode a <" + dateTimeValueType + "> .}  ";

	final  String existingEndNodeQuery =
        "SELECT ?existingEndNode WHERE { \n" +
        "?subject <" + getToDateTimeIntervalPredicate() + "> " + getNodeN3Var() + " . \n" +
        getNodeN3Var() + " a <" + intervalType + "> . \n" +
        getNodeN3Var() + " <" + intervalToEnd + "> ?existingEndNode . \n" +
        "?existingEndNode a <" + dateTimeValueType + "> .} ";

	final  String existingStartPrecisionQuery =
        "SELECT ?existingStartPrecision WHERE { \n" +
        "?subject <" + getToDateTimeIntervalPredicate() + "> " + getNodeN3Var() + " . \n" +
        getNodeN3Var() + " a <" + intervalType + "> . \n" +
        getNodeN3Var() + " <" + intervalToStart + "> ?startNode . \n" +
        "?startNode a <" + dateTimeValueType + "> . \n" +
        "?startNode <" + dateTimePrecision + "> ?existingStartPrecision . }";

	final  String existingEndPrecisionQuery =
		"SELECT ?existingEndPrecision WHERE { \n" +
        "?subject <" + getToDateTimeIntervalPredicate() + "> " + getNodeN3Var() + " . \n" +
        getNodeN3Var() + " a <" + intervalType + "> . \n" +
        getNodeN3Var() + " <" + intervalToEnd + "> ?endNode . \n" +
        "?endNode a <" + dateTimeValueType + "> . \n" +
        "?endNode <" + dateTimePrecision + "> ?existingEndPrecision . }";

	public  String getToDateTimeIntervalPredicate() {
		return toDateTimeInterval;
	}

	public  String getNodeVar() {
		return "intervalNode";
	}

	public  String getNodeN3Var() {
		return "?" + getNodeVar();
	}

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
