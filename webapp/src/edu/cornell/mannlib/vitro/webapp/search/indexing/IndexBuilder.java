/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;
import edu.cornell.mannlib.vitro.webapp.search.beans.ObjectSourceIface;

/**
 * The IndexBuilder is used to rebuild or update a search index.
 * It uses an implementation of a backend through an object that
 * implements IndexerIface.  An example of a backend is LuceneIndexer.
 *
 * The IndexBuilder implements the EntityChangeListener so it can
 * be registered for Entity changes from the GenericDB classes.
 *
 * There should be an IndexBuilder in the servlet context, try:
 *
    IndexBuilder builder = (IndexBuilder)getServletContext().getAttribute(IndexBuilder.class.getName());
    if( request.getParameter("update") != null )
        builder.doUpdateIndex();

 * @author bdc34
 *
 */
public class IndexBuilder implements Runnable {
    List<ObjectSourceIface> sourceList = new LinkedList<ObjectSourceIface>();
    IndexerIface indexer = null;
    ServletContext context = null;
    
    long lastRun = 0;
    List<String> changedUris = null;         
    
    public static final boolean UPDATE_DOCS = false;
    public static final boolean NEW_DOCS = true;
    
    private static final Log log = LogFactory.getLog(IndexBuilder.class.getName());

    public IndexBuilder(ServletContext context,
                IndexerIface indexer,
                List /*ObjectSourceIface*/ sources ){
        this.indexer = indexer;
        this.sourceList = sources;
        this.context = context;
        
        changedUris = new LinkedList<String>();        	
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

    public void doIndexBuild() throws IndexingException {
        log.debug(this.getClass().getName()
                + " performing doFullRebuildIndex()\n");

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
        doBuild( listOfIterators, true, NEW_DOCS );
        log.debug(this.getClass().getName() + ".doFullRebuildIndex() Done \n");
    }

    public void run() {
        doUpdateIndex();
    }

    public void doUpdateIndex() {        
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
                     
        List<Individual> changedInds = addDepResourceClasses(checkForDeletes(getAndEmptyChangedUris()));        
        listOfIterators.add( (new IndexBuilder.BuilderObjectSource(changedInds)).getUpdatedSinceIterator(0) );
        
        doBuild( listOfIterators, false,  UPDATE_DOCS );
    }

    private List<Individual> addDepResourceClasses(List<Individual> inds) {
    	WebappDaoFactory wdf = (WebappDaoFactory)context.getAttribute("webappDaoFactory");
    	VClassDao vClassDao = wdf.getVClassDao();
    	java.util.ListIterator<Individual> it = inds.listIterator();
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

	public void clearIndex(){
        try {
            indexer.clearIndex();
        } catch (IndexingException e) {
            log.error("error while clearing index", e);
        }   
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
    private void doBuild(List sourceIterators, boolean wipeIndexFirst, boolean newDocs ){
        try {
            indexer.startIndexing();

            if( wipeIndexFirst )
                indexer.clearIndex();

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
            log.error("\t" + ex.getMessage(),ex);
        } catch (Exception e) {
            log.error("\t"+e.getMessage(),e);
        } finally {
            indexer.endIndexing();
        }
    }
    
    /**
     * Use the back end indexer to index each object that the Iterator returns.
     * @param items
     * @return
     */
    protected void indexForSource(Iterator<Individual> individuals , boolean newDocs){
        if( individuals == null ) return;
        while(individuals.hasNext()){
            indexItem(individuals.next(), newDocs);
        }
    }

    private List<Individual> checkForDeletes(List<String> uris){
    	 WebappDaoFactory wdf = (WebappDaoFactory)context.getAttribute("webappDaoFactory");
    	List<Individual> nonDeletes = new LinkedList<Individual>();
    	for( String uri: uris){
    		if( uri != null ){
    			Individual ind = wdf.getIndividualDao().getIndividualByURI(uri);
    			if( ind != null)
    				nonDeletes.add(ind);
    			else{
    				log.debug("found delete in changed uris");
    				entityDeleted(uri);
    			}
    		}
    	}
    	return nonDeletes;
    }
    
    /**
     * Use the backend indexer to index a single item.
     * @param item
     * @return
     */
    protected void indexItem( Individual ind, boolean newDoc){
        try{
        	if( ind == null )
        		return;
        	if( ind.getVClasses() == null || ind.getVClasses().size() < 1 )
        		return;
        		
            indexer.index(ind, newDoc);
        }catch(Throwable ex){            
            log.debug("IndexBuilder.indexItem() Error indexing "
                    + ind + "\n" +ex);
        }
        return ;
    }
     
    public void entityDeleted(String entityURI) {
    	if( log.isDebugEnabled()) 
    		log.debug("IndexBuilder.entityDeleted() " + entityURI);
        Individual ent = new IndividualImpl(entityURI);
        try {
            indexer.removeFromIndex(ent);
        } catch (IndexingException e) {
            log.debug("IndexBuilder.entityDeleted failed: " + e);
        }
    }

    public synchronized void addToChangedUris(String uri){
    	changedUris.add(uri);
    }
        
    public synchronized void addToChangedUris(Collection<String> uris){
    	changedUris.addAll(uris);    	
    }
    
    private synchronized List<String> getAndEmptyChangedUris(){
    	LinkedList<String> out = new LinkedList<String>(); 
    	out.addAll( changedUris );    	
    	changedUris = new LinkedList<String>();
    	return out;
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
