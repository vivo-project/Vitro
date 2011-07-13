/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.util.ArrayList;
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
	private Log log = LogFactory.getLog(AdditionalURIsForContextNodes.class);
    
    
    public AdditionalURIsForContextNodes( OntModel jenaOntModel){
        this.model = jenaOntModel;
    }
    
    @Override
    public List<String> findAdditionalURIsToIndex(String uri) {
    	
    	List<String> queryList = new ArrayList<String>();
    	queryList.add(multiValuedQueryForInformationResource);
    	queryList.addAll(multiValuedQueriesForAgent);
    	
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
		multiValuedQueriesForAgent.add(prefix +
				"SELECT " +
				" (str(?f) as ?positionForPerson) (str(?i) as ?positionInOrganization) " +
				" WHERE {" 
				
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
				+ " ?c rdf:type core:Position . "
				
				+ " OPTIONAL   { ?c core:positionForPerson ?f . } . "
				+ " OPTIONAL { ?c core:positionInOrganization ?i . } . "
				+ " }");
		
		multiValuedQueriesForAgent.add(prefix +
				"SELECT (str(?d) as ?advisee) " +
				" (str(?f) as ?linkedAuthor) (str(?h) as ?linkedInformationResource)  WHERE {" 
				
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
				+ " ?c rdf:type core:Relationship . "
				
				+ " OPTIONAL  { ?c core:advisee ?d . } . "
				+ " OPTIONAL   { ?c core:linkedAuthor ?f . } . "
				+ " OPTIONAL { ?c core:linkedInformationResource ?h . } . "
				
				+ " } ");
		
		multiValuedQueriesForAgent.add(prefix +
				"SELECT (str(?d) as ?awardConferredBy)  (str(?e) as ?awardOrHonorFor) " +
				"WHERE {"
				
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
				+ " ?c rdf:type core:AwardReceipt . "
				
				+ " OPTIONAL { ?c core:awardConferredBy ?d . } . "
				+ " OPTIONAL { ?c core:awardOrHonorFor ?e . } ."
				+ " }");
		
		multiValuedQueriesForAgent.add(prefix +
				"SELECT (str(?Organization) as ?organization)  WHERE {" 
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
				+ " ?c rdf:type core:Role ; core:roleIn ?Organization ."
				+ " }");
		
		multiValuedQueriesForAgent.add(prefix + 
				"SELECT  " +
		         	"(str(?e) as ?trainingAtOrganization) WHERE {"
					
					+ " ?uri rdf:type foaf:Agent ; ?b ?c . "
					+ " ?c rdf:type core:EducationalTraining . "
					  
					+ " OPTIONAL { ?c core:trainingAtOrganization ?e . } . " 
					
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

    
}
