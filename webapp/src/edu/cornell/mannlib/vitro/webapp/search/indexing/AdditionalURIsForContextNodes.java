/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.search.beans.StatementToURIsToUpdate;

public class AdditionalURIsForContextNodes implements StatementToURIsToUpdate {

    private OntModel model;
	private Set<String> alreadyChecked;
	private long accumulatedTime = 0;
	
    private static final List<String> multiValuedQueriesForAgent = new ArrayList<String>();	
	private static final String multiValuedQueryForInformationResource;
	private static final List<String> multiValuedQueriesForRole = new ArrayList<String>();
	private static final List<String>queryList;	
	
	private Log log = LogFactory.getLog(AdditionalURIsForContextNodes.class);
    
    
    public AdditionalURIsForContextNodes( OntModel jenaOntModel){
        this.model = jenaOntModel; 
    }
    
    @Override
    public List<String> findAdditionalURIsToIndex(Statement stmt) {
                
        if( stmt != null ){
            long start = System.currentTimeMillis();
            
            List<String>urisToIndex = new ArrayList<String>();
            if(stmt.getSubject() != null && stmt.getSubject().isURIResource() ){        
                String subjUri = stmt.getSubject().getURI();
                if( subjUri != null && ! alreadyChecked.contains( subjUri )){
                    urisToIndex.addAll( findAdditionalURIsToIndex(subjUri));
                    alreadyChecked.add(subjUri);    
                }
            }
            
            if( stmt.getObject() != null && stmt.getObject().isURIResource() ){
                String objUri = stmt.getSubject().getURI();
                if( objUri != null && ! alreadyChecked.contains(objUri)){
                    urisToIndex.addAll( findAdditionalURIsToIndex(objUri));
                    alreadyChecked.add(objUri);
                }
            }
            
            accumulatedTime += (System.currentTimeMillis() - start ) ;
            return urisToIndex;
        }else{
            return Collections.emptyList();
        }                
    }
    
    @Override
    public void startIndexing() { 
        alreadyChecked = new HashSet<String>();
        accumulatedTime = 0L;
    }

    @Override
    public void endIndxing() {
        log.debug( "Accumulated time for this run of the index: " + accumulatedTime + " msec");
        alreadyChecked = null;        
    }
    
    protected List<String> findAdditionalURIsToIndex(String uri) {    	        
    	
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
    
    
    private static final String prefix = "prefix owl: <http://www.w3.org/2002/07/owl#> \n"
        + " prefix vitroDisplay: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#>  \n"
        + " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"
        + " prefix core: <http://vivoweb.org/ontology/core#>  \n"
        + " prefix foaf: <http://xmlns.com/foaf/0.1/> \n"
        + " prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> \n"
        + " prefix localNav: <http://vitro.mannlib.cornell.edu/ns/localnav#>  \n"
        + " prefix bibo: <http://purl.org/ontology/bibo/>  \n";
    
	static{
	    
	    // If a person changes then update
	    // organizations for positions
		multiValuedQueriesForAgent.add(prefix +
				"SELECT \n" +
				" (str(?i) as ?positionInOrganization) \n" +
				" WHERE {\n" 
				
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . \n"
				+ " ?c rdf:type core:Position . \n"
							
				+ " OPTIONAL { ?c core:positionInOrganization ?i . } . \n"
				+ " }");
		
        // If a person changes then update
		// advisee, linkedAuthor and informationResource
		multiValuedQueriesForAgent.add(prefix +
				"SELECT (str(?d) as ?advisee) \n" +
				" (str(?f) as ?linkedAuthor) (str(?h) as ?linkedInformationResource)  WHERE {\n" 
				
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . \n"
				+ " ?c rdf:type core:Relationship . \n"
				
				+ " OPTIONAL  { ?c core:advisee ?d . } . \n"
				+ " OPTIONAL   { ?c core:linkedAuthor ?f . } . \n"
				+ " OPTIONAL { ?c core:linkedInformationResource ?h . } . \n"
				
				+ " } ");
		
	    // If a person changes then update
		// award giver
		multiValuedQueriesForAgent.add(prefix +
				"SELECT (str(?d) as ?awardConferredBy)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . \n"
				+ " ?c rdf:type core:AwardReceipt . \n"
				
				+ " OPTIONAL { ?c core:awardConferredBy ?d . } . \n"
				+ " }");
		
        // If a person changes then update
		// organization for role
		multiValuedQueriesForAgent.add(prefix +
				"SELECT (str(?Organization) as ?organization)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Agent  ; ?b ?c . \n"
				+ " ?c rdf:type core:Role ; core:roleIn ?Organization .\n"
				+ " }");
		
        // If a person changes then update
		// organization in educational training
		multiValuedQueriesForAgent.add(prefix + 
				"SELECT  \n" +
		         	"(str(?e) as ?trainingAtOrganization) WHERE {\n"
					
					+ " ?uri rdf:type foaf:Agent ; ?b ?c . \n"
					+ " ?c rdf:type core:EducationalTraining . \n"
					  
					+ " OPTIONAL { ?c core:trainingAtOrganization ?e . } . \n" 					
					+"}");
		
		// If an organizatoin changes then update
        // people in head of relations
        multiValuedQueriesForAgent.add(
                " # for organization, get leader  \n" +
                prefix + 
                "SELECT  \n" +
                    "(str(?e) as ?LeaderPerson ) WHERE {\n"
                    
                    + " ?uri rdf:type foaf:Agent ; ?b ?c . \n"
                    + " ?c rdf:type core:LeaderRole . \n"
                      
                    + " OPTIONAL { ?c core:leaderRoleOf ?e . } . \n"                    
                    +"}");
        
        
        
	}
	
	//multivalued query for core:InformationResource
	static {
		
		multiValuedQueryForInformationResource = prefix + 
				"SELECT  (str(?b) as ?linkedAuthor) (str(?d) as ?linkedInformationResource) \n"
		         + "(str(?e) as ?editor) \n" +
		         		"(str(?i) as ?features) WHERE {\n"
					
					+ " ?uri rdf:type core:InformationResource . \n"
					  
					+  "OPTIONAL { ?uri core:informationResourceInAuthorship ?a . ?a core:linkedAuthor ?b ; core:linkedInformationResource ?d .\n" +
							"} . "
					+  "OPTIONAL { ?uri bibo:editor ?e . } .\n"			  
					+ " OPTIONAL { ?uri core:features ?i . } . \n" 
					
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
            + " ?person core:personInPosition ?positionNode . \n"
            +"}" );
        
        
        //if people change, update organizations 
        queries.add(
            prefix  
            + " SELECT  (str(?org) as ?orgUri) WHERE {\n"
            
            + " ?uri rdf:type foaf:Person . \n"       
            + " ?uri core:personInPosition ?positionNode .\n" 
            + " ?org core:organizationForPosition ?positionNode . \n"
            +"}" );
        return queries;     
    }
	
	static{
		//	core:AttendeeRole
		// If the person changes, update the attendee role in organization
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?organization) \n " +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Person  ; ?b ?c . \n"
				+ " ?c rdf:type core:AttendeeRole ; core:roleIn ?d .\n"
				+ " }");
		
		// If the organization changes, update the attendee role of person 
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?person)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Organization  ; ?b ?c . \n"
				+ " ?c rdf:type core:AttendeeRole ; core:roleOf ?d .\n"
				+ " }");	
			
		//	core:ClinicalRole  -- core:clinicalRoleOf
		
		// If the person changes, update the clinical role in project
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?project)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Person  ; ?b ?c . \n"
				+ " ?c rdf:type core:ClinicalRole ; core:roleIn ?d .\n"
				+ " }");
		
		// If the project changes, update the clinical role of person 
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?person)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type core:Project  ; ?b ?c . \n"
				+ " ?c rdf:type core:ClinicalRole ; core:clinicalRoleOf ?d .\n "
				+ " }");
		
		// If the service changes, update the clinical role of person 
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?person)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type core:Service  ; ?b ?c . \n"
				+ " ?c rdf:type core:ClinicalRole ; core:clinicalRoleOf ?d .\n "
				+ " }");
		
		
		//	core:LeaderRole -- core:leaderRoleOf
		
		// If the person changes, update the leader role in organization
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?organization)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Person  ; ?b ?c . \n"
				+ " ?c rdf:type core:LeaderRole ; core:roleIn ?d .\n"
				+ " }");
		
		// If the organization changes, update the leader role of person 
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?person)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Organization  ; ?b ?c . \n"
				+ " ?c rdf:type core:LeaderRole ; core:leaderRoleOf ?d .\n"
				+ " }");
		
		//	core:MemberRole -- core:memberRoleOf
		
		// If the person changes, update the member role in organization
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?organization)  \n" +
				"WHERE \n{"
				
				+ "?uri rdf:type foaf:Person  ; ?b ?c . \n"
				+ " ?c rdf:type core:MemberRole ; core:roleIn ?d .\n"
				+ " }");
		
		// If the organization changes, update the member role of person 
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?person)  \n" +
				"WHERE {"
				
				+ "?uri rdf:type foaf:Organization  ; ?b ?c . \n"
				+ " ?c rdf:type core:MemberRole ; core:memberRoleOf ?d .\n"
				+ " }");
		//	core:OrganizerRole -- core:organizerRoleOf
		
		// If the person changes, update the organizer role in organization
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?organization)  \n" +
				"WHERE {"
				
				+ "?uri rdf:type foaf:Person  ; ?b ?c . \n"
				+ " ?c rdf:type core:OrganizerRole ; core:roleIn ?d .\n"
				+ " }");
		
		// If the organization changes, update the organizer role of person 
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?person)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Organization  ; ?b ?c . \n"
				+ " ?c rdf:type core:OrganizerRole ; core:organizerRoleOf ?d .\n"
				+ " }");
		//	core:OutreachProviderRole -- core:outreachProviderRoleOf
		
		// If the person changes, update the outreach provider role in organization
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?organization)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Person  ; ?b ?c . \n"
				+ " ?c rdf:type core:OutreachProviderRole ; core:roleIn ?d .\n"
				+ " }");
		
		// If the organization changes, update the outreach provider role of person 
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?person)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Organization  ; ?b ?c . \n"
				+ " ?c rdf:type core:OutreachProviderRole ; core:outreachProviderRoleOf ?d .\n"
				+ " }");
		
		
		//	core:PresenterRole -- core:presenterRoleOf
		
		// If the person changes, update the presentation
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?organization)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Person  ; ?b ?c . \n"
				+ " ?c rdf:type core:PresenterRole ; core:roleIn ?d .\n"
				+ " }");
		
		// If the presentation changes, update the person 
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?person)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type core:Presentation  ; ?b ?c . \n"
				+ " ?c rdf:type core:PresenterRole ; core:presenterRoleOf ?d .\n"
				+ " }");
		
		//	core:ResearcherRole -- core:researcherRoleOf
		
		// If the person changes, update the grant
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?organization)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Person  ; ?b ?c . \n"
				+ " ?c rdf:type core:ResearcherRole ; core:roleIn ?d .\n"
				+ " }");
		
		// If the grant changes, update the researcher 
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?person)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type core:Grant  ; ?b ?c . \n"
				+ " ?c rdf:type core:ResearcherRole ; core:researcherRoleOf ?d .\n"
				+ " }");
		
		// If the grant changes, update the principal investigator 
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?person)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type core:Grant  ; ?b ?c . \n"
				+ " ?c rdf:type core:PrincipalInvestigatorRole ; core:principalInvestigatorRoleOf ?d .\n"
				+ " }");

		// If the grant changes, update the co-principal investigator 
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?person)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type core:Grant  ; ?b ?c . \n"
				+ " ?c rdf:type core:CoPrincipalInvestigatorRole ; core:co-PrincipalInvestigatorRoleOf ?d .\n"
				+ " }");
		
		
		// If the grant changes, update the investigator 
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?person)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type core:Grant  ; ?b ?c . \n"
				+ " ?c rdf:type core:InvestigatorRole ; core:investigatorRoleOf ?d .\n"
				+ " }");
		
		// If the project changes, update the researcher 
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?person)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type core:Project  ; ?b ?c . \n"
				+ " ?c rdf:type core:ResearcherRole ; core:researcherRoleOf ?d .\n"
				+ " }");
		
		
		
		//	core:EditorRole -- core:editorRoleOf, core:forInformationResource (person, informationresource)
		
		// If the person changes, update the editor role in organization
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?organization)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Person  ; ?b ?c . \n"
				+ " ?c rdf:type core:EditorRole ; core:roleIn ?d .\n"
				+ " }");
		
		
		// If the organization changes, update the editor role of person 
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?person)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Organization  ; ?b ?c . \n"
				+ " ?c rdf:type core:EditorRole ; core:editorRoleOf ?d .\n"
				+ " }");
		
		// If the person changes, update the information resource associated with editor role
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?informationResource)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Person  ; ?b ?c . \n"
				+ " ?c rdf:type core:EditorRole ; core:forInformationResource ?d .\n"
				+ " }");
		
		// If the organization changes, update the information resource associated with editor role
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?informationResource)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Organization  ; ?b ?c . \n"
				+ " ?c rdf:type core:EditorRole ; core:forInformationResource ?d .\n"
				+ " }");
		
		//	core:ServiceProviderRole -- core:serviceProviderRoleOf
		
		// If the person changes, update the service provider role in organization
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?organization)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Person  ; ?b ?c . \n"
				+ " ?c rdf:type core:ServiceProviderRole ; core:roleIn ?d .\n"
				+ " }");
		
		// If the organization changes, update the service provider role of person 
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?person)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Organization  ; ?b ?c . \n"
				+ " ?c rdf:type core:ServiceProviderRole ; core:serviceProviderRoleOf ?d .\n"
				+ " }");
		
		
		//	core:TeacherRole -- core:teacherRoleOf
		
		// If the person changes, update the teacher role in organization
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?organization)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Person  ; ?b ?c . \n"
				+ " ?c rdf:type core:TeacherRole ; core:roleIn ?d .\n"
				+ " }");
		
		// If the organization changes, update the teacher role of person 
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?person)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Organization  ; ?b ?c . \n"
				+ " ?c rdf:type core:TeacherRole ; core:teacherRoleOf ?d .\n"
				+ " }");
		

		//	core:ReviewerRole -- core:forInformationResource, core:reviewerRoleOf
//		core:PeerReviewerRole -- core:forInformationResource, core:reviewerRoleOf
		
		// If the person changes, update the reviewer role in organization
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?organization)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Person  ; ?b ?c . \n"
				+ " ?c rdf:type core:ReviewerRole ; core:roleIn ?d .\n"
				+ " }");
		
		
		// If the organization changes, update the reviewer role of person 
	
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?person)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Organization  ; ?b ?c . \n"
				+ " ?c rdf:type core:ReviewerRole ; core:reviewerRoleOf ?d .\n"
				+ " }");
		
		// If the person changes, update the information resource associated with reviewer role
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?informationResource) \n " +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Person  ; ?b ?c . \n"
				+ " ?c rdf:type core:ReviewerRole ; core:forInformationResource ?d .\n"
				+ " }");
		
		// If the organization changes, update the information resource associated with reviewer role
		multiValuedQueriesForRole.add(prefix +
				"SELECT (str(?d) as ?informationResource)  \n" +
				"WHERE {\n"
				
				+ "?uri rdf:type foaf:Organization  ; ?b ?c . \n"
				+ " ?c rdf:type core:ReviewerRole ; core:forInformationResource ?d .\n"
				+ " }");
		
	}
	
	static{
	    List<String> tmpList = new ArrayList<String>();
	    tmpList.add(multiValuedQueryForInformationResource);
	    tmpList.addAll(multiValuedQueriesForAgent);
	    tmpList.addAll(multiValuedQueriesForRole);
	    tmpList.addAll( queriesForAuthorship());
	    tmpList.addAll(queriesForURLLink());
	    tmpList.addAll(queriesForEducationalTraining());
	    tmpList.addAll(queriesForPosition());
	    
        queryList = Collections.unmodifiableList(tmpList);
	}

}
