/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Queue;

import javax.servlet.ServletContext;
import org.apache.solr.client.solrj.SolrServer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.search.beans.ObjectSourceIface;
import edu.cornell.mannlib.vitro.webapp.search.solr.CalculateParameters;


/**
 * The IndexBuilder is used to rebuild or update a search index.
 * There should only be one IndexBuilder in a vitro web application.
 * It uses an implementation of a back-end through an object that
 * implements IndexerIface.  An example of a back-end is LuceneIndexer.
 *
 * See the class SearchReindexingListener for an example of how a model change
 * listener can use an IndexBuilder to keep the full text index in sncy with 
 * updates to a model. It calls IndexBuilder.addToChangedUris().
 *
 * @author bdc34
 *
 */
public class IndexBuilder extends Thread {
    private List<ObjectSourceIface> sourceList = new LinkedList<ObjectSourceIface>();
    private IndexerIface indexer = null;
    private ServletContext context = null;       
    
    /* changedUris should only be accessed from synchronized blocks */
    private HashSet<String> changedUris = null;
    
    private List<Individual> updatedInds = null;
    private List<Individual> deletedInds = null;    
    
    private boolean reindexRequested = false;    
    protected boolean stopRequested = false;
    protected long reindexInterval = 1000 * 60 /* msec */ ;        
    
    public static final boolean UPDATE_DOCS = false;
    public static final boolean NEW_DOCS = true;
     
    private static final Log log = LogFactory.getLog(IndexBuilder.class);

    public IndexBuilder(ServletContext context,
                IndexerIface indexer,
                List<ObjectSourceIface> sources){
        super("IndexBuilder");
        this.indexer = indexer;
        this.sourceList = sources;
        this.context = context;    
        
        this.changedUris = new HashSet<String>();    
        this.start();
    }
    
    protected IndexBuilder(){
        //for testing only
        this( null, null, Collections.<ObjectSourceIface>emptyList());        
    }
    
    public void addObjectSource(ObjectSourceIface osi) {    	
        if (osi != null)
            sourceList.add(osi);
    }

    public boolean isIndexing(){
        return indexer.isIndexing();
    }

    public List<ObjectSourceIface> getObjectSourceList() {
        return sourceList;
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
   
    public synchronized void addToChangedUris(String uri){
    	changedUris.add(uri);
    }
    
    public synchronized void addToChangedUris(Collection<String> uris){
    	changedUris.addAll(uris);    	
    }
    
	public synchronized boolean isReindexRequested() {
		return reindexRequested;
	}
	
	public synchronized boolean isThereWorkToDo(){
		return isReindexRequested() || ! changedUris.isEmpty() ;
	}
	
	public synchronized void stopIndexingThread() {
	    stopRequested = true;
	    this.notifyAll();		
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
                log.error(e,e);
            }
        }
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
		this.updatedInds = new ArrayList<Individual>();
		this.deletedInds = new ArrayList<Individual>();
		
		WebappDaoFactory wdf = (WebappDaoFactory)context.getAttribute("webappDaoFactory");
    	for( String uri: uris){
    		if( uri != null ){
    			Individual ind = wdf.getIndividualDao().getIndividualByURI(uri);
    			if( ind != null)
    				this.updatedInds.add(ind);
    			else{
    				log.debug("found delete in changed uris");
    				this.deletedInds.add(ind);
    			}
    		}
    	}
    	
	    this.updatedInds = addDepResourceClasses(updatedInds);	            	
	}	
  
    protected void indexRebuild() {
        log.info("Rebuild of search index is starting.");

        List<Iterator<Individual>> listOfIterators = new LinkedList<Iterator<Individual>>();
        for (ObjectSourceIface objectSource: sourceList) {
            if (objectSource != null) {
                listOfIterators.add(((objectSource)
                        .getAllOfThisTypeIterator()));
            }
        }

        // clear out changed uris since we are doing a full index rebuild
        getAndEmptyChangedUris();

        if (listOfIterators.size() == 0) 
            log.warn("Warning: no ObjectSources found.");        

        doBuild(listOfIterators, Collections.<Individual>emptyList() );
        if( log != null )  //log might be null if system is shutting down.
            log.info("Rebuild of search index is complete.");
    }
      
    protected void updatedIndex() {
        log.debug("Starting updateIndex()");
        long since = indexer.getModified() - 60000;
                        
        List<Iterator<Individual>> listOfIterators = 
            new LinkedList<Iterator<Individual>>();
        
        for (ObjectSourceIface objectSource: sourceList) {
        	if (objectSource != null) {
                listOfIterators.add(((objectSource)
                        .getUpdatedSinceIterator(since)));
        	}
        }
                     
        makeAddAndDeleteLists( getAndEmptyChangedUris());                   
        listOfIterators.add( (new IndexBuilder.BuilderObjectSource(updatedInds)).getUpdatedSinceIterator(0) );
        
        doBuild( listOfIterators, deletedInds );
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
    private void doBuild(List<Iterator<Individual>> sourceIterators, Collection<Individual> deletes ){
        boolean aborted = false;
        boolean newDocs = reindexRequested;
        boolean forceNewIndex = reindexRequested;
        
        try {
            if( reindexRequested )
                indexer.prepareForRebuild();
            
            indexer.startIndexing();
            reindexRequested = false;
            
            if( ! forceNewIndex ){                
                for(Individual deleteMe : deletes ){
                    indexer.removeFromIndex(deleteMe);
                }
            }            

            //get an iterator for all of the sources of indexable objects
            for (Iterator<Individual> sourceIterator: sourceIterators) {
            	if (sourceIterator == null) {
                	log.warn("skipping null iterator");
            	} else {
                    indexForSource(sourceIterator, newDocs);
            	}
            }
        } catch (AbortIndexing abort){
            if( log != null)
                log.debug("aborting the indexing because thread stop was requested");
            aborted = true;                            
        } catch (Exception e) {
            log.error(e,e);
        }
        
        if( aborted && forceNewIndex ){
            indexer.abortIndexingAndCleanUp();
        }else{
            indexer.endIndexing();
        }
        
    }
    
    /**
     * Use the back end indexer to index each object that the Iterator returns.
     * @throws AbortIndexing 
     */
    private void indexForSource(Iterator<Individual> individuals , boolean newDocs) throws AbortIndexing{     
             
        int count = 0;
        int numOfThreads = 10;
       
      
        List<IndexWorkerThread> workers = new ArrayList<IndexWorkerThread>();
        boolean distributing = true;
        
        IndexWorkerThread.setStartTime(System.currentTimeMillis());
       
        for(int i = 0; i< numOfThreads ;i++){
        	workers.add(new IndexWorkerThread(indexer,i,distributing)); // made a pool of workers
        }
        
        log.info("Indexing worker pool ready for indexing.");
       
        // starting worker threads
        
        for(int i =0; i < numOfThreads; i++){
        	workers.get(i).start();
        }
        
        
        while(individuals.hasNext()){
            if( stopRequested )
                throw new AbortIndexing();
            
            Individual ind = null;
            try{
                ind = individuals.next();     
                          
                workers.get(count%numOfThreads).addToQueue(ind); // adding individual to worker queue.
                
            }catch(Throwable ex){
                if( stopRequested || log == null){//log might be null if system is shutting down.
                    throw new AbortIndexing();
                }
                String uri = ind!=null?ind.getURI():"null";
                log.warn("Error indexing individual " + uri + " " + ex.getMessage());
            }
            count++;          
        }
        
        for(int i =0 ; i < numOfThreads; i ++){
        	workers.get(i).setDistributing(false);
        }
        for(int i =0; i < numOfThreads; i++){
        	try{
        		workers.get(i).join();
        	}catch(InterruptedException e){
        		log.error(e,e);
        	}
        }
        
        IndexWorkerThread.resetCount();
        
    }        

    
    
    /**
     * For a list of individuals, this builds a list of dependent resources and returns it.  
     */
    private List<Individual> addDepResourceClasses(List<Individual> inds) {
        WebappDaoFactory wdf = (WebappDaoFactory)context.getAttribute("webappDaoFactory");
        VClassDao vClassDao = wdf.getVClassDao();
        Iterator<Individual> it = inds.iterator();
        VClass depResVClass = new VClass(VitroVocabulary.DEPENDENT_RESORUCE); 
        while(it.hasNext()){
            Individual ind = it.next();
            List<VClass> classes = ind.getVClasses();
            boolean isDepResource = false;
            for( VClass clazz : classes){
                if( !isDepResource && VitroVocabulary.DEPENDENT_RESORUCE.equals(  clazz.getURI() ) ){                   
                    isDepResource = true;
                    break;
                }
            }
            if( ! isDepResource ){ 
                for( VClass clazz : classes){                   
                    List<String> superClassUris = vClassDao.getAllSuperClassURIs(clazz.getURI());
                    for( String uri : superClassUris){
                        if( VitroVocabulary.DEPENDENT_RESORUCE.equals( uri ) ){                         
                            isDepResource = true;
                            break;
                        }
                    }
                    if( isDepResource )
                        break;                  
                }
            }
            if( isDepResource){
                classes.add(depResVClass);
                ind.setVClasses(classes, true);
            }
        }
        return inds;
    }    
    
    /* maybe ObjectSourceIface should be replaced with just an iterator. */
    private class BuilderObjectSource implements ObjectSourceIface {
        private final List<Individual> individuals; 
        public BuilderObjectSource( List<Individual>  individuals){
            this.individuals=individuals;
        }        
        @Override
		public Iterator<Individual> getAllOfThisTypeIterator() {
            return new Iterator<Individual>(){
                final Iterator<Individual> it = individuals.iterator();
                
                @Override
				public boolean hasNext() {
                    return it.hasNext();
                }
                
                @Override
				public Individual next() {
                    return it.next();
                }
                
                @Override
				public void remove() { /* not implemented */}               
            };
        }        
        @Override
		public Iterator<Individual> getUpdatedSinceIterator(long msSinceEpoc) {
            return getAllOfThisTypeIterator();
        }
    }
    
    private class AbortIndexing extends Exception {
    	// Just a vanilla exception
    }  
    
}

