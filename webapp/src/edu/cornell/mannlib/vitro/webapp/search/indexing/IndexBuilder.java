package edu.cornell.mannlib.vitro.webapp.search.indexing;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;
import edu.cornell.mannlib.vitro.webapp.search.beans.ObjectSourceIface;
import edu.cornell.mannlib.vitro.webapp.utils.EntityChangeListener;

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
public class IndexBuilder implements Runnable, EntityChangeListener{
    List sourceList = new LinkedList();
    IndexerIface indexer = null;

    public static final boolean UPDATE_DOCS = false;
    public static final boolean NEW_DOCS = true;
    
    private static final Log log = LogFactory.getLog(IndexBuilder.class.getName());

    public IndexBuilder(ServletContext context,
                IndexerIface indexer,
                List /*ObjectSourceIface*/ sources ){
        this.indexer = indexer;
        this.sourceList = sources;

        //add this to the context as a EntityChangeListener so that we can
        //be notified of entity changes.
        context.setAttribute(EntityChangeListener.class.getName(), this);
    }

    public void addObjectSource(ObjectSourceIface osi) {
        if (osi != null)
            sourceList.add(osi);
    }

    public boolean isIndexing(){
        return indexer.isIndexing();
    }

    public List getObjectSourceList() {
        return sourceList;
    }

    public void doIndexBuild() throws IndexingException {
        log.debug(this.getClass().getName()
                + " performing doFullRebuildIndex()\n");

        Iterator sources = sourceList.iterator();
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
        List<Iterator<ObjectSourceIface>> listOfIterators = 
            new LinkedList<Iterator<ObjectSourceIface>>();
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
        doBuild( listOfIterators, false,  UPDATE_DOCS );
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
    protected void indexForSource(Iterator items , boolean newDocs){
        if( items == null ) return;
        while(items.hasNext()){
            indexItem(items.next(), newDocs);
        }
    }

    /**
     * Use the backend indexer to index a single item.
     * @param item
     * @return
     */
    protected void indexItem( Object item, boolean newDoc){
        try{
            indexer.index(item, newDoc);
        }catch(Throwable ex){            
            log.debug("IndexBuilder.indexItem() Error indexing "
                    + item + "\n" +ex);
        }
        return ;
    }

    /* These methods are so that the IndexBuilder may register for entity changes */
    public void entityAdded(String entityURI) {
        log.debug("IndexBuilder.entityAdded() " + entityURI);
        (new Thread(this)).start();
    }

    public void entityDeleted(String entityURI) {
        log.debug("IndexBuilder.entityDeleted() " + entityURI);
        Individual ent = new IndividualImpl(entityURI);
        try {
            indexer.removeFromIndex(ent);
        } catch (IndexingException e) {
            log.debug("IndexBuilder.entityDeleted failed: " + e);
        }
    }

    public void entityUpdated(String entityURI) {
        log.debug("IndexBuilder.entityUpdate() " + entityURI);
        (new Thread(this)).start();
    }
}
