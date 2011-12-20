/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

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
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;


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
public class IndexBuilder extends VitroBackgroundThread {
    private WebappDaoFactory wdf;    
	private final IndexerIface indexer;           

    /** Statements that have changed in the model.  The SearchReindexingListener
     * and other similar objects will use methods on IndexBuilder to add statements
     * to this queue.   
     */
    private final ConcurrentLinkedQueue<Statement> changedStmtQueue = new ConcurrentLinkedQueue<Statement>();
    
    /** This is a list of objects that will compute what URIs need to be
     * updated in the search index when a statement changes.  */     
    private final List<StatementToURIsToUpdate> stmtToURIsToIndexFunctions;    
    
    /** Indicates that a full index re-build has been requested. */
    private volatile boolean reindexRequested = false;
    
    /** Indicates that a stop of the indexing objects has been requested. */
    private volatile boolean stopRequested = false;
    
    /** Length of time to wait before looking for work (if not wakened sooner). */
    public static final long MAX_IDLE_INTERVAL = 1000 * 60 /* msec */ ;        
    
    /** Length of pause between when work comes into queue to when indexing starts */
    public static final long WAIT_AFTER_NEW_WORK_INTERVAL = 500; //msec
    
    /** Flag so we can tell that the index is being updated. */
    public static final String FLAG_UPDATING = "updating";
    
    /** Flag so we can tell that the index is being rebuilt. */
    public static final String FLAG_REBUILDING = "rebuilding";
    
    /** Number of threads to use during indexing. */
    protected int numberOfThreads = 10;
    
    /** List of IndexingEventListeners */
    protected LinkedList<IndexingEventListener> indexingEventListeners = 
        new LinkedList<IndexingEventListener>();
    
    public static final int MAX_REINDEX_THREADS= 10;
    public static final int MAX_UPDATE_THREADS= 10;    
    public static final int MAX_THREADS = Math.max( MAX_UPDATE_THREADS, MAX_REINDEX_THREADS);
    
    private static final Log log = LogFactory.getLog(IndexBuilder.class);
    
    public static IndexBuilder getBuilder(ServletContext ctx) {
    	Object o = ctx.getAttribute(IndexBuilder.class.getName());
    	if (o instanceof IndexBuilder) {
    		return (IndexBuilder) o;
    	} else {
    		log.error("IndexBuilder has not been initialized.");
    		return null;
    	}
    }

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
	public void addToChanged(Statement stmt) {
		changedStmtQueue.add(stmt);
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
     * This will re-index Individuals were added with addToChanged(). 
     */
    public synchronized void doUpdateIndex() {        	    
    	//wake up thread and it will attempt to index anything in changedUris
        this.notifyAll();    	    	   
    }
    
    /**
     * Add a listener for indexing events.  Methods on listener will be called when
     * events happen in the IndexBuilder.  This is not a Jena ModelListener.
     */
    public synchronized void addIndexBuilderListener(IndexingEventListener listener){
        indexingEventListeners.add(listener);
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
                if( reindexRequested ){
                	setWorkLevel(WorkLevel.WORKING, FLAG_REBUILDING);
                    log.debug("full re-index requested");
                    
                    notifyListeners( IndexingEventListener.EventTypes.START_FULL_REBUILD );
                    indexRebuild();
                    notifyListeners( IndexingEventListener.EventTypes.FINISH_FULL_REBUILD );
                    
                    setWorkLevel(WorkLevel.IDLE);
                }else if( !changedStmtQueue.isEmpty() ){                       
                	setWorkLevel(WorkLevel.WORKING, FLAG_UPDATING);                	
                	
                	//wait a bit to let a bit more work to come into the queue
                    Thread.sleep(WAIT_AFTER_NEW_WORK_INTERVAL);                     
                    log.debug("work found for IndexBuilder, starting update");
                    
                    notifyListeners( IndexingEventListener.EventTypes.START_UPDATE );
                    updatedIndex();
                    notifyListeners( IndexingEventListener.EventTypes.FINISHED_UPDATE );
                    setWorkLevel(WorkLevel.IDLE);
                } else {
                    log.debug("there is no indexing working to do, waiting for work");              
                    synchronized (this) { this.wait(MAX_IDLE_INTERVAL); }                         
                }
            } catch (InterruptedException e) {
                log.debug("woken up",e);
            }catch(Throwable e){
            	log.error(e,e);
            }
        }
        
        if( indexer != null)
            indexer.abortIndexingAndCleanUp();        
    }
    
    
    public static void checkIndexOnRootLogin(HttpServletRequest req){
    	HttpSession session = req.getSession();
    	ServletContext context = session.getServletContext();
    	IndexBuilder indexBuilder = (IndexBuilder)context.getAttribute(IndexBuilder.class.getName());
    	
    	log.debug("Checking if the index is empty");
    	if(indexBuilder.indexer.isIndexEmpty()){
    		log.info("Search index is empty. Running a full index rebuild.");
    		indexBuilder.doIndexRebuild();
    	}
    }	
    
    
    /* ******************** non-public methods ************************* */
    
    /**
     * Take the changed statements from the queue and determine which URIs that need to be updated in
     * the index.
     */
    private Collection<String> changedStatementsToUris(){
        //inform StatementToURIsToUpdate that index is starting
        for( StatementToURIsToUpdate stu : stmtToURIsToIndexFunctions ) {
            stu.startIndexing();        
        }
                
        Collection<String> urisToUpdate = new HashSet<String>();
        
        Statement stmt ;
        while (null != (stmt = changedStmtQueue.poll())) {
        	for( StatementToURIsToUpdate stu : stmtToURIsToIndexFunctions ){
        		urisToUpdate.addAll( stu.findAdditionalURIsToIndex(stmt) );
        	}
        }
        
        //inform StatementToURIsToUpdate that they are done
        for( StatementToURIsToUpdate stu : stmtToURIsToIndexFunctions ) {
            stu.endIndxing();
        }
        
        return urisToUpdate;        
    }
    
	/**
	 * Take the URIs that we got from the changedStmtQueue, and create the lists
	 * of updated URIs and deleted URIs.
	 */
	private UriLists makeAddAndDeleteLists(Collection<String> uris) {
		IndividualDao indDao = wdf.getIndividualDao();

		UriLists uriLists = new UriLists();
		for (String uri : uris) {
			if (uri != null) {
				try {
					Individual ind = indDao.getIndividualByURI(uri);
					if (ind != null) {
					    log.debug("uri to update or add to search index: " + uri);
						uriLists.updatedUris.add(uri);
					} else {
						log.debug("found delete in changed uris: " + uri);
						uriLists.deletedUris.add(uri);
					}
				} catch (QueryParseException ex) {
					log.error("could not get Individual " + uri, ex);
				}
			}
		}
		return uriLists;
	}	

	/**
	 * This rebuilds the whole index.
	 */
    protected void indexRebuild() {
        log.info("Rebuild of search index is starting.");

        // clear out changed URIs since we are doing a full index rebuild
		changedStmtQueue.clear();
        
        log.debug("Getting all URIs in the model");
        Iterator<String> uris = wdf.getIndividualDao().getAllOfThisTypeIterator();
         
        this.numberOfThreads = MAX_REINDEX_THREADS;
        doBuild(uris, Collections.<String>emptyList() );
        
        if( log != null )  //log might be null if system is shutting down.
            log.info("Rebuild of search index is complete.");
    }
      
    protected void updatedIndex() {
        log.debug("Starting updateIndex()");       
                     
        UriLists uriLists = makeAddAndDeleteLists( changedStatementsToUris() );
        
        this.numberOfThreads = Math.max( MAX_UPDATE_THREADS, uriLists.updatedUris.size() / 20); 
        doBuild( uriLists.updatedUris.iterator(), uriLists.deletedUris );
        
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
            
        //make lists of work URIs for workers
        List<List<String>> workLists = makeWorkerUriLists(updateUris, numberOfThreads);                                     

        //setup workers with work
        List<IndexWorkerThread> workers = new ArrayList<IndexWorkerThread>();
        for(int i = 0; i< numberOfThreads ;i++){
            Iterator<Individual> workToDo = new UriToIndividualIterator(workLists.get(i), wdf);
            workers.add( new IndexWorkerThread(indexer, i, workToDo) ); 
        }
        
        // reset the counters so we can monitor the progress
        IndexWorkerThread.resetCounters(System.currentTimeMillis(), figureWorkLoad(workLists));

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
    
    private long figureWorkLoad(List<List<String>> workLists) {
    	long load = 0;
    	for (List<String> list: workLists) {
    		load += list.size();
    	}
    	return load;
    }
    
    public long getCompletedCount() {
    	return IndexWorkerThread.getCount();
    }
    
    public long getTotalToDo() {
    	return IndexWorkerThread.getCountToIndex();
    }
    
    private static class UriLists {
        private final List<String> updatedUris = new ArrayList<String>();
        private final List<String> deletedUris = new ArrayList<String>();
    }
    
    protected void notifyListeners(IndexingEventListener.EventTypes event){
        for ( IndexingEventListener listener : indexingEventListeners ){
            try{
                if(listener != null )
                    listener.notifyOfIndexingEvent( event );
            }catch(Throwable th){
                log.error("problem during NotifyListeners(): " , th);
            }
        }
    }
}

