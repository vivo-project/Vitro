/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
//Returns the appropriate n3 based on data getter
public class ProcessSparqlDataGetterN3 implements ProcessDataGetterN3 {
	private static String classType = "java:edu.cornell.mannlib.vitro.webapp.utils.dataGetter.SparqlQueryDataGetter";
	private JSONObject jsonObject = null;
	
	public ProcessSparqlDataGetterN3(JSONObject inputJsonObject) {
		jsonObject = inputJsonObject;
	}
	//Pass in variable that represents the counter 

	//TODO: ensure correct model returned
    public List<String> retrieveN3Required(int counter) {
    	String dataGetterVar = getDataGetterVar(counter);
    	String n3 = dataGetterVar + " a <" + classType + ">; \n" + 
    	"display:queryModel <" + jsonObject.getString("queryModel") + ">; \n" + 
    	"display:saveToVar '" + jsonObject.getString("saveToVar") + "'; \n" + 
    	"display:query \"\"\"" + jsonObject.getString("query") + "\"\"\" .";
    	return Arrays.asList(getPrefixes() + n3);
    	
    }
    public List<String> retrieveN3Optional(int counter) {
    	return null;
    }
    
    private String getDataGetterVar(int counter) {
    	return "dataGetter" + counter;
    }
    
    private String getPrefixes() {
    	return "@prefix display: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#> . \n" + 
    			"@prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> . \n";
    }
  
    
    //Need to add method sfor returning the fields, literals on form, and all that
    /*
     * addLiteralsAndUrisOnForm(pn, counter);
			// Add fields
			addFields(pn, counter);
			//Add input values to submission
			addInputsToSubmission(pn, counter);
     */
    public List<String> retrieveLiteralsOnForm(int counter) {
    	List<String> literalsOnForm = new ArrayList<String>();
    	literalsOnForm.add("saveToVar" + counter);
    	literalsOnForm.add("query" + counter);
    	return literalsOnForm;
    	
    }
    
     
    public List<String> retrieveUrissOnForm(int counter) {
    	List<String> urisOnForm = new ArrayList<String>();
    	//We have no uris as far as I know.. well query Model is a uri
    	urisOnForm.add("queryModel" + counter);
    	return urisOnForm;
    	
    }
    
   public List<FieldVTwo> retrieveFields(int counter) {
	   List<FieldVTwo> fields = new ArrayList<FieldVTwo>();
	   fields.add(new FieldVTwo().setName("queryModel" + counter));
	   fields.add(new FieldVTwo().setName("saveToVar" + counter));
	   fields.add(new FieldVTwo().setName("query" + counter));

	   return fields;
   }
   
   public List<String> getLiteralVarNamesBase() {
	   return Arrays.asList("savetoVar", "query");   
   }

   public List<String> getUriVarNamesBase() {
	   return Arrays.asList("queryModel");   
   }

   

}


