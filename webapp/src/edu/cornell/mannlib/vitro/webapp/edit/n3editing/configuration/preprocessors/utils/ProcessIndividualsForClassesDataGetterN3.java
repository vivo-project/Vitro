/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
//Returns the appropriate n3 for selection of classes from within class group
public  class ProcessIndividualsForClassesDataGetterN3 extends ProcessClassGroupDataGetterN3 {
	private static String classType = "java:edu.cornell.mannlib.vitro.webapp.utils.dataGetter.IndividualsForClassesDataGetter";
	private JSONObject values = null;
	int classCount = 0;
	private static String individualClassVarNameBase = "classesSelectedInClassGroup";
	public ProcessIndividualsForClassesDataGetterN3(JSONObject jsonObject){
		this.values = jsonObject;
		if(values != null && values.containsKey(individualClassVarNameBase)) {
			//Check how many individual classes are in json object
			JSONArray ja = values.getJSONArray(individualClassVarNameBase);
			classCount = ja.size();
		}
	}
	//Pass in variable that represents the counter 

	//TODO: ensure correct model returned
	//We shouldn't use the ACTUAL values here but generate the n3 required
    public List<String> retrieveN3Required(int counter) {
    	List<String> classGroupN3 = super.retrieveN3Required(counter);
    	classGroupN3.addAll(this.addIndividualClassesN3(counter));
    	return classGroupN3;
    	
    }
    
    
    private List<String> addIndividualClassesN3(int counter) {
		List<String> classN3 = new ArrayList<String>();
		if(classCount > 0) {
			classN3.add(generateIndividualClassN3(counter));
		}
		return classN3;
	}
    
    private String generateIndividualClassN3(int counter) {
    	String dataGetterVar = getDataGetterVar(counter);
    	String n3 = dataGetterVar + " <" + DisplayVocabulary.GETINDIVIDUALS_FOR_CLASS + "> ";
    	//Consider a multi-valued field - in this case single field with multiple values
    	n3 += getN3VarName(individualClassVarNameBase, counter);
    	/*
    	int i;
    	for(i  = 0; i < classCount; i++) {
    		if(i != 0) {
    			n3+= ",";
    		}
    		n3 += getN3VarName(individualClassVarNameBase + counter, classCount);
    	}*/
    	n3 += " .";
    	return n3;
    	
    }
	public List<String> retrieveN3Optional(int counter) {
    	return null;
    }
    
    //These methods will return the literals and uris expected within the n3
    //and the counter is used to ensure they are numbered correctly 
    
    public List<String> retrieveLiteralsOnForm(int counter) {
    	//no literals, just the class group URI
    	List<String> literalsOnForm = new ArrayList<String>();
    	return literalsOnForm;
    	
    }
    
     
    public List<String> retrieveUrisOnForm(int counter) {
    	//get class group uris
    	List<String> urisOnForm = super.retrieveUrisOnForm(counter);
    	//now get individual classes selected uri
    	//urisOnForm.addAll(getIndividualClassesVarNames(counter));
    	//here again,consider multi-valued
    	urisOnForm.add(getVarName(individualClassVarNameBase, counter));
    	return urisOnForm;
    	
    }
    
   private List<String> getIndividualClassesVarNames(int counter) {
		List<String> individualClassUris = new ArrayList<String>();
		int i;
		for(i = 0; i < classCount; i++) {
			individualClassUris.add(getVarName(individualClassVarNameBase + counter, classCount));
		}
		return individualClassUris;
		
	}
   
   public List<FieldVTwo> retrieveFields(int counter) {
	   List<FieldVTwo> fields = super.retrieveFields(counter);
	   fields.add(new FieldVTwo().setName(getVarName(individualClassVarNameBase, counter)));
	   //Add fields for each class selected
	/*   List<String> classVarNames = getIndividualClassesVarNames(counter);
	   for(String v:classVarNames) {
		   fields.add(new FieldVTwo().setName(v));

	   }*/
	   return fields;
   }
   
   //These var names  match the names of the elements within the json object returned with the info required for the data getter
   
   public List<String> getLiteralVarNamesBase() {
	   return Arrays.asList();   
   }

   //these are for the fields ON the form
   public List<String> getUriVarNamesBase() {
	   return Arrays.asList("classGroup", individualClassVarNameBase);   
   }


}


