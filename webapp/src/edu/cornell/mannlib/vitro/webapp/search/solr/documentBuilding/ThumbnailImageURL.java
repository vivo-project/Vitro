/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr.documentBuilding;

import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.THUMBNAIL;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.THUMBNAIL_URL;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

public class ThumbnailImageURL implements DocumentModifier {
	
    private static final String PREFIX = "prefix owl: <http://www.w3.org/2002/07/owl#> "
        + " prefix vitroDisplay: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#>  "
        + " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
        + " prefix core: <http://vivoweb.org/ontology/core#>  "
        + " prefix foaf: <http://xmlns.com/foaf/0.1/> "
        + " prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> "
        + " prefix localNav: <http://vitro.mannlib.cornell.edu/ns/localnav#>  "
        + " prefix bibo: <http://purl.org/ontology/bibo/>  ";
    
	private static final String QUERY_TEMPLATE = PREFIX
		+ " SELECT (str(?downloadLocation) as ?DownloadLocation) WHERE { "
		+ " ?uri <http://vitro.mannlib.cornell.edu/ns/vitro/public#mainImage> ?a . "
		+ " ?a <http://vitro.mannlib.cornell.edu/ns/vitro/public#downloadLocation> ?downloadLocation . } ";
    
    private RDFServiceFactory rsf;
    private Log log = LogFactory.getLog(ThumbnailImageURL.class);
    
	
	public ThumbnailImageURL( RDFServiceFactory rsf ){
		this.rsf = rsf;
	}
	
	@Override
	public void modifyDocument(Individual individual, SearchInputDocument doc,
			StringBuffer addUri) throws SkipIndividualException {
		
		//add a field for storing the location of thumbnail for the individual.
		doc.addField(THUMBNAIL_URL, runQueryForThumbnailLocation(individual));
		addThumbnailExistence(individual, doc);
	}

	/**
     * Adds if the individual has a thumbnail image or not.
     */
    protected void addThumbnailExistence(Individual ind, SearchInputDocument doc) {
        try{
            if(ind.hasThumb())
                doc.addField(THUMBNAIL, "1");
            else
                doc.addField(THUMBNAIL, "0");
        }catch(Exception ex){
            log.debug("could not index thumbnail: " + ex);
        }        
    }

	protected String runQueryForThumbnailLocation(Individual individual) {
		StringBuffer result = new StringBuffer();

		String uri = "<" + individual.getURI() + "> ";
		String query = QUERY_TEMPLATE.replaceAll("\\?uri", uri);

		RDFService rdf = rsf.getRDFService();
		try{
			ResultSet results = RDFServiceUtils.sparqlSelectQuery(query, rdf);
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
			rdf.close();
		}				
		return result.toString();
	}

	@Override
	public void shutdown() {		
		// nothing to release.
	}

}
