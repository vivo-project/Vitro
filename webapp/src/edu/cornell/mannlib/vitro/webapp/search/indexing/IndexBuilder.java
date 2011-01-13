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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;
import edu.cornell.mannlib.vitro.webapp.search.beans.ObjectSourceIface;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;

/**
 * The IndexBuilder is used to rebuild or update a search index.
 * It uses an implementation of a back-end through an object that
 * implements IndexerIface.  An example of a back-end is LuceneIndexer.
 *
 * See the class SearchReindexingListener for an example of how a model change
 * listener can use an IndexBuilder to keep the full text index in sncy with 
 * updates to a model.
 *
 * There should be an IndexBuilder in the servlet context, try:
 *
    IndexBuilder builder = (IndexBuilder)getServletContext().getAttribute(IndexBuilder.class.getName());
    if( request.getParameter("update") != null )
        builder.doUpdateIndex();

 * @author bdc34
 *
 */
public class IndexBuilder {
    private List<ObjectSourceIface> sourceList = new LinkedList<ObjectSourceIface>();
    private IndexerIface indexer = null;
    private ServletContext context = null;
    private ProhibitedFromSearch classesProhibitedFromSearch = null;    
    
    private long lastRun = 0;
     
    private HashSet<String> changedUris = null;         
    
    private List<Individual> updatedInds = null;
    private List<Individual> deletedInds = null;
    
    private IndexBuilderThread indexingThread = null;
    
    //shared with IndexBuilderThread
    private boolean reindexRequested = false;
    
    public static final boolean UPDATE_DOCS = false;
    public static final boolean NEW_DOCS = true;
     
    private static final Log log = LogFactory.getLog(IndexBuilder.class);

    public IndexBuilder(ServletContext context,
                IndexerIface indexer,
                List /*ObjectSourceIface*/ sources){
        this.indexer = indexer;
        this.sourceList = sources;
        this.context = context;    
        
        this.changedUris = new HashSet<String>();
        this.indexingThread = new IndexBuilderThread(this); 
        this.indexingThread.start();
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

    public void doIndexRebuild() throws IndexingException {
    	//set up full index rebuild
    	setReindexRequested( true );    	
    	//wake up indexing thread
    	synchronized (this.indexingThread) {					
    		this.indexingThread.notifyAll();
    	}
    }

    /**
     * This will re-index Individuals that changed because of modtime or because they
     * were added with addChangedUris(). 
     */
    public void doUpdateIndex() {        	    
    	//wake up thread
    	synchronized (this.indexingThread) {					
    		this.indexingThread.notifyAll();
    	}    	    
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
	
	public ProhibitedFromSearch getClassesProhibitedFromSearch() {
		return classesProhibitedFromSearch;
	}

	public void setClassesProhibitedFromSearch(
			ProhibitedFromSearch classesProhibitedFromSearch) {
		this.classesProhibitedFromSearch = classesProhibitedFromSearch;
	}	
	
	public void killIndexingThread() {
		this.indexingThread.kill();		
	}
	/* ******************** non-public methods ************************* */
	
	private synchronized void setReindexRequested(boolean reindexRequested) {
		this.reindexRequested = reindexRequested;
	}
	
    private synchronized Collection<String> getAndEmptyChangedUris(){
    	Collection<String> out = changedUris;     	    
    	changedUris = new HashSet<String>();
    	return out;
    }
    
    protected void indexRebuild() throws IndexingException {    	
        log.info("Rebuild of search index is starting.");

        Iterator<ObjectSourceIface> sources = sourceList.iterator();
        List listOfIterators = new LinkedList();
        while(sources.hasNext()){
            Object obj = sources.next();
             if( obj != null && obj instanceof ObjectSourceIface )
                 listOfIterators.add((((ObjectSourceIface) obj)
                        .getAllOfThisTypeIterator()));
             else
                 log.debug("\tskipping object of class "
                         + obj.getClass().getName() + "\n"
                         + "\tIt doesn not implement ObjectSourceIface.\n");
        }
        
        //clear out changed uris since we are doing a full index rebuild
        getAndEmptyChangedUris();
        
        if( listOfIterators.size() == 0){ log.debug("Warning: no ObjectSources found.");}
                
        setReindexRequested(false);
        doBuild( listOfIterators, Collections.EMPTY_LIST, true, NEW_DOCS );
        log.info("Rebuild of search index is complete.");
    }
    
    protected void updatedIndex() throws IndexingException{
    	log.debug("Starting updateIndex()");
		long since = indexer.getModified() - 60000;
			    		
	    Iterator<ObjectSourceIface> sources = sourceList.iterator();
	    
	    List<Iterator<Individual>> listOfIterators = 
	        new LinkedList<Iterator<Individual>>();
	    
	    while (sources.hasNext()) {
	        Object obj = sources.next();
	        if (obj != null && obj instanceof ObjectSourceIface)
	            listOfIterators.add((((ObjectSourceIface) obj)
	                    .getUpdatedSinceIterator(since)));
	        else
	            log.debug("\tskipping object of class "
	                    + obj.getClass().getName() + "\n"
	                    + "\tIt doesn not implement " + "ObjectSourceIface.\n");
	    }
	                 
	    buildAddAndDeleteLists( getAndEmptyChangedUris());                   
	    listOfIterators.add( (new IndexBuilder.BuilderObjectSource(updatedInds)).getUpdatedSinceIterator(0) );
	    
	    doBuild( listOfIterators, deletedInds, false,  UPDATE_DOCS );
	    log.debug("Ending updateIndex()");
    }
    
	/**
	 * Sets updatedUris and deletedUris.
	 * @param changedUris
	 */
	private void buildAddAndDeleteLists( Collection<String> uris){	    
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
    private void doBuild(List sourceIterators, Collection<Individual> deletes, boolean forceNewIndex, boolean newDocs ){
        try {
            if( forceNewIndex )
                indexer.prepareForRebuild();
            
            indexer.startIndexing();

            if( ! forceNewIndex ){                
            	for(Individual deleteMe : deletes ){
            		indexer.removeFromIndex(deleteMe);
            	}
            }            

            //get an iterator for all of the sources of indexable objects
            Iterator sourceIters = sourceIterators.iterator();
            Object obj = null;
            while (sourceIters.hasNext()) {
                obj = sourceIters.next();
                if (obj == null || !(obj instanceof Iterator)) {
                    log.debug("\tskipping object of class "
                            + obj.getClass().getName() + "\n"
                            + "\tIt doesn not implement "
                            + "Iterator.\n");
                    continue;
                }
                indexForSource((Iterator)obj, newDocs);
            }
        } catch (IndexingException ex) {
            log.error(ex,ex);
        } catch (Exception e) {
            log.error(e,e);
        } finally {
            indexer.endIndexing();
        }
    }
    
    /**
     * Use the back end indexer to index each object that the Iterator returns.
     * @param items
     * @return
     */
    private void indexForSource(Iterator<Individual> individuals , boolean newDocs){
        if( individuals == null ) return;
        while(individuals.hasNext()){
            indexItem(individuals.next(), newDocs);
        }
    }
    
    /**
     * Use the backend indexer to index a single item.
     * @param item
     * @return
     */
    private void indexItem( Individual ind, boolean newDoc){
        try{
        	if( ind == null )
        		return;
        	if( ind.getVClasses() == null || ind.getVClasses().size() < 1 )
        		return;
        	boolean prohibitedClass = false;
        	if(  classesProhibitedFromSearch != null ){
        		for( VClass vclass : ind.getVClasses() ){
        			if( classesProhibitedFromSearch.isClassProhibited(vclass.getURI()) ){        			
        				prohibitedClass = true;
        				log.debug("removing " + ind.getURI() + " from index because class " + vclass.getURI() + " is on prohibited list.");
        				break;
        			}        		
        		}
        	}
        	if( !prohibitedClass ){
        	    log.debug("Indexing '" + ind.getName() + "' URI: " + ind.getURI());
        		indexer.index(ind, newDoc);
        	}else{        	    
        		indexer.removeFromIndex(ind);
        	}
        	
        }catch(Throwable ex){            
            log.debug("IndexBuilder.indexItem() Error indexing "
                    + ind + "\n" +ex);
        }
        return ;
    } 
    
    private class BuilderObjectSource implements ObjectSourceIface {
    	private final List<Individual> individuals; 
    	public BuilderObjectSource( List<Individual>  individuals){
    		this.individuals=individuals;
    	}
		
		public Iterator getAllOfThisTypeIterator() {
			return new Iterator(){
				final Iterator it = individuals.iterator();
				
				public boolean hasNext() {
					return it.hasNext();
				}
				
				public Object next() {
					return it.next();
				}
				
				public void remove() { /* not implemented */}				
			};
		}
		
		public Iterator getUpdatedSinceIterator(long msSinceEpoc) {
			return getAllOfThisTypeIterator();
		}
    }

	


}
