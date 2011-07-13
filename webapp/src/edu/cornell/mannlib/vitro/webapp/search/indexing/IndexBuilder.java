/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;


/**
 * The IndexBuilder is used to rebuild or update a search index.
 * There should only be one IndexBuilder in a vitro web application.
 * It uses an implementation of a back-end through an object that
 * implements IndexerIface.  An example of a back-end is SolrIndexer.
 *
 * See the class SearchReindexingListener for an example of how a model change
 * listener can use an IndexBuilder to keep the full text index in sncy with 
 * updates to a model. It calls IndexBuilder.addToChangedUris().
 *
 * @author bdc34
 *
 */
public class IndexBuilder extends Thread {
    private WebappDaoFactory wdf;    
	private final IndexerIface indexer;
    private final ServletContext context;       
    
    /* changedUris should only be accessed from synchronized blocks */
    private HashSet<String> changedUris = null;
    
    private List<String> updatedInds = null;
    private List<String> deletedInds = null;    
    
    private boolean reindexRequested = false;    
    protected boolean stopRequested = false;
    protected long reindexInterval = 1000 * 60 /* msec */ ;        
    
    protected int numberOfThreads = 10;
    protected List<AdditionalURIsToIndex> additionalURIsFinders;
    
    public static final boolean UPDATE_DOCS = false;
    public static final boolean NEW_DOCS = true;
      
    private static final Log log = LogFactory.getLog(IndexBuilder.class);

    public IndexBuilder(
                ServletContext context,
                IndexerIface indexer,
                WebappDaoFactory wdf,
                List<AdditionalURIsToIndex> additionalURIsFinders){
        super("IndexBuilder");
        this.indexer = indexer;
        this.wdf = wdf;
        this.context = context;            
        this.additionalURIsFinders = additionalURIsFinders;
        this.changedUris = new HashSet<String>();    
        this.start();
    }
    
    protected IndexBuilder(){
        //for testing only
        this( null, null, null, null);        
    }
    
    public void setWdf(WebappDaoFactory wdf){    	
        this.wdf = wdf;
    }

    public boolean isIndexing(){
        return indexer.isIndexing();
    }

    public synchronized void doIndexRebuild() {
    	//set flag for full index rebuild
    	this.reindexRequested = true; 	
    	//wake up     						
    	this.notifyAll();    	
    }

    /** 
     * This will re-index Individuals that changed because of modtime or because they
     * were added with addChangedUris(). 
     */
    public synchronized void doUpdateIndex() {        	    
    	//wake up thread and it will attempt to index anything in changedUris
        this.notifyAll();    	    	   
    }
   
    /**
     * Use this method to add URIs that need to be indexed. 
     */
    public synchronized void addToChangedUris(String uri){
    	changedUris.add(uri);
    }
    
    /**
     * Use this method to add URIs that need to be indexed. 
     */
    public synchronized void addToChangedUris(Collection<String> uris){
    	changedUris.addAll(uris);    	
    }
    
	public synchronized boolean isReindexRequested() {
		return reindexRequested;
	}
	
	public synchronized boolean isThereWorkToDo(){
		return isReindexRequested() || ! changedUris.isEmpty() ;
	}
	
	
	/**
	 * This is called when the system shuts down.
	 */
	public synchronized void stopIndexingThread() {
	    stopRequested = true;
	    this.notifyAll();		    
	    this.interrupt();
	}
	
    @Override
    public void run() {
        while(! stopRequested ){                        
            try{
                if( !stopRequested && isReindexRequested() ){
                    log.debug("full re-index requested");
                    indexRebuild();
                }else if( !stopRequested && isThereWorkToDo() ){                       
                    Thread.sleep(250); //wait a bit to let a bit more work to come into the queue
                    log.debug("work found for IndexBuilder, starting update");
                    updatedIndex();
                }else if( !stopRequested && ! isThereWorkToDo() ){
                    log.debug("there is no indexing working to do, waiting for work");              
                    synchronized (this) { this.wait(reindexInterval); }                         
                }
            } catch (InterruptedException e) {
                log.debug("woken up",e);
            }catch(Throwable e){
                if( log != null )//may be null on shutdown
                    log.error(e,e);
            }
        }
        
        if( indexer != null)
            indexer.abortIndexingAndCleanUp();
        
        if(log != null )//may be null on shutdown 
            log.info("Stopping IndexBuilder thread");
    }

  
	/* ******************** non-public methods ************************* */
    private synchronized Collection<String> getAndEmptyChangedUris(){
    	Collection<String> out = changedUris;     	    
    	changedUris = new HashSet<String>();
    	return out;
    }
    
	/**
	 * Sets updatedUris and deletedUris lists.
	 */
	private void makeAddAndDeleteLists( Collection<String> uris){	    
		/* clear updateInds and deletedUris.  This is the only method that should set these. */
		this.updatedInds = new ArrayList<String>();
		this.deletedInds = new ArrayList<String>();
				
    	for( String uri: uris){
    		if( uri != null ){
    			Individual ind = wdf.getIndividualDao().getIndividualByURI(uri);
    			if( ind != null)
    				this.updatedInds.add(uri);
    			else{
    				log.debug("found delete in changed uris");
    				this.deletedInds.add(uri);
    			}
    		}
    	}    		    	            	
	}	
  
	/**
	 * This rebuilds the whole index.
	 */
    protected void indexRebuild() {
        log.info("Rebuild of search index is starting.");

        // clear out changed uris since we are doing a full index rebuild
        getAndEmptyChangedUris();
       
        log.debug("Getting all URIs in the model");
        Iterator<String> uris = wdf.getIndividualDao().getAllOfThisTypeIterator();
        
        doBuild(uris, Collections.<String>emptyList() );
        
        if( log != null )  //log might be null if system is shutting down.
            log.info("Rebuild of search index is complete.");
    }
      
    protected void updatedIndex() {
        log.debug("Starting updateIndex()");
        //long since = indexer.getModified() - 60000;                               
        //List updatedUris = wdf.getIndividualDao().getUpdatedSinceIterator(since);        
                     
        makeAddAndDeleteLists( getAndEmptyChangedUris() );                           
        
        doBuild( updatedInds.iterator(), deletedInds );
        log.debug("Ending updateIndex()");
    }
    
    /**
     * For each sourceIterator, get all of the objects and attempt to
     * index them.
     *
     * This takes a list of source Iterators and, for each of these,
     * calls indexForSource.
     *
     * @param sourceIterators
     * @param newDocs true if we know that the document is new. Set
     * to false if we want to attempt to remove the object from the index before
     * attempting to index it.  If an object is not on the list but you set this
     * to false, and a check is made before adding, it will work fine; but
     * checking if an object is on the index is slow.
     */
    private void doBuild(Iterator<String> updates, Collection<String> deletes ){
        boolean aborted = false;
        boolean newDocs = reindexRequested;
        boolean forceNewIndex = reindexRequested;
        
        try {
            if( reindexRequested )
                indexer.prepareForRebuild();
            
            indexer.startIndexing();
            reindexRequested = false;
            
            if( ! forceNewIndex ){                
                for(String deleteMe : deletes ){
                    try{
                        indexer.removeFromIndex(deleteMe);                    
                    }catch(Exception ex){ 
                    	log.debug(ex.getMessage());
                        log.debug("could not remove individual " + deleteMe 
                                + " from index, usually this is harmless",ex);
                    }
                }
            }            

            indexUriList(updates, newDocs);
        } catch (AbortIndexing abort){
            if( log != null)
                log.debug("aborting the indexing because thread stop was requested");
            aborted = true;                            
        } catch (Exception e) {
            log.error(e,e);
        }
        
        if( aborted ){
            indexer.abortIndexingAndCleanUp();
        }else{
            indexer.endIndexing();
        }
        
    }
    
    /**
     * Use the back end indexer to index each object that the Iterator returns.
     * @throws AbortIndexing 
     */
    private void indexUriList(Iterator<String> updateUris , boolean newDocs) throws AbortIndexing{
        //make a copy of numberOfThreads so the local copy is safe during this method.
        int numberOfThreads = this.numberOfThreads;
        IndexWorkerThread.setStartTime(System.currentTimeMillis());
                                                                                                                              
        //make lists of work URIs for workers
        List<List<String>> workLists = makeWorkerUriLists(updateUris, numberOfThreads);                                     

        //setup workers with work
        List<IndexWorkerThread> workers = new ArrayList<IndexWorkerThread>();
        for(int i = 0; i< numberOfThreads ;i++){
            Iterator<Individual> workToDo = new UriToIndividualIterator(workLists.get(i), wdf);
            workers.add( new IndexWorkerThread(indexer, i, workToDo) ); 
        }        

        log.debug("Starting the building and indexing of documents in worker threads");
        // starting worker threads        
        for(int i =0; i < numberOfThreads; i++){
            workers.get(i).start();
        }          
        
        //waiting for all the work to finish
        for(int i =0; i < numberOfThreads; i++){
        	try{
        		workers.get(i).join();
        	}catch(InterruptedException e){
        	    //this thread will get interrupted if the system is trying to shut down.        	    
        	    if( log != null )
        	        log.debug(e,e);
        	    for( IndexWorkerThread thread: workers){
        	        thread.requestStop();
        	    }
        	    return;
        	}
        }
        
        IndexWorkerThread.resetCount();        
    }        

    
   
    
    /* maybe ObjectSourceIface should be replaced with just an iterator. */
    protected class UriToIndividualIterator implements Iterator<Individual>{        
        private final Iterator<String> uris;
        private final WebappDaoFactory wdf;
        
        public UriToIndividualIterator( Iterator<String>  uris, WebappDaoFactory wdf){
            this.uris= uris;
            this.wdf = wdf;
        }
        
        public UriToIndividualIterator( List<String>  uris, WebappDaoFactory wdf){
            this.uris= uris.iterator();
            this.wdf = wdf;
        }                        

        @Override
        public boolean hasNext() {
            return uris.hasNext();
        }

        /** may return null */
        @Override
        public Individual next() {
            String uri = uris.next();
            return wdf.getIndividualDao().getIndividualByURI(uri);  
        }

        @Override
        public void remove() {
            throw new IllegalAccessError("");            
        }
    }
    
    private static List<List<String>> makeWorkerUriLists(Iterator<String> uris,int workers){
        List<List<String>> work = new ArrayList<List<String>>(workers);
        for(int i =0; i< workers; i++){
            work.add( new ArrayList<String>() );
        }
        
        int counter = 0;
        while(uris.hasNext()){
            work.get( counter % workers ).add( uris.next() );
            counter ++;
        }
        log.info("Number of individuals to be indexed : " + counter);
        return work;        
    }
    
    
    private class AbortIndexing extends Exception {
    	// Just a vanilla exception
    }  

    
}

