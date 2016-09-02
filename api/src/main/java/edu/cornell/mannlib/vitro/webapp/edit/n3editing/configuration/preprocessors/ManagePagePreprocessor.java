/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.BaseEditSubmissionPreprocessorVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.MultiValueEditSubmission;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfigurationConstants;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.ManagePageGenerator;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils.ProcessDataGetterN3;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils.ProcessDataGetterN3Utils;
public class ManagePagePreprocessor extends
		BaseEditSubmissionPreprocessorVTwo {

	protected static final Log log = LogFactory
			.getLog(ManagePagePreprocessor.class.getName());

	
	private static MultiValueEditSubmission submission = null;
	private static EditConfigurationVTwo editConfiguration = null;
	private static Map<String, List<String>> transformedLiteralsFromForm = null;
	private static Map<String, List<String>> urisFromForm = null;
	private static List<String> pageContentUnits = null;//String submission from form
	private static List<JSONObject> pageContentUnitsJSON = null;//converted to JSON objects that can be read
	// String datatype

	// Will be editing the edit configuration as well as edit submission here

	public ManagePagePreprocessor(EditConfigurationVTwo editConfig) {
		super(editConfig);
		editConfiguration = editConfig;
	}

	public void preprocess(MultiValueEditSubmission inputSubmission, VitroRequest vreq) {
		submission = inputSubmission;
		// Get the input elements for concept node and concept label as well
		// as vocab uri (which is based on thge
		// For query parameters, check whether CUI
		copySubmissionValues();
		processDataGetters();
		//In case of edit, need to force deletion of existing values where necessary
		//In our case, values that already exist and will be overwritten will be in submission already
		//just as new values will 
		//Anything left over should be replaced with blank value sentinel as that would
		//no longer be on the form and have a value submitted and we can delete that statement
		//if it exists
		processExistingValues();
		

	}
	
	//Since we will change the uris and literals from form, we should make copies
	//of the original values and store them, this will also make iterations
	//and updates to the submission independent from accessing the values
	private void copySubmissionValues() {
		Map<String, List<Literal>> literalsFromForm = submission.getLiteralsFromForm();
		transformedLiteralsFromForm = copyMap(EditConfigurationUtils.transformLiteralMap(literalsFromForm));
		urisFromForm = copyMap(submission.getUrisFromForm());
		pageContentUnits = transformedLiteralsFromForm.get("pageContentUnit");
	}

	private Map<String, List<String>> copyMap(
			Map<String, List<String>> originalMap) {
		Map<String, List<String>> copyMap = new HashMap<String, List<String>>();
		copyMap.putAll(originalMap);
		return copyMap;
	}
	
	private void processExistingValues() {
		//For all literals that were originally in scope that don't have values on the form
		//anymore, replace with null value
		//For those literals, those values will be replaced with form values where overwritten
		//And will be deleted where not overwritten which is the behavior we desire
		if(this.editConfiguration.isParamUpdate()) {
			Map<String, List<Literal>> literalsInScope = this.editConfiguration.getLiteralsInScope();
			Map<String, List<String>> urisInScope = this.editConfiguration.getUrisInScope();
			List<String> literalKeys = new ArrayList<String>(literalsInScope.keySet());
	
			
			List<String> uriKeys = new ArrayList<String>(urisInScope.keySet());
			for(String literalName: literalKeys) {
				
				//if submission already has value for this, then leave be
				//otherwise replace with null which will not be valid N3
				//TODO: Replace with better solution for forcing literal deletion
				boolean haslv = submission.hasLiteralValue(literalName);
				if(!submission.hasLiteralValue(literalName)) {
					submission.addLiteralToForm(editConfiguration, 
							 editConfiguration.getField(literalName), 
							 literalName, 
							 (new String[] {null}));
				}
			}
			
			
			for(String uriName: uriKeys) {
				//these values should never be overwritten or deleted
				//if(uriName != "page" && uriName != "menuItem" && !uriName.startsWith("dataGetter")) {
				if(uriName != "page") {
					boolean hasuv = submission.hasUriValue(uriName);
					if(!submission.hasUriValue(uriName)) {
						submission.addUriToForm(editConfiguration, 
								 uriName, 
								 (new String[] {EditConfigurationConstants.BLANK_SENTINEL}));
					}	
				}
			}
		}
		
	}
	

	private void processDataGetters() {
		convertToJson();
		int counter = 0;
		for(JSONObject jsonObject:pageContentUnitsJSON) {
			String dataGetterClass = getDataGetterClass(jsonObject);
			ProcessDataGetterN3 pn = ProcessDataGetterN3Utils.getDataGetterProcessorN3(dataGetterClass, jsonObject);
			//UPDATE: using class type to indicate class type/ could also get it from 
			//processor but already have it here
			jsonObject.put("classType", pn.getClassType());
			//Removing n3 required b/c retracts in edit case depend on both n3 required and n3 optional
			//To not muddle up logic, we will just add ALL required and optional statements
			//from data getters directly to N3 optional
			//Add n3 required
			//addN3Required(pn, counter);
			//Add N3 Optional as well
			addN3Optional(pn, counter);
			// Add URIs on Form and Add Literals On Form
			addLiteralsAndUrisOnForm(pn, counter);
			// Add fields
			addFields(pn, counter);
			//Add new resources - data getters need to be new resources
			addNewResources(pn, counter);
			//Add input values to submission
			addInputsToSubmission(pn, counter, jsonObject);
			counter++;
		}
	}
	
	

	private void addNewResources(ProcessDataGetterN3 pn, int counter) {
		// TODO Auto-generated method stub
		List<String> newResources = pn.getNewResources(counter);
		for(String newResource:newResources) {
			//Will null get us display vocabulary or something else?
			
			editConfiguration.addNewResource(newResource, null);
			//Weirdly enough, the defaultDisplayNS doesn't act as a namespace REALLY
			//as it first gets assigned as the URI itself and this lead to an error
			//instead of repetitively trying to get another URI
			//editConfiguration.addNewResource(newResource, ManagePageGenerator.defaultDisplayNs );
		}
		
	}

	private void convertToJson() {
		//Iterate through list of inputs
		pageContentUnitsJSON = new ArrayList<JSONObject>();
		//page content units might return null in case self-contained template is selected
		//otherwise there should be page content units returned from the form
		if(pageContentUnits != null) {
			for(String pageContentUnit: pageContentUnits) {
				JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( pageContentUnit );
				pageContentUnitsJSON.add(jsonObject);
			}
		}
	}

	//This is where the actual values will be submitted as if they were separate input fields
	//Each field name will correspond to the names of the fileds/uris on form/literals on form
	//generated here

	private void addInputsToSubmission(ProcessDataGetterN3 pn, int counter, JSONObject jsonObject) {
		 List<String> literalLabels = pn.getLiteralVarNamesBase();
		 List<String> uriLabels = pn.getUriVarNamesBase();
		 
		 for(String literalLabel:literalLabels) {
			 List<String> literalValues = new ArrayList<String>();
			 Object jsonValue = jsonObject.get(literalLabel);
			//Var names will depend on which data getter object this is on the page, so depends on counter
			 String submissionLiteralName = pn.getVarName(literalLabel, counter);
			 //Single value
			 if(jsonValue instanceof String) {
				 //TODO: Deal with multiple submission values
				 //This retrieves the value for this particular json object
				 String jsonString = jsonObject.getString(literalLabel);
				 jsonString = pn.replaceEncodedQuotesWithEscapedQuotes(jsonString);
				 literalValues.add(jsonString);
			 } else if(jsonValue instanceof JSONArray) {
				 JSONArray values = jsonObject.getJSONArray(literalLabel);
				 literalValues = (List<String>) JSONSerializer.toJava(values);
				 //Replacing encoded quotes here as well
				 this.replaceEncodedQuotesInList(pn, literalValues);
			 } else if(jsonValue instanceof Boolean) {
				 Boolean booleanValue = jsonObject.getBoolean(literalLabel);
				 //Adds string version
				 literalValues.add(booleanValue.toString());
			 }
			 String[] literalValuesSubmission = new String[literalValues.size()];
			 literalValuesSubmission = literalValues.toArray(literalValuesSubmission);
			 //This adds literal, connecting the field with 
			 submission.addLiteralToForm(editConfiguration, 
					 editConfiguration.getField(submissionLiteralName), 
					 submissionLiteralName, 
					 literalValuesSubmission);
		 }
		 
		 for(String uriLabel:uriLabels) {
			 List<String> uriValues = new ArrayList<String>();
			 Object jsonValue = jsonObject.get(uriLabel);
			//Var names will depend on which data getter object this is on the page, so depends on counter
			 String submissionUriName = pn.getVarName(uriLabel, counter);
			 //if single value, then, add to values
			 if(jsonValue instanceof String) {
				 //Var names will depend on which data getter object this is on the page, so depends on counter
				 //This retrieves the value for this particular json object and adds to list
				 uriValues.add(jsonObject.getString(uriLabel));

			 } else if(jsonValue instanceof JSONArray) {
				 //multiple values
				 JSONArray values = jsonObject.getJSONArray(uriLabel);
				 uriValues = (List<String>) JSONSerializer.toJava(values);
				
			 } else {
				 //This may include JSON Objects but no way to deal with these right now
			 }
			 String[] uriValuesSubmission = new String[uriValues.size()];

			 uriValuesSubmission = uriValues.toArray(uriValuesSubmission);
			 //This adds literal, connecting the field with the value
			 submission.addUriToForm(editConfiguration, submissionUriName, uriValuesSubmission);
			 
		 }
		
		 //To get data getter uris, check if editing an existing set and include those as form inputs
		 if(editConfiguration.isParamUpdate()) {
			 //Although this is editing an existing page, new content might have been added which would not include
			 //existing data getter URIs, so important to check whether the key exists within the json object in the first place
			 String dataGetterURISubmissionName = pn.getDataGetterVarName(counter);
			 if(jsonObject.containsKey("URI")) {
				 String URIValue = jsonObject.getString("URI");
				 if(URIValue != null) {
					 log.debug("Existing URI for data getter found: " + URIValue);
					 submission.addUriToForm(editConfiguration, dataGetterURISubmissionName, new String[]{URIValue});
				 }
			 } else {
				 //if the URI is not included in the json object, this is a NEW data getter
				 //and as such as we must ensure the URI is created
				 submission.addUriToForm(editConfiguration, dataGetterURISubmissionName, new String[]{EditConfigurationConstants.NEW_URI_SENTINEL});

			 }
		 }
		
	}

	private void replaceEncodedQuotesInList(ProcessDataGetterN3 pn, List<String> values) {
		int i;
		int len = values.size();
		for(i = 0; i < len; i++) {
			String value = values.get(i);
			if(value.contains("&quot;") || value.contains("&#39;")) {
				value = pn.replaceEncodedQuotesWithEscapedQuotes(value);
				values.set(i,value);
			}
		}
	}


	private void addFields(ProcessDataGetterN3 pn, int counter) {
		List<FieldVTwo> fields = pn.retrieveFields(counter);
		//Check if fields don't already exist in case of editing
		Map<String, FieldVTwo> existingFields = editConfiguration.getFields();
		for(FieldVTwo newField: fields) {
			String newFieldName = newField.getName();
			//if not already in list and about the same
			if(existingFields.containsKey(newFieldName)) {
				FieldVTwo existingField = existingFields.get(newFieldName);
				if(existingField.isEqualTo(newField)) {
					log.debug("This field already exists and so will not be added:" + newFieldName);
				} else {
					log.error("The field with the same name is different and will not be added as a different field exists which is different:" + newFieldName);
				}
			} else {
				editConfiguration.addField(newField);
			}
		}
	}
	
	

	//original literals on form: label, uris on form: conceptNode and conceptSource
	//This will overwrite the original values in the edit configuration
	private void addLiteralsAndUrisOnForm(ProcessDataGetterN3 pn, int counter) {
		List<String> literalsOnForm = pn.retrieveLiteralsOnForm(counter);
		editConfiguration.addLiteralsOnForm(literalsOnForm);
		List<String> urisOnForm = pn.retrieveUrisOnForm(counter);
		editConfiguration.addUrisOnForm(urisOnForm);
	}

	// N3 being reproduced
	/*
	 * ?subject ?predicate ?conceptNode .
	 */
	//NOT Using this right now
	//This will overwrite the original with the set of new n3 required
	/*
	private void addN3Required(ProcessDataGetterN3 pn, int counter) {
		//Use the process utils to figure out what class required to retrieve the N3 required
		List<String> requiredList = pn.retrieveN3Required(counter);
		//Add connection between data getter and page
		requiredList.addAll(getPageToDataGetterN3(pn, counter));
		if(requiredList != null) {
			editConfiguration.addN3Required(requiredList);
		}
	}*/
	private List<String> getPageToDataGetterN3(
			ProcessDataGetterN3 pn, int counter) {
		String dataGetterVar = pn.getDataGetterVar(counter);
		//Put this method in the generator but can be put elsewhere
		String pageToDataGetterN3 = ManagePageGenerator.getDataGetterN3(dataGetterVar);
		return Arrays.asList(pageToDataGetterN3);
		
	}

	//Add n3 optional
	
	private void addN3Optional(ProcessDataGetterN3 pn, int counter) {
		List<String> addList = new ArrayList<String>();
		//Get required list
		List<String> requiredList = pn.retrieveN3Required(counter);
		//Add connection between data getter and page
		requiredList.addAll(getPageToDataGetterN3(pn, counter));
		//get optional n3
		List<String> optionalList = pn.retrieveN3Optional(counter);
		if(requiredList != null) {
			addList.addAll(requiredList);
		}
		
		if(optionalList != null) {
			addList.addAll(optionalList);
		}
		
		editConfiguration.addN3Optional(addList);

	}
	
	
	//Each JSON Object will indicate the type of the data getter within it
	private String getDataGetterClass(JSONObject jsonObject) {
		String javaURI = jsonObject.getString("dataGetterClass");
		return getQualifiedDataGetterName(javaURI);
		
		
	}
	
	//Get rid of java: in front of class name
	private String getQualifiedDataGetterName(String dataGetterTypeURI) {
		String javaURI = "java:";
		
		if(dataGetterTypeURI.startsWith(javaURI)) {
			int beginIndex = javaURI.length();
			return dataGetterTypeURI.substring(beginIndex);
		}
		return dataGetterTypeURI;
	}

}
