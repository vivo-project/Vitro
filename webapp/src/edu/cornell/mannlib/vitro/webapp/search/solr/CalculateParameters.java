/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

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
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;


public class CalculateParameters implements DocumentModifier {

	private Dataset dataset;
    public static int totalInd=1;
    protected Map<String,Float> betaMap = new Hashtable<String,Float>();
    private static final String prefix = "prefix owl: <http://www.w3.org/2002/07/owl#> "
		+ " prefix vitroDisplay: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#>  "
		+ " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
		+ " prefix core: <http://vivoweb.org/ontology/core#>  "
		+ " prefix foaf: <http://xmlns.com/foaf/0.1/> "
		+ " prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> "
		+ " prefix localNav: <http://vitro.mannlib.cornell.edu/ns/localnav#>  "
		+ " prefix bibo: <http://purl.org/ontology/bibo/>  ";
    
    private static final String betaQuery = prefix + " SELECT count(distinct ?inLinks) " +
    		" WHERE { " +
    		" ?uri rdf:type owl:Thing . " +
    		" ?inLinks ?prop ?uri . " +
    		" } ";
    
    private static final String totalCountQuery = prefix + " SELECT count(distinct ?ind) " +
	" WHERE { " +
	" ?ind rdf:type owl:Thing . " +
	" } ";
     
    private static Log log = LogFactory.getLog(CalculateParameters.class);
    
    private static final String[] fieldsToAddBetaTo = {
        VitroSearchTermNames.NAME_RAW,
        VitroSearchTermNames.NAME_LOWERCASE,
        VitroSearchTermNames.NAME_UNSTEMMED,
        VitroSearchTermNames.NAME_STEMMED
    };
    
    private static final String[] fieldsToMultiplyBetaBy = {
        VitroSearchTermNames.ALLTEXT,
        VitroSearchTermNames.ALLTEXTUNSTEMMED,                
    };
	
	public CalculateParameters(Dataset dataset){
		 this.dataset =dataset;
		 new Thread(new TotalInd(this.dataset,totalCountQuery)).start();
	}
	
	public CalculateParameters(){
		super();
	}
	
	public float calculateBeta(String uri){
		float beta=0;
		int Conn=0; 
		Query query;
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		QuerySolution soln = null;
		Resource uriResource = ResourceFactory.createResource(uri);
		initialBinding.add("uri", uriResource);
		dataset.getLock().enterCriticalSection(Lock.READ);

		try{
			query = QueryFactory.create(betaQuery,Syntax.syntaxARQ);
			QueryExecution qexec = QueryExecutionFactory.create(query,dataset,initialBinding);
			ResultSet results = qexec.execSelect();
			List<String> resultVars = results.getResultVars();
			if(resultVars!=null && resultVars.size()!=0){
				soln = results.next();
				Conn = Integer.parseInt(soln.getLiteral(resultVars.get(0)).getLexicalForm());
			}
		}catch(Throwable t){
			log.error(t,t);
		}finally{
			dataset.getLock().leaveCriticalSection();
		}

		beta = (float)Conn/totalInd;
		beta *= 100;
		beta += 1;
		return beta; 
    }
	
	public float calculatePhi(StringBuffer adjNodes){

		StringTokenizer nodes = new StringTokenizer(adjNodes.toString()," ");
		String uri=null;
		int size=0;
		float phi = 0.1F;
		while(nodes.hasMoreTokens()){
			size++;
			uri = nodes.nextToken();
		    phi += getBeta(uri);
		}
		if(size>0)
			phi = (float)phi/size;
		else
			phi = 1;
		return phi;
	}
	
	public synchronized Float getBeta(String uri){
		
		float beta;
		 if(betaMap.containsKey(uri)){
	            beta = betaMap.get(uri);
	        }else{
	            beta = calculateBeta(uri); // or calculate & put in map
	            betaMap.put(uri, beta);
	        } 
        return beta;
		
    }
    
    
    public String[] getAdjacentNodes(String uri){
		
    	List<String> queryList = new ArrayList<String>();
    	Set<String> adjacentNodes = new HashSet<String>();
    	Set<String> coauthorNames = new HashSet<String>();
    	String[] info = new String[]{"",""};
    	StringBuffer adjacentNodesConcat = new StringBuffer();
    	StringBuffer coauthorBuff = new StringBuffer();
    	adjacentNodesConcat.append("");
    	coauthorBuff.append("");
    	
    	queryList.add(prefix + 
    			" SELECT ?adjobj (str(?adjobjLabel) as ?coauthor) " +
    			" WHERE { " +
    			" ?uri rdf:type <http://xmlns.com/foaf/0.1/Person> . " +
    			" ?uri ?prop ?obj . " +
    			" ?obj rdf:type <http://vivoweb.org/ontology/core#Relationship> . " +
    			" ?obj ?prop2 ?obj2 . " +
    			" ?obj2 rdf:type <http://vivoweb.org/ontology/core#InformationResource> . " +
    			" ?obj2 ?prop3 ?obj3 . " +
    			" ?obj3 rdf:type <http://vivoweb.org/ontology/core#Relationship> . " +
    			" ?obj3 ?prop4 ?adjobj . " +
    			" ?adjobj rdfs:label ?adjobjLabel . " +
    			" ?adjobj rdf:type <http://xmlns.com/foaf/0.1/Person> . " +

    			" FILTER (?prop !=rdf:type) . " +
    			" FILTER (?prop2!=rdf:type) . " +
    			" FILTER (?prop3!=rdf:type) . " +
    			" FILTER (?prop4!=rdf:type) . " +
    			" FILTER (?adjobj != ?uri) . " +
    	"}");

    	queryList.add(prefix +
    			" SELECT ?adjobj " +
    			" WHERE{ " +

    			" ?uri rdf:type foaf:Agent . " +
    			" ?uri ?prop ?obj . " +
    			" ?obj ?prop2 ?adjobj . " +


    			" FILTER (?prop !=rdf:type) . " +
    			" FILTER isURI(?obj) . " +

    			" FILTER (?prop2!=rdf:type) . " +
    			" FILTER (?adjobj != ?uri) . " +
    			" FILTER isURI(?adjobj) . " +

    			" { ?adjobj rdf:type <http://xmlns.com/foaf/0.1/Organization> . } " +
    			" UNION " +
    			" { ?adjobj rdf:type <http://xmlns.com/foaf/0.1/Person> . } " +
    			" UNION " +
    			" { ?adjobj rdf:type <http://vivoweb.org/ontology/core#InformationResource> . } " +
    			" UNION " +
    			" { ?adjobj rdf:type <http://vivoweb.org/ontology/core#Location> . } ." +
    	"}");
	
    	Query query;
    	
    	QuerySolution soln;
    	QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(uri);
		
		initialBinding.add("uri", uriResource);
    	
    	Iterator<String> queryItr = queryList.iterator();
    	
    	dataset.getLock().enterCriticalSection(Lock.READ);
    	Resource adjacentIndividual = null;
    	RDFNode coauthor = null;
    	try{
    		while(queryItr.hasNext()){
    			/*if(!isPerson){
    				queryItr.next(); // we don't want first query to execute if the ind is not a person. 
    			}*/
    			query = QueryFactory.create(queryItr.next(),Syntax.syntaxARQ);
    			QueryExecution qexec = QueryExecutionFactory.create(query,dataset,initialBinding);
    			try{
    					ResultSet results = qexec.execSelect();
    					while(results.hasNext()){
    						soln = results.nextSolution();

    						adjacentIndividual = (Resource)soln.get("adjobj");
    						if(adjacentIndividual!=null){
    							adjacentNodes.add(adjacentIndividual.getURI());
    						}	

    						coauthor = soln.get("coauthor");
    						if(coauthor!=null){
    							coauthorNames.add(" co-authors " + coauthor.toString() + " co-authors ");
    						}	
    					}
    			}catch(Exception e){
    				log.error("Error found in getAdjacentNodes method of SearchQueryHandler");
    			}finally{
    				qexec.close();
    			}	
    		}
    		queryList = null;	
    		Iterator<String> itr = adjacentNodes.iterator();
    		while(itr.hasNext()){
    			adjacentNodesConcat.append(itr.next() + " ");
    		}
    		
    		info[0] = adjacentNodesConcat.toString();
    		
    		itr = coauthorNames.iterator();
    		while(itr.hasNext()){
    			coauthorBuff.append(itr.next());
    		}
    		
    		info[1] = coauthorBuff.toString();
    		
    	}
    	catch(Throwable t){
    		log.error(t,t);
    	}finally{
    		dataset.getLock().leaveCriticalSection();
    		adjacentNodes = null;
    		adjacentNodesConcat = null;
    		coauthorBuff = null;
    	}
    	return info;
	}
   
	@Override
	public void modifyDocument(Individual individual, SolrInputDocument doc, StringBuffer addUri) {
		// TODO Auto-generated method stub
		 // calculate beta value.  
        log.debug("Parameter calculation starts..");
        
        String uri = individual.getURI();
        String adjInfo[] = getAdjacentNodes(uri);
        StringBuffer info = new StringBuffer();
        info.append(adjInfo[0]);
        info.append(addUri.toString());
        float phi = calculatePhi(info);
        
        for(String term: fieldsToAddBetaTo){
            SolrInputField f = doc.getField( term );
            f.setBoost( getBeta(uri) + phi + IndividualToSolrDocument.NAME_BOOST);
        }
        
        for(String term: fieldsToMultiplyBetaBy){
            SolrInputField f = doc.getField( term );
            f.addValue(info.toString(),getBeta(uri)*phi*IndividualToSolrDocument.ALL_TEXT_BOOST);
        }
        
        SolrInputField f = doc.getField(VitroSearchTermNames.targetInfo);
        f.addValue(adjInfo[1],f.getBoost());
        doc.setDocumentBoost(getBeta(uri)*phi*IndividualToSolrDocument.ALL_TEXT_BOOST);   
        
        log.debug("Parameter calculation is done");
	}
	
	public void clearMap(){
		betaMap.clear();
	}
	
}

class TotalInd implements Runnable{
	private Dataset dataset;
	private String totalCountQuery;
	private static Log log = LogFactory.getLog(TotalInd.class);
	
	public TotalInd(Dataset dataset,String totalCountQuery){
		this.dataset = dataset;
		this.totalCountQuery = totalCountQuery;
		
	}
	public void run(){
		    int totalInd=0;
	        Query query;
	    	QuerySolution soln = null;
			dataset.getLock().enterCriticalSection(Lock.READ);
			
			try{
				query = QueryFactory.create(totalCountQuery,Syntax.syntaxARQ);
				QueryExecution qexec = QueryExecutionFactory.create(query,dataset);
				ResultSet results = qexec.execSelect();
				List<String> resultVars = results.getResultVars();
				
				if(resultVars!=null && resultVars.size()!=0){
					soln = results.next();
					totalInd = Integer.parseInt(soln.getLiteral(resultVars.get(0)).getLexicalForm());
				}
				CalculateParameters.totalInd = totalInd;
				log.info("Total number of individuals in the system are : " + CalculateParameters.totalInd);
			}catch(Throwable t){
				log.error(t,t);
			}finally{
				dataset.getLock().leaveCriticalSection();
			}
		
	}
}
