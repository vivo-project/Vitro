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
import com.hp.hpl.jena.rdf.model.Resource;
import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
//Returns the appropriate n3 based on data getter
public  class ProcessSparqlDataGetterN3 extends ProcessDataGetterAbstract {
	private static String classType = "java:edu.cornell.mannlib.vitro.webapp.utils.dataGetter.SparqlQueryDataGetter";
	private Log log = LogFactory.getLog(ProcessSparqlDataGetterN3.class);

	public ProcessSparqlDataGetterN3(){
		
	}
	//Pass in variable that represents the counter 

	//We shouldn't use the ACTUAL values here but generate the n3 required
    public List<String> retrieveN3Required(int counter) {
    	String dataGetterVar = getDataGetterVar(counter);
    	String n3 = dataGetterVar + " a <" + classType + ">; \n" + 
    	"display:queryModel " + getN3VarName("queryModel", counter) + "; \n" + 
    	"display:saveToVar " + getN3VarName("saveToVar", counter) + "; \n" + 
    	"display:query " + getN3VarName("query", counter) + " .";
    	List<String> requiredList = new ArrayList<String>();
    	requiredList.add(getPrefixes() + n3);
    	return requiredList;
    	
    }
    public List<String> retrieveN3Optional(int counter) {
    	return null;
    }
  
    
    public List<String> retrieveLiteralsOnForm(int counter) {
    	List<String> literalsOnForm = new ArrayList<String>();
    	literalsOnForm.add(getVarName("saveToVar",counter));
    	literalsOnForm.add(getVarName("query", counter));
    	return literalsOnForm;
    	
    }
    
     
    public List<String> retrieveUrisOnForm(int counter) {
    	List<String> urisOnForm = new ArrayList<String>();
    	//We have no uris as far as I know.. well query Model is a uri
    	urisOnForm.add(getVarName("queryModel", counter));
    	return urisOnForm;
    	
    }
    
   public List<FieldVTwo> retrieveFields(int counter) {
	   List<FieldVTwo> fields = new ArrayList<FieldVTwo>();
	  
	   //For existing data getters
	   //fields.add(new FieldVTwo().setName(getVarName("dataGetter", counter)));
	   fields.add(new FieldVTwo().setName(getVarName("queryModel", counter)));
	   fields.add(new FieldVTwo().setName(getVarName("saveToVar", counter)));
	   fields.add(new FieldVTwo().setName(getVarName("query", counter)));

	   return fields;
   }
   
   public List<String> getLiteralVarNamesBase() {
	   return Arrays.asList("saveToVar", "query");   
   }

   //these are for the fields ON the form
   public List<String> getUriVarNamesBase() {
	   return Arrays.asList("queryModel");   
   }
   
   //Existing values
   //TODO: Correct
   
   public void populateExistingValues(String dataGetterURI, int counter, OntModel queryModel) {
	   //First, put dataGetterURI within scope as well
	   this.populateExistingDataGetterURI(dataGetterURI, counter);
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
        	   Literal queryLiteral = qs.getLiteral("query");
        	   Resource queryModelResource = qs.getResource("queryModel");
        	   existingLiteralValues.put(this.getVarName("saveToVar", counter),
        			   new ArrayList<Literal>(Arrays.asList(saveToVarLiteral)));
       
        	   existingLiteralValues.put(this.getVarName("query", counter),
        			   new ArrayList<Literal>(Arrays.asList(queryLiteral)));
        	   
        	   existingUriValues.put(this.getVarName("queryModel", counter), 
        			   new ArrayList<String>(Arrays.asList(queryModelResource.getURI())));

        	   
           }
       } catch(Exception ex) {
    	   log.error("Exception occurred in retrieving existing values with query " + querystr, ex);
       }
	   
	   
   }
  
	
   
   //?dataGetter a SparqlDataGetter ; display:saveToVar ?saveToVar; display:queryModel ?queryModel;
	//display:query ?query ..
   protected String getExistingValuesSparqlQuery(String dataGetterURI) {
	   String query = this.getSparqlPrefix() + "SELECT ?saveToVar ?query ?queryModel WHERE {" + 
			   "<" + dataGetterURI + "> display:query ?query . \n" + 
			   "OPTIONAL {<" + dataGetterURI + "> display:saveToVar ?saveToVar .} \n" + 
			   "OPTIONAL {<" + dataGetterURI + "> display:queryModel ?queryModel . }\n" + 
			   "}";
	   return query;
   }


   
   public JSONObject getExistingValuesJSON(String dataGetterURI, OntModel queryModel, ServletContext context) {
	   JSONObject jObject = new JSONObject();
	   jObject.element("dataGetterClass", classType);
	   String querystr = getExistingValuesSparqlQuery(dataGetterURI);
	   QueryExecution qe = null;
       try{
           Query query = QueryFactory.create(querystr);
           qe = QueryExecutionFactory.create(query, queryModel);
           ResultSet results = qe.execSelect();
           while( results.hasNext()){
        	   QuerySolution qs = results.nextSolution();
        	   Literal saveToVarLiteral = qs.getLiteral("saveToVar");
        	   Literal queryLiteral = qs.getLiteral("query");
        	   Resource queryModelResource = qs.getResource("queryModel");
        	   jObject.element("saveToVar", saveToVarLiteral.getString());
        	   jObject.element("query", queryLiteral.getString());
        	   if(queryModelResource != null) {
        	   jObject.element("queryModel", queryModelResource.getURI());
        	   } else {
            	   jObject.element("queryModel", "");

        	   }
           }
       } catch(Exception ex) {
    	   log.error("Exception occurred in retrieving existing values with query " + querystr, ex);
       }
	   
	   return jObject;
   }

}


