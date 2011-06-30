/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;
import edu.cornell.mannlib.vitro.webapp.search.docbuilder.Obj2DocIface;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexerIface;

public class SolrIndexer implements IndexerIface {
    private final static Log log = LogFactory.getLog(SolrIndexer.class);
    
    protected SolrServer server;
    protected boolean indexing;        
    protected HashSet<String> urisIndexed;    
    protected IndividualToSolrDocument individualToSolrDoc;
    
    public SolrIndexer( SolrServer server, IndividualToSolrDocument indToDoc){
        this.server = server; 
        this.individualToSolrDoc = indToDoc;        
    }
    
    @Override
    public void index(Individual ind) throws IndexingException {
        if( ! indexing )
            throw new IndexingException("SolrIndexer: must call " +
                    "startIndexing() before index().");        
        
        if( ind == null )
            log.debug("Individual to index was null, ignoring.");
        
        try{
            if( urisIndexed.contains(ind.getURI()) ){
                log.debug("already indexed " + ind.getURI() );
                return;
            }else{
            	SolrInputDocument solrDoc = null;
            	synchronized(this){
            		urisIndexed.add(ind.getURI());
            	}
                log.debug("indexing " + ind.getURI());      
                solrDoc = individualToSolrDoc.translate(ind);

                if( solrDoc != null){
                    //sending each doc individually is inefficient
                   // Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
                   // docs.add( solrDoc );
                    UpdateResponse res = server.add( solrDoc );
                    log.debug("response after adding docs to server: "+ res);                
                }else{
                    log.debug("removing from index " + ind.getURI());
                    //TODO: how do we delete document?                    
                    //writer.deleteDocuments((Term)obj2doc.getIndexId(ind));
                }                            
            }
        } catch (IOException ex) {
            throw new IndexingException(ex.getMessage());
        } catch (SolrServerException ex) {
            throw new IndexingException(ex.getMessage());
        }        
        
    	if( ! indexing )
    		throw new IndexingException("SolrIndexer: must call " +
    		"startIndexing() before index().");        

    	if( ind == null )
    		log.debug("Individual to index was null, ignoring.");

    	try{
    		if( urisIndexed.contains(ind.getURI()) ){
    			log.debug("already indexed " + ind.getURI() );
    			return;
    		}else{
    			SolrInputDocument solrDoc = null;
    			synchronized(this){
    				urisIndexed.add(ind.getURI());
    			}
    			log.debug("indexing " + ind.getURI());      

    			solrDoc = individualToSolrDoc.translate(ind);

    			if( solrDoc != null){
    				UpdateResponse res = server.add( solrDoc );
    				log.debug("response after adding docs to server: "+ res);                
    			}else{
    				log.debug("removing from index " + ind.getURI());
    				//TODO: how do we delete document?                    
    				//writer.deleteDocuments((Term)obj2doc.getIndexId(ind));
    			}                            
    		}
    	} catch (IOException ex) {
    		throw new IndexingException(ex.getMessage());
    	} catch (SolrServerException ex) {
    		throw new IndexingException(ex.getMessage());    		
    	}        
    }

    @Override
    public boolean isIndexing() {     
        return indexing;
    }

    @Override
    public void prepareForRebuild() throws IndexingException {
        // TODO Auto-generated method stub        
    }

    @Override
    public void removeFromIndex(String uri) throws IndexingException {
        if( uri != null ){            
            try {                        
                server.deleteByQuery( individualToSolrDoc.getQueryForId(uri));
                log.debug("deleted " + " " + uri);                                       
            } catch (SolrServerException e) {
                log.error( "could not delete individual " + uri, e);
            } catch (IOException e) {
                log.error( "could not delete individual " + uri, e);
            }
        }        
    }

    @Override
    public synchronized void startIndexing() throws IndexingException {        
        while( indexing ){ //wait for indexing to end.
            log.debug("SolrIndexer.startIndexing() waiting...");
            try{ wait(); } catch(InterruptedException ex){}
        }
               
        log.debug("Starting to index");        
        indexing = true;
        urisIndexed = new HashSet<String>();        
        notifyAll();        
    }
    
    
    public synchronized void addObj2Doc(Obj2DocIface o2d) {
        //no longer used
    }

    public synchronized List<Obj2DocIface> getObj2DocList() {
        //no longer used
        return null;
    }
    
    @Override
    public void abortIndexingAndCleanUp() {
        try{
            server.commit();            
        }catch(SolrServerException e){
            if( log != null)
                log.debug("could not commit to solr server, " +
                		"this should not be a problem since solr will do autocommit");
        } catch (IOException e) {
            if( log != null)
                log.debug("could not commit to solr server, " +
                        "this should not be a problem since solr will do autocommit");
        }
        try{
            individualToSolrDoc.shutdown();
        }catch(Exception e){
            if( log != null)
                log.warn(e,e);
        }
    }
   
    @Override
    public synchronized void endIndexing() {
        try {
           UpdateResponse res = server.commit();
           log.debug("Response after committing to server: "+ res );
        } catch (SolrServerException e) {
            log.error("Could not commit to solr server", e);
        } catch(IOException e){
        	log.error("Could not commit to solr server", e);
        }
        try {
            server.optimize();
        } catch (Exception e) {
            log.error("Could not optimize solr server", e);
        }
        indexing = false;
        notifyAll();
    }

    @Override
    public long getModified() {
    	long modified = 0;

    	SolrQuery query = new SolrQuery();
    	query.setQuery("*:*");
    	query.addSortField("indexedTime", SolrQuery.ORDER.desc);

    	try {
    		QueryResponse rsp = server.query(query);
    		SolrDocumentList docs = rsp.getResults();
    		if(docs!=null){
    			modified = (Long)docs.get(0).getFieldValue("indexedTime");	
    		}
    	} catch (SolrServerException e) {
    		// TODO Auto-generated catch block
    		log.error(e,e);
    	}

    	return modified;
    }

    public boolean isIndexEmpty() {
    	SolrQuery query = new SolrQuery();
    	query.setQuery("*:*");
    	try {
    		QueryResponse rsp = server.query(query);
    		SolrDocumentList docs = rsp.getResults();
    		if(docs==null || docs.size()==0){
    			return true;
    		}
    	} catch (SolrServerException e) {
    		// TODO Auto-generated catch block
    		log.error(e,e);
    	}
        return false;
    }

    
   
   

}
