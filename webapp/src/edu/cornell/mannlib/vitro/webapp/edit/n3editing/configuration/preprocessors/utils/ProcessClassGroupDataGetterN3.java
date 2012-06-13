/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
//Returns the appropriate n3 based on data getter
public  class ProcessClassGroupDataGetterN3 extends ProcessDataGetterAbstract {
	private static String classType = "java:edu.cornell.mannlib.vitro.webapp.utils.dataGetter.ClassGroupPageData";
	private static  String classGroupVarBase = "classGroup";
	private Log log = LogFactory.getLog(ProcessClassGroupDataGetterN3.class);

	public ProcessClassGroupDataGetterN3(){
		
	}
	//Pass in variable that represents the counter 

	//TODO: ensure correct model returned
	//We shouldn't use the ACTUAL values here but generate the n3 required
    public List<String> retrieveN3Required(int counter) {
    	return this.retrieveN3ForTypeAndClassGroup(counter);
    	
    }
    public List<String> retrieveN3Optional(int counter) {
    	return null;
    }
    
    public List<String> retrieveN3ForTypeAndClassGroup(int counter) {
    	String n3ForType = this.getN3ForTypePartial(counter);
    	String n3 = n3ForType +"; \n" + 
    		"<" + DisplayVocabulary.FOR_CLASSGROUP + "> " + getN3VarName(classGroupVarBase, counter) + " .";
    	List<String> n3List = new ArrayList<String>();
    	n3List.add(getPrefixes() + n3);
    	return n3List;
    }
    
    public String getN3ForTypePartial(int counter) {
    	String dataGetterVar = getDataGetterVar(counter);
    	String n3 = dataGetterVar + " a <" + getClassType() + ">";
    	return n3;
    }
    
    //These methods will return the literals and uris expected within the n3
    //and the counter is used to ensure they are numbered correctly 
    
    public List<String> retrieveLiteralsOnForm(int counter) {
    	//no literals, just the class group URI
    	List<String> literalsOnForm = new ArrayList<String>();
    	return literalsOnForm;
    	
    }
    
     
    public List<String> retrieveUrisOnForm(int counter) {
    	List<String> urisOnForm = new ArrayList<String>();
    	//Class group is a URI
    	urisOnForm.add(getVarName("classGroup", counter));
    	return urisOnForm;
    	
    }
    
   public List<FieldVTwo> retrieveFields(int counter) {
	   List<FieldVTwo> fields = new ArrayList<FieldVTwo>();
	   fields.add(new FieldVTwo().setName(getVarName("classGroup", counter)));
	   return fields;
   }
   
   //These var names  match the names of the elements within the json object returned with the info required for the data getter
   
   public List<String> getLiteralVarNamesBase() {
	   return Arrays.asList();   
   }

   //these are for the fields ON the form
   public List<String> getUriVarNamesBase() {
	   return Arrays.asList("classGroup");   
   }

   //This class can be extended so returning type here
   public String getClassType() {
	   return classType;
   }
   
   //for existing values
   //TODO: Update
   public void populateExistingValues(String dataGetterURI, int counter, OntModel queryModel) {
	   //First, put dataGetterURI within scope as well
	   existingUriValues.put(this.getDataGetterVar(counter), new ArrayList<String>(Arrays.asList(dataGetterURI)));
	   //Sparql queries for values to be executed
	   //And then placed in the correct place/literal or uri
	   String querystr = getExistingValuesSparqlQuery(dataGetterURI);
	   QueryExecution qe = null;
       try{
           Query query = QueryFactory.create(querystr);
           qe = QueryExecutionFactory.create(query, queryModel);
           ResultSet results = qe.execSelect();
           while( results.hasNext()){
        	   QuerySolution qs = results.nextSolution();
        	   Literal saveToVarLiteral = qs.getLiteral("saveToVar");
        	   Literal htmlValueLiteral = qs.getLiteral("htmlValue");
        	   //Put both literals in existing literals
        	   existingLiteralValues.put(this.getVarName("saveToVar", counter),
        			   new ArrayList<Literal>(Arrays.asList(saveToVarLiteral, htmlValueLiteral)));
           }
       } catch(Exception ex) {
    	   log.error("Exception occurred in retrieving existing values with query " + querystr, ex);
       }
	   
	   
   }
  
   
   //?dataGetter a FixedHTMLDataGetter ; display:saveToVar ?saveToVar; display:htmlValue ?htmlValue .
   protected String getExistingValuesSparqlQuery(String dataGetterURI) {
	   String query = this.getSparqlPrefix() + "SELECT ?saveToVar ?htmlValue WHERE {" + 
			   "<" + dataGetterURI + "> display:saveToVar ?saveToVar . \n" + 
			   "<" + dataGetterURI + "> display:htmlValue ?htmlValue . \n" + 
			   "}";
	   return query;
   }
   
   public JSONObject getExistingValuesJSON(String dataGetterURI, OntModel queryModel) {
	   JSONObject jo = new JSONObject();
	   return jo;
   }
}


