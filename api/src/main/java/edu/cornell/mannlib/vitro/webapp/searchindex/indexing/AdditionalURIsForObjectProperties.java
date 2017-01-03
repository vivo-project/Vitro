/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.indexing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.dao.jena.QueryUtils;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ContextModelsUser;

/**
 * For a given statement, return the URIs that may need to be updated in
 * the search index because of their object property relations to the resources
 * in the statement.    
 * 
 * Context nodes are not handled here. They are taken care of in AdditionalURIsForContextNodex.
 */
public class AdditionalURIsForObjectProperties implements IndexingUriFinder, ContextModelsUser {
    protected static final Log log = LogFactory.getLog(AdditionalURIsForObjectProperties.class);
    
    protected RDFService rdfService;
    
    @Override
	public void setContextModels(ContextModelAccess models) {
    	this.rdfService = models.getRDFService();
	}

	@Override
    public List<String> findAdditionalURIsToIndex(Statement stmt) {
        if(  stmt == null )
            return Collections.emptyList();
        
        if( stmt.getObject().isLiteral() )
            return doDataPropertyStmt( stmt );
        else 
            return doObjectPropertyStmt( stmt );
    }

    @Override
    public void startIndexing() { /* nothing to prepare */ }

    @Override
    public void endIndexing() { /* nothing to do */ }
    
    protected List<String> doObjectPropertyStmt(Statement stmt) {
        // Only need to consider the object since the subject 
        // will already be updated in search index as part of 
        // SearchReindexingListener.
        
        // Also, context nodes are not handled here. They are
        // taken care of in AdditionalURIsForContextNodex.
        if( stmt.getObject().isURIResource() )
            return Collections.singletonList( stmt.getObject().as(Resource.class).getURI() );
        else
            return Collections.emptyList();
    }

    protected List<String> doDataPropertyStmt(Statement stmt) {
        
        if( RDFS.label.equals( stmt.getPredicate()  )){
            // If the property is rdfs:labe then we need to update
            // all the individuals related by object properties. This
            // does not need to account for context nodes as that
            // is handled in AdditionalURIsForContextNodex.
            if( stmt.getSubject().isURIResource() ){
                return allIndividualsRelatedByObjectPropertyStmts( stmt.getSubject().getURI() );
            }else{ 
                log.debug("ignored bnode");
                return Collections.emptyList();
            }
        }else{
            // This class does not need to account for context nodes because that
            // is handled in AdditionalURIsForContextNodex.
            return Collections.emptyList();
        }
        
    }

    protected List<String> allIndividualsRelatedByObjectPropertyStmts( String uri ) {
        List<String> additionalUris = new ArrayList<String>();
        
        QuerySolutionMap initialBinding = new QuerySolutionMap();
        Resource uriResource = ResourceFactory.createResource(uri);        
        initialBinding.add("uri", uriResource);
        
		ResultSet results = QueryUtils.getQueryResults(QUERY_FOR_RELATED,
				initialBinding, rdfService);

        while(results.hasNext()){                    
            QuerySolution soln = results.nextSolution();                                   
            Iterator<String> iter =  soln.varNames() ;
            while( iter.hasNext()){
                String name = iter.next();
                RDFNode node = soln.get( name );
                if( node != null ){
                    if( node.isURIResource() ){
                        additionalUris.add( node.as( Resource.class ).getURI()  );
                    }else{
                        log.warn( "value from query for var " + name + "  was not a URIResource, it was " + node);    
                    }
                }else{
                    log.warn("value for query for var " + name + " was null");
                }                        
            }
        }

        return additionalUris;
    }

    protected static final String prefixs = 
        "prefix owl:  <http://www.w3.org/2002/07/owl#> \n" +
        "prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n" +
        "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";

    protected final String QUERY_FOR_RELATED = 
        prefixs + 
        "SELECT ?related WHERE { \n" +        
        "  ?uri ?p ?related  \n " +
        "  filter( isURI( ?related ) && ?p != rdf:type )  \n" +                
        "}" ;
    
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
