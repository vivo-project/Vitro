/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.BaseEditSubmissionPreprocessorVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.MultiValueEditSubmission;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.ManagePageGenerator;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils.ProcessDataGetterN3;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils.ProcessDataGetterN3Utils;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.util.JSONUtils;
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

	public void preprocess(MultiValueEditSubmission inputSubmission) {
		submission = inputSubmission;
		// Get the input elements for concept node and concept label as well
		// as vocab uri (which is based on thge
		// For query parameters, check whether CUI
		copySubmissionValues();
		processDataGetters();
		

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

	private void processDataGetters() {
		convertToJson();
		int counter = 0;
		for(JSONObject jsonObject:pageContentUnitsJSON) {
			String dataGetterClass = getDataGetterClass(jsonObject);
			ProcessDataGetterN3 pn = ProcessDataGetterN3Utils.getDataGetterProcessorN3(dataGetterClass, jsonObject);
			//Add n3 required
			addN3Required(pn, counter);
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
		for(String pageContentUnit: pageContentUnits) {
			JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( pageContentUnit );
			pageContentUnitsJSON.add(jsonObject);
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
				 literalValues.add(jsonObject.getString(literalLabel));
			 } else if(jsonValue instanceof JSONArray) {
				 JSONArray values = jsonObject.getJSONArray(literalLabel);
				 literalValues = (List<String>) JSONSerializer.toJava(values);
			 }
			 //This adds literal, connecting the field with 
			 submission.addLiteralToForm(editConfiguration, 
					 editConfiguration.getField(submissionLiteralName), 
					 submissionLiteralName, 
					 (String[])literalValues.toArray());
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
			 submission.addLiteralToForm(editConfiguration, 
					 editConfiguration.getField(submissionUriName), 
					 submissionUriName, 
					 uriValuesSubmission);
			 
		 }
		 //this needs to be different
		 //Get the literals directly from the processor - which you can get based on counter
		 //Problem then is we know what the connection is - some way to put that logic within the processor itself?
		 //It already knows the json object
		 
		 //--> Get field for this base label, with counter value - that you get from configuration
/*
		 while(jsonObject.keys().hasNext())
		 {
			 //Other than class, all other variables considered a submission value corresponding to field
			 String key = (String) jsonObject.keys().next();
			 if(key != "dataGetterClass") {
				 //not expecting multiple values, so will need to either make this array or 
				 //think about this some more
				 //TODO: Consider multiple values here
				 Map<String, String[]> submissionValues = new HashMap<String, String[]>();
				 submissionValues.put(key, new String[]{jsonObject.getString(key)} );
				
				 if(literalLabels.contains(key)) {
					 submission.addLiteralToForm(editConfiguration.getField(key), field, var, valuesArray)
				 }
			 }
			 
		 }
		 List<String> uris = pn.retrieveUrissOnForm(counter);
		 for(String l:literals) {
			 //json object should have 
			 submissionValues.put(l, new String[]{jsonObject.getString(l)} );
		 }*/
		
	}

	  


	private void addFields(ProcessDataGetterN3 pn, int counter) {
		List<FieldVTwo> fields = pn.retrieveFields(counter);
		editConfiguration.addFields(fields);
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
	//This will overwrite the original with the set of new n3 required
	private void addN3Required(ProcessDataGetterN3 pn, int counter) {
		//Use the process utils to figure out what class required to retrieve the N3 required
		List<String> requiredList = pn.retrieveN3Required(counter);
		//Add connection between data getter and page
		requiredList.addAll(getPageToDataGetterN3(pn, counter));
		if(requiredList != null) {
			editConfiguration.addN3Required(requiredList);
		}
	}
	private List<String> getPageToDataGetterN3(
			ProcessDataGetterN3 pn, int counter) {
		String dataGetterVar = pn.getDataGetterVar(counter);
		//Put this method in the generator but can be put elsewhere
		String pageToDataGetterN3 = ManagePageGenerator.getDataGetterN3(dataGetterVar);
		return Arrays.asList(pageToDataGetterN3);
		
	}

	//Add n3 optional
	
	private void addN3Optional(ProcessDataGetterN3 pn, int counter) {
		List<String> optionalList = pn.retrieveN3Optional(counter);
		if(optionalList != null) {
			editConfiguration.addN3Optional(optionalList);
		}
	}

	private String[] convertDelimitedStringToArray(String inputString) {
		String[] inputArray = new String[1];
		if (inputString.indexOf(",") != -1) {
			inputArray = inputString.split(",");
		} else {
			inputArray[0] = inputString;
		}
		return inputArray;

	}
	
	
	
	
	private Object getFirstElement(List inputList) {
		if(inputList == null || inputList.size() == 0)
			return null;
		return inputList.get(0);
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
