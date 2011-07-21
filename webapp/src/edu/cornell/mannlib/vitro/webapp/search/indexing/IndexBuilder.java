/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.search.beans.IndexerIface;
import edu.cornell.mannlib.vitro.webapp.search.beans.StatementToURIsToUpdate;


/**
 * The IndexBuilder is used to rebuild or update a search index.
 * There should only be one IndexBuilder in a vitro web application.
 * It uses an implementation of a back-end through an object that
 * implements IndexerIface.  An example of a back-end is SolrIndexer.
 *
 * See the class SearchReindexingListener for an example of how a model change
 * listener can use an IndexBuilder to keep the full text index in sncy with 
 * updates to a model. It calls IndexBuilder.addToChangedUris().  
 */
public class IndexBuilder extends Thread {
    private WebappDaoFactory wdf;    
	private final IndexerIface indexer;           

    /** Statements that have changed in the model.  The SearchReindexingListener
     * and other similar objects will use methods on IndexBuilder to add statements
     * to this queue. This should only be accessed from blocks synchronized on
     * the changedStmtQueue object.   
     */
    protected List<Statement> changedStmtQueue;
    
    /** This is the list of URIs that need to be updated in the search
     * index.  The IndexBuilder thread will process Statements in changedStmtQueue
     * to create this set of URIs. 
     * This should only be accessed by the IndexBuilder thread.  */
    private HashSet<String> urisRequiringUpdate;
    
    /** This is a list of objects that will compute what URIs need to be
     * updated in the search index when a statement changes.  */     
    protected List<StatementToURIsToUpdate> stmtToURIsToIndexFunctions;    
    
    /**
     * updatedUris will only be accessed from the IndexBuilder thread
     * so it doesn't need to be synchronized.
     */
    private List<String> updatedUris = null;
    /**
     * deletedUris will only be accessed from the IndexBuilder thread
     * so it doesn't need to be synchronized.
     */
    private List<String> deletedUris = null;    
    
    /**
     * Indicates that a full index re-build has been requested.
     */
    private boolean reindexRequested = false;
    
    /** Indicates that a stop of the indexing objects has been requested. */
    protected boolean stopRequested = false;
    
    /** Length of pause between a model change an the start of indexing. */
    protected long reindexInterval = 1000 * 60 /* msec */ ;        
    
    /** Length of pause between when work comes into queue to when indexing starts */
    protected long waitAfterNewWorkInterval = 500; //msec
    
    /** Number of threads to use during indexing. */
    protected int numberOfThreads = 10;
    
    public static final int MAX_REINDEX_THREADS= 10;
    public static final int MAX_UPDATE_THREADS= 10;    
    public static final int MAX_THREADS = Math.max( MAX_UPDATE_THREADS, MAX_REINDEX_THREADS);
    
    //public static final boolean UPDATE_DOCS = false;
    //public static final boolean NEW_DOCS = true;
      
    private static final Log log = LogFactory.getLog(IndexBuilder.class);

    public IndexBuilder(IndexerIface indexer,
                        WebappDaoFactory wdf,
                        List<StatementToURIsToUpdate> stmtToURIsToIndexFunctions ){
        super("IndexBuilder");
        
        this.indexer = indexer;
        this.wdf = wdf;            
                
        if( stmtToURIsToIndexFunctions != null )
            this.stmtToURIsToIndexFunctions = stmtToURIsToIndexFunctions;
        else
            this.stmtToURIsToIndexFunctions = Collections.emptyList();
        
        this.changedStmtQueue = new LinkedList<Statement>();
        this.urisRequiringUpdate = new HashSet<String>();
        this.start();
    }
    
    protected IndexBuilder(){
        //for testing only
        this( null, null, null);        
    }
    
    /**
     * Use this method to add URIs that need to be indexed.  Should be
     * able to add to changedStmtQueue while indexing is in process. 
     * 
     * If you have a statement that has been added or removed from the 
     * RDF model and you would like it to take effect in the search
     * index this is the method you should use.  Follow the adding of
     * your changes with a call to doUpdateIndex().
     */
    public void addToChanged(Statement stmt){
        synchronized(changedStmtQueue){
            changedStmtQueue.add(stmt);
        }
    } 
    
    /**
     * This method will cause the IndexBuilder to completely rebuild
     * the index.
     */
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
       
    public boolean isIndexing(){
        return indexer.isIndexing();
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
                if( !stopRequested && reindexRequested ){
                    log.debug("full re-index requested");
                    indexRebuild();
                }else if( !stopRequested && isThereWorkToDo() ){                       
                    Thread.sleep(waitAfterNewWorkInterval); //wait a bit to let a bit more work to come into the queue
                    log.debug("work found for IndexBuilder, starting update");
                    updatedIndex();
                } else {
                    log.debug("there is no indexing working to do, waiting for work");              
                    synchronized (this) { this.wait(reindexInterval); }                         
                }
            } catch (InterruptedException e) {
                log.debug("woken up",e);
            }catch(Throwable e){
                if( ! stopRequested && log != null )//may be null on shutdown
                    log.error(e,e);
            }
        }
        
        if( indexer != null)
            indexer.abortIndexingAndCleanUp();
        
        if(! stopRequested && log != null )//may be null on shutdown 
            log.info("Stopping IndexBuilder thread");
    }
    
    
    public static void checkIndexOnRootLogin(HttpServletRequest req){
    	HttpSession session = req.getSession();
    	ServletContext context = session.getServletContext();
    	IndexBuilder indexBuilder = (IndexBuilder)context.getAttribute(IndexBuilder.class.getName());
    	
    	log.debug("Checking if the index is empty");
    	if(indexBuilder.indexer.isIndexEmpty()){
    		log.info("Index is empty. Running a full index rebuild!");
    		indexBuilder.doIndexRebuild();
    	}
    }

  
	/* ******************** non-public methods ************************* */
    
    private List<Statement> getAndEmptyChangedStatements(){
        List<Statement> localChangedStmt = null;
        synchronized( changedStmtQueue ){
            localChangedStmt = new ArrayList<Statement>(changedStmtQueue.size());
            localChangedStmt.addAll( changedStmtQueue );
            changedStmtQueue.clear();            
        }
        return localChangedStmt;        
    }
    
    /**
     * For a collection of statements, find the URIs that need to be updated in
     * the index.
     */
    private Collection<String> statementsToUris( Collection<Statement> localChangedStmt ){
        Collection<String> urisToUpdate = new HashSet<String>();
        for( Statement stmt : localChangedStmt){
            if( stmt == null )
                continue;
            for( StatementToURIsToUpdate stu : stmtToURIsToIndexFunctions ){
                urisToUpdate.addAll( stu.findAdditionalURIsToIndex(stmt) );
            }
        }               
        return urisToUpdate;        
    }
    
	/**
	 * Sets updatedUris and deletedUris lists from the changedStmtQueue.
	 * updatedUris and deletedUris will only be accessed from the IndexBuilder thread
	 * so they don't need to be synchronized.
	 */
	private void makeAddAndDeleteLists( Collection<String> uris){	    				
	    IndividualDao indDao = wdf.getIndividualDao();
	    
		/* clear updateInds and deletedUris.  This is the only method that should set these. */
		this.updatedUris = new LinkedList<String>();
		this.deletedUris = new LinkedList<String>();
						
    	for( String uri: uris){
    		if( uri != null ){
    			try{
    			    Individual ind = indDao.getIndividualByURI(uri);    			    
	    			if( ind != null)
	    				this.updatedUris.add(uri);
	    			else{
	    				log.debug("found delete in changed uris");
	    				this.deletedUris.add(uri);
	    			}
    			} catch(QueryParseException ex){
    				log.error("could not get Individual "+ uri,ex);
    			}
    		}
    	}    		    	            	
	}	

	/**
	 * This rebuilds the whole index.
	 */
    protected void indexRebuild() {
        log.info("Rebuild of search index is starting.");

        // clear out changed URIs since we are doing a full index rebuild
        getAndEmptyChangedStatements();
       
        log.debug("Getting all URIs in the model");
        Iterator<String> uris = wdf.getIndividualDao().getAllOfThisTypeIterator();
        
        this.numberOfThreads = MAX_REINDEX_THREADS;
        doBuild(uris, Collections.<String>emptyList() );
        
        if( log != null )  //log might be null if system is shutting down.
            log.info("Rebuild of search index is complete.");
    }
      
    protected void updatedIndex() {
        log.debug("Starting updateIndex()");       
                     
        makeAddAndDeleteLists( statementsToUris(getAndEmptyChangedStatements()) );
        
        this.numberOfThreads = Math.max( MAX_UPDATE_THREADS, updatedUris.size() / 20); 
        doBuild( updatedUris.iterator(), deletedUris );
        
        this.updatedUris = null;
        this.deletedUris = null;
        
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
        boolean updateRequested = ! reindexRequested;
        
        try {
            if( reindexRequested ){
                indexer.prepareForRebuild();
            }
            
            indexer.startIndexing();
            reindexRequested = false;
            
            if( updateRequested ){
                //if this is not a full reindex, deleted indivdiuals need to be removed from the index
                for(String deleteMe : deletes ){
                    try{
                        indexer.removeFromIndex(deleteMe);                    
                    }catch(Exception ex){             
                        log.debug("could not remove individual " + deleteMe 
                                + " from index, usually this is harmless",ex);
                    }
                }
            }
            
            indexUriList(updates);
            
        } catch (Exception e) {
            if( log != null) log.debug("Exception during indexing",e);            
        }
        
        indexer.endIndexing();                
    }
    
    /**
     * Use the back end indexer to index each object that the Iterator returns.
     * @throws AbortIndexing 
     */
    private void indexUriList(Iterator<String> updateUris ) {
        //make a copy of numberOfThreads so the local copy is safe during this method.
        int numberOfThreads = this.numberOfThreads;
        if( numberOfThreads > MAX_THREADS )
            numberOfThreads = MAX_THREADS;            
            
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
    
    protected boolean isThereWorkToDo(){
        synchronized( changedStmtQueue ){
            return reindexRequested || ! changedStmtQueue.isEmpty() ;
        }
    }        
}

