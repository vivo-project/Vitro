/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
//Returns the appropriate n3 for selection of classes from within class group
public  class ProcessIndividualsForClassesDataGetterN3 extends ProcessClassGroupDataGetterN3 {
	private static String classType = "java:edu.cornell.mannlib.vitro.webapp.utils.dataGetter.IndividualsForClassesDataGetter";
	protected static String individualClassVarNameBase = "classesSelectedInClassGroup";
	private Log log = LogFactory.getLog(ProcessIndividualsForClassesDataGetterN3.class);

	public ProcessIndividualsForClassesDataGetterN3(){
		
	}
	//Pass in variable that represents the counter 

	//TODO: ensure correct model returned
	//We shouldn't use the ACTUAL values here but generate the n3 required
    public List<String> retrieveN3Required(int counter) {
    	
    	List<String> classGroupN3 = this.retrieveN3ForTypeAndClassGroup(counter);
    	classGroupN3.addAll(this.addIndividualClassesN3(counter));
    	return classGroupN3;
    	
    }
    
    
    protected List<String> addIndividualClassesN3(int counter) {
		List<String> classN3 = new ArrayList<String>();
		classN3.add(generateIndividualClassN3(counter));
		return classN3;
	}
    
   protected String generateIndividualClassN3(int counter) {
    	String dataGetterVar = getDataGetterVar(counter);
    	String n3 = dataGetterVar + " <" + DisplayVocabulary.GETINDIVIDUALS_FOR_CLASS + "> ";
    	//Consider a multi-valued field - in this case single field with multiple values
    	n3 += getN3VarName(individualClassVarNameBase, counter);
    	
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
	   return Arrays.asList("classGroup", individualClassVarNameBase,  classTypeVarBase);   
   }
   
   @Override
   public String getClassType() {
	   return classType;
   }
   
   //Existing values
   //TODO: Correct
   
   public void populateExistingValues(String dataGetterURI, int counter, OntModel queryModel) {
	   //First, put dataGetterURI within scope as well
	   this.populateExistingDataGetterURI(dataGetterURI, counter);
	   //UPDATE: Put in type
	   this.populateExistingClassType(this.getClassType(), counter);
	   //Sparql queries for values to be executed
	   //And then placed in the correct place/literal or uri
	   String querystr = getExistingValuesIndividualsForClasses(dataGetterURI);
	   QueryExecution qe = null;
       try{
           Query query = QueryFactory.create(querystr);
           qe = QueryExecutionFactory.create(query, queryModel);
           ResultSet results = qe.execSelect();
           List<String> individualsForClasses = new ArrayList<String>();
           while( results.hasNext()){
        	   QuerySolution qs = results.nextSolution();
        	   Resource classGroupResource = qs.getResource("classGroup");
        	   String classGroupVarName = this.getVarName(classGroupVarBase, counter);
        	   if(!existingUriValues.containsKey(classGroupVarName)) {
	        	   //Put both literals in existing literals
	        	   existingUriValues.put(this.getVarName(classGroupVarBase, counter),
	        			   new ArrayList<String>(Arrays.asList(classGroupResource.getURI())));
        	   }
        	   Resource individualForClassResource = qs.getResource("individualForClass");
        	   individualsForClasses.add(individualForClassResource.getURI());
        	 //Put both literals in existing literals
        	   
           }
           
           existingUriValues.put(this.getVarName(individualClassVarNameBase, counter),
    			   new ArrayList<String>(individualsForClasses));
       } catch(Exception ex) {
    	   log.error("Exception occurred in retrieving existing values with query " + querystr, ex);
       }
	   
	   
   }
  
   
   //?dataGetter a FixedHTMLDataGetter ; display:saveToVar ?saveToVar; display:htmlValue ?htmlValue .
   protected String getExistingValuesIndividualsForClasses(String dataGetterURI) {
	   String query = this.getSparqlPrefix() + "SELECT ?classGroup  ?individualForClass WHERE {" + 
			   "<" + dataGetterURI + "> <" + DisplayVocabulary.FOR_CLASSGROUP + "> ?classGroup  . \n" +
			   "<" + dataGetterURI + "> <" + DisplayVocabulary.GETINDIVIDUALS_FOR_CLASS + "> ?individualForClass . \n" + 
			   "}";
	   return query;
   }

   
   public JSONObject getExistingValuesJSON(String dataGetterURI, OntModel queryModel, ServletContext context) {
	   JSONObject jObject = new JSONObject();
	   jObject.element("dataGetterClass", classType);
	   //Update to include class type as variable
	   jObject.element(classTypeVarBase, classType);
	   //Get selected class group and which classes were selected
	   getExistingClassGroupAndIndividuals(dataGetterURI, jObject, queryModel);
	   //Get all classes within the class group
	   super.getExistingClassesInClassGroup(context, dataGetterURI, jObject);
	   return jObject;
   }
   
   private void getExistingClassGroupAndIndividuals(String dataGetterURI, JSONObject jObject, OntModel queryModel) {
	   String querystr = getExistingValuesIndividualsForClasses(dataGetterURI);
	   QueryExecution qe = null;
       try{
           Query query = QueryFactory.create(querystr);
           qe = QueryExecutionFactory.create(query, queryModel);
           ResultSet results = qe.execSelect();
           JSONArray individualsForClasses = new JSONArray();
           String classGroupURI = null;
           while( results.hasNext()){
        	   QuerySolution qs = results.nextSolution();
        	   if(classGroupURI == null) {
	        	   Resource classGroupResource = qs.getResource("classGroup");
	        	   classGroupURI = classGroupResource.getURI();
        	   }
        	   Resource individualForClassResource = qs.getResource("individualForClass");
        	   individualsForClasses.add(individualForClassResource.getURI());
        	 //Put both literals in existing literals
        	   
           }
           
          jObject.element("classGroup", classGroupURI);
          //this is a json array
          jObject.element(individualClassVarNameBase, individualsForClasses);
       } catch(Exception ex) {
    	   log.error("Exception occurred in retrieving existing values with query " + querystr, ex);
       }
   }

}


