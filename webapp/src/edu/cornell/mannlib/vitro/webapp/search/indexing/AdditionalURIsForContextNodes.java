/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.Lock;

public class AdditionalURIsForContextNodes implements AdditionalURIsToIndex {

    private OntModel model;
	private static final List<String> multiValuedQueriesForAgent = new ArrayList<String>();	
	private static final String multiValuedQueryForInformationResource;
	private static final List<String>queryList;
	
	private Log log = LogFactory.getLog(AdditionalURIsForContextNodes.class);
    
    
    public AdditionalURIsForContextNodes( OntModel jenaOntModel){
        this.model = jenaOntModel;
    }
    
    @Override
    public List<String> findAdditionalURIsToIndex(String uri) {
    	
    	
    	List<String> uriList = new ArrayList<String>();

    	for(String query : queryList){
    		
    		//log.info("Executing query: "+ query);
    		
	        QuerySolutionMap initialBinding = new QuerySolutionMap();
	        Resource uriResource = ResourceFactory.createResource(uri);        
	        initialBinding.add("uri", uriResource);
	        
	        Query sparqlQuery = QueryFactory.create( query, Syntax.syntaxARQ);
	        model.getLock().enterCriticalSection(Lock.READ);
	        try{
	            QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, model, initialBinding);
	            try{                
	                ResultSet results = qExec.execSelect();                
	                while(results.hasNext()){                    
	                    QuerySolution soln = results.nextSolution();                                   
	                    Iterator<String> iter =  soln.varNames() ;
	                    while( iter.hasNext()){
	                        String name = iter.next();
	                        RDFNode node = soln.get( name );
	                        if( node != null ){
	                        	uriList.add("" + node.toString());
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
	            model.getLock().leaveCriticalSection();
	        }
    	}
        
    	if( log.isDebugEnabled() )
    	    log.debug( "additional uris for " + uri + " are " + uriList);
    	
        return uriList;
    }
    
    
    private static final String prefix = "prefix owl: <http://www.w3.org/2002/07/owl#> "
        + " prefix vitroDisplay: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#>  "
        + " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
        + " prefix core: <http://vivoweb.org/ontology/core#>  "
        + " prefix foaf: <http://xmlns.com/foaf/0.1/> "
        + " prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> "
        + " prefix localNav: <http://vitro.mannlib.cornell.edu/ns/localnav#>  "
        + " prefix bibo: <http://purl.org/ontology/bibo/>  ";
    
	static{
	    
	    // If a person changes then update
	    // organizations for positions
		multiValuedQueriesForAgent.add(prefix +
				"SELECT " +
				" (str(?i) as ?positionInOrganization) " +
				" WHERE {" 
				
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
				+ " ?c rdf:type core:Position . "
							
				+ " OPTIONAL { ?c core:positionInOrganization ?i . } . "
				+ " }");
		
        // If a person changes then update
		// advisee, linkedAuthor and informationResource
		multiValuedQueriesForAgent.add(prefix +
				"SELECT (str(?d) as ?advisee) " +
				" (str(?f) as ?linkedAuthor) (str(?h) as ?linkedInformationResource)  WHERE {" 
				
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
				+ " ?c rdf:type core:Relationship . "
				
				+ " OPTIONAL  { ?c core:advisee ?d . } . "
				+ " OPTIONAL   { ?c core:linkedAuthor ?f . } . "
				+ " OPTIONAL { ?c core:linkedInformationResource ?h . } . "
				
				+ " } ");
		
	    // If a person changes then update
		// award giver
		multiValuedQueriesForAgent.add(prefix +
				"SELECT (str(?d) as ?awardConferredBy)  " +
				"WHERE {"
				
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
				+ " ?c rdf:type core:AwardReceipt . "
				
				+ " OPTIONAL { ?c core:awardConferredBy ?d . } . "
				+ " }");
		
        // If a person changes then update
		// organization for role
		multiValuedQueriesForAgent.add(prefix +
				"SELECT (str(?Organization) as ?organization)  " +
				"WHERE {"
				
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
				+ " ?c rdf:type core:Role ; core:roleIn ?Organization ."
				+ " }");
		
        // If a person changes then update
		// organization in educational training
		multiValuedQueriesForAgent.add(prefix + 
				"SELECT  " +
		         	"(str(?e) as ?trainingAtOrganization) WHERE {"
					
					+ " ?uri rdf:type foaf:Agent ; ?b ?c . "
					+ " ?c rdf:type core:EducationalTraining . "
					  
					+ " OPTIONAL { ?c core:trainingAtOrganization ?e . } . " 					
					+"}");
		
		// If an organizatoin changes then update
        // people in head of relations
        multiValuedQueriesForAgent.add(
                " # for organization, get leader  \n" +
                prefix + 
                "SELECT  " +
                    "(str(?e) as ?LeaderPerson ) WHERE {"
                    
                    + " ?uri rdf:type foaf:Agent ; ?b ?c . "
                    + " ?c rdf:type core:LeaderRole . "
                      
                    + " OPTIONAL { ?c core:leaderRoleOf ?e . } . "                    
                    +"}");
        
        
        
	}
	
	//multivalued query for core:InformationResource
	static {
		
		multiValuedQueryForInformationResource = prefix + 
				"SELECT  (str(?b) as ?linkedAuthor) (str(?d) as ?linkedInformationResource) "
		         + "(str(?e) as ?editor) " +
		         		"(str(?i) as ?features) WHERE {"
					
					+ " ?uri rdf:type core:InformationResource . "
					  
					+  "OPTIONAL { ?uri core:informationResourceInAuthorship ?a . ?a core:linkedAuthor ?b ; core:linkedInformationResource ?d ." +
							"} . "
					+  "OPTIONAL { ?uri bibo:editor ?e . } ."			  
					+ " OPTIONAL { ?uri core:features ?i . } . " 
					
					+"}" ;
	
	}


	protected static List<String> queriesForAuthorship(){
	    List<String> queries = new ArrayList<String>();
	    
	    //get additional URIs of information resources from author side
	    queries.add(
	     prefix  
         + "SELECT  (str(?a) as ?infoResource) WHERE {\n"
            
         + " ?uri rdf:type foaf:Person . \n"
         + " ?uri core:authorInAuthorship ?aship .\n"                       
         +  "OPTIONAL { ?aship core:linkedInformationResource ?a } .\n"            
         +"}" );
	    
	    //get additional URIs of authors from information resource side
        queries.add(
         prefix  
         + "SELECT  (str(?a) as ?author ) WHERE {\n"
            
         + " ?uri rdf:type core:InformationResource . \n"
         + " ?uri core:informationResourceInAuthorship ?aship .\n"                       
         +  "OPTIONAL { ?aship core:linkedAuthor ?a } .\n"            
         +"}" );
	    return queries;
	}
	
	protected static List<String> queriesForURLLink(){
	    List<String> queries = new ArrayList<String>();
        
        //get additional URIs when URLLink is changed
        queries.add(
         prefix  
         + "SELECT  (str(?x) as ?individual) WHERE {\n"
         
         + " ?uri rdf:type  core:URLLink . \n"
         + " ?uri core:webpageOf ?x .\n"            
         +"}" );
        
        return queries;	    
	}
	
	protected static List<String> queriesForEducationalTraining(){
        List<String> queries = new ArrayList<String>();
        
        //if person changes, no additional URIs need to be
        //changed because the person is not displayed on the
        //degree individual or on the degree granting organization
        
        //if the degree changes, the person needs to be updated
        //since the degree name is shown on the person page.
        queries.add(
            prefix  
            + " SELECT  (str(?person) as ?personUri) WHERE {\n"
            
            + " ?uri rdf:type core:AcademicDegree . \n"                        
            + " ?uri core:degreeOutcomeOf ?edTrainingNode .\n" 
            + " ?edTrainingNode core:educationalTrainingOf ?person . \n"
            +"}" );
        
        //if the organization changes the person needs to be updated
        //since the organization name is shown on the person page.
        queries.add(
            prefix  
            + " SELECT  (str(?person) as ?personUri) WHERE {\n"
            
            + " ?uri rdf:type foaf:Organization . \n"                        
            + " ?uri core:organizationGrantingDegree ?edTrainingNode .\n" 
            + " ?edTrainingNode core:educationalTrainingOf ?person . \n"
            +"}" );
        return queries;     
    }
	
	protected static List<String> queriesForPosition(){
        List<String> queries = new ArrayList<String>();  
                
        //If an organization changes, update people
        queries.add(
            prefix  
            + " SELECT  (str(?person) as ?personUri) WHERE {\n"
            
            + " ?uri rdf:type foaf:Organization . \n"                        
            + " ?uri core:organizationForPosition ?positionNode .\n" 
            + " ?positionNode core:positionForPerson ?person . \n"
            +"}" );
        
        
        //if people change, update organizations 
        queries.add(
            prefix  
            + " SELECT  (str(?org) as ?orgUri) WHERE {\n"
            
            + " ?uri rdf:type foaf:Person . \n"       
            + " ?uri core:personInPosition ?positionNode .\n" 
            + " ?positionNode core:positionForOrganization ?org . \n"
            +"}" );
        return queries;     
    }
	
	static{
	    List<String> tmpList = new ArrayList<String>();
	    tmpList.add(multiValuedQueryForInformationResource);
	    tmpList.addAll(multiValuedQueriesForAgent);
	    tmpList.addAll( queriesForAuthorship());
	    tmpList.addAll(queriesForURLLink());
	    tmpList.addAll(queriesForEducationalTraining());
	    tmpList.addAll(queriesForPosition());
	    
        queryList = Collections.unmodifiableList(tmpList);
	}
}
