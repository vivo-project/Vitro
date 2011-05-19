package edu.cornell.mannlib.vitro.webapp.search.beans;

import javax.servlet.ServletContext;

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
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;

public class ContextNodesInclusionFactory {

	private OntModel fullModel;
	private String contextNodeURI;
	//private String query = "";
	
//	private static final String queryForEducationalTraining = "SELECT ?query WHERE {" +
//		"?searchConfig <"+ DisplayVocabulary.QUERY_FOR_EDUCATIONAL_TRAINING + "> ?query . }";				
				
	private static Log log = LogFactory.getLog(ContextNodesInclusionFactory.class);
	
	public ContextNodesInclusionFactory(String contextNodeURI,
			OntModel displayOntModel, ServletContext context) {
		this.fullModel = ModelContext.getJenaOntModel(context);
		this.contextNodeURI = contextNodeURI;
		//query = getQueryFromModel(contextNodeURI, displayOntModel);
	}
	
	/*
	 * bk392 : The original idea behind writing up this method was to check
	 * if I can read the queries from search.n3, write them to the displayOntModel(during startup) and 
	 * read them in this class.
	 * 
	 * Eventually, its going to be like that. All these hardcoded queries
	 * will go into search.n3 and will be written into the display model.
	 * ContextNodesInclusionFactors gets the queries out from the display Model
	 * and fires them, gets the values, concatenates them and passes them back to
	 * IndividualToSolrDoc.
	 */
//	private String getQueryFromModel(String uri, OntModel displayOntModel) {
//		
//		String resultQuery = "";
//		QuerySolutionMap initialBinding = new QuerySolutionMap();
//		Resource searchConfig = ResourceFactory.createResource(uri);
//		
//		initialBinding.add("searchConfig", searchConfig);
//		
//		Query query = QueryFactory.create(queryForEducationalTraining);
//		displayOntModel.enterCriticalSection(Lock.READ);
//		try{
//			QueryExecution qExec = QueryExecutionFactory.create(query, displayOntModel, initialBinding);
//			try{
//				ResultSet results = qExec.execSelect();
//				while(results.hasNext()){
//					QuerySolution soln = results.nextSolution();
//					Literal node = soln.getLiteral("query");
//					if(node.isLiteral()){
//						resultQuery = node.toString();
//					}else{
//						log.warn("unexpected literal in the object position for context node queries " + node.toString());
//					}
//				} 
//			}catch(Throwable t){
//				log.error(t,t);
//			} finally{
//				qExec.close();
//			} 
//		}finally{
//				displayOntModel.leaveCriticalSection();
//		}
//		
//		return resultQuery.substring(0, resultQuery.length() - 3);
//	}
	
	
//	public List<Field> getFieldValues(String uri, Model modelToQuery, List<String> queries){
		
		//what do the queries need to be like?
		// SELECT ?field ?value WHERE ....
		
		// what to do with multiple values for a field?
		
//	}
	
	
	
	//in different object:
 /*
  * get queries from somewhere
  * get model to run queries on
  * get list of individuals
  * for each individual:
  *    fields = getFieldValues(uri, model, queiries)
  *    index(fields)?
  *    
  *    
  */
	
	public String getPropertiesAssociatedWithPosition(String uri){
		
		StringBuffer propertyValues = new StringBuffer();
		
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String prefix = "prefix owl: <http://www.w3.org/2002/07/owl#> "
			+ " prefix vitroDisplay: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#>  "
			+ " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
			+ " prefix core: <http://vivoweb.org/ontology/core#>  "
			+ " prefix foaf: <http://xmlns.com/foaf/0.1/> "
			+ " prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> "
			+ " prefix localNav: <http://vitro.mannlib.cornell.edu/ns/localnav#>  "
			+ " prefix bibo: <http://purl.org/ontology/bibo/>  ";		
		
		String thisQuery = prefix +
		"SELECT " +
		"(str(?HRJobTitle) as ?hrJobTitle)  (str(?InvolvedOrganizationName) as ?involvedOrganizationName) " +
		" (str(?PositionForPerson) as ?positionForPerson) (str(?PositionInOrganization) as ?positionInOrganization) " +
		" (str(?TitleOrRole) as ?titleOrRole) (str(?PositionLabel) as ?positionLabel) WHERE {" 
		
		+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
		+ " ?c rdf:type core:Position . "
		
		+ " OPTIONAL  { ?c core:hrJobTitle ?HRJobTitle . } . "
		+ " OPTIONAL { ?c core:involvedOrganizationName ?InvolvedOrganizationName . } ."
		+ " OPTIONAL   { ?c core:positionForPerson ?f . ?f rdfs:label ?PositionForPerson . } . "
		+ " OPTIONAL { ?c core:positionInOrganization ?i . ?i rdfs:label ?PositionInOrganization . } . "
		+ " OPTIONAL { ?c core:titleOrRole ?TitleOrRole . } . "
		+ " OPTIONAL { ?c rdfs:label ?PositionLabel . } "
		
		+ " } ORDER BY ?PositionLabel ";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
									
					RDFNode hrJobTitle = soln.get("hrJobTitle");
					if(hrJobTitle != null){
						propertyValues.append(" " + hrJobTitle.toString());
					}else{
						log.warn("hrJobTitle is null ");
					}
					
					RDFNode involvedOrganizationName = soln.get("involvedOrganizationName");
					if(involvedOrganizationName != null){
						propertyValues.append(" " + involvedOrganizationName.toString());
					}else{
						log.warn("involvedOrganizationName is null ");
					}
					
					RDFNode positionForPerson = soln.get("positionForPerson");
					if(positionForPerson != null){
						propertyValues.append(" " + positionForPerson.toString());
					}else{
						log.warn("positionForPerson is null ");
					}
					
					RDFNode positionInOrganization = soln.get("positionInOrganization");
					if(positionInOrganization != null){
						propertyValues.append(" " + positionInOrganization.toString());
					}else{
						log.warn("positionInOrganization is null ");
					}
					
					RDFNode titleOrRole = soln.get("titleOrRole");
					if(titleOrRole != null){
						propertyValues.append(" " + titleOrRole.toString());
					}else{
						log.warn("titleOrRole is null ");
					}
					
					RDFNode positionLabel = soln.get("positionLabel");
					if(positionLabel != null){
						propertyValues.append(" " + positionLabel.toString());
					}else{
						log.warn("positionLabel is null ");
					}
										
				} 
			}catch(Throwable t){
				log.error(t,t);
			} finally{
				qExec.close();
			} 
		}finally{
				fullModel.leaveCriticalSection();
		}
		
		return propertyValues.toString();
	}
	
	public String getPropertiesAssociatedWithRelationship(String uri){
		
		StringBuffer propertyValues = new StringBuffer();
		
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String prefix = "prefix owl: <http://www.w3.org/2002/07/owl#> "
			+ " prefix vitroDisplay: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#>  "
			+ " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
			+ " prefix core: <http://vivoweb.org/ontology/core#>  "
			+ " prefix foaf: <http://xmlns.com/foaf/0.1/> "
			+ " prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> "
			+ " prefix localNav: <http://vitro.mannlib.cornell.edu/ns/localnav#>  "
			+ " prefix bibo: <http://purl.org/ontology/bibo/>  ";		
		
		String thisQuery = prefix +
		"SELECT (str(?Advisee) as ?advisee)  (str(?DegreeCandidacy) as ?degreeCandidacy) " +
		" (str(?LinkedAuthor) as ?linkedAuthor) (str(?LinkedInformationResource) as ?linkedInformationResource)  WHERE {" 
		
		+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
		+ " ?c rdf:type core:Relationship . "
		
		+ " OPTIONAL  { ?c core:advisee ?d . ?d rdfs:label ?Advisee . } . "
		+ " OPTIONAL { ?c core:degreeCandidacy ?e . ?e rdfs:label ?DegreeCandidacy . } ."
		+ " OPTIONAL   { ?c core:linkedAuthor ?f . ?f rdfs:label ?LinkedAuthor . } . "
		+ " OPTIONAL { ?c core:linkedInformationResource ?h . ?h rdfs:label ?LinkedInformationResource . } . "
		
		+ " } ";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
									
					RDFNode advisee = soln.get("advisee");
					if(advisee != null){
						propertyValues.append(" " + advisee.toString());
					}else{
						log.warn("advisee is null ");
					}
					
					RDFNode degreeCandidacy = soln.get("degreeCandidacy");
					if(degreeCandidacy != null){
						propertyValues.append(" " + degreeCandidacy.toString());
					}else{
						log.warn("degreeCandidacy is null ");
					}
					
					RDFNode linkedAuthor = soln.get("linkedAuthor");
					if(linkedAuthor != null){
						propertyValues.append(" " + linkedAuthor.toString());
					}else{
						log.warn("linkedAuthor is null ");
					}
					
					RDFNode linkedInformationResource = soln.get("linkedInformationResource");
					if(linkedInformationResource != null){
						propertyValues.append(" " + linkedInformationResource.toString());
					}else{
						log.warn("linkedInformationResource is null ");
					}					
										
				} 
			}catch(Throwable t){
				log.error(t,t);
			} finally{
				qExec.close();
			} 
		}finally{
				fullModel.leaveCriticalSection();
		}
		
		return propertyValues.toString();
	}	

	
	public String getPropertiesAssociatedWithAwardReceipt(String uri){
		
		StringBuffer propertyValues = new StringBuffer();
		
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String prefix = "prefix owl: <http://www.w3.org/2002/07/owl#> "
			+ " prefix vitroDisplay: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#>  "
			+ " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
			+ " prefix core: <http://vivoweb.org/ontology/core#>  "
			+ " prefix foaf: <http://xmlns.com/foaf/0.1/> "
			+ " prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> "
			+ " prefix localNav: <http://vitro.mannlib.cornell.edu/ns/localnav#>  "
			+ " prefix bibo: <http://purl.org/ontology/bibo/>  ";		
		
		String thisQuery = prefix +
		"SELECT (str(?AwardConferredBy) as ?awardConferredBy)  (str(?AwardOrHonorFor) as ?awardOrHonorFor) " +
		" (str(?Description) as ?description) (str(?AwardReceiptLabel) as ?awardReceiptLabel)  WHERE {" 
		
		+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
		+ " ?c rdf:type core:AwardReceipt . "
		
		+ " OPTIONAL { ?c core:awardConferredBy ?d . ?d rdfs:label ?AwardConferredBy } . "
		+ " OPTIONAL { ?c core:awardOrHonorFor ?e . ?e rdfs:label ?AwardOrHonorFor } ."
		+ " OPTIONAL   { ?c core:description ?Description . } . "
		+ " OPTIONAL { ?c rdfs:label ?AwardReceiptLabel . } . "
		
		+ " } ORDER BY ?AwardReceiptLabel";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
									
					RDFNode awardConferredBy = soln.get("awardConferredBy");
					if(awardConferredBy != null){
						propertyValues.append(" " + awardConferredBy.toString());
					}else{
						log.warn("awardConferredBy is null ");
					}
					
					RDFNode awardOrHonorFor = soln.get("awardOrHonorFor");
					if(awardOrHonorFor != null){
						propertyValues.append(" " + awardOrHonorFor.toString());
					}else{
						log.warn("awardOrHonorFor is null ");
					}
					
					RDFNode description = soln.get("description");
					if(description != null){
						propertyValues.append(" " + description.toString());
					}else{
						log.warn("description is null ");
					}
					
					RDFNode awardReceiptLabel = soln.get("awardReceiptLabel");
					if(awardReceiptLabel != null){
						propertyValues.append(" " + awardReceiptLabel.toString());
					}else{
						log.warn("awardReceiptLabel is null ");
					}					
										
				} 
			}catch(Throwable t){
				log.error(t,t);
			} finally{
				qExec.close();
			} 
		}finally{
				fullModel.leaveCriticalSection();
		}
		
		return propertyValues.toString();
	}	
	
	public String getPropertiesAssociatedWithRole(String uri){
		
		StringBuffer propertyValues = new StringBuffer();
		
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String prefix = "prefix owl: <http://www.w3.org/2002/07/owl#> "
			+ " prefix vitroDisplay: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#>  "
			+ " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
			+ " prefix core: <http://vivoweb.org/ontology/core#>  "
			+ " prefix foaf: <http://xmlns.com/foaf/0.1/> "
			+ " prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> "
			+ " prefix localNav: <http://vitro.mannlib.cornell.edu/ns/localnav#>  "
			+ " prefix bibo: <http://purl.org/ontology/bibo/>  ";		
		
		String thisQuery = prefix +
		"SELECT DISTINCT (str(?OrganizationLabel) as ?organizationLabel)  WHERE {" 
		+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
		+ " ?c rdf:type core:Role ; core:roleIn ?Organization ."
		+ " ?Organization rdfs:label ?OrganizationLabel . "
		+ " } ORDER BY ?OrganizationLabel ";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
									
					RDFNode organizationLabel = soln.get("organizationLabel");
					if(organizationLabel != null){
						propertyValues.append(" " + organizationLabel.toString());
					}else{
						log.warn("organizationLabel is null ");
					}
										
				} 
			}catch(Throwable t){
				log.error(t,t);
			} finally{
				qExec.close();
			} 
		}finally{
				fullModel.leaveCriticalSection();
		}
		
		return propertyValues.toString();
	}
	
	
	
	public String getPropertiesAssociatedWithEducationalTraining(String uri){
		
		StringBuffer propertyValues = new StringBuffer();
		
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String prefix = "prefix owl: <http://www.w3.org/2002/07/owl#> "
					+ " prefix vitroDisplay: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#>  "
					+ " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
					+ " prefix foaf: <http://xmlns.com/foaf/0.1/> "
					+ " prefix core: <http://vivoweb.org/ontology/core#>  "
					+ " prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> "
					+ " prefix localNav: <http://vitro.mannlib.cornell.edu/ns/localnav#>  "
					+ " prefix bibo: <http://purl.org/ontology/bibo/>  ";
		
		String thisQuery = prefix + 
		"SELECT  (str(?AcademicDegreeLabel) as ?academicDegreeLabel) (str(?AcademicDegreeAbbreviation) as ?academicDegreeAbbreviation) "
         + "(str(?MajorField) as ?majorField) (str(?DepartmentOrSchool) as ?departmentOrSchool) " +
         	"(str(?TrainingAtOrganizationLabel) as ?trainingAtOrganizationLabel) WHERE {"
			
			+ " ?uri rdf:type foaf:Agent ; ?b ?c . "
			+ " ?c rdf:type core:EducationalTraining . "
			  
			+  "OPTIONAL { ?c core:degreeEarned ?d . ?d rdfs:label ?AcademicDegreeLabel ; core:abbreviation ?AcademicDegreeAbbreviation . } . "
			+  "OPTIONAL { ?c core:majorField ?MajorField .} ."			  
			+ " OPTIONAL { ?c core:departmentOrSchool ?DepartmentOrSchool . }"			  
			+ " OPTIONAL { ?c core:trainingAtOrganization ?e . ?e rdfs:label ?TrainingAtOrganizationLabel . } . " 
			
			+"}";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
					
					RDFNode academicDegreeLabel = soln.get("academicDegreeLabel");
					if(academicDegreeLabel != null){
						propertyValues.append(" " + academicDegreeLabel.toString());
					}else{
						log.warn("academicDegreeLabel is null ");
					}
					
					RDFNode academicDegreeAbbreviation = soln.get("academicDegreeAbbreviation");
					if(academicDegreeAbbreviation != null){
						propertyValues.append(" " + academicDegreeAbbreviation.toString());
					}else{
						log.warn("academicDegreeAbbreviation is null ");
					}
					
					RDFNode majorField = soln.get("majorField");
					if(majorField != null){
						propertyValues.append("  " + majorField.toString());
					}else{
						log.warn("majorField is null ");
					}
					
					RDFNode trainingAtDepartmentOrSchool = soln.get("departmentOrSchool");
					if(trainingAtDepartmentOrSchool != null){
						propertyValues.append(" " + trainingAtDepartmentOrSchool.toString());
					}else{
						log.warn("trainingAtDepartmentOrSchool is null ");
					}
					
					RDFNode trainingAtOrganizationLabel = soln.get("trainingAtOrganizationLabel");
					if(trainingAtOrganizationLabel != null){
						propertyValues.append(" " + trainingAtOrganizationLabel.toString());
					}else{
						log.warn("trainingAtOrganizationLabel is null ");
					}
										
				} 
			}catch(Throwable t){
				log.error(t,t);
			} finally{
				qExec.close();
			} 
		}finally{
				fullModel.leaveCriticalSection();
		}		
		return propertyValues.toString();
		
	}
	
	public String getPropertiesAssociatedWithInformationResource(String uri){
		
		StringBuffer propertyValues = new StringBuffer();
		
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String prefix = "prefix owl: <http://www.w3.org/2002/07/owl#> "
					+ " prefix vitroDisplay: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#>  "
					+ " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
					+ " prefix foaf: <http://xmlns.com/foaf/0.1/> "
					+ " prefix core: <http://vivoweb.org/ontology/core#>  "
					+ " prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> "
					+ " prefix localNav: <http://vitro.mannlib.cornell.edu/ns/localnav#>  "
					+ " prefix bibo: <http://purl.org/ontology/bibo/>  ";
		
		String thisQuery = prefix + 
		"SELECT  (str(?LinkedAuthor) as ?linkedAuthor) (str(?LinkedInformationResource) as ?linkedInformationResource) "
         + "(str(?Editor) as ?editor) (str(?SubjectArea) as ?subjectArea) (str(?ResearchAreaOf) as ?researchAreaOf) " +
         		"(str(?Features) as ?features) WHERE {"
			
			+ " ?uri rdf:type core:InformationResource . "
			  
			+  "OPTIONAL { ?uri core:informationResourceInAuthorship ?a . ?a core:linkedAuthor ?b ; core:linkedInformationResource ?d ." +
					"?b rdfs:label ?LinkedAuthor . ?d rdfs:label ?LinkedInformationResource } . "
			+  "OPTIONAL { ?uri bibo:editor ?e . ?e rdfs:label ?Editor  . } ."			  
			+ " OPTIONAL { ?uri core:hasSubjectArea ?f . ?f rdfs:label ?SubjectArea ; core:researchAreaOf ?h . ?h rdfs:label ?ResearchAreaOf . } "			  
			+ " OPTIONAL { ?uri core:features ?i . ?i rdfs:label ?Features . } . " 
			
			+"}";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
					
					RDFNode linkedAuthor = soln.get("linkedAuthor");
					if(linkedAuthor != null){
						propertyValues.append(" " + linkedAuthor.toString());
					}else{
						log.warn("linkedAuthor is null ");
					}
					
					RDFNode linkedInformationResource = soln.get("linkedInformationResource");
					if(linkedInformationResource != null){
						propertyValues.append(" " + linkedInformationResource.toString());
					}else{
						log.warn("linkedInformationResource is null ");
					}
					
					RDFNode editor = soln.get("editor");
					if(editor != null){
						propertyValues.append("  " + editor.toString());
					}else{
						log.warn("editor is null ");
					}
					
					RDFNode subjectArea = soln.get("subjectArea");
					if(subjectArea != null){
						propertyValues.append(" " + subjectArea.toString());
					}else{
						log.warn("subjectArea is null ");
					}
					
					RDFNode researchAreaOf = soln.get("researchAreaOf");
					if(researchAreaOf != null){
						propertyValues.append(" " + researchAreaOf.toString());
					}else{
						log.warn("researchAreaOf is null ");
					}
					
					RDFNode features = soln.get("features");
					if(features != null){
						propertyValues.append(" " + features.toString());
					}else{
						log.warn("features is null ");
					}
										
				} 
			}catch(Throwable t){
				log.error(t,t);
			} finally{
				qExec.close();
			} 
		}finally{
				fullModel.leaveCriticalSection();
		}		
		return propertyValues.toString();
		
	}
	 
	 

}
