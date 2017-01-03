/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class FoafNameToRdfsLabelPreprocessor implements ModelChangePreprocessor {

    private static final String FOAF = "http://xmlns.com/foaf/0.1/";
	private Log log = LogFactory.getLog(FoafNameToRdfsLabelPreprocessor.class);

	@Override
	public void preprocess(Model retractionsModel, Model additionsModel,
			HttpServletRequest request) {
		updateModelWithLabel(additionsModel);
	}
	
	private String getSparqlQuery() {
		String queryStr = "SELECT ?subject ?firstName ?middleName ?lastName where {" + 
				"?subject <http://purl.obolibrary.org/obo/ARG_2000028>  ?individualVcard ." +
				"?individualVcard <http://www.w3.org/2006/vcard/ns#hasName> ?fullName ." +
				"?fullName <http://www.w3.org/2006/vcard/ns#givenName> ?firstName ." +
				"?fullName <http://www.w3.org/2006/vcard/ns#familyName> ?lastName ." +
				"OPTIONAL {?subject <http://vivoweb.org/ontology/core#middleName> ?middleName .}" +  
				"}";
		return queryStr;
	}
	
	private void updateModelWithLabel(Model additionsModel) {
		Model changesModel = ModelFactory.createDefaultModel();
		String queryStr = getSparqlQuery();
		Property rdfsLabelP = additionsModel.getProperty(VitroVocabulary.LABEL);

		Query query = null;
        QueryExecution qe = null;

        additionsModel.getLock().enterCriticalSection(Lock.READ);
        try {
            query = QueryFactory.create(queryStr);
            qe = QueryExecutionFactory.create(
                    query, additionsModel);
            ResultSet res = qe.execSelect();
            while( res.hasNext() ){
				String newLabel = ""; 
				Resource subject = null;
            	QuerySolution qs = res.nextSolution();
            	subject = qs.getResource("subject");
            	//Get first and last names, and middle names if they exist
            	if(qs.getLiteral("firstName") != null && qs.getLiteral("lastName") != null) {
            		Literal firstNameLiteral = qs.getLiteral("firstName");
    				Literal lastNameLiteral = qs.getLiteral("lastName");
    				String firstNameLanguage = firstNameLiteral.getLanguage();
    				String lastNameLanguage = lastNameLiteral.getLanguage();
    				newLabel = lastNameLiteral.getString() + ", " + firstNameLiteral.getString();
    				
    				if(qs.getLiteral("middleName") != null) {
    					Literal middleNameLiteral = qs.getLiteral("middleName");
    					newLabel += " " + middleNameLiteral.getString();
                	}
    				
    				if(subject != null && 
    						firstNameLanguage != null && lastNameLanguage != null 
    						&& firstNameLanguage.equals(lastNameLanguage)) {
    					//create a literal with the appropriate value and the language
    					Literal labelWithLanguage = changesModel.createLiteral(newLabel, firstNameLanguage);
    					changesModel.add(subject, rdfsLabelP, labelWithLanguage);
    				} else {
    					changesModel.add(subject, rdfsLabelP, newLabel );
    				}
            	}
            	
            	
            }

        } catch(Throwable th){
           log.error("An error occurred in executing query:" + queryStr);
        } finally {
            if (qe != null) {
                qe.close();
            }
            additionsModel.getLock().leaveCriticalSection();
        }
        
        //Write changes model to additions model
        additionsModel.getLock().enterCriticalSection(Lock.WRITE);
        try {
        	additionsModel.add(changesModel);
        }catch(Throwable th){
            log.error("An error occurred in writing model", th);
         } finally {
             
             additionsModel.getLock().leaveCriticalSection();
         }


	}

}
