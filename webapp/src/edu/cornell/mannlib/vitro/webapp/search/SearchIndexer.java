/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search;

import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery.Order;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocumentList;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;
import edu.cornell.mannlib.vitro.webapp.search.beans.IndexerIface;


public class SearchIndexer implements IndexerIface {
    private final static Log log = LogFactory.getLog(SearchIndexer.class);
    
    protected SearchEngine server;
    protected boolean indexing;        
    protected HashSet<String> urisIndexed;    
    
    /**
     * System is shutting down if true.
     */
    protected boolean shutdownRequested = false;
    
    /**
     * This records when a full re-index starts so that once it is done
     * all the documents in the search index that are earlier than the
     * reindexStart can be removed. 
     */
    protected long reindexStart = 0L;
    
    /**
     * If true, then a full index rebuild was requested and reindexStart
     * will be used to determine what documents to remove from the index
     * once the re-index is complete.
     */
    protected boolean doingFullIndexRebuild = false;
    
    public SearchIndexer( SearchEngine server){
        this.server = server; 
    }
    
    @Override
    public void index(Individual ind) throws IndexingException {
        if( ! indexing )
            throw new IndexingException("SearchIndexer: must call " +
                    "startIndexing() before index().");        
        
        if( ind == null ) {
            log.debug("Individual to index was null, ignoring.");
            return;
        }
        
        try{
            if( urisIndexed.contains(ind.getURI()) ){
                log.debug("already indexed " + ind.getURI() );
                return;
            }else{
            	SearchInputDocument doc = null;
            	synchronized(this){
            		urisIndexed.add(ind.getURI());
            	}
                log.debug("indexing " + ind.getURI());      
//                doc = individualToSearchDoc.translate(ind);

                if( doc != null){
                	if( log.isDebugEnabled()){
                		log.info("boost for " + ind.getName() + " is " + doc.getDocumentBoost());
                		log.debug( doc.toString() );
                	}                	                	                	
                	
                    server.add( doc );
                    log.debug("Added docs to server.");                
                }else{
                    log.debug("removing from index " + ind.getURI());                    
                    removeFromIndex(ind.getURI());
                }                            
            }
        } catch (SearchEngineException ex) {
            throw new IndexingException(ex.getMessage());
        }                 
    }

    @Override
    public boolean isIndexing() {     
        return indexing;
    }

    @Override
    public void prepareForRebuild() throws IndexingException {
        reindexStart = System.currentTimeMillis();
        doingFullIndexRebuild = true;
    }

    @Override
    public void removeFromIndex(String uri) throws IndexingException {
        if( uri != null ){            
            try {
//                server.deleteById(individualToSearchDoc.getIdForUri(uri));
                log.debug("deleted " + " " + uri);                                       
            } catch (Exception e) {
                log.error( "could not delete individual " + uri, e);
            }
        }        
    }

    @Override
    public synchronized void startIndexing() throws IndexingException {
        if( indexing)
            log.debug("SearchIndexer.startIndexing() Indexing in progress, waiting for completion...");
        while( indexing && ! shutdownRequested ){ //wait for indexing to end.            
            try{ wait( 250 ); } 
            catch(InterruptedException ex){}
        }
               
        log.debug("Starting to index");
        indexing = true;
        urisIndexed = new HashSet<String>();        
        notifyAll();        
    }        
    
    @Override
    public void abortIndexingAndCleanUp() {
        shutdownRequested = true;
        try{
//            individualToSearchDoc.shutdown();
        }catch(Exception e){
            if( log != null)
                log.debug(e,e);
        }                
        endIndexing();
    }
   
    @Override
    public synchronized void endIndexing() {
        try {
            if( doingFullIndexRebuild ){
                removeDocumentsFromBeforeRebuild( );
            }
         } catch (Throwable e) {
             if( ! shutdownRequested )
                 log.debug("could not remove documents from before build, " ,e);
        }
        try {
           server.commit();           
        } catch (Throwable e) {
            if( ! shutdownRequested ){
                log.debug("could not commit to the search engine, " +
                "this should not be a problem since the search engine will do autocommit");
            }
        }                
        indexing = false;
        notifyAll();
    }

    protected void removeDocumentsFromBeforeRebuild(){
        try {
            server.deleteByQuery("indexedTime:[ * TO " + reindexStart + " ]");
            server.commit();            
        } catch (SearchEngineException e) {
            if( ! shutdownRequested )
                log.error("could not delete documents from before rebuild.",e);            
        }
    }
    
    
    @Override
    public long getModified() {
    	long modified = 0;

    	SearchQuery query = ApplicationUtils.instance().getSearchEngine().createQuery();
    	query.setQuery("*:*");
    	query.addSortField("indexedTime", Order.DESC);

    	try {
    		SearchResponse rsp = server.query(query);
    		SearchResultDocumentList docs = rsp.getResults();
    		if(docs!=null){
    			modified = (Long)docs.get(0).getFirstValue("indexedTime");	
    		}
    	} catch (SearchEngineException e) {
    		log.error(e,e);
    	}

    	return modified;
    }

    /**
     * Returns true if there are documents in the index, false if there are none,
     * and returns false on failure to connect to server.
     */
    @Override
	public boolean isIndexEmpty() {
    	try {
    		return server.documentCount() == 0;
    	} catch (SearchEngineException e) {
    		log.error("Could not connect to the search engine." ,e.getCause());
    	}
        return false;
    }

}
