/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import java.util.Iterator;

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
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;

public class ThumbnailImageURL implements DocumentModifier {
	
    private static final String prefix = "prefix owl: <http://www.w3.org/2002/07/owl#> "
        + " prefix vitroDisplay: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#>  "
        + " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
        + " prefix core: <http://vivoweb.org/ontology/core#>  "
        + " prefix foaf: <http://xmlns.com/foaf/0.1/> "
        + " prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> "
        + " prefix localNav: <http://vitro.mannlib.cornell.edu/ns/localnav#>  "
        + " prefix bibo: <http://purl.org/ontology/bibo/>  ";
    
    private static final String query = prefix + 
    
    " SELECT (str(?downloadLocation) as ?DownloadLocation) WHERE { " +
    " ?uri <http://vitro.mannlib.cornell.edu/ns/vitro/public#mainImage> ?a . " +
    " ?a <http://vitro.mannlib.cornell.edu/ns/vitro/public#downloadLocation> ?downloadLocation . } " ;
    //" ?b <http://vitro.mannlib.cornell.edu/ns/vitro/public#directDownloadUrl> ?thumbnailLocationURL . } ";
    
    private Model model;
    private Log log = LogFactory.getLog(ThumbnailImageURL.class);
    
	static VitroSearchTermNames term = new VitroSearchTermNames();
	String fieldForThumbnailURL = term.THUMBNAIL_URL;
	
	
	public ThumbnailImageURL(Model model){
		this.model = model;
	}
	
	@Override
	public void modifyDocument(Individual individual, SolrInputDocument doc,
			StringBuffer addUri) throws SkipIndividualException {
		
		//add a field for storing the location of thumbnail for the individual.
		doc.addField(fieldForThumbnailURL, runQueryForThumbnailLocation(individual));
	
	}

	protected String runQueryForThumbnailLocation(Individual individual) {
		
		StringBuffer result = new StringBuffer();
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource uriResource = ResourceFactory.createResource(individual.getURI());
		initialBinding.add("uri", uriResource);
		
		Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
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
                            result.append("" + node.toString());
                        }else{
                            log.info(name + " is null");
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
		
		return result.toString();
	}

	@Override
	public void shutdown() {		
	}

}
