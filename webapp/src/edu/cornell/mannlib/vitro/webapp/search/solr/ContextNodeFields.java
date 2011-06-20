/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.search.VitroTermNames;

public class ContextNodeFields implements DocumentModifier{
	
	private static final String prefix = "prefix owl: <http://www.w3.org/2002/07/owl#> "
		+ " prefix vitroDisplay: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#>  "
		+ " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
		+ " prefix core: <http://vivoweb.org/ontology/core#>  "
		+ " prefix foaf: <http://xmlns.com/foaf/0.1/> "
		+ " prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> "
		+ " prefix localNav: <http://vitro.mannlib.cornell.edu/ns/localnav#>  "
		+ " prefix bibo: <http://purl.org/ontology/bibo/>  ";
	
	private static final List<String> singleValuedQueriesForAgent = new ArrayList<String>();
	private static final List<String> singleValuedQueriesForInformationResource = new ArrayList<String>();
	private static final List<String> multiValuedQueriesForAgent = new ArrayList<String>();
	private static final String multiValuedQueryForInformationResource;
	
   private Log log = LogFactory.getLog(ContextNodeFields.class);
   private Dataset dataset;
        
    
    public ContextNodeFields(Dataset dataset){
        this.dataset = dataset;
    }
    
	/* TODO: consider a constructor like this:
	 * public ContextNodeFields(OntModel fullModel, List<String> queries )
	 */
	
	/*
	 *TODO:
	 * consider reducing the code in this class using a method like the following:
	 */	 
	public String runQuery( Individual individual, String query ){
	    StringBuffer propertyValues = new StringBuffer();
            
        QuerySolutionMap initialBinding = new QuerySolutionMap();
        Resource uriResource = ResourceFactory.createResource(individual.getURI());        
        initialBinding.add("uri", uriResource);
        
        Query sparqlQuery = QueryFactory.create( query, Syntax.syntaxARQ);
		dataset.getLock().enterCriticalSection(Lock.READ);
        try{
            QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, dataset, initialBinding);
            try{                
                ResultSet results = qExec.execSelect();                
                while(results.hasNext()){                    
                    QuerySolution soln = results.nextSolution();                                   
                    Iterator<String> iter =  soln.varNames() ;
                    while( iter.hasNext()){
                        String name = iter.next();
                        RDFNode node = soln.get( name );
                        if( node != null ){
                            propertyValues.append(" " + node.toString());
                        }else{
                            log.debug(name + " is null");
                        }                        
                    }
                }
            }catch(Throwable t){
                log.error(t,t);
            } finally{
                qExec.close();
            } 
        }finally{
            dataset.getLock().leaveCriticalSection();
        }
        
        return propertyValues.toString();
	}	 		


	
	
    @Override
    public void modifyDocument(Individual individual, SolrInputDocument doc) {
    	
        log.debug("retrieving context node values..");

    	SolrInputField field = doc.getField(VitroTermNames.ALLTEXT);
    	SolrInputField targetField = doc.getField(VitroTermNames.targetInfo);
    	StringBuffer objectProperties = new StringBuffer();
  
    	if(IndividualToSolrDocument.superClassNames.contains("Agent")){
    		objectProperties.append(" ");
    		for(String query : multiValuedQueriesForAgent){
    			objectProperties.append(runQuery(individual, query));
    		}
    	}

    	if(IndividualToSolrDocument.superClassNames.contains("InformationResource")){
    		targetField.addValue(" " + runQuery(individual, multiValuedQueryForInformationResource), targetField.getBoost());
    	}
    	
    	field.addValue(objectProperties, field.getBoost());
        log.debug("context node values are retrieved");
    
    }
    
	//single valued queries for foaf:Agent
	static {
		singleValuedQueriesForAgent.add(prefix + 		"SELECT " +
		"(str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" +
		"?uri rdf:type foaf:Agent  ; ?b ?c . " +
		" ?c rdf:type core:Position . " +
		" ?c core:hrJobTitle ?ContextNodeProperty . }");
		
		singleValuedQueriesForAgent.add(prefix + 		"SELECT " +
				"(str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" +
				"?uri rdf:type foaf:Agent  ; ?b ?c . " +
				" ?c rdf:type core:Position . " +
				" ?c core:involvedOrganizationName ?ContextNodeProperty . }");
		
		singleValuedQueriesForAgent.add(prefix + 		"SELECT " +
				"(str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" +
				"?uri rdf:type foaf:Agent  ; ?b ?c . " +
				" ?c rdf:type core:Position . " +
				" ?c core:positionForPerson ?f . ?f rdfs:label ?ContextNodeProperty . }");
		
		singleValuedQueriesForAgent.add(prefix + 		"SELECT " +
				"(str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" +
				"?uri rdf:type foaf:Agent  ; ?b ?c . " +
				" ?c rdf:type core:Position . " +
				" ?c core:positionInOrganization ?i . ?i rdfs:label ?ContextNodeProperty . }");
		
		singleValuedQueriesForAgent.add(prefix + 		"SELECT " +
				"(str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" +
				"?uri rdf:type foaf:Agent  ; ?b ?c . " +
				" ?c rdf:type core:Position . " +
				" ?c core:titleOrRole ?ContextNodeProperty .  }");
		
		singleValuedQueriesForAgent.add(prefix + 		"SELECT " +
				"(str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" +
				"?uri rdf:type foaf:Agent  ; ?b ?c . " +
				" ?c rdf:type core:Relationship . " +
				" ?c core:advisee ?d . ?d rdfs:label ?ContextNodeProperty . }");
		
		singleValuedQueriesForAgent.add(prefix + 		"SELECT " +
				"(str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" +
				"?uri rdf:type foaf:Agent  ; ?b ?c . " +
				" ?c rdf:type core:Relationship . " +
				" ?c core:degreeCandidacy ?e . ?e rdfs:label ?ContextNodeProperty . }");
				
		singleValuedQueriesForAgent.add(prefix + 		"SELECT " +
				"(str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" +
				"?uri rdf:type foaf:Agent  ; ?b ?c . " +
				" ?c rdf:type core:Relationship . " +
				" ?c core:linkedAuthor ?f . ?f rdfs:label ?ContextNodeProperty . }");
		
		singleValuedQueriesForAgent.add(prefix + 		"SELECT " +
				"(str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" +
				"?uri rdf:type foaf:Agent  ; ?b ?c . " +
				" ?c rdf:type core:Relationship . " +
				" ?c core:linkedInformationResource ?h . ?h rdfs:label ?ContextNodeProperty . }");
		
		singleValuedQueriesForAgent.add(prefix + 		"SELECT " +
				"(str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" +
				"?uri rdf:type foaf:Agent  ; ?b ?c . " +
				" ?c rdf:type core:AwardReceipt . " +
				" ?c core:awardConferredBy ?d . ?d rdfs:label ?ContextNodeProperty . }");
		
		singleValuedQueriesForAgent.add(prefix + 		"SELECT " +
				"(str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" +
				"?uri rdf:type foaf:Agent  ; ?b ?c . " +
				" ?c rdf:type core:AwardReceipt . " +
				" ?c core:awardOrHonorFor ?e . ?e rdfs:label ?ContextNodeProperty . }");
		
		singleValuedQueriesForAgent.add(prefix + 		"SELECT " +
				"(str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" +
				"?uri rdf:type foaf:Agent  ; ?b ?c . " +
				" ?c rdf:type core:AwardReceipt . " +
				" ?c core:description ?ContextNodeProperty . }");
		
		singleValuedQueriesForAgent.add(prefix + 		"SELECT DISTINCT (str(?ContextNodeProperty) as ?contextNodeProperty)  WHERE {" 
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
				+ " ?c rdf:type core:Role ; core:roleIn ?Organization ."
				+ " ?Organization rdfs:label ?ContextNodeProperty . "
				+ " } ORDER BY ?ContextNodeProperty ");
		
		singleValuedQueriesForAgent.add(prefix + 		"SELECT (str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" 
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
				+ " ?c rdf:type core:EducationalTraining . "
				+ " ?c core:degreeEarned ?d . ?d rdfs:label ?ContextNodeProperty ."
				+ " }");
		
		singleValuedQueriesForAgent.add(prefix + 		"SELECT (str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" 
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
				+ " ?c rdf:type core:EducationalTraining . "
				+ " ?c core:degreeEarned ?d . ?d core:abbreviation ?ContextNodeProperty ."
				+ " }");
		
		singleValuedQueriesForAgent.add(prefix + 		"SELECT (str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" 
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
				+ " ?c rdf:type core:EducationalTraining . "
				+ " ?c core:majorField ?ContextNodeProperty ."
				+ " }");
		singleValuedQueriesForAgent.add(prefix + 		"SELECT (str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" 
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
				+ " ?c rdf:type core:EducationalTraining . "
				+ " ?c core:departmentOrSchool ?ContextNodeProperty ."
				+ " }");
		
		singleValuedQueriesForAgent.add(prefix + 		"SELECT (str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" 
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
				+ " ?c rdf:type core:EducationalTraining . "
				+ " ?c core:trainingAtOrganization ?e . ?e rdfs:label ?ContextNodeProperty . "
				+ " }");
		
		
		
	}
	
	//single valued queries for core:InformationResource
	static {
		singleValuedQueriesForInformationResource.add(prefix +
				"SELECT (str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" 
				+ " ?uri rdf:type core:InformationResource . "
				+ "?uri core:informationResourceInAuthorship ?a . ?a core:linkedAuthor ?b ." 
				+ "?b rdfs:label ?ContextNodeProperty .}");
		
		singleValuedQueriesForInformationResource.add(prefix +
				"SELECT (str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" 
				+ " ?uri rdf:type core:InformationResource . "
				+ " ?uri core:linkedInformationResource ?d ." 
				+ " ?d rdfs:label ?ContextNodeProperty . }");
		
		singleValuedQueriesForInformationResource.add(prefix +
				"SELECT (str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" 
				+ "?uri rdf:type core:InformationResource . "
				+ "?uri core:features ?i . ?i rdfs:label ?ContextNodeProperty ." 
				+ "}");
		
		singleValuedQueriesForInformationResource.add(prefix +
				"SELECT (str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" 
				+ "?uri rdf:type core:InformationResource . "
				+ "?uri bibo:editor ?e . ?e rdfs:label ?ContextNodeProperty  ." 
				+ "}");
		
		singleValuedQueriesForInformationResource.add(prefix +
				"SELECT (str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" 
				+ "?uri rdf:type core:InformationResource . "
				+ "?uri core:hasSubjectArea ?f . ?f rdfs:label ?ContextNodeProperty ." 
				+ "}");
		
		singleValuedQueriesForInformationResource.add(prefix +
				"SELECT (str(?ContextNodeProperty) as ?contextNodeProperty) WHERE {" 
				+ "?uri rdf:type core:InformationResource . "
				+ "?uri core:hasSubjectArea ?f . ?f core:researchAreaOf ?h . ?h rdfs:label ?ContextNodeProperty ." 
				+ "}");
	}
	
	//multi valued queries
	
	static{
		multiValuedQueriesForAgent.add(prefix +
				"SELECT " +
				"(str(?HRJobTitle) as ?hrJobTitle)  (str(?InvolvedOrganizationName) as ?involvedOrganizationName) " +
				" (str(?PositionForPerson) as ?positionForPerson) (str(?PositionInOrganization) as ?positionInOrganization) " +
				" (str(?TitleOrRole) as ?titleOrRole) WHERE {" 
				
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
				+ " ?c rdf:type core:Position . "
				
				+ " OPTIONAL  { ?c core:hrJobTitle ?HRJobTitle . } . "
				+ " OPTIONAL { ?c core:involvedOrganizationName ?InvolvedOrganizationName . } ."
				+ " OPTIONAL   { ?c core:positionForPerson ?f . ?f rdfs:label ?PositionForPerson . } . "
				+ " OPTIONAL { ?c core:positionInOrganization ?i . ?i rdfs:label ?PositionInOrganization . } . "
				+ " OPTIONAL { ?c core:titleOrRole ?TitleOrRole . } . "
				+ " }");
		
		multiValuedQueriesForAgent.add(prefix +
				"SELECT (str(?Advisee) as ?advisee)  (str(?DegreeCandidacy) as ?degreeCandidacy) " +
				" (str(?LinkedAuthor) as ?linkedAuthor) (str(?LinkedInformationResource) as ?linkedInformationResource)  WHERE {" 
				
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
				+ " ?c rdf:type core:Relationship . "
				
				+ " OPTIONAL  { ?c core:advisee ?d . ?d rdfs:label ?Advisee . } . "
				+ " OPTIONAL { ?c core:degreeCandidacy ?e . ?e rdfs:label ?DegreeCandidacy . } ."
				+ " OPTIONAL   { ?c core:linkedAuthor ?f . ?f rdfs:label ?LinkedAuthor . } . "
				+ " OPTIONAL { ?c core:linkedInformationResource ?h . ?h rdfs:label ?LinkedInformationResource . } . "
				
				+ " } ");
		
		multiValuedQueriesForAgent.add(prefix +
				"SELECT (str(?AwardConferredBy) as ?awardConferredBy)  (str(?AwardOrHonorFor) as ?awardOrHonorFor) " +
				" (str(?Description) as ?description)   WHERE {"
				
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
				+ " ?c rdf:type core:AwardReceipt . "
				
				+ " OPTIONAL { ?c core:awardConferredBy ?d . ?d rdfs:label ?AwardConferredBy } . "
				+ " OPTIONAL { ?c core:awardOrHonorFor ?e . ?e rdfs:label ?AwardOrHonorFor } ."
				+ " OPTIONAL { ?c core:description ?Description . } . "
				+ " }");
		
		multiValuedQueriesForAgent.add(prefix +
				"SELECT (str(?OrganizationLabel) as ?organizationLabel)  WHERE {" 
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
				+ " ?c rdf:type core:Role ; core:roleIn ?Organization ."
				+ " ?Organization rdfs:label ?OrganizationLabel . "
				+ " }");
		
		multiValuedQueriesForAgent.add(prefix + 
				"SELECT  (str(?AcademicDegreeLabel) as ?academicDegreeLabel) (str(?AcademicDegreeAbbreviation) as ?academicDegreeAbbreviation) "
		         + "(str(?MajorField) as ?majorField) (str(?DepartmentOrSchool) as ?departmentOrSchool) " +
		         	"(str(?TrainingAtOrganizationLabel) as ?trainingAtOrganizationLabel) WHERE {"
					
					+ " ?uri rdf:type foaf:Agent ; ?b ?c . "
					+ " ?c rdf:type core:EducationalTraining . "
					  
					+  "OPTIONAL { ?c core:degreeEarned ?d . ?d rdfs:label ?AcademicDegreeLabel ; core:abbreviation ?AcademicDegreeAbbreviation . } . "
					+  "OPTIONAL { ?c core:majorField ?MajorField .} ."			  
					+ " OPTIONAL { ?c core:departmentOrSchool ?DepartmentOrSchool . }"			  
					+ " OPTIONAL { ?c core:trainingAtOrganization ?e . ?e rdfs:label ?TrainingAtOrganizationLabel . } . " 
					
					+"}");
		
	}
	
	//multivalued query for core:InformationResource
	static {
		
		multiValuedQueryForInformationResource = prefix + 
				"SELECT  (str(?LinkedAuthor) as ?linkedAuthor) (str(?LinkedInformationResource) as ?linkedInformationResource) "
		         + "(str(?Editor) as ?editor) (str(?SubjectArea) as ?subjectArea) (str(?ResearchAreaOf) as ?researchAreaOf) " +
		         		"(str(?Features) as ?features) WHERE {"
					
					+ " ?uri rdf:type core:InformationResource . "
					  
					+  "OPTIONAL { ?uri core:informationResourceInAuthorship ?a . ?a core:linkedAuthor ?b ; core:linkedInformationResource ?d ." +
							"?b rdfs:label ?LinkedAuthor . ?d rdfs:label ?LinkedInformationResource } . "
					+  "OPTIONAL { ?uri bibo:editor ?e . ?e rdfs:label ?Editor  . } ."			  
					+ " OPTIONAL { ?uri core:hasSubjectArea ?f . ?f rdfs:label ?SubjectArea ; core:researchAreaOf ?h . ?h rdfs:label ?ResearchAreaOf . } "			  
					+ " OPTIONAL { ?uri core:features ?i . ?i rdfs:label ?Features . } . " 
					
					+"}" ;
	
	}

    
}
