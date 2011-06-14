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

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.search.VitroTermNames;


public class CalculateParameters implements DocumentModifier {

	Model fullModel;
    int totalInd;
    public static Map<String,Float> betaMap = new Hashtable<String,Float>();
    private float phi;
    private static final String prefix = "prefix owl: <http://www.w3.org/2002/07/owl#> "
		+ " prefix vitroDisplay: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#>  "
		+ " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
		+ " prefix core: <http://vivoweb.org/ontology/core#>  "
		+ " prefix foaf: <http://xmlns.com/foaf/0.1/> "
		+ " prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> "
		+ " prefix localNav: <http://vitro.mannlib.cornell.edu/ns/localnav#>  "
		+ " prefix bibo: <http://purl.org/ontology/bibo/>  ";
    
    private static Log log = LogFactory.getLog(CalculateParameters.class);
    
    private static final String[] fieldsToAddBetaTo = {
        VitroTermNames.NAME_RAW,
        VitroTermNames.NAME_LOWERCASE,
        VitroTermNames.NAME_UNSTEMMED,
        VitroTermNames.NAME_STEMMED
    };
    
    private static final String[] fieldsToMultiplyBetaBy = {
        VitroTermNames.ALLTEXT,
        VitroTermNames.ALLTEXTUNSTEMMED,                
    };
	
	public CalculateParameters(OntModel fullModel){
		 this.fullModel=fullModel;
	     this.totalInd = fullModel.listIndividuals().toList().size();
	}
	
	public float calculateBeta(String uri){
        float beta=0;
        RDFNode node = (Resource) fullModel.getResource(uri); 
        StmtIterator stmtItr = fullModel.listStatements((Resource)null, (Property)null,node);
        int Conn = stmtItr.toList().size();
        beta = (float)Conn/totalInd;
        beta *= 100;
        beta += 1;
        return beta; 
    }
	
	public float calculatePhi(StringBuffer adjNodes){

		StringTokenizer nodes = new StringTokenizer(adjNodes.toString()," ");
		String uri=null;
		float beta=0;
		int size=0;
		phi = 0.1F;
		while(nodes.hasMoreTokens()){
			size++;
			uri = nodes.nextToken();
			if(hasBeta(uri)){ // get if already calculated
				phi += getBeta(uri);
			}else{						// query if not calculated and put in map
				beta = calculateBeta(uri);
				setBeta(uri, beta);
				phi+=beta;
			}
		}
		if(size>0)
			phi = (float)phi/size;
		else
			phi = 1;
		return phi;
	}
	
	public Float getBeta(String uri){
        return betaMap.get(uri);
    }
    public float getPhi(){
		return phi;
	}
    public boolean hasBeta(String uri){
    	return betaMap.containsKey(uri);
    }
    public void setBeta(String uri, float beta){
    	betaMap.put(uri, beta);
    }
    
    public String[] getAdjacentNodes(String uri,boolean isPerson){
		
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
    	
    	fullModel.enterCriticalSection(Lock.READ);
    	Resource adjacentIndividual = null;
    	RDFNode coauthor = null;
    	try{
    		while(queryItr.hasNext()){
    			if(!isPerson){
    				queryItr.next(); // we don't want first query to execute if the ind is not a person. 
    			}
    			query = QueryFactory.create(queryItr.next(),Syntax.syntaxARQ);
    			QueryExecution qexec = QueryExecutionFactory.create(query,fullModel,initialBinding);
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
    		fullModel.leaveCriticalSection();
    		adjacentNodes = null;
    		adjacentNodesConcat = null;
    		coauthorBuff = null;
    	}
    	return info;
	}
   
	@Override
	public void modifyDocument(Individual individual, SolrInputDocument doc) {
		// TODO Auto-generated method stub
		 // calculate beta value.  
        log.debug("Parameter calculation starts..");
        
        float beta = 0;
        String uri = individual.getURI();
        if(hasBeta(uri)){
            beta = getBeta(uri);
        }else{
            beta = calculateBeta(uri); // or calculate & put in map
            setBeta(uri,beta);
        } 
        
        boolean isPerson = (IndividualToSolrDocument.superClassNames.contains("Person")) ? true : false ;
        String adjInfo[] = getAdjacentNodes(uri,isPerson);
        StringBuffer info = new StringBuffer();
        info.append(adjInfo[0]);
        info.append(IndividualToSolrDocument.addUri.toString());
        phi = calculatePhi(info);
        
        for(String term: fieldsToAddBetaTo){
            SolrInputField f = doc.getField( term );
            f.setBoost( getBeta(uri) + phi + IndividualToSolrDocument.NAME_BOOST);
        }
        
        for(String term: fieldsToMultiplyBetaBy){
            SolrInputField f = doc.getField( term );
            f.addValue(info.toString(),getBeta(uri)*phi*IndividualToSolrDocument.ALL_TEXT_BOOST);
        }
        
        SolrInputField f = doc.getField(VitroTermNames.targetInfo);
        f.addValue(adjInfo[1],f.getBoost());
        doc.setDocumentBoost(getBeta(uri)*phi*IndividualToSolrDocument.ALL_TEXT_BOOST);   
        
        log.debug("Parameter calculation is done");
	}
	
}
