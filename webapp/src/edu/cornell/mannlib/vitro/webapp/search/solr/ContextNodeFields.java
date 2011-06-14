/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.ontology.OntModel;
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
import com.hp.hpl.jena.sdb.script.QExec;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.search.VitroTermNames;
import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc.VitroLuceneTermNames;

public class ContextNodeFields implements DocumentModifier{
	
	Model fullModel;
	
	private Log log = LogFactory.getLog(ContextNodeFields.class);
	
	private static final String prefix = "prefix owl: <http://www.w3.org/2002/07/owl#> "
		+ " prefix vitroDisplay: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#>  "
		+ " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
		+ " prefix core: <http://vivoweb.org/ontology/core#>  "
		+ " prefix foaf: <http://xmlns.com/foaf/0.1/> "
		+ " prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> "
		+ " prefix localNav: <http://vitro.mannlib.cornell.edu/ns/localnav#>  "
		+ " prefix bibo: <http://purl.org/ontology/bibo/>  ";
	
	
	public ContextNodeFields(OntModel fullModel){
		this.fullModel = fullModel;
	}
	
	
    @Override
    public void modifyDocument(Individual individual, SolrInputDocument doc) {
    	
        log.debug("retrieving context node values..");

        
    	SolrInputField field = doc.getField(VitroTermNames.ALLTEXT);
    	SolrInputField targetField = doc.getField(VitroTermNames.targetInfo);
    	StringBuffer objectProperties = new StringBuffer();
  
    	if(IndividualToSolrDocument.superClassNames.contains("Agent")){
    		objectProperties.append(" ");
    		
    		
    		
//    		objectProperties.append(getPropertiesAssociatedWithEducationalTraining(individual.getURI()));
//    		objectProperties.append(" ");
    		
    		// properties related to core:EducationalTraining
    		
    		objectProperties.append(getAcademicDegreeLabelAndAbbreviation(individual.getURI()));
    		objectProperties.append(" ");

    		objectProperties.append(getMajorField(individual.getURI()));
    		objectProperties.append(" ");

    		objectProperties.append(getDepartmentOrSchool(individual.getURI()));
    		objectProperties.append(" ");

    		objectProperties.append(getTrainingAtOrganization(individual.getURI()));
    		objectProperties.append(" ");

    		
    		objectProperties.append(getPropertiesAssociatedWithRole(individual.getURI()));
    		objectProperties.append(" ");
    		
//    		objectProperties.append(getPropertiesAssociatedWithPosition(individual.getURI()));
//    		objectProperties.append(" ");
    		
    		//properties related to core:Position
    		objectProperties.append(getHRJobTitle(individual.getURI()));
    		objectProperties.append(" ");

    		objectProperties.append(getPositionForPerson(individual.getURI()));
    		objectProperties.append(" ");

    		objectProperties.append(getPositionInOrganization(individual.getURI()));
    		objectProperties.append(" ");

    		objectProperties.append(getTitleOrRole(individual.getURI()));
    		objectProperties.append(" ");

    		objectProperties.append(getInvolvedOrganizationName(individual.getURI()));
    		objectProperties.append(" ");

//    		objectProperties.append(getPropertiesAssociatedWithRelationship(individual.getURI()));
//    		objectProperties.append(" ");
    		
    		//properties related to core:Relationship
    		objectProperties.append(getLinkedAuthor(individual.getURI()));
    		objectProperties.append(" ");

    		objectProperties.append(getLinkedInformationResource(individual.getURI()));
    		objectProperties.append(" ");

    		objectProperties.append(getAdvisee(individual.getURI()));
    		objectProperties.append(" ");

    		objectProperties.append(getDegreeCandidacy(individual.getURI()));
    		objectProperties.append(" ");

//    		objectProperties.append(getPropertiesAssociatedWithAwardReceipt(individual.getURI()));
    		//properties related to core:AwardReceipt
    		objectProperties.append(getAwardConferredBy(individual.getURI()));
    		objectProperties.append(" ");
    		
    		objectProperties.append(getAwardOrHonorFor(individual.getURI()));
    		objectProperties.append(" ");
    		
    		objectProperties.append(getDescription(individual.getURI()));
    		objectProperties.append(" ");


    	}
    	if(IndividualToSolrDocument.superClassNames.contains("InformationResource")){
    		//targetField.addValue(" " + getPropertiesAssociatedWithInformationResource(individual.getURI()), targetField.getBoost());
    		//properties related to core:InformationResource
    		targetField.addValue(" " + getLinkedAuthorandLinkedInformationResource(individual.getURI()), targetField.getBoost());
    		targetField.addValue(" " + getFeatures(individual.getURI()), targetField.getBoost());
    		targetField.addValue(" " + getEditor(individual.getURI()), targetField.getBoost());
    		targetField.addValue(" " + getSubjectAreaAndResearchAreaOf(individual.getURI()), targetField.getBoost());
    	}
    	
    	field.addValue(objectProperties, field.getBoost());
    	
        log.debug("context node values are retrieved");
    
    }
    
    //Position related Queries
    public String getHRJobTitle(String uri){
    	
    	StringBuffer propertyValues = new StringBuffer();
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix + 
		"SELECT " +
		"(str(?HRJobTitle) as ?hrJobTitle) WHERE {" +
		"?uri rdf:type foaf:Agent  ; ?b ?c . " +
		" ?c rdf:type core:Position . " +
		" ?c core:hrJobTitle ?HRJobTitle . }";
		
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
						log.debug("hrJobTitle is null ");
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
    
    public String getInvolvedOrganizationName(String uri){

    	StringBuffer propertyValues = new StringBuffer();
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix + 
		"SELECT " +
		"(str(?InvolvedOrganizationName) as ?involvedOrganizationName) WHERE {" +
		"?uri rdf:type foaf:Agent  ; ?b ?c . " +
		" ?c rdf:type core:Position . " +
		" ?c core:involvedOrganizationName ?InvolvedOrganizationName . }";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
					RDFNode involvedOrganizationName = soln.get("involvedOrganizationName");
					if(involvedOrganizationName != null){
						propertyValues.append(" " + involvedOrganizationName.toString());
					}else{
						log.debug("involvedOrganizationName is null ");
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
    
    public String getPositionForPerson(String uri){
    	
    	StringBuffer propertyValues = new StringBuffer();
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix + 
		"SELECT " +
		"(str(?PositionForPerson) as ?positionForPerson) WHERE {" +
		"?uri rdf:type foaf:Agent  ; ?b ?c . " +
		" ?c rdf:type core:Position . " +
		" ?c core:positionForPerson ?f . ?f rdfs:label ?PositionForPerson . }";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
					RDFNode positionForPerson = soln.get("positionForPerson");
					if(positionForPerson != null){
						propertyValues.append(" " + positionForPerson.toString());
					}else{
						log.debug("positionForPerson is null ");
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
    
    public String getPositionInOrganization(String uri){
    	
    	StringBuffer propertyValues = new StringBuffer();
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix + 
		"SELECT " +
		"(str(?PositionInOrganization) as ?positionInOrganization) WHERE {" +
		"?uri rdf:type foaf:Agent  ; ?b ?c . " +
		" ?c rdf:type core:Position . " +
		" ?c core:positionInOrganization ?i . ?i rdfs:label ?PositionInOrganization . }";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
					RDFNode positionInOrganization = soln.get("positionInOrganization");
					if(positionInOrganization != null){
						propertyValues.append(" " + positionInOrganization.toString());
					}else{
						log.debug("positionInOrganization is null ");
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
    
    public String getTitleOrRole(String uri){
    	
    	StringBuffer propertyValues = new StringBuffer();
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix + 
		"SELECT " +
		"(str(?TitleOrRole) as ?titleOrRole) WHERE {" +
		"?uri rdf:type foaf:Agent  ; ?b ?c . " +
		" ?c rdf:type core:Position . " +
		" ?c core:titleOrRole ?TitleOrRole .  }";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
					RDFNode titleOrRole = soln.get("titleOrRole");
					if(titleOrRole != null){
						propertyValues.append(" " + titleOrRole.toString());
					}else{
						log.debug("titleOrRole is null ");
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
    
//	public String getPropertiesAssociatedWithPosition(String uri){
//		
//		StringBuffer propertyValues = new StringBuffer();
//		
//		QuerySolutionMap initialBinding = new QuerySolutionMap();
//		Resource uriResource = ResourceFactory.createResource(uri);
//		
//		initialBinding.add("uri", uriResource);
//		
//		String thisQuery = prefix +
//		"SELECT " +
//		"(str(?HRJobTitle) as ?hrJobTitle)  (str(?InvolvedOrganizationName) as ?involvedOrganizationName) " +
//		" (str(?PositionForPerson) as ?positionForPerson) (str(?PositionInOrganization) as ?positionInOrganization) " +
//		" (str(?TitleOrRole) as ?titleOrRole) WHERE {" //(str(?PositionLabel) as ?positionLabel) 
//		
//		+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
//		+ " ?c rdf:type core:Position . "
//		
//		+ " OPTIONAL  { ?c core:hrJobTitle ?HRJobTitle . } . "
//		+ " OPTIONAL { ?c core:involvedOrganizationName ?InvolvedOrganizationName . } ."
//		+ " OPTIONAL   { ?c core:positionForPerson ?f . ?f rdfs:label ?PositionForPerson . } . "
//		+ " OPTIONAL { ?c core:positionInOrganization ?i . ?i rdfs:label ?PositionInOrganization . } . "
//		+ " OPTIONAL { ?c core:titleOrRole ?TitleOrRole . } . "
//		//+ " OPTIONAL { ?c rdfs:label ?PositionLabel . } "
//		
//		+ " } ORDER BY ?PositionLabel ";
//		
//		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
//		fullModel.enterCriticalSection(Lock.READ);
//		
//		try{
//			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
//			try{
//				
//				ResultSet results = qExec.execSelect();
//				
//				while(results.hasNext()){
//					
//					QuerySolution soln = results.nextSolution();
//									
//					RDFNode hrJobTitle = soln.get("hrJobTitle");
//					if(hrJobTitle != null){
//						propertyValues.append(" " + hrJobTitle.toString());
//					}else{
//						log.debug("hrJobTitle is null ");
//					}
//					
//					RDFNode involvedOrganizationName = soln.get("involvedOrganizationName");
//					if(involvedOrganizationName != null){
//						propertyValues.append(" " + involvedOrganizationName.toString());
//					}else{
//						log.debug("involvedOrganizationName is null ");
//					}
//					
//					RDFNode positionForPerson = soln.get("positionForPerson");
//					if(positionForPerson != null){
//						propertyValues.append(" " + positionForPerson.toString());
//					}else{
//						log.debug("positionForPerson is null ");
//					}
//					
//					RDFNode positionInOrganization = soln.get("positionInOrganization");
//					if(positionInOrganization != null){
//						propertyValues.append(" " + positionInOrganization.toString());
//					}else{
//						log.debug("positionInOrganization is null ");
//					}
//					
//					RDFNode titleOrRole = soln.get("titleOrRole");
//					if(titleOrRole != null){
//						propertyValues.append(" " + titleOrRole.toString());
//					}else{
//						log.debug("titleOrRole is null ");
//					}
//					
//					/*RDFNode positionLabel = soln.get("positionLabel");
//					if(positionLabel != null){
//						propertyValues.append(" " + positionLabel.toString());
//					}else{
//						log.debug("positionLabel is null ");
//					}*/
//										
//				} 
//			}catch(Throwable t){
//				log.error(t,t);
//			} finally{
//				qExec.close();
//			} 
//		}finally{
//				fullModel.leaveCriticalSection();
//		}
//		
//		return propertyValues.toString();
//	}
	
    
    
    //Relationship related queries
    
    public String getAdvisee(String uri){
    	
    	StringBuffer propertyValues = new StringBuffer();
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix + 
		"SELECT " +
		"(str(?Advisee) as ?advisee) WHERE {" +
		"?uri rdf:type foaf:Agent  ; ?b ?c . " +
		" ?c rdf:type core:Relationship . " +
		" ?c core:advisee ?d . ?d rdfs:label ?Advisee . }";
		
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
						log.debug("advisee is null ");
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
    
    public String getDegreeCandidacy(String uri){
    	
    	StringBuffer propertyValues = new StringBuffer();
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix + 
		"SELECT " +
		"(str(?DegreeCandidacy) as ?degreeCandidacy) WHERE {" +
		"?uri rdf:type foaf:Agent  ; ?b ?c . " +
		" ?c rdf:type core:Relationship . " +
		" ?c core:degreeCandidacy ?e . ?e rdfs:label ?DegreeCandidacy . }";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
					RDFNode degreeCandidacy = soln.get("degreeCandidacy");
					if(degreeCandidacy != null){
						propertyValues.append(" " + degreeCandidacy.toString());
					}else{
						log.debug("degreeCandidacy is null ");
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
    
    public String getLinkedAuthor(String uri){
    	
    	StringBuffer propertyValues = new StringBuffer();
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix + 
		"SELECT " +
		"(str(?LinkedAuthor) as ?linkedAuthor) WHERE {" +
		"?uri rdf:type foaf:Agent  ; ?b ?c . " +
		" ?c rdf:type core:Relationship . " +
		" ?c core:linkedAuthor ?f . ?f rdfs:label ?LinkedAuthor . }";
		
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
						log.debug("linkedAuthor is null ");
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
    
    public String getLinkedInformationResource(String uri){
    	
    	StringBuffer propertyValues = new StringBuffer();
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix + 
		"SELECT " +
		"(str(?LinkedInformationResource) as ?linkedInformationResource) WHERE {" +
		"?uri rdf:type foaf:Agent  ; ?b ?c . " +
		" ?c rdf:type core:Relationship . " +
		" ?c core:linkedInformationResource ?h . ?h rdfs:label ?LinkedInformationResource . }";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
					RDFNode linkedInformationResource = soln.get("linkedInformationResource");
					if(linkedInformationResource != null){
						propertyValues.append(" " + linkedInformationResource.toString());
					}else{
						log.debug("linkedInformationResource is null ");
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
    

//    public String getPropertiesAssociatedWithRelationship(String uri){
//		
//		StringBuffer propertyValues = new StringBuffer();
//		
//		QuerySolutionMap initialBinding = new QuerySolutionMap();
//		Resource uriResource = ResourceFactory.createResource(uri);
//		
//		initialBinding.add("uri", uriResource);
//		
//		String thisQuery = prefix +
//		"SELECT (str(?Advisee) as ?advisee)  (str(?DegreeCandidacy) as ?degreeCandidacy) " +
//		" (str(?LinkedAuthor) as ?linkedAuthor) (str(?LinkedInformationResource) as ?linkedInformationResource)  WHERE {" 
//		
//		+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
//		+ " ?c rdf:type core:Relationship . "
//		
//		+ " OPTIONAL  { ?c core:advisee ?d . ?d rdfs:label ?Advisee . } . "
//		+ " OPTIONAL { ?c core:degreeCandidacy ?e . ?e rdfs:label ?DegreeCandidacy . } ."
//		+ " OPTIONAL   { ?c core:linkedAuthor ?f . ?f rdfs:label ?LinkedAuthor . } . "
//		+ " OPTIONAL { ?c core:linkedInformationResource ?h . ?h rdfs:label ?LinkedInformationResource . } . "
//		
//		+ " } ";
//		
//		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
//		fullModel.enterCriticalSection(Lock.READ);
//		
//		try{
//			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
//			try{
//				
//				ResultSet results = qExec.execSelect();
//				
//				while(results.hasNext()){
//					
//					QuerySolution soln = results.nextSolution();
//									
//					RDFNode advisee = soln.get("advisee");
//					if(advisee != null){
//						propertyValues.append(" " + advisee.toString());
//					}else{
//						log.debug("advisee is null ");
//					}
//					
//					RDFNode degreeCandidacy = soln.get("degreeCandidacy");
//					if(degreeCandidacy != null){
//						propertyValues.append(" " + degreeCandidacy.toString());
//					}else{
//						log.debug("degreeCandidacy is null ");
//					}
//					
//					RDFNode linkedAuthor = soln.get("linkedAuthor");
//					if(linkedAuthor != null){
//						propertyValues.append(" " + linkedAuthor.toString());
//					}else{
//						log.debug("linkedAuthor is null ");
//					}
//					
//					RDFNode linkedInformationResource = soln.get("linkedInformationResource");
//					if(linkedInformationResource != null){
//						propertyValues.append(" " + linkedInformationResource.toString());
//					}else{
//						log.debug("linkedInformationResource is null ");
//					}					
//										
//				} 
//			}catch(Throwable t){
//				log.error(t,t);
//			} finally{
//				qExec.close();
//			} 
//		}finally{
//				fullModel.leaveCriticalSection();
//		}
//		
//		return propertyValues.toString();
//	}	
	
    
    public String getAwardConferredBy(String uri){
    	
    	StringBuffer propertyValues = new StringBuffer();
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix + 
		"SELECT " +
		"(str(?AwardConferredBy) as ?awardConferredBy) WHERE {" +
		"?uri rdf:type foaf:Agent  ; ?b ?c . " +
		" ?c rdf:type core:AwardReceipt . " +
		" ?c core:awardConferredBy ?d . ?d rdfs:label ?AwardConferredBy . }";
		
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
						log.debug("awardConferredBy is null ");
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
    
    
    public String getAwardOrHonorFor(String uri){
    	
    	StringBuffer propertyValues = new StringBuffer();
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix + 
		"SELECT " +
		"(str(?AwardOrHonorFor) as ?awardOrHonorFor) WHERE {" +
		"?uri rdf:type foaf:Agent  ; ?b ?c . " +
		" ?c rdf:type core:AwardReceipt . " +
		" ?c core:awardOrHonorFor ?e . ?e rdfs:label ?AwardOrHonorFor . }";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
					RDFNode awardOrHonorFor = soln.get("awardOrHonorFor");
					if(awardOrHonorFor != null){
						propertyValues.append(" " + awardOrHonorFor.toString());
					}else{
						log.debug("awardOrHonorFor is null ");
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
    
    public String getDescription(String uri){
    	
    	StringBuffer propertyValues = new StringBuffer();
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix + 
		"SELECT " +
		"(str(?Description) as ?description) WHERE {" +
		"?uri rdf:type foaf:Agent  ; ?b ?c . " +
		" ?c rdf:type core:AwardReceipt . " +
		" ?c core:description ?Description . }";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
					RDFNode description = soln.get("description");
					if(description != null){
						propertyValues.append(" " + description.toString());
					}else{
						log.debug("description is null ");
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
    
    
//	public String getPropertiesAssociatedWithAwardReceipt(String uri){
//		
//		StringBuffer propertyValues = new StringBuffer();
//		
//		QuerySolutionMap initialBinding = new QuerySolutionMap();
//		Resource uriResource = ResourceFactory.createResource(uri);
//		
//		initialBinding.add("uri", uriResource);
//		
//		String thisQuery = prefix +
//		"SELECT (str(?AwardConferredBy) as ?awardConferredBy)  (str(?AwardOrHonorFor) as ?awardOrHonorFor) " +
//		" (str(?Description) as ?description)   WHERE {" //(str(?AwardReceiptLabel) as ?awardReceiptLabel)
//		
//		+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
//		+ " ?c rdf:type core:AwardReceipt . "
//		
//		+ " OPTIONAL { ?c core:awardConferredBy ?d . ?d rdfs:label ?AwardConferredBy } . "
//		+ " OPTIONAL { ?c core:awardOrHonorFor ?e . ?e rdfs:label ?AwardOrHonorFor } ."
//		+ " OPTIONAL   { ?c core:description ?Description . } . "
//		//+ " OPTIONAL { ?c rdfs:label ?AwardReceiptLabel . } . "
//		
//		+ " } ORDER BY ?AwardReceiptLabel";
//		
//		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
//		fullModel.enterCriticalSection(Lock.READ);
//		
//		try{
//			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
//			try{
//				
//				ResultSet results = qExec.execSelect();
//				
//				while(results.hasNext()){
//					
//					QuerySolution soln = results.nextSolution();
//									
//					RDFNode awardConferredBy = soln.get("awardConferredBy");
//					if(awardConferredBy != null){
//						propertyValues.append(" " + awardConferredBy.toString());
//					}else{
//						log.debug("awardConferredBy is null ");
//					}
//					
//					RDFNode awardOrHonorFor = soln.get("awardOrHonorFor");
//					if(awardOrHonorFor != null){
//						propertyValues.append(" " + awardOrHonorFor.toString());
//					}else{
//						log.debug("awardOrHonorFor is null ");
//					}
//					
//					RDFNode description = soln.get("description");
//					if(description != null){
//						propertyValues.append(" " + description.toString());
//					}else{
//						log.debug("description is null ");
//					}
//					
//					/*RDFNode awardReceiptLabel = soln.get("awardReceiptLabel");
//					if(awardReceiptLabel != null){
//						propertyValues.append(" " + awardReceiptLabel.toString());
//					}else{
//						log.debug("awardReceiptLabel is null ");
//					}*/					
//										
//				} 
//			}catch(Throwable t){
//				log.error(t,t);
//			} finally{
//				qExec.close();
//			} 
//		}finally{
//				fullModel.leaveCriticalSection();
//		}
//		
//		return propertyValues.toString();
//	}	
		
	
	public String getPropertiesAssociatedWithRole(String uri){
		
		StringBuffer propertyValues = new StringBuffer();
		
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
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
	
	
	public String getAcademicDegreeLabelAndAbbreviation(String uri){
		
		StringBuffer propertyValues = new StringBuffer();
		
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix +
		"SELECT (str(?AcademicDegreeLabel) as ?academicDegreeLabel) (str(?AcademicDegreeAbbreviation) as ?academicDegreeAbbreviation) WHERE {" 
		+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
		+ " ?c rdf:type core:EducationalTraining . "
		+ " ?c core:degreeEarned ?d . ?d rdfs:label ?AcademicDegreeLabel ; core:abbreviation ?AcademicDegreeAbbreviation ."
		+ " }";
		
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
						log.debug("academicDegreeLabel is null ");
					}
					
					RDFNode academicDegreeAbbreviation = soln.get("academicDegreeAbbreviation");
					if(academicDegreeAbbreviation != null){
						propertyValues.append(" " + academicDegreeAbbreviation.toString());
					}else{
						log.debug("academicDegreeAbbreviation is null ");
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
	
	
	public String getMajorField(String uri){
		
		StringBuffer propertyValues = new StringBuffer();
		
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix +
		"SELECT (str(?MajorField) as ?majorField) WHERE {" 
		+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
		+ " ?c rdf:type core:EducationalTraining . "
		+ " ?c core:majorField ?MajorField ."
		+ " }";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
									
					RDFNode majorField = soln.get("majorField");
					if(majorField != null){
						propertyValues.append("  " + majorField.toString());
					}else{
						log.debug("majorField is null ");
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
		
	
	public String getDepartmentOrSchool(String uri){
		
		StringBuffer propertyValues = new StringBuffer();
		
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix +
		"SELECT (str(?DepartmentOrSchool) as ?departmentOrSchool) WHERE {" 
		+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
		+ " ?c rdf:type core:EducationalTraining . "
		+ " ?c core:departmentOrSchool ?DepartmentOrSchool ."
		+ " }";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
									
					RDFNode trainingAtDepartmentOrSchool = soln.get("departmentOrSchool");
					if(trainingAtDepartmentOrSchool != null){
						propertyValues.append(" " + trainingAtDepartmentOrSchool.toString());
					}else{
						log.debug("trainingAtDepartmentOrSchool is null ");
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
	
	public String getTrainingAtOrganization(String uri){
		
		StringBuffer propertyValues = new StringBuffer();
		
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix +
		"SELECT (str(?TrainingAtOrganizationLabel) as ?trainingAtOrganizationLabel) WHERE {" 
		+ "?uri rdf:type foaf:Agent  ; ?b ?c . "
		+ " ?c rdf:type core:EducationalTraining . "
		+ " ?c core:trainingAtOrganization ?e . ?e rdfs:label ?TrainingAtOrganizationLabel . "
		+ " }";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
									
					RDFNode trainingAtOrganizationLabel = soln.get("trainingAtOrganizationLabel");
					if(trainingAtOrganizationLabel != null){
						propertyValues.append(" " + trainingAtOrganizationLabel.toString());
					}else{
						log.debug("trainingAtOrganizationLabel is null ");
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
	
//	public String getPropertiesAssociatedWithEducationalTraining(String uri){
//		
//		StringBuffer propertyValues = new StringBuffer();
//		
//		QuerySolutionMap initialBinding = new QuerySolutionMap();
//		Resource uriResource = ResourceFactory.createResource(uri);
//		
//		initialBinding.add("uri", uriResource);
//		
//		String thisQuery = prefix + 
//		"SELECT  (str(?AcademicDegreeLabel) as ?academicDegreeLabel) (str(?AcademicDegreeAbbreviation) as ?academicDegreeAbbreviation) "
//         + "(str(?MajorField) as ?majorField) (str(?DepartmentOrSchool) as ?departmentOrSchool) " +
//         	"(str(?TrainingAtOrganizationLabel) as ?trainingAtOrganizationLabel) WHERE {"
//			
//			+ " ?uri rdf:type foaf:Agent ; ?b ?c . "
//			+ " ?c rdf:type core:EducationalTraining . "
//			  
//			+  "OPTIONAL { ?c core:degreeEarned ?d . ?d rdfs:label ?AcademicDegreeLabel ; core:abbreviation ?AcademicDegreeAbbreviation . } . "
//			+  "OPTIONAL { ?c core:majorField ?MajorField .} ."			  
//			+ " OPTIONAL { ?c core:departmentOrSchool ?DepartmentOrSchool . }"			  
//			+ " OPTIONAL { ?c core:trainingAtOrganization ?e . ?e rdfs:label ?TrainingAtOrganizationLabel . } . " 
//			
//			+"}";
//		
//		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
//		fullModel.enterCriticalSection(Lock.READ);
//		
//		try{
//			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
//			try{
//				
//				ResultSet results = qExec.execSelect();
//				
//				while(results.hasNext()){
//					
//					QuerySolution soln = results.nextSolution();
//					
//					RDFNode academicDegreeLabel = soln.get("academicDegreeLabel");
//					if(academicDegreeLabel != null){
//						propertyValues.append(" " + academicDegreeLabel.toString());
//					}else{
//						log.debug("academicDegreeLabel is null ");
//					}
//					
//					RDFNode academicDegreeAbbreviation = soln.get("academicDegreeAbbreviation");
//					if(academicDegreeAbbreviation != null){
//						propertyValues.append(" " + academicDegreeAbbreviation.toString());
//					}else{
//						log.debug("academicDegreeAbbreviation is null ");
//					}
//					
//					RDFNode majorField = soln.get("majorField");
//					if(majorField != null){
//						propertyValues.append("  " + majorField.toString());
//					}else{
//						log.debug("majorField is null ");
//					}
//					
//					RDFNode trainingAtDepartmentOrSchool = soln.get("departmentOrSchool");
//					if(trainingAtDepartmentOrSchool != null){
//						propertyValues.append(" " + trainingAtDepartmentOrSchool.toString());
//					}else{
//						log.debug("trainingAtDepartmentOrSchool is null ");
//					}
//					
//					RDFNode trainingAtOrganizationLabel = soln.get("trainingAtOrganizationLabel");
//					if(trainingAtOrganizationLabel != null){
//						propertyValues.append(" " + trainingAtOrganizationLabel.toString());
//					}else{
//						log.debug("trainingAtOrganizationLabel is null ");
//					}
//										
//				} 
//			}catch(Throwable t){
//				log.error(t,t);
//			} finally{
//				qExec.close();
//			} 
//		}finally{
//				fullModel.leaveCriticalSection();
//		}		
//		return propertyValues.toString();
//		
//	}
	
	
	
	public String getLinkedAuthorandLinkedInformationResource(String uri){
		
		StringBuffer propertyValues = new StringBuffer();
		
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix +
		"SELECT (str(?LinkedAuthor) as ?linkedAuthor) (str(?LinkedInformationResource) as ?linkedInformationResource) WHERE {" 
		+ " ?uri rdf:type core:InformationResource . "
		+ "?uri core:informationResourceInAuthorship ?a . ?a core:linkedAuthor ?b ; core:linkedInformationResource ?d ." 
		+ "?b rdfs:label ?LinkedAuthor . ?d rdfs:label ?LinkedInformationResource . }";
		
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
						propertyValues.append(" publications " + linkedAuthor.toString() + " publications ");
					}else{
						log.debug("linkedAuthor is null ");
					}
					
					RDFNode linkedInformationResource = soln.get("linkedInformationResource");
					if(linkedInformationResource != null){
						propertyValues.append(" " + linkedInformationResource.toString());
					}else{
						log.debug("linkedInformationResource is null ");
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
	
	
	public String getEditor(String uri){
		
		StringBuffer propertyValues = new StringBuffer();
		
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix +
		"SELECT (str(?Editor) as ?editor) WHERE {" 
		+ "?uri rdf:type core:InformationResource . "
		+ "?uri bibo:editor ?e . ?e rdfs:label ?Editor  ." 
		+ "}";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
									
					RDFNode editor = soln.get("editor");
					if(editor != null){
						propertyValues.append(" " + editor.toString());
					}else{
						log.debug("editor is null ");
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
	
	
	public String getSubjectAreaAndResearchAreaOf(String uri){
		
		StringBuffer propertyValues = new StringBuffer();
		
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix +
		"SELECT (str(?SubjectArea) as ?subjectArea) (str(?ResearchAreaOf) as ?researchAreaOf) WHERE {" 
		+ "?uri rdf:type core:InformationResource . "
		+ "?uri core:hasSubjectArea ?f . ?f rdfs:label ?SubjectArea ; core:researchAreaOf ?h . ?h rdfs:label ?ResearchAreaOf ." 
		+ "}";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
									
					RDFNode subjectArea = soln.get("subjectArea");
					if(subjectArea != null){
						propertyValues.append(" " + subjectArea.toString());
					}else{
						log.debug("subjectArea is null ");
					}
					
					RDFNode researchAreaOf = soln.get("researchAreaOf");
					if(researchAreaOf != null){
						propertyValues.append(" " + researchAreaOf.toString());
					}else{
						log.debug("researchAreaOf is null ");
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
	
	public String getFeatures(String uri){
		
		StringBuffer propertyValues = new StringBuffer();
		
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
		
		String thisQuery = prefix +
		"SELECT (str(?Features) as ?features) WHERE {" 
		+ "?uri rdf:type core:InformationResource . "
		+ "?uri core:features ?i . ?i rdfs:label ?Features ." 
		+ "}";
		
		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
		fullModel.enterCriticalSection(Lock.READ);
		
		try{
			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
			try{
				
				ResultSet results = qExec.execSelect();
				
				while(results.hasNext()){
					
					QuerySolution soln = results.nextSolution();
									
					RDFNode features = soln.get("features");
					if(features != null){
						propertyValues.append(" publications " + features.toString() + " publications ");
					}else{
						log.debug("features is null ");
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
	
	
//	public String getPropertiesAssociatedWithInformationResource(String uri){
//		
//		StringBuffer propertyValues = new StringBuffer();
//		
//		QuerySolutionMap initialBinding = new QuerySolutionMap();
//		Resource uriResource = ResourceFactory.createResource(uri);
//		
//		initialBinding.add("uri", uriResource);
//		
//		String thisQuery = prefix + 
//		"SELECT  (str(?LinkedAuthor) as ?linkedAuthor) (str(?LinkedInformationResource) as ?linkedInformationResource) "
//         + "(str(?Editor) as ?editor) (str(?SubjectArea) as ?subjectArea) (str(?ResearchAreaOf) as ?researchAreaOf) " +
//         		"(str(?Features) as ?features) WHERE {"
//			
//			+ " ?uri rdf:type core:InformationResource . "
//			  
//			+  "OPTIONAL { ?uri core:informationResourceInAuthorship ?a . ?a core:linkedAuthor ?b ; core:linkedInformationResource ?d ." +
//					"?b rdfs:label ?LinkedAuthor . ?d rdfs:label ?LinkedInformationResource } . "
//			+  "OPTIONAL { ?uri bibo:editor ?e . ?e rdfs:label ?Editor  . } ."			  
//			+ " OPTIONAL { ?uri core:hasSubjectArea ?f . ?f rdfs:label ?SubjectArea ; core:researchAreaOf ?h . ?h rdfs:label ?ResearchAreaOf . } "			  
//			+ " OPTIONAL { ?uri core:features ?i . ?i rdfs:label ?Features . } . " 
//			
//			+"}";
//		
//		Query sparqlQuery = QueryFactory.create(thisQuery, Syntax.syntaxARQ);
//		fullModel.enterCriticalSection(Lock.READ);
//		
//		try{
//			QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, fullModel, initialBinding);
//			try{
//				
//				ResultSet results = qExec.execSelect();
//				
//				while(results.hasNext()){
//					
//					QuerySolution soln = results.nextSolution();
//					
//					RDFNode linkedAuthor = soln.get("linkedAuthor");
//					if(linkedAuthor != null){
//						propertyValues.append(" publications " + linkedAuthor.toString() + " publications ");
//					}else{
//						log.debug("linkedAuthor is null ");
//					}
//					
//					RDFNode linkedInformationResource = soln.get("linkedInformationResource");
//					if(linkedInformationResource != null){
//						propertyValues.append(" " + linkedInformationResource.toString());
//					}else{
//						log.debug("linkedInformationResource is null ");
//					}
//					
//					RDFNode editor = soln.get("editor");
//					if(editor != null){
//						propertyValues.append(" " + editor.toString());
//					}else{
//						log.debug("editor is null ");
//					}
//					
//					RDFNode subjectArea = soln.get("subjectArea");
//					if(subjectArea != null){
//						propertyValues.append(" " + subjectArea.toString());
//					}else{
//						log.debug("subjectArea is null ");
//					}
//					
//					RDFNode researchAreaOf = soln.get("researchAreaOf");
//					if(researchAreaOf != null){
//						propertyValues.append(" " + researchAreaOf.toString());
//					}else{
//						log.debug("researchAreaOf is null ");
//					}
//					
//					RDFNode features = soln.get("features");
//					if(features != null){
//						propertyValues.append(" publications " + features.toString() + " publications ");
//					}else{
//						log.debug("features is null ");
//					}
//										
//				} 
//			}catch(Throwable t){
//				log.error(t,t);
//			} finally{
//				qExec.close();
//			} 
//		}finally{
//				fullModel.leaveCriticalSection();
//		}		
//		return propertyValues.toString();
//		
//	}
    
}
